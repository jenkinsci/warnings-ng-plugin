package hudson.plugins.analysis.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.RepositoryCallback;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

/**
 * Assigns git blames to warnings. Based on the solution by John Gibson, see JENKINS-6748.
 *
 * @author Lukas Krose
 * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-6748">Issue 6748</a>
 */
public class GitBlamer extends AbstractBlamer {
    private final String pathToGit;

    /**
     * Creates a new blamer for Git.
     *
     * @param pathToGit   path to git binary
     * @param environment {@link EnvVars environment} of the build
     * @param workspace   workspace of the build
     * @param listener    task listener to print logging statements to
     */
    public GitBlamer(final String pathToGit, final EnvVars environment, FilePath workspace, final TaskListener listener) {
        super(environment, workspace, listener);
        this.pathToGit = pathToGit;

        log("Using GitBlamer to create author and commit information for all warnings.%n");
        log("Getting blame results for all files in %s.%n", workspace);
    }

    @Override
    protected Map<String, BlameRequest> blame(final Map<String, BlameRequest> linesOfConflictingFiles) throws InterruptedException, IOException {
        return fillBlameResults(linesOfConflictingFiles, loadBlameResultsForFiles(linesOfConflictingFiles));
    }

    private Map<String, BlameResult> loadBlameResultsForFiles(final Map<String, BlameRequest> linesOfConflictingFiles)
            throws InterruptedException, IOException {
        GitClient git = Git.with(getListener(), getEnvironment()).in(getWorkspace()).using(pathToGit).getClient();
        return git.withRepository(new BlameCallback(this, getHead(git), linesOfConflictingFiles.values()));
    }

    private ObjectId getHead(final GitClient git) throws InterruptedException {
        String gitCommit = getEnvironment().get("GIT_COMMIT");
        ObjectId headCommit;
        if (StringUtils.isBlank(gitCommit)) {
            log("No GIT_COMMIT environment variable found, using HEAD.%n");

            headCommit = git.revParse("HEAD");
        }
        else {
            headCommit = git.revParse(gitCommit);
        }
        return headCommit;
    }

    private Map<String, BlameRequest> fillBlameResults(final Map<String, BlameRequest> linesOfConflictingFiles,
            final Map<String, BlameResult> blameResults) {
        for (String fileName : linesOfConflictingFiles.keySet()) {
            BlameRequest request = linesOfConflictingFiles.get(fileName);
            BlameResult blame = blameResults.get(request.getFileName());
            if (blame == null) {
                log("No blame details found for %s.%n", fileName);
            }
            else {
                for (int line : request) {
                    int lineIndex = line - 1; // first line is index 0
                    if (lineIndex < blame.getResultContents().size()) {
                        PersonIdent who = blame.getSourceAuthor(lineIndex);
                        if (who == null) {
                            log("No author information found for line %d in file %s.%n", lineIndex, fileName);
                        }
                        else {
                            request.setName(line, who.getName());
                            request.setEmail(line, who.getEmailAddress());
                        }
                        RevCommit commit = blame.getSourceCommit(lineIndex);
                        if (commit == null) {
                            log("No commit ID found for line %d in file %s.%n", lineIndex, fileName);
                        }
                        else {
                            request.setCommit(line, commit.getName());
                        }
                    }
                }
            }
        }
        return linesOfConflictingFiles;
    }

    private static class BlameCallback implements RepositoryCallback<Map<String, BlameResult>> {
        private GitBlamer gitBlamer;
        private ObjectId headCommit;
        private Collection<BlameRequest> requests;

        public BlameCallback(final GitBlamer gitBlamer, final ObjectId headCommit, final Collection<BlameRequest> requests) {
            this.gitBlamer = gitBlamer;
            this.headCommit = headCommit;
            this.requests = requests;
        }

        @Override
        public Map<String, BlameResult> invoke(final Repository repo, final VirtualChannel channel) throws IOException, InterruptedException {
            Map<String, BlameResult> blameResults = new HashMap<String, BlameResult>();
            if (headCommit == null) {
                gitBlamer.error("Could not retrieve HEAD commit, aborting.");

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
                        gitBlamer.log("No blame results for request <%s>.%n", request);
                    }
                    else {
                        blameResults.put(fileName, result);
                    }
                    if (Thread.interrupted()) {
                        String message = "Thread was interrupted while computing blame information.";
                        gitBlamer.log(message);
                        throw new InterruptedException(message);
                    }
                }
                catch (GitAPIException e) {
                    String message = "Error running git blame on " + fileName + " with revision: " + headCommit;
                    gitBlamer.error(message);
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
