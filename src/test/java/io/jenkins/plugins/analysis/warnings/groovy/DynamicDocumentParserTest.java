package io.jenkins.plugins.analysis.warnings.groovy;

import edu.hm.hafner.analysis.AbstractParserTest;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.analysis.assertj.Assertions;
import edu.hm.hafner.analysis.assertj.SoftAssertions;

/**
 * Tests the class {@link DynamicDocumentParser}. Creates a new Eclipse parser in Groovy. All Eclipse test cases are
 * reused.
 *
 * @author Ullrich Hafner
 */
class DynamicDocumentParserTest extends AbstractParserTest {
    private static final String ANT_ECLIPSE_WARNING_PATTERN =
            "(?:\\[?(?:INFO|WARNING|ERROR)\\]?.*)?" + // Ignore leading type (output embedded in output)
                    "\\[?(INFO|WARNING|ERROR)\\]?" +          // group 1 'type': INFO, WARNING or ERROR in optional []
                    "\\s*(?:in)?" +                           // optional " in"
                    "\\s*(.*)" +                              // group 2 'filename'
                    "(?:\\(at line\\s*(\\d+)\\)|" +           // either group 3 'lineNumber': at line dd
                    ":\\[(\\d+)).*" +                         // or group 4 'rowNumber': eg :[row,col] - col ignored
                    "(?:\\r?\\n[^\\^\\n]*)+?" +               // 1+ ignored lines (no column pointer) eg source excerpt
                    "\\r?\\n.*\\t([^\\^]*)" +                 // newline then group 5 (indent for column pointers)
                    "([\\^]+).*" +                            // group 6 column pointers (^^^^^)
                    "\\r?\\n(?:\\s*\\[.*\\]\\s*)?" +          // newline then optional ignored text in [] (eg [javac])
                    "(.*)";                                   // group 7 'message'
    DynamicDocumentParserTest() {
        super("eclipse.txt");
    }

    @Override
    protected DynamicDocumentParser createParser() {
        return new DynamicDocumentParser(ANT_ECLIPSE_WARNING_PATTERN, toString("eclipse.groovy"));
    }

    @Override
    protected void assertThatIssuesArePresent(final Report report, final SoftAssertions softly) {
        Assertions.assertThat(report).hasSize(8);
        Issue annotation = report.get(0);
        softly.assertThat(annotation)
                .hasSeverity(Severity.WARNING_NORMAL)
                .hasLineStart(3)
                .hasLineEnd(3)
                .hasMessage("The serializable class AttributeException does not declare a static final serialVersionUID field of type long")
                .hasFileName("C:/Desenvolvimento/Java/jfg/src/jfg/AttributeException.java");
    }
}