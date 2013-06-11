package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link RobocopyParser}.
 */
public class RobocopyParserTest extends ParserTester {
    private static final String TYPE = new RobocopyParser().getGroup();
    private static final String FILENAME = "a.log";

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
                TYPE, "EXTRA File", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                FILENAME,
                FILENAME,
                TYPE, "New File", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                FILENAME,
                FILENAME,
                TYPE, "same", Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "robocopy.txt";
    }
}

