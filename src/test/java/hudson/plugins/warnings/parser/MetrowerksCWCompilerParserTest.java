package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link MetrowerksCWCompilerParser}.
 */
public class MetrowerksCWCompilerParserTest extends ParserTester {
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected.";
    /** An info. */
    private static final String INFO_CATEGORY = "Info";
    /** A warning. */
    private static final String WARNING_CATEGORY = "Warning";
    /** An error. */
    private static final String ERROR_CATEGORY = "ERROR";
    private static final String WARNING_TYPE = new MetrowerksCWCompilerParser().getGroup();

    /**
     * Parses a file with two GCC warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new MetrowerksCWCompilerParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 5, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                570,
                "Warning-directive found: EEPROM_QUEUE_BUFFER_SIZE instead of MONITOR_ERROR_DATA_LENGTH is used here. This must be fixed sooner or later",
                "E:/work/PATH/PATH/PATH/PATH/Test1.c",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                305,
                "Possible loss of data",
                "E:/work/PATH/PATH/PATH/Test2.c",
                WARNING_TYPE, WARNING_CATEGORY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                1501,
                "bla not declared (or typename)",
                "E:/work/PATH/PATH/Test3.c",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                1502,
                "';' missing",
                "E:/work/PATH/Test4.c",
                WARNING_TYPE, ERROR_CATEGORY, Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                480,
                "Inline expansion done for function call",
                "E:/work/PATH/PATH/PATH/PATH/PATH/PATH/PATH/Test5.c",
                WARNING_TYPE, INFO_CATEGORY, Priority.LOW);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "MetrowerksCWCompiler.txt";
    }
}

