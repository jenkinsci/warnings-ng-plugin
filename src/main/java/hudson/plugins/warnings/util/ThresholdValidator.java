package hudson.plugins.warnings.util;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Validates a threshold parameter. A threshold must be an integer value greater
 * or equal 0 or the empty string.
 *
 * @author Ulli Hafner
 */
public class ThresholdValidator extends SingleFieldValidator {
    /**
     * Creates a new instance of {@link ThresholdValidator}.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public ThresholdValidator(final StaplerRequest request, final StaplerResponse response) {
        super(request, response);
    }

    /** {@inheritDoc} */
    @Override
    public void check(final String value) throws IOException, ServletException {
        if (!StringUtils.isEmpty(value)) {
            try {
                int integer = Integer.valueOf(value);
                if (integer < 0) {
                    error(Messages.FieldValidator_Error_Threshold());
                    return;
                }
            }
            catch (NumberFormatException exception) {
                error(Messages.FieldValidator_Error_Threshold());
                return;
            }
        }

        ok();
    }
}

