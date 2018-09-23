package io.jenkins.plugins.analysis.core.scm;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.RepositoryCallback;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import jenkins.MasterToSlaveFileCallable;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.remoting.VirtualChannel;

/**
 * Assigns git blames to warnings. Based on the solution by John Gibson, see JENKINS-6748.
 *
 * @author Lukas Krose
 * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-6748">Issue 6748</a>
 */
public class GitBlamer implements Blamer {
    private final GitClient git;
    private final String gitCommit;
    private final FilePath workspace;
    private final TaskListener listener;

    /**
     * Creates a new blamer for Git.
     *
     * @param git
     *         git client
     * @param gitCommit
     *         content of environment variable GIT_COMMIT
     * @param listener
     *         task listener to print logging statements to
     */
    public GitBlamer(final GitClient git, @CheckForNull final String gitCommit, final TaskListener listener) {
        this.workspace = git.getWorkTree();
        this.listener = listener;
        this.git = git;
        this.gitCommit = StringUtils.defaultString(gitCommit, "HEAD");
    }

    /**
     * Computes for each conflicting file a {@link BlameRequest}. Note that this call is executed on a build agent.
     * I.e., the blamer instance and all transfer objects need to be serializable.
     *
     * @param blames
     *         a mapping of file names to blame request. Each blame request defines the lines that need to be mapped to
     *         an author.
     *
     * @return the same mapping, now filled with blame information
     * @throws InterruptedException
     *         if this operation has been canceled
     * @throws IOException
     *         in case of an error
     */
    protected Blames blameOnAgent(final Blames blames) throws InterruptedException, IOException {
        blames.logInfo("Using GitBlamer to create author and commit information for all warnings.%n");
        blames.logInfo("GIT_COMMIT=%s, workspace=%s%n", gitCommit, git.getWorkTree());

        return fillBlameResults(blames, loadBlameResultsForFiles(blames));
    }

    private Map<String, BlameResult> loadBlameResultsForFiles(final Blames blames)
            throws InterruptedException, IOException {
        try {
            ObjectId headCommit = git.revParse(gitCommit);
            return git.withRepository(new BlameCallback(blames, headCommit, blames.getRequests()));
        }
        catch (GitException exception) {
            blames.logError("Can't determine head commit using 'git rev-parse'. Skipping blame. %n%s%n",
                    exception.getMessage());

            return Collections.emptyMap();
        }
    }

    private Blames fillBlameResults(final Blames blames,
            final Map<String, BlameResult> blameResults) {
        for (String fileName : blames.getFiles()) {
            BlameRequest request = blames.getRequest(fileName);
            BlameResult blame = blameResults.get(request.getFileName());
            if (blame == null) {
                blames.logInfo("No blame details found for %s.%n", fileName);
            }
            else {
                for (int line : request) {
                    int lineIndex = line - 1; // first line is index 0
                    if (lineIndex < blame.getResultContents().size()) {
                        PersonIdent who = blame.getSourceAuthor(lineIndex);
                        if (who == null) {
                            blames.logInfo("No author information found for line %d in file %s.%n", lineIndex,
                                    fileName);
                        }
                        else {
                            request.setName(line, who.getName());
                            request.setEmail(line, who.getEmailAddress());
                        }
                        RevCommit commit = blame.getSourceCommit(lineIndex);
                        if (commit == null) {
                            blames.logInfo("No commit ID found for line %d in file %s.%n", lineIndex, fileName);
                        }
                        else {
                            request.setCommit(line, commit.getName());
                        }
                    }
                }
            }
        }
        return blames;
    }

    @Override
    public Blames blame(final Report report) {
        try {
            if (!report.isEmpty()) {
                return invokeBlamer(report);
            }
        }
        catch (IOException e) {
            report.logError("Computing blame information failed with an exception:%n%s%n%s",
                    e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
        catch (InterruptedException e) {
            // nothing to do, already logged
        }
        return new Blames();
    }

    private Blames invokeBlamer(final Report report) throws IOException, InterruptedException {
        final Blames blames = extractConflictingFiles(report);

        Blames blamesOfConflictingFiles = getWorkspace().act(
                new MasterToSlaveFileCallable<Blames>() {
                    @Override
                    public Blames invoke(final File workspace, final VirtualChannel channel)
                            throws IOException, InterruptedException {
                        return blameOnAgent(blames);
                    }
                });

        return blamesOfConflictingFiles;
    }

    /**
     * Extracts the relative file names of the files that contain annotations to make sure every file is blamed only
     * once.
     *
     * @param report
     *         the issues to extract the file names from
     *
     * @return a mapping of absolute to relative file names of the conflicting files
     */
    protected Blames extractConflictingFiles(final Report report) {
        Blames blames = new Blames();

        String workspacePath = getWorkspacePath();
        List<String> errorLog = new ArrayList<>();

        for (Issue issue : report) {
            if (issue.getLineStart() > 0) {
                String storedFileName = issue.getFileName();
                if (blames.contains(storedFileName)) {
                    blames.addLine(storedFileName, issue.getLineStart());
                }
                else {
                    String absoluteFileName = getCanonicalPath(storedFileName);
                    if (absoluteFileName.startsWith(workspacePath)) {
                        String relativeFileName = absoluteFileName.substring(workspacePath.length());
                        if (relativeFileName.startsWith("/") || relativeFileName.startsWith("\\")) {
                            relativeFileName = relativeFileName.substring(1);
                        }
                        blames.addRequest(storedFileName,
                                new BlameRequest(relativeFileName, issue.getLineStart()));
                    }
                    else {
                        int error = errorLog.size();
                        if (error < 5) {
                            errorLog.add(String.format(
                                    "Skipping non-workspace file %s (workspace = %s, absolute = %s).%n",
                                    storedFileName, workspacePath, absoluteFileName));
                        }
                        else if (error == 5) {
                            errorLog.add("  ... skipped logging of additional non-workspace file errors ...");
                        }
                    }
                }
            }
        }

        if (blames.isEmpty()) {
            report.logError("Created no blame requests - Git blame will be skipped");
            errorLog.forEach(report::logError);
        }
        else {
            report.logInfo("Created blame requests for %d files - invoking Git blame on agent for each of the requests",
                    blames.size());
            errorLog.forEach(report::logError);
        }
        return blames;
    }

    private String getWorkspacePath() {
        return getCanonicalPath(workspace.getRemote());
    }

    private String getCanonicalPath(final String path) {
        try {
            return new File(path).getCanonicalPath().replace('\\', '/');
        }
        catch (IOException e) {
            return path;
        }
    }

    /**
     * Prints the specified error message.
     *
     * @param message
     *         the message (format string)
     * @param args
     *         the arguments for the message format
     */
    protected final void error(final String message, final Object... args) {
        listener.error("<Git Blamer> " + String.format(message, args));
    }

    /**
     * Returns the workspace path on the agent.
     *
     * @return workspace path
     */
    protected FilePath getWorkspace() {
        return workspace;
    }

    /**
     * Returns the listener for logging statements.
     *
     * @return logger
     */
    protected TaskListener getListener() {
        return listener;
    }

    private static class BlameCallback implements RepositoryCallback<Map<String, BlameResult>> {
        private Blames blames;
        private ObjectId headCommit;
        private Collection<BlameRequest> requests;

        public BlameCallback(final Blames blames, final ObjectId headCommit,
                final Collection<BlameRequest> requests) {
            this.blames = blames;
            this.headCommit = headCommit;
            this.requests = requests;
        }

        @Override
        public Map<String, BlameResult> invoke(final Repository repo, final VirtualChannel channel)
                throws InterruptedException {
            Map<String, BlameResult> blameResults = new HashMap<String, BlameResult>();
            if (headCommit == null) {
                blames.logError("Could not retrieve HEAD commit, aborting.");

                return blameResults;
            }

            for (BlameRequest request : requests) {
                BlameCommand blame = new BlameCommand(repo);
                String fileName = request.getFileName();
                blame.setFilePath(fileName);
                blame.setStartCommit(headCommit);
                try {
                    BlameResult result = blame.call();
                    if (result == null) {
                        blames.logInfo("No blame results for request <%s>.%n", request);
                    }
                    else {
                        blameResults.put(fileName, result);
                    }
                    if (Thread.interrupted()) {
                        String message = "Thread was interrupted while computing blame information.";
                        blames.logInfo(message);
                        throw new InterruptedException(message);
                    }
                }
                catch (GitAPIException e) {
                    blames.logError("Error running git blame on %s with revision %s", fileName, headCommit);
                }
            }

            return blameResults;
        }
    }

//    /**
//     * Get a repository browser link for the specified commit.
//     *
//     * @param commitId the id of the commit to be linked.
//     * @return The link or {@code null} if one is not available.
//     */
//
//    public URL urlForCommitId(final String commitId) {
//        if (commitUrlsAttempted) {
//            return commitUrls == null ? null : commitUrls.get(commitId);
//        }
//        commitUrlsAttempted = true;
//
//        Run<?, ?> run = getOwner();
//        if (run.getParent() instanceof AbstractProject) {
//            AbstractProject aProject = (AbstractProject) run.getParent();
//            SCM scm = aProject.getScm();
//            //SCM scm = getOwner().getParent().getScm();
//            if ((scm == null) || (scm instanceof NullSCM)) {
//                scm = aProject.getRootProject().getScm();
//            }
//
//            final HashSet<String> commitIds = new HashSet<String>(getAnnotations().size());
//            for (final FileAnnotation annot : getAnnotations()) {
//                commitIds.add(annot.getCommitId());
//            }
//            commitIds.remove(null);
//            try {
//                commitUrls = computeUrlsForCommitIds(scm, commitIds);
//                if (commitUrls != null) {
//                    return commitUrls.get(commitId);
//                }
//            }
//            catch (NoClassDefFoundError e) {
//                // Git wasn't installed, ignore
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Creates links for the specified commitIds using the repository browser.
//     *
//     * @param scm the {@code SCM} of the owning project.
//     * @param commitIds the commit ids in question.
//     * @return a mapping of the links or {@code null} if the {@code SCM} isn't a
//     *  {@code GitSCM} or if a repository browser isn't set or if it isn't a
//     *  {@code GitRepositoryBrowser}.
//     */
//
//    @SuppressWarnings("REC_CATCH_EXCEPTION")
//    public static Map<String, URL> computeUrlsForCommitIds(final SCM scm, final Set<String> commitIds) {
//        if (!(scm instanceof GitSCM)) {
//            return null;
//        }
//        if (commitIds.isEmpty()) {
//            return null;
//        }
//
//        GitSCM gscm = (GitSCM) scm;
//        GitRepositoryBrowser browser = gscm.getBrowser();
//        if (browser == null) {
//            RepositoryBrowser<?> ebrowser = gscm.getEffectiveBrowser();
//            if (ebrowser instanceof GitRepositoryBrowser) {
//                browser = (GitRepositoryBrowser) ebrowser;
//            }
//            else {
//                return null;
//            }
//        }
//
//        // This is a dirty hack because the only way to create changesets is to do it by parsing git log messages
//        // Because what we're doing is fairly dangerous (creating minimal commit messages) just give up if there is an error
//        try {
//            HashMap<String, URL> result = new HashMap<String, URL>((int) (commitIds.size() * 1.5f));
//            for (final String commitId : commitIds) {
//                GitChangeSet cs = new GitChangeSet(Collections.singletonList("commit " + commitId), true);
//                if (cs.getId() != null) {
//                    result.put(commitId, browser.getChangeSetLink(cs));
//                }
//            }
//
//            return result;
//        }
//        // CHECKSTYLE:OFF
//        catch (Exception e) {
//            // CHECKSTYLE:ON
//            // TODO: log?
//            return null;
//        }
//    }
//
//    /**
//     * Get a {@code User} that corresponds to this author.
//     *
//     * @return a {@code User} or {@code null} if one can't be created.
//     */
//    public User getUser() {
//        if (userAttempted) {
//            return user;
//        }
//        userAttempted = true;
//        if ("".equals(authorName)) {
//            return null;
//        }
//        Run<?, ?> run = getOwner();
//        if (run.getParent() instanceof AbstractProject) {
//            AbstractProject aProject = (AbstractProject) run.getParent();
//            SCM scm = aProject.getScm();
//
//
//            if ((scm == null) || (scm instanceof NullSCM)) {
//                scm = aProject.getRootProject().getScm();
//            }
//            try {
//                user = findOrCreateUser(authorName, authorEmail, scm);
//            }
//            catch (NoClassDefFoundError e) {
//                // Git wasn't installed, ignore
//            }
//        }
//        return user;
//    }
//
//
//    /**
//     * Returns user of the change set.  Stolen from hudson.plugins.git.GitChangeSet.
//     *
//     * @param fullName user name.
//     * @param email user email.
//     * @param scm the SCM of the owning project.
//     * @return {@link User} or {@code null} if the {@Code SCM} isn't a {@code GitSCM}.
//     */
//    public static User findOrCreateUser(final String fullName, final String email, final SCM scm) {
//        if (!(scm instanceof GitSCM)) {
//            return null;
//        }
//
//        GitSCM gscm = (GitSCM) scm;
//        boolean createAccountBasedOnEmail = gscm.isCreateAccountBasedOnEmail();
//
//        User user;
//        if (createAccountBasedOnEmail) {
//            user = User.get(email, false);
//
//            if (user == null) {
//                try {
//                    user = User.get(email, true);
//                    user.setFullName(fullName);
//                    user.addProperty(new Mailer.UserProperty(email));
//                    user.save();
//                }
//                catch (IOException e) {
//                    // add logging statement?
//                }
//            }
//        }
//        else {
//            user = User.get(fullName, false);
//
//            if (user == null) {
//                user = User.get(email.split("@")[0], true);
//            }
//        }
//        return user;
//    }
}
