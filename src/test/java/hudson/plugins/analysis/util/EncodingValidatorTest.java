package hudson.plugins.analysis.util;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Tests the class {@link EncodingValidator}.
 *
 * @author Ulli Hafner
 */
public class EncodingValidatorTest extends AbstractValidatorTest {
    /**
     * Test some valid encodings.
     */
    @Test
    public void testValidEncodings() throws Exception {
        assertThatInputIsValid("");
        assertThatInputIsValid("UTF8");
        assertThatInputIsValid("UTF-8");
        assertThatInputIsValid("CP1252");
        assertThatInputIsValid("ISO-8859-1");
        assertThatInputIsValid("ISO-8859-5");
        assertThatInputIsValid("ISO-8859-9");
    }

    /**
     * Test some invalid encodings.
     */
    @Test
    public void testInvalidEncodings() throws Exception {
        assertThatInputIsInvalid("NIX");
        assertThatInputIsInvalid("UTF-9");
        assertThatInputIsInvalid("ISO-8859-42");
    }

    /** {@inheritDoc} */
    @Override
    protected SingleFieldValidator createValidator(final StaplerRequest request, final StaplerResponse response) {
        return new EncodingValidator(request, response) {
            /** {@inheritDoc} */
            @Override
            public void error(final String message) throws IOException, ServletException {
                setError();
            }
        };
    }
}
