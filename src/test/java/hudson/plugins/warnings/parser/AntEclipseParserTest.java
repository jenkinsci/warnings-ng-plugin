package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link AntEclipseParser}.
 */
public class AntEclipseParserTest extends ParserTester {
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected.";

    /**
     * Creates a new instance of {@link AntEclipseParserTest}.
     */
    public AntEclipseParserTest() {
        super(AntEclipseParser.class);
    }

    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseDeprecation() throws IOException {
        Collection<FileAnnotation> warnings = new AntEclipseParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 8, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                3,
                "The serializable class AttributeException does not declare a static final serialVersionUID field of type long",
                "C:/Desenvolvimento/Java/jfg/src/jfg/AttributeException.java",
                AntEclipseParser.WARNING_TYPE, "", Priority.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "eclipse.txt";
    }
}

