package io.jenkins.plugins.analysis.core.quality;

import java.io.Serializable;

import com.google.errorprone.annotations.FormatMethod;

import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.model.DeltaReport;
import io.jenkins.plugins.analysis.core.quality.ThresholdSet.ThresholdSetBuilder;

/**
 * Defines quality gates for a static analysis report.
 *
 * @author Michael Schmid
 */
public class QualityGate implements Serializable {
    private static final long serialVersionUID = 7408382033276007723L;
    
    private static final String TOTAL_NUMBER_OF_ISSUES = "Total number of issues";
    private static final String NUMBER_OF_NEW_ISSUES = "Number of new issues";

    private final ThresholdSet totalUnstableThreshold;
    private final ThresholdSet totalFailedThreshold;
    private final ThresholdSet newUnstableThreshold;
    private final ThresholdSet newFailedThreshold;

    /**
     * Creates a new instance of {@link QualityGate}.
     *
     * @param thresholds
     *         the thresholds to apply
     */
    public QualityGate(final Thresholds thresholds) {
        ThresholdSetBuilder builder = new ThresholdSetBuilder();

        builder.setTotalThreshold(thresholds.failedTotalAll);
        builder.setHighThreshold(thresholds.failedTotalHigh);
        builder.setNormalThreshold(thresholds.failedTotalNormal);
        builder.setLowThreshold(thresholds.failedTotalLow);
        totalFailedThreshold = builder.build();

        builder.setTotalThreshold(thresholds.unstableTotalAll);
        builder.setHighThreshold(thresholds.unstableTotalHigh);
        builder.setNormalThreshold(thresholds.unstableTotalNormal);
        builder.setLowThreshold(thresholds.unstableTotalLow);
        totalUnstableThreshold = builder.build();

        builder.setTotalThreshold(thresholds.failedNewAll);
        builder.setHighThreshold(thresholds.failedNewHigh);
        builder.setNormalThreshold(thresholds.failedNewNormal);
        builder.setLowThreshold(thresholds.failedNewLow);
        newFailedThreshold = builder.build();

        builder.setTotalThreshold(thresholds.unstableNewAll);
        builder.setHighThreshold(thresholds.unstableNewHigh);
        builder.setNormalThreshold(thresholds.unstableNewNormal);
        builder.setLowThreshold(thresholds.unstableNewLow);
        newUnstableThreshold = builder.build();
    }

    private QualityGate(final ThresholdSet totalFailedThreshold, final ThresholdSet totalUnstableThreshold,
            final ThresholdSet newFailedThreshold, final ThresholdSet newUnstableThreshold) {
        this.totalFailedThreshold = totalFailedThreshold;
        this.totalUnstableThreshold = totalUnstableThreshold;
        this.newFailedThreshold = newFailedThreshold;
        this.newUnstableThreshold = newUnstableThreshold;
    }

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
    // TODO: l10n
    public QualityGateStatus evaluate(final DeltaReport report, final FormattedLogger logger) {
        QualityGateStatus totalFailedStatus = totalFailedThreshold.evaluate(
                report.getTotalSize(),
                report.getTotalSizeOf(Severity.WARNING_HIGH),
                report.getTotalSizeOf(Severity.WARNING_NORMAL),
                report.getTotalSizeOf(Severity.WARNING_LOW),
                TOTAL_NUMBER_OF_ISSUES, QualityGateStatus.FAILED, logger);
        QualityGateStatus totalUnstableStatus = totalUnstableThreshold.evaluate(
                report.getTotalSize(),
                report.getTotalSizeOf(Severity.WARNING_HIGH),
                report.getTotalSizeOf(Severity.WARNING_NORMAL),
                report.getTotalSizeOf(Severity.WARNING_LOW),
                TOTAL_NUMBER_OF_ISSUES, QualityGateStatus.WARNING, logger);
        QualityGateStatus newFailedStatus = newFailedThreshold.evaluate(
                report.getNewSize(),
                report.getNewSizeOf(Severity.WARNING_HIGH),
                report.getNewSizeOf(Severity.WARNING_NORMAL),
                report.getNewSizeOf(Severity.WARNING_LOW),
                NUMBER_OF_NEW_ISSUES, QualityGateStatus.FAILED, logger);
        QualityGateStatus newUnstableStatus = newUnstableThreshold.evaluate(
                report.getNewSize(),
                report.getNewSizeOf(Severity.WARNING_HIGH),
                report.getNewSizeOf(Severity.WARNING_NORMAL),
                report.getNewSizeOf(Severity.WARNING_LOW),
                NUMBER_OF_NEW_ISSUES, QualityGateStatus.WARNING, logger);
        if (!totalFailedStatus.isSuccessful() || !newFailedStatus.isSuccessful()) {
            return QualityGateStatus.FAILED;
        }
        if (!totalUnstableStatus.isSuccessful() || !newUnstableStatus.isSuccessful()) {
            return QualityGateStatus.WARNING;
        }
        return QualityGateStatus.PASSED;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QualityGate that = (QualityGate) o;

        return totalUnstableThreshold.equals(that.totalUnstableThreshold) && totalFailedThreshold.equals(
                that.totalFailedThreshold) && newUnstableThreshold.equals(that.newUnstableThreshold)
                && newFailedThreshold.equals(that.newFailedThreshold);
    }

    @Override
    public int hashCode() {
        int result = totalUnstableThreshold.hashCode();
        result = 31 * result + totalFailedThreshold.hashCode();
        result = 31 * result + newUnstableThreshold.hashCode();
        result = 31 * result + newFailedThreshold.hashCode();
        return result;
    }

    public boolean isEnabled() {
        return totalFailedThreshold.isEnabled()
                || totalUnstableThreshold.isEnabled()
                || newFailedThreshold.isEnabled()
                || newUnstableThreshold.isEnabled();
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
         *
         */
        @FormatMethod
        void print(String format, Object... args);
    }

    /**
     * Creates {@link QualityGate} instances using the builder pattern.
     */
    public static class QualityGateBuilder {
        private ThresholdSet totalUnstableThreshold = new ThresholdSet(0, 0, 0, 0);
        private ThresholdSet totalFailedThreshold = new ThresholdSet(0, 0, 0, 0);
        private ThresholdSet newUnstableThreshold = new ThresholdSet(0, 0, 0, 0);
        private ThresholdSet newFailedThreshold = new ThresholdSet(0, 0, 0, 0);

        /**
         * Creates a new {@link QualityGate} instance using the defined parameters.
         *
         * @return the created quality gate
         */
        public QualityGate build() {
            return new QualityGate(totalFailedThreshold, totalUnstableThreshold, newFailedThreshold,
                    newUnstableThreshold);
        }

        /**
         * Set the total unstable threshold.
         *
         * @param totalUnstableThreshold
         *         the threshold
         *
         * @return a quality gate builder with total unstable threshold
         */
        public QualityGateBuilder setTotalUnstableThreshold(final ThresholdSet totalUnstableThreshold) {
            this.totalUnstableThreshold = totalUnstableThreshold;
            return this;
        }

        /**
         * Set the total failed threshold.
         *
         * @param totalFailedThreshold
         *         the threshold
         *
         * @return a quality gate builder with total failed threshold
         */
        public QualityGateBuilder setTotalFailedThreshold(final ThresholdSet totalFailedThreshold) {
            this.totalFailedThreshold = totalFailedThreshold;
            return this;
        }

        /**
         * Set the new unstable threshold.
         *
         * @param newUnstableThreshold
         *         the threshold
         *
         * @return a quality gate builder with new unstable threshold
         */
        public QualityGateBuilder setNewUnstableThreshold(final ThresholdSet newUnstableThreshold) {
            this.newUnstableThreshold = newUnstableThreshold;
            return this;
        }

        /**
         * Set the unstable threshold.
         *
         * @param newFailedThreshold
         *         the threshold
         *
         * @return a quality gate builder with new failed threshold
         */
        public QualityGateBuilder setNewFailedThreshold(final ThresholdSet newFailedThreshold) {
            this.newFailedThreshold = newFailedThreshold;
            return this;
        }
    }
}
