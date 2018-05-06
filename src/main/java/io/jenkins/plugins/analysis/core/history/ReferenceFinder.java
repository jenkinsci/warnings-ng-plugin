package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.Result;
import hudson.model.Run;

import edu.hm.hafner.analysis.Report;

/**
 * Finds a previous result of an analysis run for the same software artifact. Selection of the previous result is
 * delegated to a provided strategy.
 *
 * @author Ullrich Hafner
 * @see ResultSelector
 */
public abstract class ReferenceFinder extends BuildHistory implements ReferenceProvider {
    /**
     * Creates a {@link ReferenceProvider} instance based on the specified properties.
     *
     * @param run
     *         the run to use as baseline when searching for a reference
     * @param selector
     *         selects the type of the result (to get a result for the same type of static analysis)
     * @param ignoreAnalysisResult
     *         if {@code true} then the result of the previous analysis run is ignored when searching for the reference,
     *         otherwise the result of the static analysis reference must be {@link Result#SUCCESS}.
     * @param overallResultMustBeSuccess
     *         if  {@code true} then only runs with an overall result of {@link Result#SUCCESS} are considered as a
     *         reference, otherwise every run that contains results of the same static analysis configuration is
     *         considered
     *
     * @return the created {@link ReferenceProvider}
     */
    @SuppressWarnings("BooleanParameter")
    public static ReferenceProvider create(final Run<?, ?> run, final ResultSelector selector,
            final boolean ignoreAnalysisResult, final boolean overallResultMustBeSuccess) {
        if (ignoreAnalysisResult) {
            return new PreviousRunReference(run, selector, overallResultMustBeSuccess);
        }
        else {
            return new StablePluginReference(run, selector, overallResultMustBeSuccess);
        }
    }

    /**
     * Creates a new instance of {@link ReferenceFinder}.
     *
     * @param baseline
     *         the run to start the history from
     * @param selector
     *         selects the type of the result (to get a result for the same type of static analysis)
     */
    protected ReferenceFinder(final Run<?, ?> baseline, final ResultSelector selector) {
        super(baseline, selector);
    }

    /**
     * Returns the action of the reference build.
     *
     * @return the action of the reference build
     */
    protected abstract Optional<ResultAction> getReferenceAction();

    @Override
    public Optional<Run<?, ?>> getAnalysisRun() {
        return getReferenceAction()
                .map(pipelineResultAction -> pipelineResultAction.getOwner());
    }

    @Override
    public Report getIssues() {
        return getReferenceAction()
                .map(resultAction -> resultAction.getResult().getIssues())
                .orElseGet(Report::new);
    }
}
