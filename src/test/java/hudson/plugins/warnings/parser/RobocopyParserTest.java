package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link RobocopyParser}.
 */
public class RobocopyParserTest extends ParserTester {
    /**
     * Parses a file with three Robocopy warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new RobocopyParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 3, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "b",
                "b",
                RobocopyParser.WARNING_TYPE, "EXTRA File", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "a.log",
                "a.log",
                RobocopyParser.WARNING_TYPE, "New File", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "a.log",
                "a.log",
                RobocopyParser.WARNING_TYPE, "same", Priority.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "robocopy.txt";
    }
}

