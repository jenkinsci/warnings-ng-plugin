package hudson.plugins.analysis.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

import jenkins.MasterToSlaveFileCallable;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.git.GitSCM;
import hudson.remoting.VirtualChannel;

/**
 * Assigns git blames to warnings.
 * Based on the solution by John Gibson.
 *
 * @author Lukas Krose
 */
public class GitBlamer extends AbstractBlamer {
    private final GitSCM scm;

    public GitBlamer(AbstractBuild<?, ?> build, final GitSCM scm, FilePath workspace, PluginLogger logger, final TaskListener listener) {
        super(build, workspace, listener, logger);
        this.scm = scm;

        log("Using GitBlamer to create author and commit information for all warnings");
    }

    @Override
    public void blame(final Set<FileAnnotation> annotations) {
        try {
            if (annotations.isEmpty()) {
                return;
            }

            computeBlamesOnSlave(annotations);
        }
        catch (IOException e) {
            log("Mapping annotations to Git commits IDs and authors failed with an exception:%n%s%n%s",
                    e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
        catch (InterruptedException e) {
            // nothing to do, already logged
        }
    }

    private void computeBlamesOnSlave(final Set<FileAnnotation> annotations) throws IOException, InterruptedException {
        final Map<String, BlameRequest> linesOfConflictingFiles = extractConflictingFiles(annotations);

        Map<String, BlameRequest> blamesOfConflictingFiles = getWorkspace().act(new MasterToSlaveFileCallable<Map<String, BlameRequest>>() {
            @Override
            public Map<String, BlameRequest> invoke(final File workspace, final VirtualChannel channel) throws IOException, InterruptedException {
                Map<String, BlameResult> blameResults = loadBlameResultsForFiles(linesOfConflictingFiles);
                return fillBlameResults(linesOfConflictingFiles, blameResults);
            }
        });

        for (FileAnnotation annotation : annotations) {
            BlameRequest blame = blamesOfConflictingFiles.get(annotation.getFileName());
            int line = annotation.getPrimaryLineNumber();
            annotation.setAuthorName(blame.getName(line));
            annotation.setAuthorEmail(blame.getEmail(line));
            annotation.setCommitId(blame.getCommit(line));
        }
    }

    private Map<String, BlameResult> loadBlameResultsForFiles(final Map<String, BlameRequest> linesOfConflictingFiles)
            throws InterruptedException, IOException {
        EnvVars environment = getBuild().getEnvironment(getListener());
        String gitCommit = environment.get("GIT_COMMIT");
        String gitExe = scm.getGitExe(getBuild().getBuiltOn(), getListener());

        GitClient git = Git.with(getListener(), environment).in(getWorkspace()).using(gitExe).getClient();

        ObjectId headCommit;
        if (StringUtils.isBlank(gitCommit)) {
            log("No GIT_COMMIT environment variable found, using HEAD.");

            headCommit = git.revParse("HEAD");
        }
        else {
            headCommit = git.revParse(gitCommit);
        }

        Map<String, BlameResult> blameResults = new HashMap<String, BlameResult>();
        if (headCommit == null) {
            log("Could not retrieve HEAD commit, aborting.");

            return blameResults;
        }

        for (BlameRequest request : linesOfConflictingFiles.values()) {
            BlameCommand blame = new BlameCommand(git.getRepository());
            String fileName = request.getFileName();
            blame.setFilePath(fileName);
            blame.setStartCommit(headCommit);
            try {
                BlameResult result = blame.call();
                if (result == null) {
                    log("No blame results for file: " + request);
                }
                else {
                    blameResults.put(fileName, result);
                }
                if (Thread.interrupted()) {
                    String message = "Thread was interrupted while computing blame information.";
                    log(message);
                    throw new InterruptedException(message);
                }
            }
            catch (GitAPIException e) {
                String message = "Error running git blame on " + fileName + " with revision: " + headCommit;
                log(message);
            }
        }

        return blameResults;
    }

    private Map<String, BlameRequest> fillBlameResults(final Map<String, BlameRequest> linesOfConflictingFiles,
            final Map<String, BlameResult> blameResults) {
        for (String fileName : linesOfConflictingFiles.keySet()) {
            BlameRequest request = linesOfConflictingFiles.get(fileName);
            BlameResult blame = blameResults.get(request.getFileName());
            if (blame == null) {
                log("No blame details found for " + fileName);
            }
            else {
                for (int line : request) {
                    int lineIndex = line - 1; // first line is index 0
                    if (lineIndex < blame.getResultContents().size()) {
                        PersonIdent who = blame.getSourceAuthor(lineIndex);
                        if (who == null) {
                            log("No author information found for line %d in file %s.", lineIndex, fileName);
                        }
                        else {
                            request.setName(line, who.getName());
                            request.setEmail(line, who.getEmailAddress());
                        }
                        RevCommit commit = blame.getSourceCommit(lineIndex);
                        if (commit == null) {
                            log("No commit ID found for line %d in file %s.", lineIndex, fileName);
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
}
