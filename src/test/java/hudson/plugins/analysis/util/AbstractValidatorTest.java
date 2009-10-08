package hudson.plugins.analysis.util;

import hudson.util.FormValidation;

import org.junit.Assert;

/**
 * Base class to test {@link Validator} classes.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractValidatorTest {
    /**
     * Factory method to create the validator.
     *
     * @return the validator under test
     */
    protected abstract Validator createValidator();

    /**
     * Runs the validator with the specified input string and verifies that the
     * result is valid.
     *
     * @param inputValue
     *            the input value to test
     */
    protected final void assertThatInputIsValid(final String inputValue) {
        verifyInput(inputValue, true);
    }

    /**
     * Runs the validator with the specified input string and verifies that the
     * result is invalid.
     *
     * @param inputValue
     *            the input value to test
     */
    protected final void assertThatInputIsInvalid(final String inputValue) {
        verifyInput(inputValue, false);
    }

    /**
     * Runs the validator with the specified input string and verifies the
     * result.
     *
     * @param inputValue
     *            the input value to test
     * @param expectedIsValid
     *            the expected validation result, <code>true</code> for success
     */
    protected void verifyInput(final String inputValue, final boolean expectedIsValid) {
        Validator validator = createValidator();
        boolean actualIsValid;
        try {
            validator.check(inputValue);
            actualIsValid = true;
        }
        catch (FormValidation exception) {
            actualIsValid = false;
        }

        Assert.assertEquals("Wrong validation of input string " + inputValue, expectedIsValid, actualIsValid);
    }
}
