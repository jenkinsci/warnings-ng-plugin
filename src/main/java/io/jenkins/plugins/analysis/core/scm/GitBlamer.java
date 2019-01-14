package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.hm.hafner.analysis.FilteredLog;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.RepositoryCallback;
import hudson.FilePath;
import hudson.plugins.git.GitException;
import hudson.remoting.VirtualChannel;

/**
 * Assigns git blames to warnings. Based on the solution by John Gibson, see JENKINS-6748. This code is intended to run
 * on the agent.
 *
 * @author Lukas Krose
 * @author Ullrich Hafner
 * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-6748">Issue 6748</a>
 */
@SuppressFBWarnings(value = "SE", justification = "GitClient implementation is Serializable")
class GitBlamer implements Blamer {
    private static final long serialVersionUID = -619059996626444900L;

    private final GitClient git;
    private final String gitCommit;
    private final FilePath workspace;

    /**
     * Creates a new blamer for Git.
     *
     * @param git
     *         git client
     * @param gitCommit
     *         content of environment variable GIT_COMMIT
     */
    GitBlamer(final GitClient git, final String gitCommit) {
        this.workspace = git.getWorkTree();
        this.git = git;
        this.gitCommit = gitCommit;
    }

    @Override
    public Blames blame(final Report report) {
        try {
            report.logInfo("Invoking Git blamer to create author and commit information for all affected files");
            report.logInfo("GIT_COMMIT env = '%s'", gitCommit);
            report.logInfo("Git working tree = '%s'", git.getWorkTree());

            ObjectId headCommit = git.revParse(gitCommit);
            if (headCommit == null) {
                report.logError("Could not retrieve HEAD commit, aborting");
                return new Blames();
            }
            report.logInfo("Git commit ID = '%s'", headCommit.getName());

            String workspacePath = getWorkspacePath();
            report.logInfo("Job workspace = '%s'", workspacePath);
            return git.withRepository(new BlameCallback(report, headCommit, workspacePath));
        }
        catch (IOException exception) {
            report.logException(exception, "Computing blame information failed with an exception:");
        }
        catch (GitException exception) {
            report.logException(exception, "Can't determine head commit using 'git rev-parse'. Skipping blame.");
        }
        catch (InterruptedException e) {
            // nothing to do, already logged
        }
        return new Blames();
    }

    private String getWorkspacePath() throws IOException {
        return Paths.get(workspace.getRemote()).toAbsolutePath().normalize().toRealPath().toString();
    }

    /**
     * Starts the blame commands.
     */
    static class BlameCallback implements RepositoryCallback<Blames> {
        private static final long serialVersionUID = 8794666938104738260L;

        private final Report report;
        private final ObjectId headCommit;
        private final String workspace;

        BlameCallback(final Report report, final ObjectId headCommit, final String workspace) {
            this.report = report;
            this.headCommit = headCommit;
            this.workspace = workspace;
        }

        @Override
        public Blames invoke(final Repository repo, final VirtualChannel channel) throws InterruptedException {
            BlameRunner blameRunner = new BlameRunner(repo, headCommit);
            Blames blames = extractAffectedFiles();

            for (BlameRequest request : blames.getRequests()) {
                run(request, blameRunner);
                if (Thread.interrupted()) { // Cancel request by user
                    String message = "Thread was interrupted while computing blame information";
                    report.logInfo(message);
                    throw new InterruptedException(message);
                }
            }

            report.logInfo("-> blamed authors of issues in %d files", blames.size());

            return blames;
        }

        /**
         * Extracts the relative file names of the files that contain annotations to make sure every file is blamed only
         * once.
         *
         * @return a mapping of absolute to relative file names of the conflicting files
         */
        private Blames extractAffectedFiles() {
            Blames blames = new Blames(workspace);

            FilteredLog log = new FilteredLog(report, "Can't create blame requests for some affected files:");
            for (Issue issue : report) {
                if (issue.getLineStart() > 0 && issue.hasFileName()) {
                    String storedFileName = issue.getFileName();
                    if (blames.contains(storedFileName)) {
                        blames.addLine(storedFileName, issue.getLineStart(), log);
                    }
                    else {
                        blames.addLine(storedFileName, issue.getLineStart(), log);
                    }
                }
            }

            log.logSummary();
            if (blames.isEmpty()) {
                report.logInfo("Created no blame requests - Git blame will be skipped");
            }
            else {
                report.logInfo(
                        "Created blame requests for %d files - invoking Git blame on agent for each of the requests",
                        blames.size());
            }
            return blames;
        }

        void run(final BlameRequest request, final BlameRunner blameRunner) {
            FilteredLog log = new FilteredLog(report, "Git blame errors:");
            String fileName = request.getFileName();
            try {
                BlameResult blame = blameRunner.run(fileName);
                if (blame == null) {
                    log.logError("- no blame results for request <%s>.%n", request);
                }
                else {
                    for (int line : request) {
                        int lineIndex = line - 1; // first line is index 0
                        if (lineIndex < blame.getResultContents().size()) {
                            PersonIdent who = blame.getSourceAuthor(lineIndex);
                            if (who == null) {
                                log.logError("- no author information found for line %d in file %s", lineIndex,
                                        fileName);
                            }
                            else {
                                request.setName(line, who.getName());
                                request.setEmail(line, who.getEmailAddress());
                            }
                            RevCommit commit = blame.getSourceCommit(lineIndex);
                            if (commit == null) {
                                log.logError("- no commit ID found for line %d in file %s", lineIndex, fileName);
                            }
                            else {
                                request.setCommit(line, commit.getName());
                            }
                        }
                    }
                }
            }
            catch (GitAPIException | JGitInternalException exception) {
                log.logException(exception, "- error running git blame on '%s' with revision '%s'", fileName, headCommit);
            }
            log.logSummary();
        }
    }

    /**
     * Executes the Git blame command.
     */
    static class BlameRunner {
        private final Repository repo;
        private final ObjectId headCommit;

        BlameRunner(final Repository repo, final ObjectId headCommit) {
            this.repo = repo;
            this.headCommit = headCommit;
        }

        @Nullable
        BlameResult run(final String fileName) throws GitAPIException {
            BlameCommand blame = new BlameCommand(repo);
            blame.setFilePath(fileName);
            blame.setStartCommit(headCommit);
            return blame.call();
        }
    }
}
