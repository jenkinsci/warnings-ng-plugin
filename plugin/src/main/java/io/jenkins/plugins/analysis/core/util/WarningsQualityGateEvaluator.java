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
    static final String MODIFIED_CODE_UNKNOWN_MESSAGE
            = "Modified code is unknown: the SCM code delta could not be computed";

    private final IssuesStatistics statistics;
    private final boolean isModifiedCodeAvailable;

    /**
     * Creates a new instance of {@link WarningsQualityGateEvaluator}.
     *
     * @param qualityGates
     *         the quality gates to evaluate
     * @param statistics
     *         the statistics to evaluate
     */
    public WarningsQualityGateEvaluator(final Collection<? extends WarningsQualityGate> qualityGates,
            final IssuesStatistics statistics) {
        this(qualityGates, statistics, true);
    }

    /**
     * Creates a new instance of {@link WarningsQualityGateEvaluator}.
     *
     * @param qualityGates
     *         the quality gates to evaluate
     * @param statistics
     *         the statistics to evaluate
     * @param isModifiedCodeAvailable
     *         determines whether the modified code of the current build is known, i.e., whether the SCM code delta
     *         between the current build and the reference build has been computed successfully. If the code delta is
     *         not available, then all quality gates that are based on the modified code will be skipped rather than
     *         silently evaluated against a size of zero.
     */
    public WarningsQualityGateEvaluator(final Collection<? extends WarningsQualityGate> qualityGates,
            final IssuesStatistics statistics, final boolean isModifiedCodeAvailable) {
        super(qualityGates);

        this.statistics = statistics;
        this.isModifiedCodeAvailable = isModifiedCodeAvailable;
    }

    @Override
    protected void evaluate(final WarningsQualityGate qualityGate, final QualityGateResult result) {
        if (!isModifiedCodeAvailable && qualityGate.getType().isBasedOnModifiedCode()) {
            result.add(qualityGate, QualityGateStatus.INACTIVE, MODIFIED_CODE_UNKNOWN_MESSAGE);
        }
        else if (qualityGate.getThreshold() > 0) {
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
