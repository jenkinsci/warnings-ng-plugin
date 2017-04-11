package hudson.plugins.warnings.parser;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.Test;

/**
 * Tests the class {@link GroovyExpressionMatcher}.
 *
 * @author Ullrich Hafner
 */
public class GroovyExpressionMatcherTest {
    private static final String LEGAL_PARSER_SCRIPT = "import hudson.plugins.warnings.parser.Warning";
    private static final String ILLEGAL_PARSER_SCRIPT = "0:0";

    /**
     * Compiles a valid Groovy snippet.
     */
    @Test
    public void shouldCompileValidScript() {
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(LEGAL_PARSER_SCRIPT, null);
        matcher.compile();
    }

    /**
     * Finds a compile error in a Groovy snippet.
     */
    @Test(expected = CompilationFailedException.class)
    public void shouldFindCompileError() {
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(ILLEGAL_PARSER_SCRIPT, null);
        matcher.compile();
    }
}