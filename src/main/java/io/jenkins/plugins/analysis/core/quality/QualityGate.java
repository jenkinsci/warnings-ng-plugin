package io.jenkins.plugins.analysis.core.quality;

import hudson.model.Result;

/**
 * Defines quality gates for a static analysis run.
 *
 * @author Ullrich Hafner
 */
public class QualityGate {
    private final ThresholdSet totalUnstableThreshold;
    private final ThresholdSet totalFailedThreshold;
    private final ThresholdSet newUnstableThreshold;
    private final ThresholdSet newFailedThreshold;

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

    public Result evaluate(final StaticAnalysisRun run) {
        if (getTotalFailedThreshold().isTotalThresholdReached(run.getTotalSize())
                || getTotalFailedThreshold().isHighThresholdReached(run.getTotalHighPrioritySize())
                || getTotalFailedThreshold().isNormalThresholdReached(run.getTotalNormalPrioritySize())
                || getTotalFailedThreshold().isNormalThresholdReached(run.getTotalNormalPrioritySize())
                || getTotalFailedThreshold().isLowThresholdReached(run.getTotalLowPrioritySize())
                || getNewFailedThreshold().isTotalThresholdReached(run.getNewSize())
                || getNewFailedThreshold().isHighThresholdReached(run.getNewHighPrioritySize())
                || getNewFailedThreshold().isNormalThresholdReached(run.getNewNormalPrioritySize())
                || getNewFailedThreshold().isLowThresholdReached(run.getNewLowPrioritySize())) {
            return Result.FAILURE;
        }

        if (getTotalUnstableThreshold().isTotalThresholdReached(run.getTotalSize())
                || getTotalUnstableThreshold().isHighThresholdReached(run.getTotalHighPrioritySize())
                || getTotalUnstableThreshold().isNormalThresholdReached(run.getTotalNormalPrioritySize())
                || getTotalUnstableThreshold().isLowThresholdReached(run.getTotalLowPrioritySize())
                || getNewUnstableThreshold().isTotalThresholdReached(run.getNewSize())
                || getNewUnstableThreshold().isHighThresholdReached(run.getNewHighPrioritySize())
                || getNewUnstableThreshold().isNormalThresholdReached(run.getNewNormalPrioritySize())
                || getNewUnstableThreshold().isLowThresholdReached(run.getNewLowPrioritySize())) {
            return Result.UNSTABLE;
        }

        return Result.SUCCESS;
    }



    static class QualitiyGateBuilder {
        private ThresholdSet totalUnstableThreshold = new ThresholdSet(0, 0, 0, 0);
        private ThresholdSet totalFailedThreshold = new ThresholdSet(0, 0, 0, 0);
        private ThresholdSet newUnstableThreshold = new ThresholdSet(0, 0, 0, 0);
        private ThresholdSet newFailedThreshold = new ThresholdSet(0, 0, 0, 0);

        public QualityGate build() {
            return new QualityGate(totalFailedThreshold, totalUnstableThreshold, newFailedThreshold, newUnstableThreshold);
        }

        public QualitiyGateBuilder setTotalUnstableThreshold(final ThresholdSet totalUnstableThreshold) {
            this.totalUnstableThreshold = totalUnstableThreshold;
            return this;
        }

        public QualitiyGateBuilder setTotalFailedThreshold(final ThresholdSet totalFailedThreshold) {
            this.totalFailedThreshold = totalFailedThreshold;
            return this;
        }

        public QualitiyGateBuilder setNewUnstableThreshold(final ThresholdSet newUnstableThreshold) {
            this.newUnstableThreshold = newUnstableThreshold;
            return this;
        }

        public QualitiyGateBuilder setNewFailedThreshold(final ThresholdSet newFailedThreshold) {
            this.newFailedThreshold = newFailedThreshold;
            return this;
        }
    }

    static class ThresholdSet{
        private final int totalThreshold;
        private final int highThreshold;
        private final int normalThreshold;
        private final int lowThreshold;

        public ThresholdSet(final int totalThreshold, final int highThreshold, final int normalThreshold, final int lowThreshold) {
            this.totalThreshold = totalThreshold;
            this.highThreshold = highThreshold;
            this.normalThreshold = normalThreshold;
            this.lowThreshold = lowThreshold;
        }

        public boolean isTotalThresholdReached(int toCheck) {
            return isThresholdReached(getTotalThreshold(), toCheck);
        }

        private int getTotalThreshold() {
            return totalThreshold;
        }

        public boolean isHighThresholdReached(int toCheck) {
            return isThresholdReached(getHighThreshold(), toCheck);
        }

        private int getHighThreshold() {
            return highThreshold;
        }

        public boolean isNormalThresholdReached(int toCheck) {
            return isThresholdReached(getNormalThreshold(), toCheck);
        }

        private int getNormalThreshold() {
            return normalThreshold;
        }

        public boolean isLowThresholdReached(int toCheck) {
            return isThresholdReached(getLowThreshold(), toCheck);
        }

        private int getLowThreshold() {
            return lowThreshold;
        }

        private boolean isThresholdReached(int threshold, int toCheck) {
            boolean result = false;
            if(threshold > 0 && toCheck >= threshold) {
                result = true;
            }
            return result;
        }
    }

}
