package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link MavenConsoleParser}.
 *
 * @author Ulli Hafner
 */
public class MavenConsoleParserTest extends ParserTester {
    /**
     * Verifies that errors and warnings are correctly picked up.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testParsing() throws IOException {
        Collection<FileAnnotation> warnings = new MavenConsoleParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 4, warnings.size());
        AnnotationContainer result = new DefaultAnnotationContainer(warnings);
        assertEquals("Wrong number of errors detected.", 2, result.getNumberOfAnnotations(Priority.HIGH));
        assertEquals("Wrong number of warnings detected.", 2, result.getNumberOfAnnotations(Priority.NORMAL));
    }

    /**
     * Parses a file with three warnings, two of them will be ignored beacuse they are blank.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-16826">Issue 16826</a>
     */
    @Test
    public void issue16826() throws IOException {
        Collection<FileAnnotation> warnings = new MavenConsoleParser().parse(openFile("issue16826.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
    }

    /**
     * Parses a file with three warnings, two of them will be ignored beacuse they are blank.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-16826">Issue 16826</a>
     */
    @Test
    public void largeFile() throws IOException {
        Collection<FileAnnotation> warnings = new MavenConsoleParser().parse(openFile("maven-large.log"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
    }

    @Override
    protected String getWarningsFile() {
        return "maven-console.txt";
    }
}

