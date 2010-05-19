package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

/**
 * Tests the class {@link DoxygenParser}.
 */
public class DoxygenParserTest extends ParserTester {
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected.";

    /**
     * Parses a file with Doxygen warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new DoxygenParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 19, warnings.size());
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "doxygen.txt";
    }
}

