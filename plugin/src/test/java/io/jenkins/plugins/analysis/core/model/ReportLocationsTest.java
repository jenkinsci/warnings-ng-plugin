package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link ReportLocations}.
 *
 * @author Ullrich Hafner
 */
class ReportLocationsTest {
    private static final String TXT_FILE = "file.txt";
    private static final String JAVA_FILE = "file.java";
    private static final String WORKSPACE = "/workspace";

    @Test
    void shouldConvertEmptyReport() {
        var report = new Report();

        var empty = new ReportLocations().toFileLocations(report);

        assertThat(empty.getFiles()).isEmpty();
    }

    @Test
    void shouldConvertReportWithOneWarning() {
        try (var builder = new IssueBuilder()) {
            var report = new Report();

            builder.setDirectory(WORKSPACE);
            report.add(builder.setFileName(TXT_FILE).setLineStart(1).build());

            var singleLine = new ReportLocations().toFileLocations(report);

            assertThat(singleLine.getFiles()).containsExactly(absolute(TXT_FILE));
            assertThat(singleLine.getLines(absolute(TXT_FILE))).containsExactly(1);
        }
    }

    @Test
    void shouldConvertReportWithTwoLinesInOneFile() {
        try (var builder = new IssueBuilder()) {
            var report = new Report();

            builder.setDirectory(WORKSPACE);
            report.add(builder.setFileName(TXT_FILE).setLineStart(1).build());
            report.add(builder.setFileName(TXT_FILE).setLineStart(5).build());

            var twoLines = new ReportLocations().toFileLocations(report);

            assertThat(twoLines.getFiles()).containsExactly(absolute(TXT_FILE));
            assertThat(twoLines.getLines(absolute(TXT_FILE))).containsExactly(1, 5);
        }
    }

    @Test
    void shouldConvertReport() {
        try (var builder = new IssueBuilder()) {
            var report = new Report();

            builder.setDirectory(WORKSPACE);
            report.add(builder.setFileName(TXT_FILE).setLineStart(1).build());
            report.add(builder.setFileName(JAVA_FILE).setLineStart(10).build());

            var twoFiles = new ReportLocations().toFileLocations(report);

            assertThat(twoFiles.getFiles()).containsExactlyInAnyOrder(absolute(TXT_FILE), absolute(JAVA_FILE));
            assertThat(twoFiles.getLines(absolute(TXT_FILE))).containsExactly(1);
            assertThat(twoFiles.getLines(absolute(JAVA_FILE))).containsExactly(10);
        }
    }

    private String absolute(final String fileName) {
        return WORKSPACE + "/" + fileName;
    }
}
