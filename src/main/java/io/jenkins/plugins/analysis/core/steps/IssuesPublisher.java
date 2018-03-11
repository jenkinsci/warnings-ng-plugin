package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.Nonnull;

import io.jenkins.plugins.analysis.core.views.ResultAction;

import jenkins.tasks.SimpleBuildStep;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

/**
 * Freestyle or Maven job {@link Recorder} that scans files or the console log for issues. Publishes the created issues
 * in a {@link ResultAction} in the associated run.
 *
 * @author Ullrich Hafner
 */
public class IssuesPublisher extends Recorder implements SimpleBuildStep {
    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher, @Nonnull final TaskListener listener) {

    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
