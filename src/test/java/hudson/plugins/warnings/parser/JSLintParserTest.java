package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.analysis.util.model.WorkspaceFile;

/**
 * Tests the class {@link JSLintParser}.
 *
 * @author Gavin Mogan
 */
public class JSLintParserTest extends ParserTester {
    private static final String EXPECTED_FILE_NAME = "duckworth/hudson-jslint-freestyle/src/prototype.js";

    /**
     * Parses a file with one warning that are started by ant.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-19127">Issue 19127</a>
     */
    @Test
    public void issue19127() throws IOException {
        Collection<FileAnnotation> warnings = new JSLintParser().parse(openFile("jslint/jslint.xml"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 197, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(), 3, 5,
                "'window' is not defined.", "C:/DVR/lint_Mobile-Localization_ws/evWebService/WebClientApi/api-v1.js",
                JSLintXMLSaxParser.CATEGORY_UNDEFINED_VARIABLE, Priority.HIGH);
    }

    /**
     * Tests the JS-Lint parsing for warnings in different files.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void testParse() throws IOException {
        Collection<FileAnnotation> results = createParser().parse(openFile());
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 102, results.size());

        DefaultAnnotationContainer container = new DefaultAnnotationContainer(results);
        Collection<WorkspaceFile> files = container.getFiles();
        assertEquals("Wrong number of files", 2, files.size());

        List<WorkspaceFile> sortedFiles = Lists.newArrayList(files);
        Collections.sort(sortedFiles);

        verifyFileName(sortedFiles, EXPECTED_FILE_NAME, 0);
        verifyFileName(sortedFiles, "duckworth/hudson-jslint-freestyle/src/scriptaculous.js", 1);

        FileAnnotation firstWarning = results.iterator().next();
        checkWarning(firstWarning, 10, 3, "Expected 'Version' to have an indentation at 5 instead at 3.",
                EXPECTED_FILE_NAME, JSLintXMLSaxParser.CATEGORY_PARSING, Priority.HIGH);
    }

    private void verifyFileName(final List<WorkspaceFile> sortedFiles, final String expectedName, final int position) {
        assertEquals("Wrong file found: ", expectedName, sortedFiles.get(position).getName());
    }

    /**
     * Tests the JS-Lint parsing for warnings in a single file.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void testParseWithSingleFile() throws IOException {
        Collection<FileAnnotation> results = createParser().parse(openFile("jslint/single.xml"));
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 51, results.size());
    }

    /**
     * Tests parsing of CSS-Lint files.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void testCssLint() throws IOException {
        Collection<FileAnnotation> results = createParser().parse(openFile("jslint/csslint.xml"));
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 51, results.size());
    }

    /**
     * Creates the parser.
     *
     * @return the warnings parser
     */
    protected AbstractWarningsParser createParser() {
        return new JSLintParser();
    }

    @Override
    protected String getWarningsFile() {
        return "jslint/multi.xml";
    }
}
