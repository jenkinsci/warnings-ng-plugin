package hudson.plugins.analysis.util;

import org.apache.commons.lang.StringUtils;

import hudson.util.FormValidation;

/**
 * Controls the size of a trend report.
 *
 * @author Ulli Hafner
 */
public class TrendReportHeightValidator implements Validator {
    /** Default height of the graph. */
    private static final int HEIGHT = 200;
    /** Minimum height of the graph. */
    private static final int MINIMUM_HEIGHT = 50;

    /**
     * Returns a normalized height for the trend graph (always greater than 50).
     *
     * @param height
     *            the input height value to normalize
     * @return a normalized height for the trend graph.
     */
    public static int defaultHeight(final String height) {
        int actualHeight = HEIGHT;
        if (!StringUtils.isEmpty(height)) {
            try {
                actualHeight = Math.max(MINIMUM_HEIGHT, Integer.valueOf(height));
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
        return actualHeight;
    }

    /** {@inheritDoc} */
    public FormValidation check(final String value) throws FormValidation {
        if (!StringUtils.isEmpty(value)) {
            try {
                int integer = Integer.valueOf(value);
                if (integer < MINIMUM_HEIGHT) {
                    throw FormValidation.error(Messages.FieldValidator_Error_TrendHeight(MINIMUM_HEIGHT));
                }
            }
            catch (NumberFormatException exception) {
                throw FormValidation.error(Messages.FieldValidator_Error_TrendHeight(MINIMUM_HEIGHT));
            }
        }

        return FormValidation.ok();
    }
}

