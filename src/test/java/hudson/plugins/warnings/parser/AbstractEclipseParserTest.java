package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Basic tests for the Eclipse parser.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractEclipseParserTest extends ParserTester {
    /**
     * Creates the parser under test.
     *
     * @return the created parser
     */
    protected AbstractWarningsParser createParser() {
        return new EclipseParser();
    }

    /**
     * Returns the type of the parser.
     *
     * @return the type of the parser
     */
    protected String getType() {
        return new EclipseParser().getGroup();
    }

    @Override
    protected String getWarningsFile() {
        return "eclipse.txt";
    }

    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseDeprecation() throws IOException {
        Collection<FileAnnotation> warnings = createParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 8, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                3,
                "The serializable class AttributeException does not declare a static final serialVersionUID field of type long",
                "C:/Desenvolvimento/Java/jfg/src/jfg/AttributeException.java",
                getType(), "", Priority.NORMAL);
    }
}
