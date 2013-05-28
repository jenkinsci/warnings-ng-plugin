package hudson.plugins.warnings.parser;
import org.junit.Assert;
import org.junit.Test;

import hudson.plugins.violations.types.pylint.PyLintParser;

/**
 * FIXME: Document type NewPylintParserTest.
 *
 * @author Ulli Hafner
 */
public class NewPylintParserTest extends ParserTester {
    @Test
    public void testParseLineSimple() {
        NewPyLintParser parser = new NewPyLintParser();
        PyLintParser.PyLintViolation violation = parser.getPyLintViolation("trunk/src/python/cachedhttp.py:3: [C] Line too long (85/80)");

        Assert.assertEquals("The message is incorrect", "Line too long (85/80)", violation.getMessage());
        Assert.assertEquals("The violation id is incorrect", "C", violation.getViolationId());
        Assert.assertEquals("The line str is incorrect", "3", violation.getLineStr());
        Assert.assertEquals("The file name is incorrect", "trunk/src/python/cachedhttp.py", violation.getFileName());
    }

    @Test
    public void testExtraViolationInfo() {
        PyLintParser parser = new PyLintParser();
        PyLintParser.PyLintViolation violation = parser.getPyLintViolation("trunk/src/python/tv.py:28: [C0103, Show.__init__] Invalid name \"seasonCount\" (should match [a-z_][a-z0-9_]{2,30}$)");

        Assert.assertEquals("The message is incorrect", "Invalid name \"seasonCount\" (should match [a-z_][a-z0-9_]{2,30}$)", violation.getMessage());
        Assert.assertEquals("The violation id is incorrect", "C0103", violation.getViolationId());
        Assert.assertEquals("The line str is incorrect", "28", violation.getLineStr());
        Assert.assertEquals("The file name is incorrect", "trunk/src/python/tv.py", violation.getFileName());
    }

    @Test
    public void testExtraViolationInfo2() {
        PyLintParser parser = new PyLintParser();
        PyLintParser.PyLintViolation violation = parser.getPyLintViolation("trunk/src/python/tv.py:35: [C0111, Episode] Missing docstring");

        Assert.assertEquals("The message is incorrect", "Missing docstring", violation.getMessage());
        Assert.assertEquals("The violation id is incorrect", "C0111", violation.getViolationId());
        Assert.assertEquals("The line str is incorrect", "35", violation.getLineStr());
        Assert.assertEquals("The file name is incorrect", "trunk/src/python/tv.py", violation.getFileName());
    }

}




