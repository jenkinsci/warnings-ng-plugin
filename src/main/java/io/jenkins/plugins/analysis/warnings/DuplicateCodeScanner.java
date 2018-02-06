package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.util.FormValidation;

/**
 * Provides settings for duplicate code scanners.
 *
 * @author Ullrich Hafner
 */
public abstract class DuplicateCodeScanner extends StaticAnalysisTool {
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + "dry-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + "dry-48x48.png";

    /** Validates the thresholds user input. */
    private static final ThresholdValidation THRESHOLD_VALIDATION = new ThresholdValidation();

    private int highThreshold = 50;
    private int normalThreshold = 25;

    /**
     * Returns the minimum number of duplicate lines for high priority warnings.
     *
     * @return the minimum number of duplicate lines for high priority warnings
     */
    public int getHighThreshold() {
        return THRESHOLD_VALIDATION.getHighThreshold(normalThreshold, highThreshold);
    }

    /**
     * Sets the minimum number of duplicate lines for high priority warnings.
     *
     * @param highThreshold
     *         the number of lines for priority high
     */
    @DataBoundSetter
    public void setHighThreshold(final int highThreshold) {
        this.highThreshold = highThreshold;
    }

    /**
     * Returns the minimum number of duplicate lines for normal warnings.
     *
     * @return the minimum number of duplicate lines for normal warnings
     */
    public int getNormalThreshold() {
        return THRESHOLD_VALIDATION.getNormalThreshold(normalThreshold, highThreshold);
    }

    /**
     * Sets the minimum number of duplicate lines for normal priority warnings.
     *
     * @param normalThreshold
     *         the number of lines for priority normal
     */
    @DataBoundSetter
    public void setNormalThreshold(final int normalThreshold) {
        this.normalThreshold = normalThreshold;
    }

    /** Provides icons for DRY parsers. */
    abstract static class DryLabelProvider extends DefaultLabelProvider {
        protected DryLabelProvider(final String id, final String name) {
            super(id, name);
        }

        @Override
        public String getSmallIconUrl() {
            return SMALL_ICON_URL;
        }

        @Override
        public String getLargeIconUrl() {
            return LARGE_ICON_URL;
        }
    }

    /** Descriptor for this static analysis tool. */
    abstract static class DryDescriptor extends StaticAnalysisToolDescriptor {
        private static final ThresholdValidation VALIDATION = new ThresholdValidation();

        protected DryDescriptor(final String id) {
            super(id);
        }

        /**
         * Performs on-the-fly validation on threshold for high warnings.
         *
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         *
         * @return the validation result
         */
        public FormValidation doCheckHighThreshold(@QueryParameter final String highThreshold,
                @QueryParameter final String normalThreshold) {
            return VALIDATION.validateHigh(highThreshold, normalThreshold);
        }

        /**
         * Performs on-the-fly validation on threshold for normal warnings.
         *
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         *
         * @return the validation result
         */
        public FormValidation doCheckNormalThreshold(@QueryParameter final String highThreshold,
                @QueryParameter final String normalThreshold) {
            return VALIDATION.validateNormal(highThreshold, normalThreshold);
        }
    }

    /**
     * Validates the number of lines thresholds.
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    public static class ThresholdValidation {
        /** Minimum number of duplicate lines for a warning with priority high. */
        static final int DEFAULT_HIGH_THRESHOLD = 50;
        /** Minimum number of duplicate lines for a warning with priority normal. */
        static final int DEFAULT_NORMAL_THRESHOLD = 25;

        /**
         * Performs on-the-fly validation on threshold for high warnings.
         *
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         *
         * @return the validation result
         */
        public FormValidation validateHigh(final String highThreshold, final String normalThreshold) {
            return validate(highThreshold, normalThreshold, Messages.DRY_ValidationError_HighThreshold());
        }

        /**
         * Performs on-the-fly validation on threshold for normal warnings.
         *
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         *
         * @return the validation result
         */
        public FormValidation validateNormal(final String highThreshold, final String normalThreshold) {
            return validate(highThreshold, normalThreshold, Messages.DRY_ValidationError_NormalThreshold());
        }

        /**
         * Performs on-the-fly validation on thresholds for high and normal warnings.
         *
         * @param highThreshold
         *         the threshold for high warnings
         * @param normalThreshold
         *         the threshold for normal warnings
         * @param message
         *         the validation message
         *
         * @return the validation result
         */
        private FormValidation validate(final String highThreshold, final String normalThreshold,
                final String message) {
            try {
                int high = Integer.parseInt(highThreshold);
                int normal = Integer.parseInt(normalThreshold);
                if (isValid(normal, high)) {
                    return FormValidation.ok();
                }
            }
            catch (NumberFormatException ignored) {
                // ignore and return failure
            }
            return FormValidation.error(message);
        }

        /**
         * Returns the minimum number of duplicate lines for a warning with priority high.
         *
         * @param normalThreshold
         *         the normal threshold
         * @param highThreshold
         *         the high threshold
         *
         * @return the minimum number of duplicate lines for a warning with priority high
         */
        public int getHighThreshold(final int normalThreshold, final int highThreshold) {
            if (!isValid(normalThreshold, highThreshold)) {
                return DEFAULT_HIGH_THRESHOLD;
            }
            return highThreshold;
        }

        private boolean isValid(final int normalThreshold, final int highThreshold) {
            return !(highThreshold <= 0 || normalThreshold <= 0 || highThreshold <= normalThreshold);
        }

        /**
         * Returns the minimum number of duplicate lines for a warning with priority normal.
         *
         * @param normalThreshold
         *         the normal threshold
         * @param highThreshold
         *         the high threshold
         *
         * @return the minimum number of duplicate lines for a warning with priority normal
         */
        public int getNormalThreshold(final int normalThreshold, final int highThreshold) {
            if (!isValid(normalThreshold, highThreshold)) {
                return DEFAULT_NORMAL_THRESHOLD;
            }
            return normalThreshold;
        }
    }
}
