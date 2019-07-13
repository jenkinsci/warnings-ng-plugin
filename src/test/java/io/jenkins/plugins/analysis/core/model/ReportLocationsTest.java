package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.forensics.blame.FileLocations;
import io.jenkins.plugins.forensics.blame.FileLocations.FileSystem;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        Report report = new Report();
        FileLocations empty = createLocations();

        new ReportLocations().toFileLocations(report, empty);

        assertThat(empty.getRelativePaths()).isEmpty();
    }

    @Test
    void shouldConvertReportWithOneWarning() {
        Report report = new Report();

        IssueBuilder builder = new IssueBuilder();
        builder.setDirectory(WORKSPACE);
        report.add(builder.setFileName(TXT_FILE).setLineStart(1).build());

        FileLocations singleLine = createLocations();

        new ReportLocations().toFileLocations(report, singleLine);

        assertThat(singleLine.getRelativePaths()).containsExactly(TXT_FILE);
        assertThat(singleLine.getLines(TXT_FILE)).containsExactly(1);
    }

    @Test
    void shouldConvertReportWithTwoLinesInOneFile() {
        Report report = new Report();

        IssueBuilder builder = new IssueBuilder();
        builder.setDirectory(WORKSPACE);
        report.add(builder.setFileName(TXT_FILE).setLineStart(1).build());
        report.add(builder.setFileName(TXT_FILE).setLineStart(5).build());

        FileLocations twoLines = createLocations();

        new ReportLocations().toFileLocations(report, twoLines);

        assertThat(twoLines.getRelativePaths()).containsExactly(TXT_FILE);
        assertThat(twoLines.getLines(TXT_FILE)).containsExactly(1, 5);
    }

    @Test
    void shouldConvertReport() {
        Report report = new Report();

        IssueBuilder builder = new IssueBuilder();
        builder.setDirectory(WORKSPACE);
        report.add(builder.setFileName(TXT_FILE).setLineStart(1).build());
        report.add(builder.setFileName(JAVA_FILE).setLineStart(10).build());

        FileLocations twoFiles = createLocations();

        new ReportLocations().toFileLocations(report, twoFiles);

        assertThat(twoFiles.getRelativePaths()).containsExactlyInAnyOrder(TXT_FILE, JAVA_FILE);
        assertThat(twoFiles.getLines(TXT_FILE)).containsExactly(1);
        assertThat(twoFiles.getLines(JAVA_FILE)).containsExactly(10);
    }

    private FileLocations createLocations() {
        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.resolveAbsolutePath(anyString(), any())).thenReturn(WORKSPACE);
        return new FileLocations(WORKSPACE, fileSystem);
    }
}
