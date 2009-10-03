package hudson.plugins.warnings;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import hudson.plugins.analysis.test.AbstractEnglishLocaleTest;
import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the class {@link ResultSummary}.
 */
public class ResultSummaryTest extends AbstractEnglishLocaleTest {
    /**
     * Checks the text for no warning.
     */
    @Test
    public void test0Warnings() {
        checkSummaryText(0, "Compiler Warnings: 0 warnings.");
    }

    /**
     * Checks the text for 1 warning.
     */
    @Test
    public void test1Warning() {
        checkSummaryText(1, "Compiler Warnings: <a href=\"warningsResult\">1 warning</a>.");
    }

    /**
     * Checks the text for 5 warnings.
     */
    @Test
    public void test5WarningsIn1File() {
        checkSummaryText(5, "Compiler Warnings: <a href=\"warningsResult\">5 warnings</a>.");
    }

    /**
     * Parameterized test case to check the message text for the specified
     * number of warnings and files.
     *
     * @param numberOfWarnings
     *            the number of warnings
     * @param expectedMessage
     *            the expected message
     */
    private void checkSummaryText(final int numberOfWarnings, final String expectedMessage) {
        WarningsResult result = createMock(WarningsResult.class);
        expect(result.getNumberOfAnnotations()).andReturn(numberOfWarnings).anyTimes();

        replay(result);

        Assert.assertEquals("Wrong summary message created.", expectedMessage, ResultSummary.createSummary(result));

        verify(result);
    }

    /**
     * Checks the delta message for no new and no fixed warnings.
     */
    @Test
    public void testNoDelta() {
        checkDeltaText(0, 0, "");
    }

    /**
     * Checks the delta message for 1 new and no fixed warnings.
     */
    @Test
    public void testOnly1New() {
        checkDeltaText(0, 1, "<li><a href=\"warningsResult/new\">1 new warning</a></li>");
    }

    /**
     * Checks the delta message for 5 new and no fixed warnings.
     */
    @Test
    public void testOnly5New() {
        checkDeltaText(0, 5, "<li><a href=\"warningsResult/new\">5 new warnings</a></li>");
    }

    /**
     * Checks the delta message for 1 fixed and no new warnings.
     */
    @Test
    public void testOnly1Fixed() {
        checkDeltaText(1, 0, "<li><a href=\"warningsResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and no new warnings.
     */
    @Test
    public void testOnly5Fixed() {
        checkDeltaText(5, 0, "<li><a href=\"warningsResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test5New5Fixed() {
        checkDeltaText(5, 5,
                "<li><a href=\"warningsResult/new\">5 new warnings</a></li>"
                + "<li><a href=\"warningsResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test5New1Fixed() {
        checkDeltaText(1, 5,
        "<li><a href=\"warningsResult/new\">5 new warnings</a></li>"
        + "<li><a href=\"warningsResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test1New5Fixed() {
        checkDeltaText(5, 1,
                "<li><a href=\"warningsResult/new\">1 new warning</a></li>"
                + "<li><a href=\"warningsResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test1New1Fixed() {
        checkDeltaText(1, 1,
                "<li><a href=\"warningsResult/new\">1 new warning</a></li>"
                + "<li><a href=\"warningsResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Parameterized test case to check the message text for the specified
     * number of warnings and files.
     *
     * @param numberOfFixedWarnings
     *            the number of fixed warnings
     * @param numberOfNewWarnings
     *            the number of new warnings
     * @param expectedMessage
     *            the expected message
     */
    private void checkDeltaText(final int numberOfFixedWarnings, final int numberOfNewWarnings, final String expectedMessage) {
        WarningsResult result = createMock(WarningsResult.class);
        expect(result.getNumberOfFixedWarnings()).andReturn(numberOfFixedWarnings).anyTimes();
        expect(result.getNumberOfNewWarnings()).andReturn(numberOfNewWarnings).anyTimes();

        replay(result);

        Assert.assertEquals("Wrong delta message created.", expectedMessage, ResultSummary.createDeltaMessage(result));

        verify(result);
    }
}

