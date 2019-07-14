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

        FileLocations empty = new ReportLocations().toFileLocations(report, WORKSPACE, createFileSystemStub());

        assertThat(empty.getRelativePaths()).isEmpty();
    }

    @Test
    void shouldConvertReportWithOneWarning() {
        Report report = new Report();

        IssueBuilder builder = new IssueBuilder();
        builder.setDirectory(WORKSPACE);
        report.add(builder.setFileName(TXT_FILE).setLineStart(1).build());

        FileLocations singleLine = new ReportLocations().toFileLocations(report, WORKSPACE, createFileSystemStub());

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

        FileLocations twoLines = new ReportLocations().toFileLocations(report, WORKSPACE, createFileSystemStub());

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

        FileLocations twoFiles = new ReportLocations().toFileLocations(report, WORKSPACE, createFileSystemStub());

        assertThat(twoFiles.getRelativePaths()).containsExactlyInAnyOrder(TXT_FILE, JAVA_FILE);
        assertThat(twoFiles.getLines(TXT_FILE)).containsExactly(1);
        assertThat(twoFiles.getLines(JAVA_FILE)).containsExactly(10);
    }

    private FileSystem createFileSystemStub() {
        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.resolveAbsolutePath(anyString(), any())).thenReturn(WORKSPACE);
        return fileSystem;
    }
}
