package io.jenkins.plugins.analysis.core.history;

import io.jenkins.plugins.analysis.core.steps.PipelineResultAction;

import hudson.model.Run;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public class StablePluginReference extends ReferenceFinder {
    private boolean mustBeStable;

    /**
     * Creates a new instance of {@link PreviousBuildReference}.
     *
     * @param baseline     the build to start the history from
     * @param selector     type of the action that contains the build results
     * @param mustBeStable builds must be of overall status stable
     */
    public StablePluginReference(final Run<?, ?> baseline, final ResultSelector selector, final boolean mustBeStable) {
        super(baseline, selector);
        this.mustBeStable = mustBeStable;
    }

    @Override
    protected PipelineResultAction getReferenceAction() {
        PipelineResultAction action = getAction(true, mustBeStable);
        if (action == null) {
            return getPreviousAction(); // fallback, use action of previous build regardless of result
        }
        else {
            return action;
        }
    }

}
