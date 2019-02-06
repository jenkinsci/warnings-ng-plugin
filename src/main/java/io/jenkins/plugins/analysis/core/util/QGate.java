package io.jenkins.plugins.analysis.core.util;

import java.util.function.IntSupplier;

import io.jenkins.plugins.analysis.core.util.QualityGate.FormattedLogger;

/**
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class QGate {

    /**
     * Enforces this quality gate for the specified run.
     *
     * @param report
     *         the report to evaluate
     * @param logger
     *         the logger that reports the passed and failed quality gate thresholds
     *
     * @return result of the evaluation, expressed by a build state
     */
    public QualityGateStatus evaluate(final IssuesStatistics report, final FormattedLogger logger) {
        return QualityGateStatus.PASSED;
    }

    public void add(final int size, final IntSupplier resultMethod, final QualityGateStatus status) {
    }
}
