package io.jenkins.plugins.analysis.core.scm;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;

import io.jenkins.plugins.analysis.core.scm.GitChecker.NullGsWorker;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

public class GsFactory {

    private GsFactory() {
        // prevents instantiation
    }

    public static GsWorker createGsWorker(final Run<?, ?> run, final FilePath workspace, final TaskListener listener) {
        if (new JenkinsFacade().isPluginInstalled("git")) {
            SCM scm = getScm(run);
            GitChecker gitChecker = new GitChecker();
            if (gitChecker.isGit(scm)) {
                return gitChecker.createGsWorker(run, scm, workspace, listener);
            }
        }

        listener.getLogger().println("Skipping issues blame since Git is the only supported SCM up to now.");

        return new NullGsWorker();
    }

    private static SCM getScm(final Run<?, ?> run) {
        return GitHelper.getScm(run);
    }
}
