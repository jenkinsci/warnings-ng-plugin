package hudson.plugins.analysis.util;

import org.junit.Test;

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
    public void testValidEncodings() {
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
    public void testInvalidEncodings() {
        assertThatInputIsInvalid("NIX");
        assertThatInputIsInvalid("UTF-9");
        assertThatInputIsInvalid("ISO-8859-42");
    }

    /** {@inheritDoc} */
    @Override
    protected Validator createValidator() {
        return new EncodingValidator();
    }
}
