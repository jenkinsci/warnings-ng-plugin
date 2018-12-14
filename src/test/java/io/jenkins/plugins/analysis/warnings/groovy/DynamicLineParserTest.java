package io.jenkins.plugins.analysis.warnings.groovy;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.AbstractParserTest;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import edu.hm.hafner.analysis.assertj.SoftAssertions;

/**
 * Test the class {@link DynamicLineParser}. Creates a new Pep8 parser in Groovy. All Pep8 test cases are reused.
 *
 * @author Ullrich Hafner
 */
class DynamicLineParserTest extends AbstractParserTest {
    private static final String FILE_NAME = "file-with-line-numbers.txt";

    DynamicLineParserTest() {
        super("pep8Test.txt");
    }

    @Override
    protected void assertThatIssuesArePresent(final Report report, final SoftAssertions softly) {
        softly.assertThat(report)
                .hasSize(8).hasSeverities(0, 0, 6, 2);

        softly.assertThat(report.get(0))
                .hasFileName("optparse.py")
                .hasCategory("E401")
                .hasSeverity(Severity.WARNING_NORMAL)
                .hasMessage("multiple imports on one line")
                .hasDescription("")
                .hasPackageName("-")
                .hasLineStart(69)
                .hasLineEnd(69)
                .hasColumnStart(11)
                .hasColumnEnd(11);
    }

    @Override
    public DynamicLineParser createParser() {
        return new DynamicLineParser("(.*):(\\d+):(\\d+): (\\D\\d*) (.*)", toString("pep8.groovy"));
    }
    
    @Test
    void shouldScanAllLinesAndAssignLineNumberAndFileName() {
        DynamicLineParser parser = new DynamicLineParser("^(.*)$", 
                "return builder.setFileName(fileName).setLineStart(lineNumber).setMessage(matcher.group(1)).buildOptional()");
        Report report = parser.parse(createReaderFactory(FILE_NAME));
        
        assertThat(report).hasSize(3);
        for (int i = 0; i < 3; i++) {
            assertThat(report.get(i)).hasBaseName(FILE_NAME).hasLineStart(i + 1).hasMessage(String.valueOf(i + 1));
        }
    }
}

