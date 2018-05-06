package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.Result;
import hudson.model.Run;

import edu.hm.hafner.analysis.Report;

/**
 * Find the last available analysis run for the specified job. You can specify if the overall result of the run should
 * be {@link Result#SUCCESS} and if the sub-result of the static analysis run should be considered as well.
 *
 * @author Ullrich Hafner
 */
public class OtherJobReferenceFinder extends ReferenceFinder {
    private final boolean ignoreAnalysisResult;
    private final boolean overallResultMustBeSuccess;

    /**
     * Creates a {@link ReferenceProvider} that obtains the reference results from another job.
     *
     * @param baseline
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
     */
    public OtherJobReferenceFinder(final Run<?, ?> baseline, final ResultSelector selector,
            final boolean ignoreAnalysisResult, final boolean overallResultMustBeSuccess) {
        super(baseline, selector);

        this.ignoreAnalysisResult = ignoreAnalysisResult;
        this.overallResultMustBeSuccess = overallResultMustBeSuccess;
    }

    @Override
    protected Optional<ResultAction> getReferenceAction() {
        Optional<ResultAction> baseline = getBaselineAction();
        if (baseline.isPresent()) {
            return baseline;
        }
        return getPreviousAction(ignoreAnalysisResult, overallResultMustBeSuccess);
    }

    @Override
    public Report getIssues() {
        return getReferenceAction()
                .map(resultAction -> resultAction.getResult().getIssues())
                .orElse(new Report());
    }

    @Override
    public Optional<Run<?, ?>> getAnalysisRun() {
        return getReferenceAction().map(ResultAction::getOwner);
    }
}
