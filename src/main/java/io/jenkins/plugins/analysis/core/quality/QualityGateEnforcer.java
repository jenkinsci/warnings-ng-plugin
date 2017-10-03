package io.jenkins.plugins.analysis.core.quality;

import hudson.model.Result;

/**
 * Enforces the defined quality gates.
 *
 * @author Ullrich Hafner
 */
public class QualityGateEnforcer {
    public Result evaluate(final StaticAnalysisRun run, final QualityGate qualityGate) {
        if (qualityGate.hasFailureThreshold()) {
            if (run.getTotalHighPrioritySize() >= qualityGate.getFailureThreshold()) {
                return Result.FAILURE;
            }
        }
        return Result.SUCCESS;
    }
}
