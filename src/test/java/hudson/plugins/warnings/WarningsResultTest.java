package hudson.plugins.warnings;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.NullBuildHistory;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.test.BuildResultTest;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.warnings.WarningsResult.FileChecker;
import hudson.plugins.warnings.parser.Warning;

import java.util.GregorianCalendar;

import org.junit.Assert;

import org.junit.Test;

/**
 * Tests the class {@link WarningsResult}.
 */
public class WarningsResultTest extends BuildResultTest<WarningsResult> {
    private static final int GROUP_URL = 1;
    private static final String ORIGINAL_FILENAME = WarningsResult.ORIGINAL_COMPILER_WARNINGS_XML;

    @Override
    protected WarningsResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project, final BuildHistory history) {
        return createResult(build, project, history, null);
    }

    private WarningsResult createResult(final AbstractBuild<?, ?> build, final ParserResult project, final BuildHistory history, final String group) {
        return new WarningsResult(build, history, project, "UTF-8", group, false);
    }

    /**
     * Verifies that filenames are correctly parsed.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14570">Issue 14570</a>
     */
    @Test
    public void testWarningsFileName() {
        FileChecker stub = mock(FileChecker.class);

        WarningsResult result = createResult("group1");
        verifyFileName(result, stub, "group1-warnings");

        result = createResult("charCHAR/( 2");
        verifyFileName(result, stub, "charCHAR2-warnings");

        when(stub.canRead(result.createFileName(GROUP_URL))).thenReturn(true);
        verifyFileName(result, stub, result.createFileName(GROUP_URL).replaceFirst(".xml", ""));

        when(stub.canRead(ORIGINAL_FILENAME)).thenReturn(true);
        verifyFileName(result, stub, ORIGINAL_FILENAME.replaceFirst(".xml", ""));
    }

    private WarningsResult createResult(final String group) {
        return createResult(createBuild(), new ParserResult(), new NullBuildHistory(), group);
    }

    private void verifyFileName(final WarningsResult result, final FileChecker stub, final String expected) {
        assertEquals("Wrong filename selected", expected + ".xml", result.getFileName(stub, GROUP_URL));
    }

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
        WarningsResult result = createResult(numberOfWarnings);

        Assert.assertEquals("Wrong summary message created.", expectedMessage, result.getSummary());
    }

    private WarningsResult createResult(final int numberOfWarnings) {
        return createResult(numberOfWarnings, new DefaultAnnotationContainer());
    }

    private WarningsResult createResult(final int numberOfNewWarnings, final AnnotationContainer oldWarnings) {
        ParserResult warnings = new ParserResult();
        for (int i = 0; i < numberOfNewWarnings; i++) {
            warnings.addAnnotation(new Warning("", i, "", "", ""));
        }

        return createResult(warnings, oldWarnings);
    }

    private WarningsResult createResult(final ParserResult newWarnings, final AnnotationContainer oldWarnings) {
        BuildHistory history = mock(BuildHistory.class);
        when(history.getReferenceAnnotations()).thenReturn(oldWarnings);

        return createResultUnderTest(newWarnings, history, createBuild());
    }

    @SuppressWarnings("rawtypes")
    private AbstractBuild<?, ?> createBuild() {
        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getTimestamp()).thenReturn(new GregorianCalendar());
        return build;
    }

    private WarningsResult createResultUnderTest(final ParserResult newWarnings, final BuildHistory history, @SuppressWarnings("rawtypes") final AbstractBuild build) {
        return new WarningsResult(build, history, newWarnings, "", null, false);
    }

    private WarningsResult createResult(final int numberOfFixedWarnings, final int numberOfNewWarnings) {
        AnnotationContainer oldWarnings = new DefaultAnnotationContainer();
        for (int i = 0; i < numberOfFixedWarnings; i++) {
            oldWarnings.addAnnotation(new Warning("", -1 - i, "", "", ""));
        }
        return createResult(numberOfNewWarnings, oldWarnings);
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
        WarningsResult result = createResult(numberOfFixedWarnings, numberOfNewWarnings);

        Assert.assertEquals("Wrong delta message created.", expectedMessage, result.createDeltaMessage());
    }
}

