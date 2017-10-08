package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import io.jenkins.plugins.analysis.core.steps.PipelineResultAction;

import hudson.model.Result;
import hudson.model.Run;

/**
 * Find the last available analysis run for the specified analysis configuration. The status of the analysis result is
 * ignored. You can also specify if the overall result of the run has a result of {@link Result#SUCCESS}.
 *
 * @author Ullrich Hafner
 */
public class PreviousRunReference extends ReferenceFinder {
    private final boolean overallResultMustBeSuccess;

    /**
     * Creates a new instance of {@link PreviousRunReference}.
     *
     * @param baseline
     *         the build to start the history from
     * @param selector
     *         type of the action that contains the build results
     * @param overallResultMustBeSuccess
     *         if  {@code true} then only runs with an overall result of {@link Result#SUCCESS} are considered as a
     *         reference, otherwise every run that contains results of the same static analysis configuration is
     *         considered
     */
    @SuppressWarnings("BooleanParameter")
    public PreviousRunReference(final Run<?, ?> baseline, final ResultSelector selector, final boolean overallResultMustBeSuccess) {
        super(baseline, selector);

        this.overallResultMustBeSuccess = overallResultMustBeSuccess;
    }

    @Override
    protected Optional<PipelineResultAction> getReferenceAction() {
        return getPreviousAction(true, overallResultMustBeSuccess);
    }
}
