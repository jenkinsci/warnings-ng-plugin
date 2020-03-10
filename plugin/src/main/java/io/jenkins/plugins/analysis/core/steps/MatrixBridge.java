package io.jenkins.plugins.analysis.core.steps;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;

/**
 * Provides a {@link MatrixAggregatable} for a {@link MatrixProject}.
 *
 * @author Ullrich Hafner
 */
@Extension(optional = true)
public class MatrixBridge implements MatrixAggregatable {
    @Override
    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener) {
        IssuesRecorder recorder = build.getParent().getPublishersList().get(IssuesRecorder.class);
        if (recorder == null) {
            return null;
        }
        return new IssuesAggregator(build, launcher, listener, recorder);
    }
}
