package hudson.plugins.warnings;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests the class {@link GroovyParser}.
 *
 * @author Ulli Hafner
 */
public class GroovyParserTest {
    /**
     * Verifies that multi line expressions are correctly detected.
     */
    @Test
    public void testMultiLine() {
        GroovyParser parser = new GroovyParser("name", WarningsDescriptorTest.MULTI_LINE_REGEXP, "empty");

        assertTrue("Wrong multi line support guess", parser.hasMultiLineSupport());
    }

    /**
     * Verifies that single line expressions are correctly detected.
     */
    @Test
    public void testSingleLine() {
        GroovyParser parser = new GroovyParser("name", WarningsDescriptorTest.SINGLE_LINE_REGEXP, "empty");

        assertFalse("Wrong single line support guess", parser.hasMultiLineSupport());
    }
}

