package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link EclipseParser}.
 */
public class EclipseParserTest extends AbstractEclipseParserTest {
    /**
     * Parses a warning log with previously undetected warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-21377">Issue 21377</a>
     */
    @Test
    public void issue21377() throws IOException {
        Collection<FileAnnotation> warnings = createParser().parse(openFile("issue21377.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        ParserResult result = new ParserResult(warnings);
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, result.getNumberOfAnnotations());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                13,
                "The method getOldValue() from the type SomeType is deprecated",
                "/path/to/job/job-name/module/src/main/java/com/example/Example.java",
                getType(), "", Priority.NORMAL);
    }

    /**
     * Parses a warning log with 2 previously undetected warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-13969">Issue 13969</a>
     */
    @Test
    public void issue13969() throws IOException {
        Collection<FileAnnotation> warnings = createParser().parse(openFile("issue13969.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());

        ParserResult result = new ParserResult(warnings);
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, result.getNumberOfAnnotations());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                369,
                "The method compare(List<String>, List<String>) from the type PmModelImporter is never used locally",
                "/media/ssd/multi-x-processor/workspace/msgPM_Access/com.faktorzehn.pa2msgpm.core/src/com/faktorzehn/pa2msgpm/core/loader/PmModelImporter.java",
                getType(), "", Priority.NORMAL);
        checkWarning(iterator.next(),
                391,
                "The method getTableValues(PropertyRestrictionType) from the type PmModelImporter is never used locally",
                "/media/ssd/multi-x-processor/workspace/msgPM_Access/com.faktorzehn.pa2msgpm.core/src/com/faktorzehn/pa2msgpm/core/loader/PmModelImporter.java",
                getType(), "", Priority.NORMAL);
        checkWarning(iterator.next(),
                56,
                "The value of the field PropertyImporterTest.ERROR_RESPONSE is not used",
                "/media/ssd/multi-x-processor/workspace/msgPM_Access/com.faktorzehn.pa2msgpm.core.test/src/com/faktorzehn/pa2msgpm/core/importer/PropertyImporterTest.java",
                getType(), "", Priority.NORMAL);
    }

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
}

