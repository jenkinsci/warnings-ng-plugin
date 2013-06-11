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
     * Tests the Puppet-Lint parsing.
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
