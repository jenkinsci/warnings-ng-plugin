package hudson.plugins.analysis.core;

import java.util.Locale;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the class {@link BuildResult}.
 *
 * @author Ulli Hafner
 */
public class BuildResultTest {
    /**
     * Initializes the locale to English.
     */
    @Before
    public void initializeLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    /**
     * Checks the text for no warnings from 0 files.
     */
    @Test
    public void test0WarningsIn0File() {
        checkSummaryText(0, 0, "0 warnings.");
    }

    /**
     * Checks the text for no warnings from 1 file.
     */
    @Test
    public void test0WarningsIn1File() {
        checkSummaryText(0, 1, "0 warnings from one analysis.");
    }

    /**
     * Checks the text for no warnings from 5 files.
     */
    @Test
    public void test0WarningsIn5Files() {
        checkSummaryText(0, 5, "0 warnings from 5 analyses.");
    }

    /**
     * Checks the text for 1 warning from 2 files.
     */
    @Test
    public void test1WarningIn2Files() {
        checkSummaryText(1, 2, "<a href=\"findbugsResult\">1 warning</a> from 2 analyses.");
    }

    /**
     * Checks the text for 5 warnings from 1 file.
     */
    @Test
    public void test5WarningsIn1File() {
        checkSummaryText(5, 1, "<a href=\"findbugsResult\">5 warnings</a> from one analysis.");
    }

    /**
     * Checks the text for 5 warnings from 0 file.
     */
    @Test
    public void test5WarningsIn0File() {
        checkSummaryText(5, 0, "<a href=\"findbugsResult\">5 warnings</a>.");
    }

    /**
     * Parameterized test case to check the message text for the specified
     * number of warnings and files.
     *
     * @param numberOfWarnings
     *            the number of warnings
     * @param numberOfFiles
     *            the number of files
     * @param expectedMessage
     *            the expected message
     */
    private void checkSummaryText(final int numberOfWarnings, final int numberOfFiles, final String expectedMessage) {
        Assert.assertEquals("Wrong summary message created.", expectedMessage,
                BuildResult.createDefaultSummary("findbugsResult", numberOfWarnings, numberOfFiles));
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
        checkDeltaText(0, 1, "<li><a href=\"findbugsResult/new\">1 new warning</a></li>");
    }

    /**
     * Checks the delta message for 5 new and no fixed warnings.
     */
    @Test
    public void testOnly5New() {
        checkDeltaText(0, 5, "<li><a href=\"findbugsResult/new\">5 new warnings</a></li>");
    }

    /**
     * Checks the delta message for 1 fixed and no new warnings.
     */
    @Test
    public void testOnly1Fixed() {
        checkDeltaText(1, 0, "<li><a href=\"findbugsResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and no new warnings.
     */
    @Test
    public void testOnly5Fixed() {
        checkDeltaText(5, 0, "<li><a href=\"findbugsResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test5New5Fixed() {
        checkDeltaText(5, 5,
                "<li><a href=\"findbugsResult/new\">5 new warnings</a></li>"
                + "<li><a href=\"findbugsResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test5New1Fixed() {
        checkDeltaText(1, 5,
        "<li><a href=\"findbugsResult/new\">5 new warnings</a></li>"
        + "<li><a href=\"findbugsResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test1New5Fixed() {
        checkDeltaText(5, 1,
                "<li><a href=\"findbugsResult/new\">1 new warning</a></li>"
                + "<li><a href=\"findbugsResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test1New1Fixed() {
        checkDeltaText(1, 1,
                "<li><a href=\"findbugsResult/new\">1 new warning</a></li>"
                + "<li><a href=\"findbugsResult/fixed\">1 fixed warning</a></li>");
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
        Assert.assertEquals("Wrong delta message created.", expectedMessage,
                BuildResult.createDefaultDeltaMessage("findbugsResult", numberOfNewWarnings, numberOfFixedWarnings));
    }
}

