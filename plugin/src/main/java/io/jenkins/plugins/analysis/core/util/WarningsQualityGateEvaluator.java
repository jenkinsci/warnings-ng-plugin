package io.jenkins.plugins.analysis.core.util;

import java.util.Collection;

import io.jenkins.plugins.util.QualityGateEvaluator;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;

/**
 * Evaluates a given set of quality gates.
 *
 * @author Johannes Walter
 */
public class WarningsQualityGateEvaluator extends QualityGateEvaluator<WarningsQualityGate> {
    private final IssuesStatistics statistics;

    public WarningsQualityGateEvaluator(final Collection<? extends WarningsQualityGate> qualityGates,
            final IssuesStatistics statistics) {
        super(qualityGates);

        this.statistics = statistics;
    }

    @Override
    protected void evaluate(final WarningsQualityGate qualityGate, final QualityGateResult result) {
        if (qualityGate.getThreshold() > 0) {
            int actualSize = qualityGate.getActualSizeMethodReference().apply(statistics);
            var actualValue = String.valueOf(actualSize);
            if (actualSize >= qualityGate.getThreshold()) {
                result.add(qualityGate, qualityGate.getStatus(), actualValue);
            }
            else {
                result.add(qualityGate, QualityGateStatus.PASSED, actualValue);
            }
        }
        else {
            result.add(qualityGate, QualityGateStatus.INACTIVE, "Threshold too small: " + qualityGate.getThreshold());
        }
    }
}
