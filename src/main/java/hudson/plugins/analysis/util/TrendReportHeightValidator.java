package hudson.plugins.analysis.util;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Controls the size of a trend report.
 *
 * @author Ulli Hafner
 */
public class TrendReportHeightValidator extends SingleFieldValidator {
    /** Default height of the graph. */
    private static final int HEIGHT = 200;
    /** Minimum height of the graph. */
    private static final int MINIMUM_HEIGHT = 50;
    /**
     * Creates a new instance of {@link TrendReportHeightValidator}.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public TrendReportHeightValidator(final StaplerRequest request, final StaplerResponse response) {
        super(request, response);
    }

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
    @Override
    public void check(final String value) throws IOException, ServletException {
        if (!StringUtils.isEmpty(value)) {
            try {
                int integer = Integer.valueOf(value);
                if (integer < MINIMUM_HEIGHT) {
                    error(Messages.FieldValidator_Error_TrendHeight(MINIMUM_HEIGHT));
                    return;
                }
            }
            catch (NumberFormatException exception) {
                error(Messages.FieldValidator_Error_TrendHeight(MINIMUM_HEIGHT));
                return;
            }
        }
    }
}

