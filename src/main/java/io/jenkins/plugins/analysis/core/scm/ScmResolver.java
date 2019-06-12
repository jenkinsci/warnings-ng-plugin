package io.jenkins.plugins.analysis.core.scm;

import java.util.Collection;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import jenkins.triggers.SCMTriggerItem;

/**
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class ScmResolver {
     public SCM getScm(final Run<?, ?> run) {
        Job<?, ?> job = run.getParent();
        if (run instanceof AbstractBuild) {
            AbstractProject<?, ?> project = ((AbstractBuild) run).getProject();
            if (project.getScm() != null) {
                return project.getScm();
            }
            SCM scm = project.getRootProject().getScm();
            if (scm != null) {
                return scm;
            }
        }
        else if (job instanceof SCMTriggerItem) {
            Collection<? extends SCM> scms = ((SCMTriggerItem) job).getSCMs();
            if (!scms.isEmpty()) {
                return scms.iterator().next(); // TODO: what should we do if more than one SCM has been used
            }
        }
        return new NullSCM();
    }

}
