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
    void shouldConvertReport() {
        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.resolveAbsolutePath(anyString(), any())).thenReturn(WORKSPACE);

        Report report = new Report();

        ReportLocations reportLocations = new ReportLocations();
        assertThat(reportLocations.toFileLocations(WORKSPACE, report, createFileLocations()).getRelativePaths()).isEmpty();

        IssueBuilder builder = new IssueBuilder();
        builder.setDirectory(WORKSPACE);

        report.add(builder.setFileName(TXT_FILE).setLineStart(1).build());

        FileLocations fileLocations = reportLocations.toFileLocations(WORKSPACE, report, createFileLocations());
        assertThat(fileLocations.getRelativePaths()).containsExactly(TXT_FILE);
        assertThat(fileLocations.getLines(TXT_FILE)).containsExactly(1);

        report.add(builder.setFileName(TXT_FILE).setLineStart(5).build());
        fileLocations = reportLocations.toFileLocations(WORKSPACE, report, createFileLocations());
        assertThat(fileLocations.getRelativePaths()).containsExactly(TXT_FILE);
        assertThat(fileLocations.getLines(TXT_FILE)).containsExactly(1, 5);

        report.add(builder.setFileName(JAVA_FILE).setLineStart(10).build());
        fileLocations = reportLocations.toFileLocations(WORKSPACE, report, createFileLocations());
        assertThat(fileLocations.getRelativePaths()).containsExactlyInAnyOrder(TXT_FILE, JAVA_FILE);
        assertThat(fileLocations.getLines(TXT_FILE)).containsExactly(1, 5);
        assertThat(fileLocations.getLines(JAVA_FILE)).containsExactly(10);
    }

    private FileLocations createFileLocations() {
        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.resolveAbsolutePath(anyString(), any())).thenReturn(WORKSPACE);
        FileLocations locations = new FileLocations();
        locations.setFileSystem(fileSystem);
        return locations;
    }
}
