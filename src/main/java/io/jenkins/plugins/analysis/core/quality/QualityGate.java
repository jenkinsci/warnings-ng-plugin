package io.jenkins.plugins.analysis.core.quality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.quality.ThresholdSet.ThresholdResult;
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
     * @param thresholds
     *         the thresholds to apply
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
    public QualityGateResult evaluate(final AnalysisResult run) {
        return new QualityGateResult(
                getTotalFailedThreshold().evaluate(
                        run.getTotalSize(),
                        run.getTotalHighPrioritySize(),
                        run.getTotalNormalPrioritySize(),
                        run.getTotalLowPrioritySize()),
                getTotalUnstableThreshold().evaluate(
                        run.getTotalSize(),
                        run.getTotalHighPrioritySize(),
                        run.getTotalNormalPrioritySize(),
                        run.getTotalLowPrioritySize()),
                getNewFailedThreshold().evaluate(
                        run.getNewSize(),
                        run.getNewHighPrioritySize(),
                        run.getNewNormalPrioritySize(),
                        run.getNewLowPrioritySize()),
                getNewUnstableThreshold().evaluate(
                        run.getNewSize(),
                        run.getNewHighPrioritySize(),
                        run.getNewNormalPrioritySize(),
                        run.getNewLowPrioritySize()));
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

    public boolean isEnabled() {
        return totalFailedThreshold.isEnabled()
                || totalUnstableThreshold.isEnabled()
                || newFailedThreshold.isEnabled()
                || newUnstableThreshold.isEnabled();
    }

    /**
     * Result of the {@link QualityGate} evaluation.
     */ 
    public static class QualityGateResult implements Serializable {
        private static final long serialVersionUID = -629019781304545613L;

        private final ThresholdResult totalFailed;
        private final ThresholdResult totalUnstable;
        private final ThresholdResult newFailed;
        private final ThresholdResult newUnstable;

        QualityGateResult(final ThresholdResult totalFailed,
                final ThresholdResult totalUnstable, final ThresholdResult newFailed,
                final ThresholdResult newUnstable) {
            this.totalFailed = totalFailed;
            this.totalUnstable = totalUnstable;
            this.newFailed = newFailed;
            this.newUnstable = newUnstable;
        }

        /**
         * Returns the overall result for the set quality gates. 
         * 
         * @return the overall result
         */
        public Result getOverallResult() {
            if (!totalFailed.isSuccess() || !newFailed.isSuccess()) {
                return Result.FAILURE;
            }
            if (!totalUnstable.isSuccess() || !newUnstable.isSuccess()) {
                return Result.UNSTABLE;
            }
            return Result.SUCCESS;
        }

        public Collection<String> getEvaluations(final AnalysisResult issues, final Thresholds thresholds) {
            List<String> messages = new ArrayList<>();
            if (!totalFailed.isTotalReached()) {
                messages.add(String.format("FAILURE -> Total number of issues: %d - Quality Gate: %d",
                        issues.getTotalSize(), thresholds.failedTotalAll));
            }
            if (!totalFailed.isHighReached()) {
                messages.add(String.format("FAILURE -> Number of high priority issues: %d - Quality Gate: %d",
                        issues.getTotalHighPrioritySize(), thresholds.failedTotalHigh));
            }
            if (!totalFailed.isNormalReached()) {
                messages.add(String.format("FAILURE -> Number of normal priority issues: %d - Quality Gate: %d",
                        issues.getTotalNormalPrioritySize(), thresholds.failedTotalNormal));
            }
            if (!totalFailed.isLowReached()) {
                messages.add(String.format("FAILURE -> Number of low priority issues: %d - Quality Gate: %d",
                        issues.getTotalLowPrioritySize(), thresholds.failedTotalLow));
            }
            if (!totalUnstable.isTotalReached()) {
                messages.add(String.format("UNSTABLE -> Total number of issues: %d - Quality Gate: %d",
                        issues.getTotalSize(), thresholds.unstableTotalAll));
            }
            if (!totalUnstable.isHighReached()) {
                messages.add(String.format("UNSTABLE -> Number of high priority issues: %d - Quality Gate: %d",
                        issues.getTotalHighPrioritySize(), thresholds.unstableTotalHigh));
            }
            if (!totalUnstable.isNormalReached()) {
                messages.add(String.format("UNSTABLE -> Number of normal priority issues: %d - Quality Gate: %d",
                        issues.getTotalNormalPrioritySize(), thresholds.unstableTotalNormal));
            }
            if (!totalUnstable.isLowReached()) {
                messages.add(String.format("UNSTABLE -> Number of low priority issues: %d - Quality Gate: %d",
                        issues.getTotalLowPrioritySize(), thresholds.unstableTotalLow));
            }
            if (!newFailed.isTotalReached()) {
                messages.add(String.format("FAILURE -> Number of new issues: %d - Quality Gate: %d",
                        issues.getNewSize(), thresholds.failedNewAll));
            }
            if (!newFailed.isHighReached()) {
                messages.add(String.format("FAILURE -> Number of new high priority issues: %d - Quality Gate: %d",
                        issues.getNewHighPrioritySize(), thresholds.failedNewHigh));
            }
            if (!newFailed.isNormalReached()) {
                messages.add(String.format("FAILURE -> Number of new normal priority issues: %d - Quality Gate: %d",
                        issues.getNewNormalPrioritySize(), thresholds.failedNewNormal));
            }
            if (!newFailed.isLowReached()) {
                messages.add(String.format("FAILURE -> Number of new low priority issues: %d - Quality Gate: %d",
                        issues.getNewLowPrioritySize(), thresholds.failedNewLow));
            }
            if (!newUnstable.isTotalReached()) {
                messages.add(String.format("UNSTABLE -> New number of new issues: %d - Quality Gate: %d",
                        issues.getNewSize(), thresholds.unstableNewAll));
            }
            if (!newUnstable.isHighReached()) {
                messages.add(String.format("UNSTABLE -> Number of new high priority issues: %d - Quality Gate: %d",
                        issues.getNewHighPrioritySize(), thresholds.unstableNewHigh));
            }
            if (!newUnstable.isNormalReached()) {
                messages.add(String.format("UNSTABLE -> Number of new normal priority issues: %d - Quality Gate: %d",
                        issues.getNewNormalPrioritySize(), thresholds.unstableNewNormal));
            }
            if (!newUnstable.isLowReached()) {
                messages.add(String.format("UNSTABLE -> Number of new low priority issues: %d - Quality Gate: %d",
                        issues.getNewLowPrioritySize(), thresholds.unstableNewLow));
            }
            return messages;
        }
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
         */
        public QualityGate build() {
            return new QualityGate(totalFailedThreshold, totalUnstableThreshold, newFailedThreshold,
                    newUnstableThreshold);
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
