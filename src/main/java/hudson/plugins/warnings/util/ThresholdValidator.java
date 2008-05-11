package hudson.plugins.warnings.util;

import hudson.util.FormFieldValidator;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Validates a threshold parameter. A threshold must be an integer value greater
 * or equal 0.
 *
 * @author Ulli Hafner
 */
public class ThresholdValidator extends FormFieldValidator {
    /** Error message. */
    private static final String MESSAGE = "Threshold must be an integer value greater or equal 0.";

    /**
     * Creates a new instance of <code>NumberValidator</code>.
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public ThresholdValidator(final StaplerRequest request, final StaplerResponse response) {
        super(request, response, false);
    }

    /** {@inheritDoc} */
    @Override
    protected void check() throws IOException, ServletException {
        String value = request.getParameter("value");
        if (!StringUtils.isEmpty(value)) {
            try {
                int integer = Integer.valueOf(value);
                if (integer < 0) {
                    error(MESSAGE);
                }
            }
            catch (NumberFormatException exception) {
                error(MESSAGE);
            }
        }
    }
}

