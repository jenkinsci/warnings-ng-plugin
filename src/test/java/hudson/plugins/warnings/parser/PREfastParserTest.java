package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link PREfastParser}.
 *
 * @author Charles Chan
 */
public class PREfastParserTest extends ParserTester {
    private static final String TYPE = new PREfastParser().getGroup();

    /**
     * Tests the Puppet-Lint parsing.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void testParse() throws IOException {
        Collection<FileAnnotation> results = createParser().parse(openFile());
        Iterator<FileAnnotation> iterator = results.iterator();

        // Check the first 3 warnings
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                102, "The Drivers module has inferred that the current function is a DRIVER_INITIALIZE function:  This is informational only. No problem has been detected.",
                "sys.c", TYPE, "28101", Priority.NORMAL, "-");

        annotation = iterator.next();
        checkWarning(annotation,
                116, "(PFD)Leaking memory 'device'.",
                "sys.c", TYPE, "6014", Priority.NORMAL, "-");

        annotation = iterator.next();
        checkWarning(annotation,
                137, "The function being assigned or passed should be a DRIVER_UNLOAD function:  Add the declaration 'DRIVER_UNLOAD OnUnload;' before the current first declaration of OnUnload.",
                "sys.c", TYPE, "28155", Priority.NORMAL, "-");

        // Read the remaining warnings
        final int warningsRemaining = 8;
        for (int i=0; i<warningsRemaining; ++i)
        {
            annotation = iterator.next();
        }

        // Failure
        try
        {
            annotation = iterator.next();
            fail("Please check the number of warnings in " + getWarningsFile());
        }
        catch (NoSuchElementException ex)
        {
            // Success (Expected)
        }
    }

    /**
     * Checks the properties of the specified warning.
     *
     * @param annotation
     *            the warning to check
     * @param lineNumber
     *            the expected line number
     * @param message
     *            the expected message
     * @param fileName
     *            the expected filename
     * @param type
     *            the expected type
     * @param category
     *            the expected category
     * @param priority
     *            the expected priority
     * @param packageName
     *            the expected package name
     */
    // CHECKSTYLE:OFF
    private void checkWarning(
            final FileAnnotation annotation,
            final int lineNumber,
            final String message,
            final String fileName,
            final String type,
            final String category,
            final Priority priority,
            final String packageName) {
        checkWarning(annotation, lineNumber, message, fileName, type, category, priority);
        assertEquals("Wrong packageName detected.", packageName, annotation.getPackageName());
    }
    // CHECKSTYLE:ON

    /**
     * Creates the parser.
     *
     * @return the warnings parser
     */
    protected AbstractWarningsParser createParser() {
        return new PREfastParser();
    }

    @Override
    protected String getWarningsFile() {
        return "PREfast.xml";
    }
}
