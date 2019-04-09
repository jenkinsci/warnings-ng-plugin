package io.jenkins.plugins.analysis.core.scm;

import java.util.Collection;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.NullSCM;
import hudson.scm.SCM;

public class GitHelper {

    public static SCM getScm(final Run<?, ?> run) {
        Job<?, ?> job = run.getParent();
        if (job instanceof WorkflowJob) {
            Collection<? extends SCM> scms = ((WorkflowJob) job).getSCMs();
            if (!scms.isEmpty()) {
                return scms.iterator().next(); // TODO: what should we do if more than one SCM has been used
            }
        }
        else if (run instanceof AbstractBuild) {
            AbstractProject<?, ?> project = ((AbstractBuild) run).getProject();
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
}
