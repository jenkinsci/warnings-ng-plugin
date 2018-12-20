package io.jenkins.plugins.analysis.core.util;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.QualityGate.FormattedLogger;

/**
 * Stores a set of four thresholds and checks if one or more of them are reached.
 *
 * @author Michael Schmid
 */
class ThresholdSet implements Serializable {
    private static final long serialVersionUID = -853511213890047492L;

    private final int totalThreshold;
    private final int highThreshold;
    private final int normalThreshold;
    private final int lowThreshold;

    /**
     * Creates a new instance of {@link ThresholdSet}.
     *
     * @param totalThreshold
     *         threshold for the totals number of issues
     * @param highThreshold
     *         threshold for the number of issues of {@link Severity#WARNING_HIGH}
     * @param normalThreshold
     *         threshold for the number of issues of {@link Severity#WARNING_NORMAL}
     * @param lowThreshold
     *         threshold for the number of issues of {@link Severity#WARNING_LOW}
     */
    ThresholdSet(final int totalThreshold, final int highThreshold, final int normalThreshold,
            final int lowThreshold) {
        this.totalThreshold = totalThreshold;
        this.highThreshold = highThreshold;
        this.normalThreshold = normalThreshold;
        this.lowThreshold = lowThreshold;
    }

    private boolean isTotalThresholdReached(final int toCheck) {
        return isSingleThresholdReached(totalThreshold, toCheck);
    }

    private boolean isHighThresholdReached(final int toCheck) {
        return isSingleThresholdReached(highThreshold, toCheck);
    }

    private boolean isNormalThresholdReached(final int toCheck) {
        return isSingleThresholdReached(normalThreshold, toCheck);
    }

    private boolean isLowThresholdReached(final int toCheck) {
        return isSingleThresholdReached(lowThreshold, toCheck);
    }

    /**
     * Check if the thresholds is retched or exceeded by the count of issues.
     *
     * @param threshold
     *         to check id reached or exceeded
     * @param toCheck
     *         count of issues which should be checked against the threshold
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
     * Checks whether one or more of the thresholds are exceeded.
     *
     * @param totalToCheck
     *         total count of issues
     * @param highToCheck
     *         count of high prioritized issues
     * @param normalToCheck
     *         count of normal prioritized issues
     * @param lowToCheck
     *         count of low prioritized issues
     * @param message
     *         the message that identifies total number of issues or number of new issues
     * @param qualityGateStatus
     *         the qualityGateStatus that should be returned if the threshold has been reached
     * @param logger
     *         a logger for the evaluation results
     *
     * @return the result of the evaluation
     */
    public QualityGateStatus evaluate(final int totalToCheck, final int highToCheck, final int normalToCheck,
            final int lowToCheck, final String message,
            final QualityGateStatus qualityGateStatus, final FormattedLogger logger) {
        boolean totalThresholdReached = isTotalThresholdReached(totalToCheck);
        boolean highThresholdReached = isHighThresholdReached(highToCheck);
        boolean normalThresholdReached = isNormalThresholdReached(normalToCheck);
        boolean lowThresholdReached = isLowThresholdReached(lowToCheck);
        if (totalThresholdReached) {
            logger.print("-> %s - %s: %d - Quality Gate: %d",
                    qualityGateStatus, message, totalToCheck, totalThreshold);
        }
        if (highThresholdReached) {
            logger.print("-> %s - %s (Severity High): %d - Quality Gate: %d",
                    qualityGateStatus, message, highToCheck, highThreshold);
        }
        if (normalThresholdReached) {
            logger.print("-> %s - %s (Severity Normal): %d - Quality Gate: %d",
                    qualityGateStatus, message, normalToCheck, normalThreshold);
        }
        if (lowThresholdReached) {
            logger.print("-> %s - %s (Severity Low): %d - Quality Gate: %d",
                    qualityGateStatus, message, lowToCheck, lowThreshold);
        }
        if (totalThresholdReached || highThresholdReached || normalThresholdReached || lowThresholdReached) {
            return qualityGateStatus;
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
     * Creates {@link ThresholdSet} instances using the builder pattern.
     */
    static class ThresholdSetBuilder {
        private int totalThreshold = 0;
        private int highThreshold = 0;
        private int normalThreshold = 0;
        private int lowThreshold = 0;

        /**
         * Creates the new {@link ThresholdSet} instance from the current set of properties.
         *
         * @return a new {@link ThresholdSet}
         */
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
        ThresholdSetBuilder setTotalThreshold(final int totalThreshold) {
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
        ThresholdSetBuilder setTotalThreshold(final String totalThreshold) {
            return setTotalThreshold(asInt(totalThreshold));
        }

        /**
         * Sets the threshold for the size of high severity issues.
         *
         * @param highThreshold
         *         the number of high severity issues
         *
         * @return this
         */
        ThresholdSetBuilder setHighThreshold(final int highThreshold) {
            this.highThreshold = highThreshold;
            return this;
        }

        /**
         * Sets the threshold for the size of high severity issues.
         *
         * @param highThreshold
         *         the number of high severity issues
         *
         * @return this
         */
        ThresholdSetBuilder setHighThreshold(final String highThreshold) {
            return setHighThreshold(asInt(highThreshold));
        }

        /**
         * Sets the threshold for the size of normal severity issues.
         *
         * @param normalThreshold
         *         the number of normal severity issues
         *
         * @return this
         */
        ThresholdSetBuilder setNormalThreshold(final int normalThreshold) {
            this.normalThreshold = normalThreshold;
            return this;
        }

        /**
         * Sets the threshold for the size of normal severity issues.
         *
         * @param normalThreshold
         *         the number of normal severity issues
         *
         * @return this
         */
        ThresholdSetBuilder setNormalThreshold(final String normalThreshold) {
            return setNormalThreshold(asInt(normalThreshold));
        }

        /**
         * Sets the threshold for the size of low severity issues.
         *
         * @param lowThreshold
         *         the number of low severity issues
         *
         * @return this
         */
        ThresholdSetBuilder setLowThreshold(final int lowThreshold) {
            this.lowThreshold = lowThreshold;
            return this;
        }

        /**
         * Sets the threshold for the size of low severity issues.
         *
         * @param lowThreshold
         *         the number of low severity issues
         *
         * @return this
         */
        ThresholdSetBuilder setLowThreshold(final String lowThreshold) {
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
         *         if the provided string can't be converted to an integer value greater or equal zero
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

