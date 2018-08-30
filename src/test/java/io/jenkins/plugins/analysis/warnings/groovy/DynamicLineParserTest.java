package io.jenkins.plugins.analysis.warnings.groovy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Report;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import edu.hm.hafner.analysis.parser.Pep8ParserTest;

/**
 * Test the class {@link DynamicLineParser}. Creates a new Pep8 parser in Groovy. All Pep8 test cases are reused.
 *
 * @author Ullrich Hafner
 */
class DynamicLineParserTest extends Pep8ParserTest {
    private static final String FILE_NAME = "file-with-line-numbers.txt";

    @Override
    public AbstractParser createParser() {
        return new DynamicLineParser("(.*):(\\d+):(\\d+): (\\D\\d*) (.*)", toString("pep8.groovy"));
    }
    
    @Test
    void shouldScanAllLinesAndAssignLineNumberAndFileName() {
        DynamicLineParser parser = new DynamicLineParser("^(.*)$", 
                "return builder.setFileName(fileName).setLineStart(lineNumber).setMessage(matcher.group(1)).build()");
        Report report = parser.parse(getResourceAsFile(FILE_NAME), StandardCharsets.UTF_8);
        
        assertThat(report).hasSize(3);
        for (int i = 0; i < 3; i++) {
            assertThat(report.get(i)).hasBaseName(FILE_NAME).hasLineStart(i + 1).hasMessage(String.valueOf(i + 1));
        }
    }
}

