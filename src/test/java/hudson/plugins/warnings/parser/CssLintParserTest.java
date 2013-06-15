package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Tests the class {@link JSLintParser}.
 *
 * @author Ulli Hafner
 */
public class CssLintParserTest extends ParserTester {
    /**
     * Tests parsing of CSS-Lint files.
     *
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void testCssLint() throws IOException {
        Collection<FileAnnotation> results = createParser().parse(openFile());
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 51, results.size());
    }

    /**
     * Creates the parser.
     *
     * @return the warnings parser
     */
    protected AbstractWarningsParser createParser() {
        return new CssLintParser();
    }

    @Override
    protected String getWarningsFile() {
        return "jslint/csslint.xml";
    }
}
