package hudson.plugins.warnings;

import static junit.framework.Assert.*;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Tests the class {@link WarningsDescriptor}.
 *
 * @author Ulli Hafner
 */
public class WarningsDescriptorTest {
    // CHECKSTYLE:OFF
    private static final String SINGLE_LINE_EXAMPLE = "file/name/relative/unix:42:evil: this is a warning message";
    private static final String MULTI_LINE_EXAMPLE = "    [javac] 1. WARNING in C:\\Desenvolvimento\\Java\\jfg\\src\\jfg\\AttributeException.java (at line 3)\n"
                    + "    [javac]     public class AttributeException extends RuntimeException\n"
                    + "    [javac]                  ^^^^^^^^^^^^^^^^^^\n"
                    + "    [javac] The serializable class AttributeException does not declare a static final serialVersionUID field of type long\n"
                    + "    [javac] ----------\n";
    private static final String MULTILINE_SCRIPT = "import hudson.plugins.warnings.parser.Warning\n" +
                    "import hudson.plugins.analysis.util.model.Priority\n" +
                    "\n" +
                    "String type = matcher.group(1)\n" +
                    "Priority priority;\n" +
                    "if (\"warning\".equalsIgnoreCase(type)) {\n" +
                    "    priority = Priority.NORMAL;\n" +
                    "}\n" +
                    "else {\n" +
                    "    priority = Priority.HIGH;\n" +
                    "}\n" +
                    "\n" +
                    "String fileName = matcher.group(2)\n" +
                    "String lineNumber = matcher.group(3)\n" +
                    "String message = matcher.group(4)\n" +
                    "\n" +
                    "return new Warning(fileName, Integer.parseInt(lineNumber), \"Generic Parser\", \"\", message);\n";
    public static final String MULTI_LINE_REGEXP = "(WARNING|ERROR)\\s*in\\s*(.*)\\(at line\\s*(\\d+)\\).*(?:\\r?\\n[^\\^]*)+(?:\\r?\\n.*[\\^]+.*)\\r?\\n(?:\\s*\\[.*\\]\\s*)?(.*)";
    public static final String SINGLE_LINE_REGEXP = "^\\s*(.*):(\\d+):(.*):\\s*(.*)$";
    // CHECKSTYLE:ON

    /**
     * Test the validation of the name parameter.
     */
    @Test
    public void testNameValidation() {
        WarningsDescriptor descriptor = new WarningsDescriptor(false);

        assertError(descriptor.doCheckName(null));
        assertError(descriptor.doCheckName(StringUtils.EMPTY));
        assertOk(descriptor.doCheckName("Java Parser 2"));
    }

    /**
     * Test the validation of the regexp parameter.
     */
    @Test
    public void testRegexpValidation() {
        WarningsDescriptor descriptor = new WarningsDescriptor(false);

        assertError(descriptor.doCheckRegexp(null));
        assertError(descriptor.doCheckRegexp(StringUtils.EMPTY));
        assertError(descriptor.doCheckRegexp("one brace ("));
        assertError(descriptor.doCheckRegexp("backslash \\"));

        assertOk(descriptor.doCheckRegexp("^.*[a-z]"));
    }

    /**
     * Test the validation of the script parameter.
     *
     * @throws IOException
     *             if the example file could not be read
     */
    @Test
    public void testScriptValidationWithoutExample() throws IOException {
        WarningsDescriptor descriptor = new WarningsDescriptor(false);

        assertError(descriptor.doCheckScript(null));
        assertError(descriptor.doCheckScript(StringUtils.EMPTY));
        assertError(descriptor.doCheckScript("Hello World"));

        assertOk(descriptor.doCheckScript(readScript()));
    }

    private String readScript() throws IOException {
        return IOUtils.toString(WarningsDescriptorTest.class.getResourceAsStream("groovy.snippet"));
    }

    /**
     * Test the validation of the script parameter with a given regular
     * expression and example. Expected result: the expected result is a
     * warning.
     *
     * @throws IOException
     *             if the example file could not be read
     */
    @Test
    public void testScriptValidationOneWarning() throws IOException {
        WarningsDescriptor descriptor = new WarningsDescriptor(false);

        assertOk(descriptor.doCheckExample(SINGLE_LINE_EXAMPLE, SINGLE_LINE_REGEXP, readScript()));
    }

    /**
     * Test the validation of the script parameter with a given regular
     * expression and example. Expected result: the regular expression will not
     * match.
     *
     * @throws IOException
     *             if the example file could not be read
     */
    @Test
    public void testScriptValidationNoMatchesFound() throws IOException {
        WarningsDescriptor descriptor = new WarningsDescriptor(false);

        assertError(descriptor.doCheckExample("this is a warning message", SINGLE_LINE_REGEXP, readScript()));
    }

    /**
     * Test the validation of the script parameter with a given regular
     * expression and example. Expected result: the regular expression will not
     * match.
     *
     * @throws IOException
     *             if the example file could not be read
     */
    @Test
    public void testScriptValidationIllegalMatchAccess() throws IOException {
        WarningsDescriptor descriptor = new WarningsDescriptor(false);

        assertError(descriptor.doCheckExample(SINGLE_LINE_EXAMPLE, "^\\s*(.*):(\\d+):(.*)$", readScript()));
    }

    /**
     * Test the validation of the script parameter with a given regular
     * expression and a multi-line example. Expected result: the regular
     * expression will match.
     *
     * @throws IOException
     *             if the example file could not be read
     */
    @Test
    public void testMultiLineExpressionWillMatch() throws IOException {
        WarningsDescriptor descriptor = new WarningsDescriptor(false);

        assertOk(descriptor.doCheckExample(MULTI_LINE_EXAMPLE, MULTI_LINE_REGEXP, MULTILINE_SCRIPT));
    }

    private void assertOk(final FormValidation actualResult) {
        verify(actualResult, FormValidation.Kind.OK);
    }

    private void assertError(final FormValidation actualResult) {
        verify(actualResult, FormValidation.Kind.ERROR);
    }

    private void verify(final FormValidation actualResult, final Kind expectedResult) {
        assertEquals("Wrong validation result", expectedResult, actualResult.kind);
    }
}

