package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.FilteredLog;

import io.jenkins.plugins.analysis.core.filter.FilterConfig;
import io.jenkins.plugins.analysis.core.filter.IncludeFile;
import io.jenkins.plugins.forensics.blame.Blamer;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileLocations;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the report filtering performed by {@link IssuesScanner.ReportPostProcessor}.
 *
 * @author Michael Trimarchi
 */
class ReportPostProcessorTest {
    @TempDir
    private Path workspace;

    @Test
    void shouldApplyFileFilterAndLogSummary() throws IOException {
        write("src/File.ts", "content");
        write("src/Other.ts", "content");
        write("include.txt", "src/File.ts\n");

        var result = postProcess(report("src/File.ts", "src/Other.ts"),
                new FilterConfig(List.of(), "include.txt"));

        assertThat(result.getReport()).hasSize(1);
        assertThat(result.getReport().get(0).getFileName()).endsWith("src/File.ts");
        assertThat(result.getReport().getInfoMessages())
                .anyMatch(message -> message.contains("Restricting issues to the 1 files listed"));
        assertThat(result.getReport().getInfoMessages())
                .anyMatch(message -> message.contains(
                        "Applying 1 filters on the set of 2 issues (1 issues have been removed, 1 issues will be published)"));
    }

    @Test
    void shouldApplyRegexpFilter() throws IOException {
        write("src/File.ts", "content");
        write("src/Other.ts", "content");

        var result = postProcess(report("src/File.ts", "src/Other.ts"),
                new FilterConfig(List.of(new IncludeFile(".*File\\.ts")), null));

        assertThat(result.getReport()).hasSize(1);
        assertThat(result.getReport().get(0).getFileName()).endsWith("src/File.ts");
        assertThat(result.getReport().getInfoMessages())
                .anyMatch(message -> message.contains(
                        "Applying 1 filters on the set of 2 issues (1 issues have been removed, 1 issues will be published)"));
    }

    @Test
    void shouldIgnoreBlankRegexpFilter() throws IOException {
        write("src/File.ts", "content");

        var result = postProcess(report("src/File.ts"),
                new FilterConfig(List.of(new IncludeFile("")), null));

        assertThat(result.getReport()).hasSize(1);
        assertThat(result.getReport().getInfoMessages())
                .anyMatch(message -> message.contains("No filter has been set, publishing all 1 issues"));
    }

    @Test
    void shouldPublishAllIssuesWhenNoFilterIsConfigured() throws IOException {
        write("src/File.ts", "content");
        write("src/Other.ts", "content");

        var result = postProcess(report("src/File.ts", "src/Other.ts"),
                new FilterConfig(List.of(), null));

        assertThat(result.getReport()).hasSize(2);
        assertThat(result.getReport().getInfoMessages())
                .anyMatch(message -> message.contains("No filter has been set, publishing all 2 issues"));
    }

    @Test
    void shouldReportUnreadableFilterFileButPublishAllIssues() throws IOException {
        write("src/File.ts", "content");

        var result = postProcess(report("src/File.ts"),
                new FilterConfig(List.of(), "missing.txt"));

        assertThat(result.getReport()).hasSize(1);
        assertThat(result.getReport().getErrorMessages())
                .anyMatch(message -> message.contains("Cannot read the filter file 'missing.txt'"));
        assertThat(result.getReport().getInfoMessages())
                .anyMatch(message -> message.contains("No filter has been set, publishing all 1 issues"));
    }

    private AnnotatedReport postProcess(final Report report, final FilterConfig filterConfig) {
        var blamer = mock(Blamer.class);
        when(blamer.blame(any(FileLocations.class), any(FilteredLog.class))).thenReturn(new Blames());

        var processor = new IssuesScanner.ReportPostProcessor("test", report, "UTF-8", blamer, filterConfig,
                Set.of(), Set.of(), IssuesScanner.PostProcessingMode.DISABLED, -1, "", "");

        return processor.invoke(workspace.toFile(), null);
    }

    private Report report(final String... fileNames) {
        var report = new Report();
        for (var fileName : fileNames) {
            report.add(new IssueBuilder().setFileName(fileName).build());
        }
        return report;
    }

    private void write(final String path, final String content) throws IOException {
        var file = workspace.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
