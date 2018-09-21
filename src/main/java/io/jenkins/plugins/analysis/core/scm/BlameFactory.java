package io.jenkins.plugins.analysis.core.scm;

import java.util.Collection;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import jenkins.model.Jenkins;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.core.GlobalSettings;
import hudson.scm.NullSCM;
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
     * @param run
     *         the run to get the SCM from
     * @param workspace
     *         the workspace of the build
     * @param listener
     *         the logger to use
     *
     * @return the blamer
     */
    public static Blamer createBlamer(Run<?, ?> run, final FilePath workspace, final TaskListener listener) {
        if (GlobalSettings.instance().getNoAuthors()) {
            return new NullBlamer();
        }

        Jenkins instance = Jenkins.getInstance();
        if (instance.getPlugin("git") != null) {
            SCM scm = getScm(run);
            GitChecker gitChecker = new GitChecker();
            if (gitChecker.isGit(scm)) {
                return gitChecker.createBlamer(run, scm, workspace, listener);
            }
            else {
                logOnlyGitSupported(listener);
            }
        }
        else {
            logOnlyGitSupported(listener);
        }
        return new NullBlamer();
    }

    private static SCM getScm(final Run<?, ?> run) {
        Job<?, ?> job = run.getParent();
        if (job instanceof WorkflowJob) {
            Collection<? extends SCM> scms = ((WorkflowJob) job).getSCMs();
            if (!scms.isEmpty()) {
                return scms.iterator().next(); // TODO: what should we do if more than one SCM has been used
            }
        }
        else if (run instanceof AbstractBuild) {
            AbstractProject project = ((AbstractBuild) run).getProject();
            if (project.getScm() != null) {
                return project.getScm();
            }
            SCM scm = project.getRootProject().getScm();
            if (scm != null) {
                return scm;
            }
        }
        return new NullSCM();
    }

    private static void logOnlyGitSupported(final TaskListener listener) {
        log(listener, "Skipping warnings blame since Git is the only supported SCM up to now.%n");
    }

    private static void log(final TaskListener listener, final String message) {
        listener.getLogger().println(message);
    }
}
