package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link XlcParserTest}.
 */
public class XlcParserTest extends ParserTester {
    private static final String TYPE = new XlcCompilerParser().getGroup();

    /**
     * Parses a string with xlC error.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserError() throws IOException {
        Collection<FileAnnotation> warnings = new XlcCompilerParser().parse(
                new StringReader("\"file.c\", line 9.17: 1506-098 (E) Missing argument(s)."));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                9,
                "Missing argument(s).",
                "file.c",
                TYPE,
                "1506-098",
                Priority.HIGH);
    }

    /**
     * Parses a string with xlC error.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserSevereError() throws IOException {
        Collection<FileAnnotation> warnings = new XlcCompilerParser().parse(
                new StringReader("file.c, line 11.18: 1506-189 (S) Floating point constant 10.23.3 is not valid"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                11,
                "Floating point constant 10.23.3 is not valid",
                "file.c",
                TYPE,
                "1506-189",
                Priority.HIGH);
    }

    /**
     * Parses a string with xlC error.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserUnrecoverableError1() throws IOException {
        Collection<FileAnnotation> warnings = new XlcCompilerParser().parse(
                new StringReader("file.c, line 5.1: 1506-001 (U) INTERNAL COMPILER ERROR"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                5,
                "INTERNAL COMPILER ERROR",
                "file.c",
                TYPE,
                "1506-001",
                Priority.HIGH);
    }

    /**
     * Parses a string with xlC error.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserUnrecoverableError2() throws IOException {
        Collection<FileAnnotation> warnings = new XlcCompilerParser().parse(
                new StringReader("1586-346 (U) An error occurred during code generation.  The code generation return code was 1."));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "An error occurred during code generation.  The code generation return code was 1.",
                "",
                TYPE,
                "1586-346",
                Priority.HIGH);
    }

    /**
     * Parses a string with xlC error.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserUnrecoverableError3() throws IOException {
        Collection<FileAnnotation> warnings = new XlcCompilerParser().parse(
                new StringReader("    1500-004: (U) INTERNAL COMPILER ERROR while compiling ----.  Compilation ended.  Contact your Service Representative and provide the following information: Internal abort. For more information visit: http://www.ibm.com/support/docview.wss?uid=swg21110810"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "INTERNAL COMPILER ERROR while compiling ----.  Compilation ended.  Contact your Service Representative and provide the following information: Internal abort. For more information visit: http://www.ibm.com/support/docview.wss?uid=swg21110810",
                "",
                TYPE,
                "1500-004",
                Priority.HIGH);
    }

    /**
     * Parses a string with xlC warning.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserWarning() throws IOException {
        Collection<FileAnnotation> warnings = new XlcCompilerParser().parse(
                new StringReader("file.c, line 5.9: 1506-304 (W) No function prototype given for \"printf\"."));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                5,
                "No function prototype given for \"printf\".",
                "file.c",
                TYPE,
                "1506-304",
                Priority.NORMAL);
    }

    /**
     * Parses a string with xlC informational message.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserInfo1() throws IOException {
        Collection<FileAnnotation> warnings = new XlcCompilerParser().parse(
                new StringReader("file.c, line 12.9: 1506-478 (I) The then branch of conditional is an empty statement."));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                12,
                "The then branch of conditional is an empty statement.",
                "file.c",
                TYPE,
                "1506-478",
                Priority.LOW);
    }

    /**
     * Parses a string with xlC informational message.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserInfo2() throws IOException {
        Collection<FileAnnotation> warnings = new XlcCompilerParser().parse(
                new StringReader("    1500-030: (I) INFORMATION: clazz::fun(): Additional optimization may be attained by recompiling and specifying MAXMEM option with a value greater than 8192."));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "clazz::fun(): Additional optimization may be attained by recompiling and specifying MAXMEM option with a value greater than 8192.",
                "",
                TYPE,
                "1500-030",
                Priority.LOW);
    }

    /**
     * Parses a string with xlC informational message.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserInfo3() throws IOException {
        Collection<FileAnnotation> warnings = new XlcCompilerParser().parse(
                new StringReader("1540-5336 (I) Global variable \"__td __td__Q2_3std13runtime_error\" is not used."));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "Global variable \"__td __td__Q2_3std13runtime_error\" is not used.",
                "",
                TYPE,
                "1540-5336",
                Priority.LOW);
    }


    @Override
    protected String getWarningsFile() {
        return null;
    }
}

