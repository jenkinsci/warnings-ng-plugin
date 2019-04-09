package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.RepositoryCallback;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;

public class GsWorker implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(GsWorker.class.getName());
    private static final long serialVersionUID = -9163064033730592685L;

    private final String gitCommit;
    private GitClient git;
    private FilePath workspace;

    public GsWorker(final String gitCommit, final GitClient git) {
        this(gitCommit, git, git.getWorkTree());
    }

    GsWorker(final String gitCommit, final GitClient git, final FilePath workspace) {
        this.gitCommit = gitCommit;
        this.workspace = workspace;
        this.git = git;
    }

    public GsResults process(final Report filteredReport) {
        filteredReport.logInfo("Process GsResults with '%s' results", filteredReport.getSize());

        ObjectId headCommit = null;

        try {
            headCommit = git.revParse(gitCommit);
            if (headCommit == null) {
                filteredReport.logError("Could not retrieve HEAD commit, aborting");
                return new GsResults();
            }
            for (Issue issue : filteredReport) {
                filteredReport.logInfo("issue: '%s'", issue.toString());
            }
            return git.withRepository(new GsWorker.GsCallback(filteredReport, headCommit, getWorkspacePath()));
        }
        catch (Exception e) {
            filteredReport.logException(e, "Computing gs information failed with an exception:");

        }
        return new GsResults();
    }

    private String getWorkspacePath() throws IOException {
        return Paths.get(workspace.getRemote()).toAbsolutePath().normalize().toRealPath().toString();
    }

    /**
     * Starts the blame commands.
     */
    static class GsCallback implements RepositoryCallback<GsResults> {

        private static final long serialVersionUID = 8794666938104711160L;

        private final Report report;
        private final ObjectId headCommit;
        private final String workspace;

        public GsCallback(final Report report, final ObjectId headCommit, final String workspace) {
            this.report = report;
            this.headCommit = headCommit;
            this.workspace = workspace;
        }

        @Override
        public GsResults invoke(final Repository repository, final VirtualChannel virtualChannel)
                throws IOException, InterruptedException {
            return new GsResults();
        }

    }
}
