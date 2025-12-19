package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.ResourceTest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import hudson.FilePath;

import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver.CopyResult;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver.RemoteFacade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link AffectedFilesResolver}.
 *
 * @author Ullrich Hafner
 */
class AffectedFilesResolverTest extends ResourceTest {
    private static final FilePath WORKSPACE = new FilePath(new File("workspace"));
    private static final FilePath BUILD_ROOT = new FilePath(new File("builds"));
    private static final String FILE_NAME = "file.txt";

    /**
     * Ensures that illegal file names are processed without problems.
     *
     * @param fileName
     *         the file to process
     */
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$$&%/&(", "\0 Null-Byte"})
    @DisplayName("Should ignore illegal path names")
    void shouldReturnFallbackOnError(final String fileName) throws IOException, InterruptedException {
        var report = new Report().add(new IssueBuilder().setFileName(fileName).build());

        new AffectedFilesResolver().copyAffectedFilesToBuildFolder(report, WORKSPACE, Collections.emptySet(), BUILD_ROOT);

        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        var message = report.getInfoMessages().get(0);
        assertThat(message).contains("0 copied");
        assertThat(message).contains("0 not in workspace");
        assertThat(message).contains("1 not-found");
        assertThat(message).contains("0 with I/O error");
    }

    @Test
    void shouldDoNothingForEmptyReport() throws InterruptedException, IOException {
        var resolver = new AffectedFilesResolver();

        var report = new Report();
        RemoteFacade remoteFacade = mock(RemoteFacade.class);
        when(remoteFacade.copyAllInBatch(any(Report.class), any(FilteredLog.class)))
                .thenReturn(new CopyResult(0, 0, 0));

        resolver.copyAffectedFilesToBuildFolder(report, remoteFacade);

        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        var message = report.getInfoMessages().get(0);
        assertThat(message).contains("0 copied");
        assertThat(message).contains("0 not in workspace");
        assertThat(message).contains("0 not-found");
        assertThat(message).contains("0 with I/O error");
    }

    @Test
    void shouldCopyFile() throws InterruptedException, IOException {
        var resolver = new AffectedFilesResolver();

        var report = new Report();
        var issue = new IssueBuilder().setFileName(FILE_NAME).build();
        report.add(issue);

        RemoteFacade remoteFacade = mock(RemoteFacade.class);
        when(remoteFacade.copyAllInBatch(any(Report.class), any(FilteredLog.class)))
                .thenReturn(new CopyResult(1, 0, 0));

        resolver.copyAffectedFilesToBuildFolder(report, remoteFacade);

        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        var message = report.getInfoMessages().get(0);
        assertThat(message).contains("1 copied");
        assertThat(message).contains("0 not in workspace");
        assertThat(message).contains("0 not-found");
        assertThat(message).contains("0 with I/O error");
    }

    @Test
    void shouldReportCopyExceptions() throws InterruptedException, IOException {
        var resolver = new AffectedFilesResolver();

        var report = new Report();
        var issue = new IssueBuilder().setFileName(FILE_NAME).build();
        report.add(issue);

        RemoteFacade remoteFacade = mock(RemoteFacade.class);
        when(remoteFacade.copyAllInBatch(any(Report.class), any(FilteredLog.class)))
                .thenThrow(new IOException("Batch copy failed"));

        resolver.copyAffectedFilesToBuildFolder(report, remoteFacade);

        assertThat(report.getErrorMessages())
                .hasSize(1)
                .contains("Failed to copy files in batch: Batch copy failed");
        assertThat(report.getInfoMessages()).hasSize(1);
        var message = report.getInfoMessages().get(0);
        assertThat(message).contains("0 copied");
        assertThat(message).contains("0 not in workspace");
        assertThat(message).contains("0 not-found");
        assertThat(message).contains("0 with I/O error");
    }

    @Test
    void shouldSkipNonWorkspaceFile() throws InterruptedException, IOException {
        var resolver = new AffectedFilesResolver();

        var report = new Report();
        var issue = new IssueBuilder().setFileName(FILE_NAME).build();
        report.add(issue);

        RemoteFacade remoteFacade = mock(RemoteFacade.class);
        when(remoteFacade.copyAllInBatch(any(Report.class), any(FilteredLog.class)))
                .thenReturn(new CopyResult(0, 0, 1));

        resolver.copyAffectedFilesToBuildFolder(report, remoteFacade);

        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        var message = report.getInfoMessages().get(0);
        assertThat(message).contains("0 copied");
        assertThat(message).contains("1 not in workspace");
        assertThat(message).contains("0 not-found");
        assertThat(message).contains("0 with I/O error");
    }

    @Nested
    class RemoteFacadeTest {
        @Test
        void shouldFindFileInWorkspace() {
            var workspace = WORKSPACE;
            var sub = workspace.child("sub");
            var remoteFacade = new RemoteFacade(sub, Collections.emptySet(), BUILD_ROOT);

            assertThat(remoteFacade.isInWorkspace(sub.getRemote())).isTrue();
            assertThat(remoteFacade.isInWorkspace(sub.child(FILE_NAME).getRemote())).isTrue();

            assertThat(remoteFacade.isInWorkspace(workspace.getRemote())).isFalse();
        }
    }
}
