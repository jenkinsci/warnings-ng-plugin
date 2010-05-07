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
     * Parses a warning log with a ClearCase command line that should not be parsed as a warning.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.hudson-ci.org/browse/HUDSON-6427">Issue 6427</a>
     */
    @Test
    public void issue6427() throws IOException {
        Collection<FileAnnotation> warnings = new AntEclipseParser().parse(openFile("issue6427.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 18, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                10,
                "The import com.bombardier.oldinfra.export.dataAccess.InfrastructureDiagramAPI is never used",
                "/srv/hudson/workspace/Ebitool Trunk/build/plugins/com.bombardier.oldInfra.export.jet/jet2java/org/eclipse/jet/compiled/_jet_infraSoe.java",
                AntEclipseParser.WARNING_TYPE, "", Priority.NORMAL);
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

