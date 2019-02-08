package io.jenkins.plugins.analysis.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.errorprone.annotations.FormatMethod;

import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;

/**
 * Evaluates a set of quality gates for a static analysis report.
 *
 * @author Ullrich Hafner
 */
public class QualityGateEvaluator {
    private final List<QualityGate> qualityGates = new ArrayList<>();

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
        if (qualityGates.isEmpty()) {
            logger.print("-> INACTIVE - No quality gate defined");

            return QualityGateStatus.INACTIVE;
        }

        QualityGateStatus status = QualityGateStatus.PASSED;

        for (QualityGate qualityGate : qualityGates) {
            int actualSize = qualityGate.getActualSizeMethodReference().apply(report);
            if (actualSize >= qualityGate.getThreshold()) {
                logger.print("-> %s - %s: %d - Quality QualityGate: %d",
                        qualityGate.getStatus(), qualityGate.getName(), actualSize, qualityGate.getThreshold());
                if (qualityGate.getStatus().isWorseThan(status)) {
                    status = qualityGate.getStatus();
                }
            }
            else {
                logger.print("-> PASSED - %s: %d - Quality QualityGate: %d",
                        qualityGate.getName(), actualSize, qualityGate.getThreshold());
            }
        }

        return status;
    }

    /**
     * Appends the specified quality gates to the end of the list of quality gates.
     *
     * @param size
     *         the minimum number of issues that fails the quality gate
     * @param type
     *         the type of the quality gate
     * @param strength
     *         determines whether the quality gate is a warning or failure
     */
    public void add(final int size, final QualityGateType type, final QualityGateResult strength) {
        qualityGates.add(new QualityGate(size, type, strength));
    }

    /**
     * Appends all of the quality gates in the specified collection to the end of the list of quality gates.
     *
     * @param additionalQualityGates
     *         the quality gates to add
     */
    public void addAll(final Collection<? extends QualityGate> additionalQualityGates) {
        this.qualityGates.addAll(additionalQualityGates);
    }

    /**
     * Returns whether at least one quality gate has been added.
     *
     * @return {@code true} if at least one quality gate has been added, {@code false} otherwise
     */
    public boolean isEnabled() {
        return !qualityGates.isEmpty();
    }

    /**
     * Logs results of the quality gate evaluation.
     */
    @FunctionalInterface
    public interface FormattedLogger {
        /**
         * Logs the specified message.
         *
         * @param format
         *         A <a href="../util/Formatter.html#syntax">format string</a>
         * @param args
         *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
         *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be
         *         zero.
         */
        @FormatMethod
        void print(String format, Object... args);
    }
}
