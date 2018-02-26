package io.jenkins.plugins.analysis.core.quality;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * Stores a set of four thresholds and checks if one or more of them are reached.
 *
 * @author Michael Schmid
 */
public class ThresholdSet implements Serializable {
    private final int totalThreshold;
    private final int highThreshold;
    private final int normalThreshold;
    private final int lowThreshold;

    public ThresholdSet(final int totalThreshold, final int highThreshold, final int normalThreshold,
            final int lowThreshold) {
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
        return isEnabled(threshold) && toCheck >= threshold;
    }

    private boolean isEnabled(final int threshold) {
        return threshold > 0;
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
    public boolean isThresholdReached(final int totalToCheck, final int highToCheck, final int normalToCheck,
            final int lowToCheck) {
        return isTotalThresholdReached(totalToCheck)
                || isHighThresholdReached(highToCheck)
                || isNormalThresholdReached(normalToCheck)
                || isLowThresholdReached(lowToCheck);
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
     * @return the result of the evaluation
     */
    public ThresholdResult evaluate(final int totalToCheck, final int highToCheck, final int normalToCheck,
            final int lowToCheck) {
        return new ThresholdResult(isTotalThresholdReached(totalToCheck),
                isHighThresholdReached(highToCheck),
                isNormalThresholdReached(normalToCheck),
                isLowThresholdReached(lowToCheck));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ThresholdSet that = (ThresholdSet) o;

        if (totalThreshold != that.totalThreshold) {
            return false;
        }
        if (highThreshold != that.highThreshold) {
            return false;
        }
        if (normalThreshold != that.normalThreshold) {
            return false;
        }
        return lowThreshold == that.lowThreshold;
    }

    @Override
    public int hashCode() {
        int result = totalThreshold;
        result = 31 * result + highThreshold;
        result = 31 * result + normalThreshold;
        result = 31 * result + lowThreshold;
        return result;
    }

    public boolean isEnabled() {
        return isEnabled(totalThreshold)
                || isEnabled(highThreshold)
                || isEnabled(normalThreshold)
                || isEnabled(lowThreshold);
    }

    /**
     * Result of a subset of the {@link QualityGate} evaluation.
     */
    public static class ThresholdResult {
        private final boolean isTotalReached;
        private final boolean isHighReached;
        private final boolean isNormalReached;
        private final boolean isLowReached;

        public ThresholdResult(final boolean isTotalReached, final boolean isHighReached, final boolean isNormalReached,
                final boolean isLowReached) {
            this.isTotalReached = isTotalReached;
            this.isHighReached = isHighReached;
            this.isNormalReached = isNormalReached;
            this.isLowReached = isLowReached;
        }

        public boolean isTotalReached() {
            return isTotalReached;
        }

        public boolean isHighReached() {
            return isHighReached;
        }

        public boolean isNormalReached() {
            return isNormalReached;
        }

        public boolean isLowReached() {
            return isLowReached;
        }

        @SuppressWarnings("OverlyComplexBooleanExpression")
        public boolean isSuccess() {
            return !isTotalReached && !isHighReached && !isNormalReached && !isLowReached;
        }
    }

    /**
     * Creates {@link ThresholdSet} instances using the builder pattern.
     */
    public static class ThresholdSetBuilder {
        private int totalThreshold = 0;
        private int highThreshold = 0;
        private int normalThreshold = 0;
        private int lowThreshold = 0;

        public ThresholdSet build() {
            return new ThresholdSet(getTotalThreshold(), getHighThreshold(), getNormalThreshold(), getLowThreshold());
        }

        /**
         * Sets the threshold for the total size of issues.
         *
         * @param totalThreshold
         *         the number of overall issues
         *
         * @return this
         */
        public ThresholdSetBuilder setTotalThreshold(final int totalThreshold) {
            this.totalThreshold = totalThreshold;
            return this;
        }

        /**
         * Sets the threshold for the total size of issues.
         *
         * @param totalThreshold
         *         the number of overall issues
         *
         * @return this
         */
        public ThresholdSetBuilder setTotalThreshold(final String totalThreshold) {
            return setTotalThreshold(asInt(totalThreshold));
        }

        /**
         * Sets the threshold for the size of high priority issues.
         *
         * @param highThreshold
         *         the number of high priority issues
         *
         * @return this
         */
        public ThresholdSetBuilder setHighThreshold(final int highThreshold) {
            this.highThreshold = highThreshold;
            return this;
        }

        /**
         * Sets the threshold for the size of high priority issues.
         *
         * @param highThreshold
         *         the number of high priority issues
         *
         * @return this
         */
        public ThresholdSetBuilder setHighThreshold(final String highThreshold) {
            return setHighThreshold(asInt(highThreshold));
        }

        /**
         * Sets the threshold for the size of normal priority issues.
         *
         * @param normalThreshold
         *         the number of normal priority issues
         *
         * @return this
         */
        public ThresholdSetBuilder setNormalThreshold(final int normalThreshold) {
            this.normalThreshold = normalThreshold;
            return this;
        }

        /**
         * Sets the threshold for the size of normal priority issues.
         *
         * @param normalThreshold
         *         the number of normal priority issues
         *
         * @return this
         */
        public ThresholdSetBuilder setNormalThreshold(final String normalThreshold) {
            return setNormalThreshold(asInt(normalThreshold));
        }

        /**
         * Sets the threshold for the size of low priority issues.
         *
         * @param lowThreshold
         *         the number of low priority issues
         *
         * @return this
         */
        public ThresholdSetBuilder setLowThreshold(final int lowThreshold) {
            this.lowThreshold = lowThreshold;
            return this;
        }

        /**
         * Sets the threshold for the size of low priority issues.
         *
         * @param lowThreshold
         *         the number of low priority issues
         *
         * @return this
         */
        public ThresholdSetBuilder setLowThreshold(final String lowThreshold) {
            return setLowThreshold(asInt(lowThreshold));
        }

        /**
         * Converts the provided string threshold into an integer value.
         *
         * @param threshold
         *         string representation of the threshold value
         *
         * @return integer threshold
         * @throws IllegalArgumentException
         *         if the provided string can't be converted to an integer value
         *         greater or equal zero
         */
        private int asInt(final String threshold) {
            if (StringUtils.isNotBlank(threshold)) {
                try {
                    int value = Integer.parseInt(threshold);
                    if (value >= 0) { // negative values are not allowed
                        return value;
                    }
                }
                catch (NumberFormatException exception) {
                    // not valid
                }
                throw new IllegalArgumentException("Not a readable integer value >= 0: " + threshold);
            }
            return 0; // a blank string means that a user disables the check
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

