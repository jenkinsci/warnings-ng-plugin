package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests {@link IdeaInspectionParser } parser class.
 *
 * @author Alex Lopashev, alexlopashev@gmail.com
 */
public class IdeaInspectionParserTest extends ParserTester {
    /**
     * Parses an example file with single inspection.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parse() throws IOException {
        Collection<FileAnnotation> inspections = new IdeaInspectionParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, inspections.size());

        Iterator<FileAnnotation> iterator = inspections.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                42,
                "Parameter <code>intentionallyUnusedString</code> is not used  in either this method or any of its derived methods",
                "file://$PROJECT_DIR$/src/main/java/org/lopashev/Test.java",
                "Unused method parameters",
                Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "IdeaInspectionExample.xml";
    }
}

