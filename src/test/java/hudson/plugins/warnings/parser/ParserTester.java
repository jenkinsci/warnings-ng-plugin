package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.junit.Test;


/**
 * Base class for parser tests. Provides an assertion test for warnings.
 */
public abstract class ParserTester {
    /** The parser that should find warnings. All other parsers should find no warning. */
    private final Class<? extends WarningsParser> validParser;

    /**
     * Creates a new instance of {@link ParserTester}.
     *
     * @param validParser
     *            the parser that should find warnings. All other parsers should
     *            find no warning.
     */
    public ParserTester(final Class<? extends WarningsParser> validParser) {
        this.validParser = validParser;
    }

    /**
     * Checks the properties of the specified warning.
     *
     * @param annotation the warning to check
     * @param lineNumber the expected line number
     * @param message the expected message
     * @param fileName the expected filename
     * @param type the expected type
     * @param category the expected category
     * @param priority the expected priority
     */
    protected void checkWarning(final FileAnnotation annotation, final int lineNumber, final String message, final String fileName, final String type, final String category, final Priority priority) {
        assertTrue("Annotation is of wrong type.", annotation instanceof Warning);
        Warning warning = (Warning)annotation;
        assertEquals("Wrong type of warning detected.", type, warning.getType());
        assertEquals("Wrong priotiry detected.", priority, warning.getPriority());
        assertEquals("Wrong category of warning detected.", category, warning.getCategory());
        assertEquals("Wrong number of ranges detected.", 1, warning.getLineRanges().size());
        assertEquals("Wrong ranges start detected.", lineNumber, warning.getLineRanges().iterator().next().getStart());
        assertEquals("Wrong ranges end detected.", lineNumber, warning.getLineRanges().iterator().next().getEnd());
        assertEquals("Wrong message detected.", message, warning.getMessage());
        assertEquals("Wrong filename detected.", fileName, warning.getFileName());
    }

    /**
     * Verifies that no other parser returns a warning for the log file of the
     * current parser.
     *
     * @throws IOException
     *             if the file could not be read
     * @see #getWarningsFile() needs to be implemented by subclasses
     */
    @Test
    public void verifyOtherParsers() throws IOException {
        for (WarningsParser parser : new ParserRegistry().getParsers()) {
            if (!parser.getClass().equals(validParser)) {
                Collection<FileAnnotation> warnings = parser.parse(openFile());
                assertEquals("Warning found with parser " + parser + " in file: " + getWarningsFile(),
                        0, warnings.size());
            }
        }
    }

    /**
     * Returns an input stream with the warnings.
     *
     * @return an input stream
     */
    protected InputStream openFile() {
        return ParserTester.class.getResourceAsStream(getWarningsFile());
    }

    /**
     * Returns the file name of the warnings file.
     *
     * @return the warnings file name
     */
    protected abstract String getWarningsFile();
}
