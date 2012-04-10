package hudson.plugins.analysis.util;

import org.junit.Test;

/**
 * Tests the class {@link TrendReportHeightValidator}.
 *
 * @author Ulli Hafner
 */
public class TrendReportHeightValidatorTest extends AbstractValidatorTest {
    /**
     * Test some valid encodings.
     */
    @Test
    public void testValidEncodings() {
        assertThatInputIsValid("50");
        assertThatInputIsValid("51");
        assertThatInputIsValid("52");
        assertThatInputIsValid("5000");
    }

    /**
     * Test some invalid encodings.
     */
    @Test
    public void testInvalidEncodings() {
        assertThatInputIsInvalid("NIX");
        assertThatInputIsInvalid("-1");
        assertThatInputIsInvalid("49");
    }

    @Override
    protected Validator createValidator() {
        return new TrendReportHeightValidator();
    }
}

