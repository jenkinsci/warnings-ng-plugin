package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link XlcParserTest}.
 */
public class XlcParserTest extends ParserTester {
    private static final String TYPE = new XlcCompilerParser().getGroup();

    /**
     * Parses a string with xlC error.
     */
    @Test
    public void testWarningsParserError() throws IOException {
        shouldParseWarning("\"file.c\", line 9.17: 1506-098 (E) Missing argument(s).",
                9, "Missing argument(s).", "file.c", TYPE, "1506-098", Priority.HIGH);
    }

    /**
     * Parses a string with xlC error.
     */
    @Test
    public void testWarningsParserSevereError() throws IOException {
        shouldParseWarning("file.c, line 11.18: 1506-189 (S) Floating point constant 10.23.3 is not valid",
                11, "Floating point constant 10.23.3 is not valid", "file.c", TYPE, "1506-189", Priority.HIGH);
    }

    /**
     * Parses a string with xlC error in z/OS message format.
     */
    @Test
    public void testWarningsParserSevereErrorZOS() {
        shouldParseWarning("\"./Testapi.cpp\", line 4000.22: CCN5217 (S) \"AEUPD_RQ_UPDT\" is not a member of \"struct AEUPD_RQ\".",
                4000, "\"AEUPD_RQ_UPDT\" is not a member of \"struct AEUPD_RQ\".", "./Testapi.cpp", TYPE, "CCN5217", Priority.HIGH);
    }

    /**
     * Parses a string with xlC unrecoverable error.
     */
    @Test
    public void testWarningsParserUnrecoverableError() {
        shouldParseWarning("file.c, line 5.1: 1506-001 (U) INTERNAL COMPILER ERROR",
                5, "INTERNAL COMPILER ERROR", "file.c", TYPE, "1506-001", Priority.HIGH);
        shouldParseWarning("1586-346 (U) An error occurred during code generation.  The code generation return code was 1.",
                0, "An error occurred during code generation.  The code generation return code was 1.", "", TYPE, "1586-346", Priority.HIGH);
        shouldParseWarning("    1500-004: (U) INTERNAL COMPILER ERROR while compiling ----.  Compilation ended.  Contact your Service Representative and provide the following information: Internal abort. For more information visit: http://www.ibm.com/support/docview.wss?uid=swg21110810",
                0, "INTERNAL COMPILER ERROR while compiling ----.  Compilation ended.  Contact your Service Representative and provide the following information: Internal abort. For more information visit: http://www.ibm.com/support/docview.wss?uid=swg21110810", "", TYPE, "1500-004", Priority.HIGH);
    }

    /**
     * Parses a string with xlC warning.
     */
    @Test
    public void testWarningsParserWarning() {
        shouldParseWarning("file.c, line 5.9: 1506-304 (W) No function prototype given for \"printf\".",
                5, "No function prototype given for \"printf\".", "file.c", TYPE, "1506-304", Priority.NORMAL);
    }

    /**
     * Parses a string with xlC warning message in z/OS format.
     */
    @Test
    public void testWarningsParserWarningZOS() {
        shouldParseWarning("\"./Testapi.cpp\", line 130.13: CCN5053 (W) The declaration of a class member within the class definition must not be qualified.",
                130, "The declaration of a class member within the class definition must not be qualified.", "./Testapi.cpp", TYPE, "CCN5053", Priority.NORMAL);
        shouldParseWarning("CCN7504(W) \"//''\" is not a valid suboption for \"SEARCH\".  The option is ignored.",
                0, "\"//''\" is not a valid suboption for \"SEARCH\".  The option is ignored.", "", TYPE, "CCN7504", Priority.NORMAL);
    }

    /**
     * Parses a string with xlC informational message.
     */
    @Test
    public void testWarningsParserInfo() {
        shouldParseWarning("file.c, line 12.9: 1506-478 (I) The then branch of conditional is an empty statement.",
                12, "The then branch of conditional is an empty statement.", "file.c", TYPE, "1506-478", Priority.LOW);
        shouldParseWarning("    1500-030: (I) INFORMATION: clazz::fun(): Additional optimization may be attained by recompiling and specifying MAXMEM option with a value greater than 8192.",
                0, "clazz::fun(): Additional optimization may be attained by recompiling and specifying MAXMEM option with a value greater than 8192.", "", TYPE, "1500-030", Priority.LOW);
        shouldParseWarning("1540-5336 (I) Global variable \"__td __td__Q2_3std13runtime_error\" is not used.",
                0, "Global variable \"__td __td__Q2_3std13runtime_error\" is not used.", "", TYPE, "1540-5336", Priority.LOW);
    }

    /**
     * Parses a string with xlC informational message in z/OS format.
     */
    @Test
    public void testWarningsParserInfoZOS1() {
        shouldParseWarning("\"./Testapi.cpp\", line 372.8: CCN6283 (I) \"Testapi::Test(long, long)\" is not a viable candidate.",
                372, "\"Testapi::Test(long, long)\" is not a viable candidate.", "./Testapi.cpp", TYPE, "CCN6283", Priority.LOW);
        shouldParseWarning("CCN8151(I) The option \"TARGET(0x410D0000)\" sets \"ARCH(5)\".",
                0, "The option \"TARGET(0x410D0000)\" sets \"ARCH(5)\".", "", TYPE, "CCN8151", Priority.LOW);
    }

    private void shouldParseWarning(final String log, final int lineNumber, final String message, final String fileName, final String type, final String category, final Priority priority) {
        try {
            Collection<FileAnnotation> warnings = new XlcCompilerParser().parse(
                    new StringReader(log));

            assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

            Iterator<FileAnnotation> iterator = warnings.iterator();
            FileAnnotation annotation = iterator.next();
            checkWarning(annotation, lineNumber, message, fileName, type, category, priority);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getWarningsFile() {
        return null; // StringReader used in all tests
    }
}

