package io.jenkins.plugins.analysis.core.scm;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;

import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

/**
 * Selects a matching SCM blamer for the specified job. Currently, only Git is supported.
 *
 * @author Lukas Krose
 */
public final class BlameFactory {
    /**
     * Selects a matching SCM blamer for the specified job.
     *
     * @param run
     *         the run to get the SCM from
     * @param workspace
     *         the workspace of the build
     * @param listener
     *         the logger to use
     *
     * @return the blamer
     */
    public static Blamer createBlamer(final Run<?, ?> run, final FilePath workspace, final TaskListener listener) {
        if (new JenkinsFacade().isPluginInstalled("git")) {
            SCM scm = getScm(run);
            GitChecker gitChecker = new GitChecker();
            if (gitChecker.isGit(scm)) {
                return gitChecker.createBlamer(run, scm, workspace, listener);
            }
        }

        listener.getLogger().println("Skipping issues blame since Git is the only supported SCM up to now.");

        return new NullBlamer();
    }

    private static SCM getScm(final Run<?, ?> run) {
        return GitHelper.getScm(run);
    }

    private BlameFactory() {
        // prevents instantiation
    }
}
