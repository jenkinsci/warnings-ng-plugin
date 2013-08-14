package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringEscapeUtils;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Base class for parser tests. Provides an assertion test for warnings.
 */
public abstract class ParserTester {
    static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected: ";

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
     * @param category
     *            the expected category
     * @param priority
     *            the expected priority
     */
    protected void checkWarning(final FileAnnotation annotation, final int lineNumber, final String message, final String fileName, final String category, final Priority priority) {
        Warning warning = castWarning(annotation);
        assertEquals("Wrong priority detected: ", priority, warning.getPriority());
        assertEquals("Wrong category of warning detected: ", category, warning.getCategory());
        assertEquals("Wrong number of ranges detected: ", 1, warning.getLineRanges().size());
        assertEquals("Wrong ranges start detected: ", lineNumber, warning.getLineRanges().iterator().next().getStart());
        assertEquals("Wrong ranges end detected: ", lineNumber, warning.getLineRanges().iterator().next().getEnd());
        assertEquals("Wrong message detected: ", StringEscapeUtils.escapeXml(message), warning.getMessage());
        assertEquals("Wrong filename detected: ", fileName, warning.getFileName());
    }

    /**
     * Checks the properties of the specified warning.
     *
     * @param annotation
     *            the warning to check
     * @param lineNumber
     *            the expected line number
     * @param column
     *            the expected column
     * @param message
     *            the expected message
     * @param fileName
     *            the expected filename
     * @param category
     *            the expected category
     * @param priority
     *            the expected priority
     */
    protected void checkWarning(final FileAnnotation annotation, final int lineNumber, final int column, final String message, final String fileName, final String category, final Priority priority) {
        checkWarning(annotation, lineNumber, message, fileName, category, priority);

        checkColumn(annotation, column);
    }

    private void checkColumn(final FileAnnotation annotation, final int column) {
        assertEquals("Wrong column detected: ", column, annotation.getColumnStart());
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
     */
    protected void checkWarning(final FileAnnotation annotation, final int lineNumber, final String message, final String fileName, final String type, final String category, final Priority priority) {
        assertEquals("Wrong type of warning detected.", type, castWarning(annotation).getType());

        checkWarning(annotation, lineNumber, message, fileName, category, priority);
    }

    /**
     * Checks the properties of the specified warning.
     *
     * @param annotation
     *            the warning to check
     * @param lineNumber
     *            the expected line number
     * @param column
     *            the expected column
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
     */
    protected void checkWarning(final FileAnnotation annotation, final int lineNumber, final int column, final String message, final String fileName, final String type, final String category, final Priority priority) { // NOCHECKSTYLE
        checkWarning(annotation, lineNumber, message, fileName, type, category, priority);

        checkColumn(annotation, column);
    }

    private Warning castWarning(final FileAnnotation annotation) {
        assertTrue("Annotation is of wrong type.", annotation instanceof Warning);
        return (Warning)annotation;
    }

    /**
     * Returns an input stream with the warnings.
     *
     * @return an input stream
     */
    protected Reader openFile() {
        return openFile(getWarningsFile());
    }

    /**
     * Returns an input stream with the warnings.
     *
     * @param fileName
     *            the file to read
     * @return an input stream
     */
    protected Reader openFile(final String fileName) {
        try {
            return new InputStreamReader(ParserTester.class.getResourceAsStream(fileName), "UTF-8");
        }
        catch (UnsupportedEncodingException exception) {
            return new InputStreamReader(ParserTester.class.getResourceAsStream(fileName));
        }
    }

    /**
     * Returns the file name of the warnings file.
     *
     * @return the warnings file name
     */
    protected abstract String getWarningsFile();
}
