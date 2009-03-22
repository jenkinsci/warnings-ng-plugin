package hudson.plugins.warnings.util;

import static junit.framework.Assert.*;

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
    /** Error message. */
    private static final String THRESHOLD_IS_NOT_VALID = "Threshold is not valid";
    /** Error message. */
    private static final String THRESHOLD_IS_VALID = "Threshold is valid";
    /** Error message. */
    private static final String WRONG_VALUE_CONVERTED = "Wrong value converted";

    /**
     * Tests some valid encodings.
     */
    @Test
    public void testValidEncodings() throws Exception {
        assertThatInputIsValid("0");
        assertThatInputIsValid("1");
        assertThatInputIsValid("2");
        assertThatInputIsValid("5000");
    }

    /**
     * Tests some invalid encodings.
     */
    @Test
    public void testInvalidEncodings() throws Exception {
        assertThatInputIsInvalid("NIX");
        assertThatInputIsInvalid("-1");
        assertThatInputIsInvalid("?");
    }

    /**
     * Checks the validation of thresholds.
     */
    @Test
    public void testValidation() {
        assertTrue(THRESHOLD_IS_NOT_VALID, ThresholdValidator.isValid("0"));
        assertTrue(THRESHOLD_IS_NOT_VALID, ThresholdValidator.isValid("1"));
        assertTrue(THRESHOLD_IS_NOT_VALID, ThresholdValidator.isValid("100"));

        assertFalse(THRESHOLD_IS_VALID, ThresholdValidator.isValid("-1"));
        assertFalse(THRESHOLD_IS_VALID, ThresholdValidator.isValid(""));
        assertFalse(THRESHOLD_IS_VALID, ThresholdValidator.isValid("1 1"));
        assertFalse(THRESHOLD_IS_VALID, ThresholdValidator.isValid(null));
    }

    /**
     * Checks the conversion of thresholds.
     */
    @Test
    public void testConversion() {
        assertEquals(WRONG_VALUE_CONVERTED, 0, ThresholdValidator.convert("0"));
        assertEquals(WRONG_VALUE_CONVERTED, 1, ThresholdValidator.convert("1"));
        assertEquals(WRONG_VALUE_CONVERTED, 100, ThresholdValidator.convert("100"));
    }

    /**
     * Verifies the contract of {@link ThresholdValidator#convert(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyConvertContract() {
        ThresholdValidator.convert("-1");
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

