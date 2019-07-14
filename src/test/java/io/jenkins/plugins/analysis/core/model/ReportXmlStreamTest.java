package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.ResourceTest;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

/**
 * Tests the class {@link ReportXmlStream}.
 *
 * @author Ullrich Hafner
 */
class ReportXmlStreamTest extends ResourceTest {
    @Test
    void shouldReadIssues() {
        ReportXmlStream reportXmlStream = new ReportXmlStream();

        Object restored = reportXmlStream.read(getResourceAsFile("java-report.xml"));

        Path saved = createTempFile();
        assertThat(restored).isInstanceOfSatisfying(Report.class,
                report -> {
                    assertThatReportIsCorrect(report);

                    reportXmlStream.write(saved, report);
                });

        Report newFormat = reportXmlStream.read(saved);
        assertThatReportIsCorrect(newFormat);
    }

    private void assertThatReportIsCorrect(final Report report) {
        assertThat(report).hasSize(9);

        assertThat(report.get(0))
                .hasCategory("ConstructorLeaksThis")
                .hasSeverity(Severity.WARNING_NORMAL)
                .hasLineStart(83)
                .hasColumnStart(44)
                .hasOrigin("java")
                .hasModuleName("Static Analysis Model and Parsers")
                .hasPackageName("edu.hm.hafner.analysis")
                .hasFileName("/var/data/workspace/pipeline-analysis-model/src/main/java/edu/hm/hafner/analysis/Report.java");
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile("test", ".xml");
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

}
