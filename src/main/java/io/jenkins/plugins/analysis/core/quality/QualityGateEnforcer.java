package io.jenkins.plugins.analysis.core.quality;

import hudson.model.Result;

/**
 * Enforces the defined quality gates.
 *
 * @author Ullrich Hafner
 */
public class QualityGateEnforcer {
    public Result evaluate(final StaticAnalysisRun run, final QualityGate qualityGate) {
        if (qualityGate.getUnstableThreshold().hasTotalThreshold()) {
            if (run.getTotalSize() >= qualityGate.getUnstableThreshold().getTotalThreshold()) {
                return Result.FAILURE;
            }
        }

        if (qualityGate.getUnstableThreshold().hasHighThreshold()) {
            if (run.getTotalHighPrioritySize() >= qualityGate.getUnstableThreshold().getHighThreshold()) {
                return Result.FAILURE;
            }
        }

        if (qualityGate.getUnstableThreshold().hasNormalThreshold()) {
            if (run.getTotalNormalPrioritySize() >= qualityGate.getUnstableThreshold().getNormalThreshold()) {
                return Result.FAILURE;
            }
        }

        if (qualityGate.getUnstableThreshold().hasLowThreshold()) {
            if (run.getTotalLowPrioritySize() >= qualityGate.getUnstableThreshold().getLowThreshold()) {
                return Result.FAILURE;
            }
        }


        return Result.SUCCESS;
    }
}
