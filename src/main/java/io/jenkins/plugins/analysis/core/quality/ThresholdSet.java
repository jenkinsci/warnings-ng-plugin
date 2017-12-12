package io.jenkins.plugins.analysis.core.quality;

/**
 * Stores a set of four thresholds and checks if one or more of them are reached.
 * This class is test driven developed.
 *
 * @author Michael Schmid
 */
public class ThresholdSet {
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

    static class ThresholdSetBuilder {
        private int totalThreshold = 0;
        private int highThreshold = 0;
        private int normalThreshold = 0;
        private int lowThreshold = 0;

        public ThresholdSet build() {
            return new ThresholdSet(getTotalThreshold(), getHighThreshold(), getNormalThreshold(), getLowThreshold());
        }

        public ThresholdSetBuilder setTotalThreshold(final int totalThreshold) {
            this.totalThreshold = totalThreshold;
            return this;
        }

        public ThresholdSetBuilder setHighThreshold(final int highThreshold) {
            this.highThreshold = highThreshold;
            return this;
        }

        public ThresholdSetBuilder setNormalThreshold(final int normalThreshold) {
            this.normalThreshold = normalThreshold;
            return this;
        }

        public ThresholdSetBuilder setLowThreshold(final int lowThreshold) {
            this.lowThreshold = lowThreshold;
            return this;
        }

        private int getTotalThreshold() {
            return totalThreshold;
        }

        private int getHighThreshold() {
            return highThreshold;
        }

        private int getNormalThreshold() {
            return normalThreshold;
        }

        private int getLowThreshold() {
            return lowThreshold;
        }

    }
}

