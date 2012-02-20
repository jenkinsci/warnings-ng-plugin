package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Tests the class {@link EclipseParser}.
 */
public class EclipseParserTest extends ParserTester {
    /**
     * Parses a warning log with 15 warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-12822">Issue 12822</a>
     */
    @Test
    public void issue12822() throws IOException {
        Collection<FileAnnotation> warnings = createParser().parse(openFile("issue12822.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 15, warnings.size());

        ParserResult result = new ParserResult(warnings);
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 15, result.getNumberOfAnnotations());
    }

    /**
     * Parses a warning log with console annotations which are removed.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-11675">Issue 11675</a>
     */
    @Test
    public void issue11675() throws IOException {
        Collection<FileAnnotation> warnings = createParser().parse(openFile("issue11675.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 8, warnings.size());

        for (FileAnnotation annotation : warnings) {
            checkWithAnnotation(annotation);
        }
    }

    private void checkWithAnnotation(final FileAnnotation annotation) {
        assertTrue("Wrong first characted in message", annotation.getMessage().matches("[a-zA-Z].*"));
    }

    /**
     * Parses a warning log with a ClearCase command line that should not be parsed as a warning.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-6427">Issue 6427</a>
     */
    @Test
    public void issue6427() throws IOException {
        Collection<FileAnnotation> warnings = createParser().parse(openFile("issue6427.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 18, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                10,
                "The import com.bombardier.oldinfra.export.dataAccess.InfrastructureDiagramAPI is never used",
                "/srv/hudson/workspace/Ebitool Trunk/build/plugins/com.bombardier.oldInfra.export.jet/jet2java/org/eclipse/jet/compiled/_jet_infraSoe.java",
                getType(), "", Priority.NORMAL);
    }

    /**
     * Creates the parser under test.
     *
     * @return the created parser
     */
    protected WarningsParser createParser() {
        return new EclipseParser();
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

    /**
     * Returns the type of the parser.
     *
     * @return the type of the parser
     */
    protected String getType() {
        return EclipseParser.WARNING_TYPE;
    }

    /**
     * Parses a warning log with 2 eclipse messages, the affected source text spans one and two lines.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-7077">Issue 7077</a>
     */
    @Test
    public void issue7077() throws IOException {
        Collection<FileAnnotation> warnings = createParser().parse(openFile("issue7077.txt"));
        List<FileAnnotation> sorted = Lists.newArrayList(warnings);
        Collections.sort(sorted);

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());

        checkWarning(sorted.get(0),
                90,
                "Type safety: The method setBoHandler(BoHandler) belongs to the raw type BoQuickSearchControl.Builder. References to generic type BoQuickSearchControl<S>.Builder<T> should be parameterized",
                "/ige/hudson/work/jobs/esvclient__development/workspace/target/rcp-build/plugins/ch.ipi.esv.client.customer/src/main/java/ch/ipi/esv/client/customer/search/CustomerQuickSearch.java",
                getType(), "", Priority.NORMAL);
        checkWarning(sorted.get(1),
                90,
                "Type safety: The expression of type BoQuickSearchControl needs unchecked conversion to conform to BoQuickSearchControl<CustomerBO>",
                "/ige/hudson/work/jobs/esvclient__development/workspace/target/rcp-build/plugins/ch.ipi.esv.client.customer/src/main/java/ch/ipi/esv/client/customer/search/CustomerQuickSearch.java",
                getType(), "", Priority.NORMAL);
    }

    /**
     * Parses a warning log with several eclipse messages.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-7077">Issue 7077</a>
     */
    @Test
    public void issue7077all() throws IOException {
        Collection<FileAnnotation> warnings = createParser().parse(openFile("issue7077-all.txt"));
        List<FileAnnotation> sorted = Lists.newArrayList(warnings);
        Collections.sort(sorted);

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 45, sorted.size());

        int number = 0;
        for (FileAnnotation fileAnnotation : sorted) {
            boolean containsHat = fileAnnotation.getMessage().contains("^");
            assertFalse("Message " + number + " contains ^", containsHat);
            number++;
        }
    }


    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "eclipse.txt";
    }
}

