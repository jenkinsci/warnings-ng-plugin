package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Tests the class {@link JSLintParser}.
 *
 * @author Gavin Mogan <gavin@kodekoan.com>
 */
public class JSLintParserTest extends ParserTester {
    private static final String EXPECTED_FILE_NAME = "duckworth/hudson-jslint-freestyle/src/prototype.js";

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

        Set<String> files = Sets.newHashSet();

        for (FileAnnotation warning : results) {
            files.add(warning.getFileName());
        }

        assertEquals("Wrong number of files", 2, files.size());
        assertTrue("File not found", files.contains(EXPECTED_FILE_NAME));
        assertTrue("File not found", files.contains("duckworth/hudson-jslint-freestyle/src/scriptaculous.js"));
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
