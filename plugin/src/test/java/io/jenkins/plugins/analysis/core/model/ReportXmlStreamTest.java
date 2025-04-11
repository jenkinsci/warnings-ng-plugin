package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.ResourceTest;

import java.nio.file.Path;

import static edu.hm.hafner.analysis.assertions.Assertions.*;

/**
 * Tests the class {@link ReportXmlStream}.
 *
 * @author Ullrich Hafner
 */
class ReportXmlStreamTest extends ResourceTest {
    @Test @Issue("JENKINS-61293")
    void shouldMapDescriptionsToCorrectType() {
        ReportXmlStream reportXmlStream = new ReportXmlStream();

        Object restored = reportXmlStream.read(getResourceAsFile("npe.xml"));
        assertThat(restored).isInstanceOfSatisfying(Report.class,
                report ->
                    assertThat(report).hasSize(2));
    }

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

    @Test
    void shouldStoreOriginFiles() {
        ReportXmlStream reportXmlStream = new ReportXmlStream();

        Report newIssues = reportXmlStream.read(getResourceAsFile("analysis-new-issues.xml"));
        Report outstandingIssues = reportXmlStream.read(getResourceAsFile("analysis-outstanding-issues.xml"));

        Report merged = new Report();
        merged.addAll(newIssues, outstandingIssues);

        assertThat(newIssues).hasOriginReportFiles("/var/data/workspace/freestyle-analysis-model/target/test-classes/edu/hm/hafner/analysis/parser/spotbugsXml.xml",
                "/var/data/workspace/freestyle-analysis-model/src/test/resources/edu/hm/hafner/analysis/parser/pmd/pmd.xml",
                "/var/data/workspace/freestyle-analysis-model/target/test-classes/edu/hm/hafner/analysis/parser/pmd/pmd.xml",
                "/var/data/workspace/freestyle-analysis-model/target/spotbugsXml.xml",
                "/var/data/workspace/freestyle-analysis-model/target/test-classes/edu/hm/hafner/analysis/parser/dry/cpd/cpd.xml",
                "/var/data/workspace/freestyle-analysis-model/target/pmd.xml",
                "/var/data/workspace/freestyle-analysis-model/src/test/resources/edu/hm/hafner/analysis/parser/dry/cpd/cpd.xml",
                "/var/data/workspace/freestyle-analysis-model/src/test/resources/edu/hm/hafner/analysis/parser/findbugs/spotbugsXml.xml",
                "/var/data/workspace/freestyle-analysis-model/target/cpd.xml",
                "/var/data/workspace/freestyle-analysis-model/target/test-classes/edu/hm/hafner/analysis/parser/cpd.xml",
                "jenkins-console.log",
                "/var/data/workspace/freestyle-analysis-model/src/test/resources/edu/hm/hafner/analysis/parser/cpd.xml",
                "/var/data/workspace/freestyle-analysis-model/src/test/resources/edu/hm/hafner/analysis/parser/spotbugsXml.xml",
                "/var/data/workspace/freestyle-analysis-model/target/test-classes/edu/hm/hafner/analysis/parser/findbugs/spotbugsXml.xml");
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
}
