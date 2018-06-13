package io.jenkins.plugins.analysis.warnings.groovy;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.EclipseParser;
import edu.hm.hafner.analysis.parser.EclipseParserTest;

/**
 * Tests the class {@link DynamicDocumentParser}. Creates a new Eclipse parser in Groovy. All Eclipse test cases
 * are reused.
 *
 * @author Ullrich Hafner
 */
class DynamicDocumentParserTest extends EclipseParserTest {
    @Override
    protected AbstractParser createParser() {
        return new DynamicDocumentParser(EclipseParser.ANT_ECLIPSE_WARNING_PATTERN, toString("eclipse.groovy"));
    }
}