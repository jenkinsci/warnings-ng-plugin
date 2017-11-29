package io.jenkins.plugins.analysis.core.quality;

import hudson.model.Result;

/**
 * Enforces the defined quality gates.
 *
 * @author Ullrich Hafner
 */
public class QualityGateEnforcer {
    /**
     * Evaluates the specified quality gate for the given run.
     *
     * @param run
     *         the run to evaluate
     * @param qualityGate
     *         the quality gate settings
     *
     * @return the result of the evaluation
     */
    public Result evaluate(final StaticAnalysisRun run, final QualityGate qualityGate) {
        if (qualityGate.hasFailureThreshold()) {
            if (run.getTotalSize() >= qualityGate.getFailureThreshold()) {
                return Result.FAILURE;
            }
        }
        return Result.SUCCESS;
    }
}
