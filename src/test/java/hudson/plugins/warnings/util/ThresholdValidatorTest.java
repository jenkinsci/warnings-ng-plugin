package hudson.plugins.warnings.util;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Tests the class {@link ThresholdValidator}.
 *
 * @author Ulli Hafner
 */
public class ThresholdValidatorTest extends AbstractValidatorTest {
    /**
     * Test some valid encodings.
     */
    @Test
    public void testValidEncodings() throws Exception {
        assertThatInputIsValid("0");
        assertThatInputIsValid("1");
        assertThatInputIsValid("2");
        assertThatInputIsValid("5000");
    }

    /**
     * Test some invalid encodings.
     */
    @Test
    public void testInvalidEncodings() throws Exception {
        assertThatInputIsInvalid("NIX");
        assertThatInputIsInvalid("-1");
        assertThatInputIsInvalid("?");
    }

    /** {@inheritDoc} */
    @Override
    protected SingleFieldValidator createValidator(final StaplerRequest request, final StaplerResponse response) {
        return new ThresholdValidator(request, response) {
            /** {@inheritDoc} */
            @Override
            public void error(final String message) throws IOException, ServletException {
                setError();
            }
        };
    }
}

