package io.jenkins.plugins.analysis.core.quality;

import java.io.Serializable;

import io.jenkins.plugins.analysis.core.quality.ThresholdSet.ThresholdSetBuilder;

import hudson.model.Result;

/**
 * Defines quality gates for a static analysis run.
 *
 * @author Michael Schmid
 */
public class QualityGate implements Serializable {
    private final ThresholdSet totalUnstableThreshold;
    private final ThresholdSet totalFailedThreshold;
    private final ThresholdSet newUnstableThreshold;
    private final ThresholdSet newFailedThreshold;

    /**
     * Creates a new instance of {@link QualityGate}.
     *
     * @param thresholds the thresholds to apply
     */
    public QualityGate(final hudson.plugins.analysis.core.Thresholds thresholds) {
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

    /**
     * Creates a new instance of {@link QualityGate}.
     *
     * @param thresholds the thresholds to apply
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

    private QualityGate(final ThresholdSet totalFailedThreshold, final ThresholdSet totalUnstableThreshold, final ThresholdSet newFailedThreshold, final ThresholdSet newUnstableThreshold) {
        this.totalFailedThreshold = totalFailedThreshold;
        this.totalUnstableThreshold = totalUnstableThreshold;
        this.newFailedThreshold = newFailedThreshold;
        this.newUnstableThreshold = newUnstableThreshold;
    }

    private ThresholdSet getTotalUnstableThreshold() {
        return totalUnstableThreshold;
    }

    private ThresholdSet getTotalFailedThreshold() {
        return totalFailedThreshold;
    }

    private ThresholdSet getNewUnstableThreshold() {
        return newUnstableThreshold;
    }

    private ThresholdSet getNewFailedThreshold() {
        return newFailedThreshold;
    }

    /**
     * Enforces this quality gate for the specified run.
     *
     * @param run
     *         the run to evaluate
     *
     * @return result of the evaluation, expressed by a build state
     */
    public Result evaluate(final StaticAnalysisRun run) {
        if (getTotalFailedThreshold().isThresholdReached(run.getTotalSize(), run.getTotalHighPrioritySize(), run.getTotalNormalPrioritySize(), run.getTotalLowPrioritySize())
                || getNewFailedThreshold().isThresholdReached(run.getNewSize(), run.getNewHighPrioritySize(), run.getNewNormalPrioritySize(), run.getNewLowPrioritySize())) {
            return Result.FAILURE;
        }

        if (getTotalUnstableThreshold().isThresholdReached(run.getTotalSize(), run.getTotalHighPrioritySize(), run.getTotalNormalPrioritySize(), run.getTotalLowPrioritySize())
                || getNewUnstableThreshold().isThresholdReached(run.getNewSize(), run.getNewHighPrioritySize(), run.getNewNormalPrioritySize(), run.getNewLowPrioritySize())) {
            return Result.UNSTABLE;
        }

        return Result.SUCCESS;
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

        if (!totalUnstableThreshold.equals(that.totalUnstableThreshold)) {
            return false;
        }
        if (!totalFailedThreshold.equals(that.totalFailedThreshold)) {
            return false;
        }
        if (!newUnstableThreshold.equals(that.newUnstableThreshold)) {
            return false;
        }
        return newFailedThreshold.equals(that.newFailedThreshold);
    }

    @Override
    public int hashCode() {
        int result = totalUnstableThreshold.hashCode();
        result = 31 * result + totalFailedThreshold.hashCode();
        result = 31 * result + newUnstableThreshold.hashCode();
        result = 31 * result + newFailedThreshold.hashCode();
        return result;
    }

    public static class QualityGateBuilder {
        private ThresholdSet totalUnstableThreshold = new ThresholdSet(0, 0, 0, 0);
        private ThresholdSet totalFailedThreshold = new ThresholdSet(0, 0, 0, 0);
        private ThresholdSet newUnstableThreshold = new ThresholdSet(0, 0, 0, 0);
        private ThresholdSet newFailedThreshold = new ThresholdSet(0, 0, 0, 0);

        public QualityGate build() {
            return new QualityGate(totalFailedThreshold, totalUnstableThreshold, newFailedThreshold, newUnstableThreshold);
        }

        public QualityGateBuilder setTotalUnstableThreshold(final ThresholdSet totalUnstableThreshold) {
            this.totalUnstableThreshold = totalUnstableThreshold;
            return this;
        }

        public QualityGateBuilder setTotalFailedThreshold(final ThresholdSet totalFailedThreshold) {
            this.totalFailedThreshold = totalFailedThreshold;
            return this;
        }

        public QualityGateBuilder setNewUnstableThreshold(final ThresholdSet newUnstableThreshold) {
            this.newUnstableThreshold = newUnstableThreshold;
            return this;
        }

        public QualityGateBuilder setNewFailedThreshold(final ThresholdSet newFailedThreshold) {
            this.newFailedThreshold = newFailedThreshold;
            return this;
        }
    }
}
