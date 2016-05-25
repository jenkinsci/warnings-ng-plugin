package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link MetrowerksCWLinkerParser}.
 */
public class MetrowerksCWLinkerParserTest extends ParserTester {
    private static final String INFO_CATEGORY = "Info";
    private static final String WARNING_CATEGORY = "Warning";
    private static final String ERROR_CATEGORY = "ERROR";
    private static final String TYPE = new MetrowerksCWLinkerParser().getGroup();

    /**
     * Parses a file with two GCC warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new MetrowerksCWLinkerParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "L1822: Symbol TestFunction in file e:/work/PATH/PATH/PATH/PATH/appl_src.lib is undefined",
                "See Warning message",
                TYPE, ERROR_CATEGORY, Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "L1916: Section name TEST_SECTION is too long. Name is cut to 90 characters length",
                "See Warning message",
                TYPE, WARNING_CATEGORY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "L2: Message overflow, skipping WARNING messages",
                "See Warning message",
                TYPE, INFO_CATEGORY, Priority.LOW);
    }



    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "MetrowerksCWLinker.txt";
    }
}

