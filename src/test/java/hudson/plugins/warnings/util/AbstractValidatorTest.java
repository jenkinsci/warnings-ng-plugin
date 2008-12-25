package hudson.plugins.warnings.util;

import static org.mockito.Mockito.*;
import hudson.util.FormFieldValidator;

import java.io.PrintWriter;

import org.junit.Assert;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Base class to test {@link FormFieldValidator} classes.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractValidatorTest {
    /** Determines whether the error method has been called (i.e. a validation failure). */
    private boolean isError;

    /**
     * Creates a new instance of {@link AbstractValidatorTest}.
     */
    public AbstractValidatorTest() {
        super();
    }

    /**
     * Factory method to create the validator. In order to get the test template
     * method working your instance needs to be constructed in the following
     * way:
     *
     * <pre>
     * return new FooValidator(request, response) {
     *               public void error(final String message) throws IOException, ServletException {
     *                   setError();
     *              }
     *           };
     * }
     * </pre>
     *
     * @param request
     *            request mock
     * @param response
     *            response mock
     * @return the validator under test
     */
    protected abstract Validator createValidator(final StaplerRequest request, final StaplerResponse response);

    /**
     * Runs the validator with the specified input string and verifies that the
     * result is valid.
     *
     * @param inputValue
     *            the input value to test
     * @throws Exception
     *             in case of an test initialization error
     */
    protected final void assertThatInputIsValid(final String inputValue) throws Exception {
        verifyInput(inputValue, true);
    }

    /**
     * Runs the validator with the specified input string and verifies that the
     * result is invalid.
     *
     * @param inputValue
     *            the input value to test
     * @throws Exception
     *             in case of an test initialization error
     */
    protected final void assertThatInputIsInvalid(final String inputValue) throws Exception {
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
     * @throws Exception
     *             in case of an test initialization error
     */
    protected void verifyInput(final String inputValue, final boolean expectedIsValid) throws Exception {
        StaplerRequest request = mock(StaplerRequest.class);
        StaplerResponse response = mock(StaplerResponse.class);
        when(request.getParameter("value")).thenReturn(inputValue);
        when(response.getWriter()).thenReturn(new PrintWriter(System.out));

        Validator validator = createValidator(request, response);
        isError = false;
        validator.check();

        Assert.assertEquals("Wrong validation of input string " + inputValue, expectedIsValid, !isError);
    }

    /**
     * Sets the validation result to <code>false</code>.
     */
    protected final void setError() {
        isError = true;
    }
}
