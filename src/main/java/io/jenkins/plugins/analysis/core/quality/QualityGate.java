package io.jenkins.plugins.analysis.core.quality;

import hudson.model.Result;

/**
 * Defines quality gates for a static analysis run.
 * This class is test driven developed.
 *
 * @author Michael Schmid
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

    /**
     * Evaluate if the static analysis run follows the staid quality gate.
     * @param run to check against the quality gate
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

    static class QualityGateBuilder {
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

    static class ThresholdSet {
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

        public boolean isTotalThresholdReached(final int toCheck) {
            return isSingleThresholdReached(getTotalThreshold(), toCheck);
        }

        private int getTotalThreshold() {
            return totalThreshold;
        }

        public boolean isHighThresholdReached(final int toCheck) {
            return isSingleThresholdReached(getHighThreshold(), toCheck);
        }

        private int getHighThreshold() {
            return highThreshold;
        }

        public boolean isNormalThresholdReached(final int toCheck) {
            return isSingleThresholdReached(getNormalThreshold(), toCheck);
        }

        private int getNormalThreshold() {
            return normalThreshold;
        }

        public boolean isLowThresholdReached(final int toCheck) {
            return isSingleThresholdReached(getLowThreshold(), toCheck);
        }

        private int getLowThreshold() {
            return lowThreshold;
        }

        /**
         * Check if the thresholds is retched or exceeded by the count of warnings.
         *
         * @param threshold
         *         to check id reached or exceeded
         * @param toCheck
         *         count of warnings which should be checked against the threshold
         *
         * @return true if reached or exceeded, else false
         */
        private boolean isSingleThresholdReached(final int threshold, final int toCheck) {
            boolean result = false;
            if (threshold > 0 && toCheck >= threshold) {
                result = true;
            }
            return result;
        }

        /**
         * Check if one or more of the thresholds is retched or exceeded.
         *
         * @param totalToCheck
         *         total count of warnings
         * @param highToCheck
         *         count of high prioritized warnings
         * @param normalToCheck
         *         count of normal prioritized warnings
         * @param lowToCheck
         *         count of low prioritized warnings
         *
         * @return true if one or more of the counts retched or exceeded the threshold, else false
         */
        public boolean isThresholdReached(final int totalToCheck, final int highToCheck, final int normalToCheck, final int lowToCheck) {
            return isTotalThresholdReached(totalToCheck)
                    || isHighThresholdReached(highToCheck)
                    || isNormalThresholdReached(normalToCheck)
                    || isLowThresholdReached(lowToCheck);
        }
    }

}
