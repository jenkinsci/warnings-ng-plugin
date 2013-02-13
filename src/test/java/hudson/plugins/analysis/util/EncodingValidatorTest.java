package hudson.plugins.analysis.util;

import static org.junit.Assert.*;

import java.nio.charset.Charset;

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

    /**
     * Verifies that the platform encoding is used if encoding is invalid.
     */
    @Test
    public void testDefaultEncoding() {
        assertEquals("Wrong encoding used", "UTF-8", EncodingValidator.getEncoding("UTF-8"));
        String osCharset = Charset.defaultCharset().name();
        assertEquals("Wrong encoding used", osCharset, EncodingValidator.getEncoding(""));
        assertEquals("Wrong encoding used", osCharset, EncodingValidator.getEncoding(null));
    }

    @Override
    protected Validator createValidator() {
        return new EncodingValidator();
    }
}
