package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link XlcLinkerParserTest}.
 */
public class XlcLinkerParserTest extends ParserTester {
    private static final String TYPE = new XlcLinkerParser().getGroup();

    /**
     * Parses a string with xlC linker error.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserError1() throws IOException {
        Collection<FileAnnotation> warnings = new XlcLinkerParser().parse(
                new StringReader("ld: 0711-987 Error occurred while reading file"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "Error occurred while reading file",
                "",
                TYPE,
                "0711-987",
                Priority.HIGH);
    }

    /**
     * Parses a string with xlC linker error.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserError2() throws IOException {
        Collection<FileAnnotation> warnings = new XlcLinkerParser().parse(
                new StringReader("ld: 0711-317 ERROR: Undefined symbol: nofun()"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "Undefined symbol: nofun()",
                "",
                TYPE,
                "0711-317",
                Priority.HIGH);
    }

    /**
     * Parses a string with xlC linker error.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserSevereError() throws IOException {
        Collection<FileAnnotation> warnings = new XlcLinkerParser().parse(
                new StringReader("ld: 0711-634 SEVERE ERROR: EXEC binder commands nested too deeply."));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "EXEC binder commands nested too deeply.",
                "",
                TYPE,
                "0711-634",
                Priority.HIGH);
    }

    /**
     * Parses a string with xlC linker warning.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserWarning1() throws IOException {
        Collection<FileAnnotation> warnings = new XlcLinkerParser().parse(
                new StringReader("ld: 0706-012 The -9 flag is not recognized."));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "The -9 flag is not recognized.",
                "",
                TYPE,
                "0706-012",
                Priority.LOW);
    }

    /**
     * Parses a string with xlC linker warning.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserWarning2() throws IOException {
        Collection<FileAnnotation> warnings = new XlcLinkerParser().parse(
                new StringReader("ld: 0711-224 WARNING: Duplicate symbol: dupe"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "Duplicate symbol: dupe",
                "",
                TYPE,
                "0711-224",
                Priority.NORMAL);
    }

    /**
     * Parses a string with xlC linker informational message.
     *
     * @throws IOException
     *      if IO error happened
     */
    @Test
    public void testWarningsParserInfo1() throws IOException {
        Collection<FileAnnotation> warnings = new XlcLinkerParser().parse(
                new StringReader("ld: 0711-345 Use the -bloadmap or -bnoquiet option to obtain more information."));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "Use the -bloadmap or -bnoquiet option to obtain more information.",
                "",
                TYPE,
                "0711-345",
                Priority.LOW);
    }

    @Override
    protected String getWarningsFile() {
        return null;
    }
}

