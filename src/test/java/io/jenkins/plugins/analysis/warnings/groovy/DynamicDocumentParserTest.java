package io.jenkins.plugins.analysis.warnings.groovy;

import edu.hm.hafner.analysis.AbstractParserTest;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.analysis.assertj.Assertions;
import edu.hm.hafner.analysis.assertj.SoftAssertions;
import edu.hm.hafner.analysis.parser.EclipseParser;

/**
 * Tests the class {@link DynamicDocumentParser}. Creates a new Eclipse parser in Groovy. All Eclipse test cases are
 * reused.
 *
 * @author Ullrich Hafner
 */
class DynamicDocumentParserTest extends AbstractParserTest {
    DynamicDocumentParserTest() {
        super("eclipse.txt");
    }

    @Override
    protected DynamicDocumentParser createParser() {
        return new DynamicDocumentParser(EclipseParser.ANT_ECLIPSE_WARNING_PATTERN, toString("eclipse.groovy"));
    }

    @Override
    protected void assertThatIssuesArePresent(final Report report, final SoftAssertions softly) {
        Assertions.assertThat(report).hasSize(8);
        Issue annotation = report.get(0);
        softly.assertThat(annotation)
                .hasSeverity(Severity.WARNING_NORMAL)
                .hasLineStart(3)
                .hasLineEnd(3)
                .hasMessage(
                        "The serializable class AttributeException does not declare a static final serialVersionUID field of type long")
                .hasFileName("C:/Desenvolvimento/Java/jfg/src/jfg/AttributeException.java");
    }
}