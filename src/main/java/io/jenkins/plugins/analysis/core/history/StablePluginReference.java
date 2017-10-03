package io.jenkins.plugins.analysis.core.history;

import io.jenkins.plugins.analysis.core.steps.PipelineResultAction;

import hudson.model.Result;
import hudson.model.Run;

/**
 * Find the last available analysis run with a result {@link Result#SUCCESS} for the specified analysis configuration.
 * You can also specify if the overall result of the run has a result of {@link Result#SUCCESS}.
 *
 * @author Ullrich Hafner
 */
public class StablePluginReference extends ReferenceFinder {
    private boolean overallResultMustBeSuccess;

    /**
     * Creates a new instance of {@link StablePluginReference}.
     *
     * @param baseline
     *         the build to start the history from
     * @param selector
     *         type of the action that contains the build results
     * @param overallResultMustBeSuccess
     *         builds must be of overall status stable
     */
    public StablePluginReference(final Run<?, ?> baseline, final ResultSelector selector,
            final boolean overallResultMustBeSuccess) {
        super(baseline, selector);

        this.overallResultMustBeSuccess = overallResultMustBeSuccess;
    }

    @Override
    protected PipelineResultAction getReferenceAction() {
        return getPreviousAction(false, overallResultMustBeSuccess);
    }
}
