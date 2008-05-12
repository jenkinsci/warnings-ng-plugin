package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;

/**
 * Base class for parser tests. Provides an assertion test for warnings.
 */
public class ParserTester {
    /**
     * Checks the properties of the specified warning.
     *
     * @param annotation the warning to check
     * @param lineNumber the expected line number
     * @param message the expected message
     * @param fileName the expected filename
     * @param type the expected type
     */
    protected void checkWarning(final FileAnnotation annotation, final int lineNumber, final String message, final String fileName, final String type) {
        assertTrue("Annotation is of wrong type.", annotation instanceof Warning);
        Warning warning = (Warning)annotation;
        assertEquals("Wrong type of warning detected.", type, warning.getType());
        assertEquals("Wrong category of warning detected.", "deprecation", warning.getCategory());
        assertEquals("Wrong number of ranges detected.", 1, warning.getLineRanges().size());
        assertEquals("Wrong ranges start detected.", lineNumber, warning.getLineRanges().iterator().next().getStart());
        assertEquals("Wrong ranges end detected.", lineNumber, warning.getLineRanges().iterator().next().getEnd());
        assertEquals("Wrong message detected.", message, warning.getMessage());
        assertEquals("Wrong filename detected.", fileName, warning.getFileName());
    }
}
