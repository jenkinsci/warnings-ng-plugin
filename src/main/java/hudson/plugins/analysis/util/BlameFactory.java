package hudson.plugins.analysis.util;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;

/**
 * Selects a matching SCM blamer for the specified job.
 *
 * @author Lukas Krose
 */
public class BlameFactory {
    /**
     * Selects a matching SCM blamer for the specified job.
     *
     * @param run       the run to get the SCM from
     * @param workspace the path to the workspace
     * @param logger    the plugin logger
     * @param listener  task listener
     * @return the blamer
     */
    public static Blamer createBlamer(Run<?, ?> run, FilePath workspace, PluginLogger logger, final TaskListener listener) {
        if (run instanceof AbstractBuild) { // pipelines do not have an SCM link
            AbstractBuild build = (AbstractBuild) run;
            AbstractProject project = build.getProject();
            SCM scm = project.getScm();
            if (scm instanceof GitSCM) {
                return new GitBlamer(build, (GitSCM) scm, workspace, logger, listener);
            }
        }
        return new NullBlamer();
    }
}
