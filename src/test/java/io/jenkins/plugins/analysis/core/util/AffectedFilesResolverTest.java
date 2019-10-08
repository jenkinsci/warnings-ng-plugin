package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

import hudson.FilePath;

import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver.RemoteFacade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link AffectedFilesResolver}.
 *
 * @author Ullrich Hafner
 */
class AffectedFilesResolverTest {
    private static final FilePath BUILD_ROOT = new FilePath(new File("builds"));
    private static final String FILE_NAME = "file.txt";

    /** Ensures that illegal file names are processed without problems. */
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$$&%/&(", "\0 Null-Byte"})
    @DisplayName("Should ignore illegal path names")
    void shouldReturnFallbackOnError(final String fileName) throws IOException, InterruptedException {
        Report report = new Report().add(new IssueBuilder().setFileName(fileName).build());

        new AffectedFilesResolver().copyAffectedFilesToBuildFolder(report, BUILD_ROOT, createWorkspaceStub());

        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        String message = report.getInfoMessages().get(0);
        assertThat(message).contains("0 copied");
        assertThat(message).contains("0 not in workspace");
        assertThat(message).contains("1 not-found");
        assertThat(message).contains("0 with I/O error");
    }

    private FilePath createWorkspaceStub() throws IOException {
        return new FilePath(Files.createTempDirectory("prefix").toFile());
    }

    @Test
    void shouldDoNothingForEmptyReport() throws InterruptedException {
        AffectedFilesResolver resolver = new AffectedFilesResolver();

        Report report = new Report();
        resolver.copyAffectedFilesToBuildFolder(report, mock(RemoteFacade.class));

        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        String message = report.getInfoMessages().get(0);
        assertThat(message).contains("0 copied");
        assertThat(message).contains("0 not in workspace");
        assertThat(message).contains("0 not-found");
        assertThat(message).contains("0 with I/O error");
    }

    @Test
    void shouldCopyFile() throws InterruptedException {
        AffectedFilesResolver resolver = new AffectedFilesResolver();

        Report report = new Report();
        Issue issue = new IssueBuilder().setFileName(FILE_NAME).build();
        report.add(issue);

        RemoteFacade remoteFacade = mock(RemoteFacade.class);
        when(remoteFacade.exists(FILE_NAME)).thenReturn(true);
        when(remoteFacade.isInWorkspace(FILE_NAME)).thenReturn(true);

        resolver.copyAffectedFilesToBuildFolder(report, remoteFacade);

        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        String message = report.getInfoMessages().get(0);
        assertThat(message).contains("1 copied");
        assertThat(message).contains("0 not in workspace");
        assertThat(message).contains("0 not-found");
        assertThat(message).contains("0 with I/O error");
    }

    @Test
    void shouldReportCopyExceptions() throws InterruptedException, IOException {
        AffectedFilesResolver resolver = new AffectedFilesResolver();

        Report report = new Report();
        Issue issue = new IssueBuilder().setFileName(FILE_NAME).build();
        report.add(issue);

        RemoteFacade remoteFacade = mock(RemoteFacade.class);
        when(remoteFacade.exists(FILE_NAME)).thenReturn(true);
        when(remoteFacade.isInWorkspace(FILE_NAME)).thenReturn(true);
        doThrow(IOException.class).when(remoteFacade).copy(FILE_NAME);

        resolver.copyAffectedFilesToBuildFolder(report, remoteFacade);

        assertThat(report.getErrorMessages())
                .hasSize(2)
                .contains("Can't copy some affected workspace files to Jenkins build folder:",
                        "- 'file.txt', IO exception has been thrown: java.io.IOException");
        assertThat(report.getInfoMessages()).hasSize(1);
        String message = report.getInfoMessages().get(0);
        assertThat(message).contains("0 copied");
        assertThat(message).contains("0 not in workspace");
        assertThat(message).contains("0 not-found");
        assertThat(message).contains("1 with I/O error");
    }

    @Test
    void shouldSkipNonWorkspaceFile() throws InterruptedException {
        AffectedFilesResolver resolver = new AffectedFilesResolver();

        Report report = new Report();
        Issue issue = new IssueBuilder().setFileName(FILE_NAME).build();
        report.add(issue);

        RemoteFacade remoteFacade = mock(RemoteFacade.class);
        when(remoteFacade.exists(FILE_NAME)).thenReturn(true);
        when(remoteFacade.isInWorkspace(FILE_NAME)).thenReturn(false);

        resolver.copyAffectedFilesToBuildFolder(report, remoteFacade);

        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        String message = report.getInfoMessages().get(0);
        assertThat(message).contains("0 copied");
        assertThat(message).contains("1 not in workspace");
        assertThat(message).contains("0 not-found");
        assertThat(message).contains("0 with I/O error");
    }

    @Nested
    class RemoteFacadeTest {
        @Test
        void shouldFindFileInWorkspace() throws IOException {
            FilePath buildFolderStub = createWorkspaceStub();
            FilePath workspaceStub = createWorkspaceStub();
            FilePath sourceFolderStub = createWorkspaceStub();
            RemoteFacade remoteFacade = new RemoteFacade(buildFolderStub, workspaceStub);

            assertThat(remoteFacade.isInWorkspace(workspaceStub.getRemote())).isTrue();
            assertThat(remoteFacade.isInWorkspace(workspaceStub.child(FILE_NAME).getRemote())).isTrue();

            assertThat(remoteFacade.isInWorkspace(sourceFolderStub.getRemote())).isFalse();
            assertThat(remoteFacade.isInWorkspace(sourceFolderStub.child(FILE_NAME).getRemote())).isFalse();
        }

        @Test
        void shouldFindFileInSourceFolder() throws IOException {
            FilePath buildFolderStub = createWorkspaceStub();
            FilePath workspaceStub = createWorkspaceStub();
            FilePath sourceFolderStub = createWorkspaceStub();

            RemoteFacade remoteFacade = new RemoteFacade(buildFolderStub, workspaceStub, sourceFolderStub);

            assertThat(remoteFacade.isInWorkspace(workspaceStub.getRemote())).isTrue();
            assertThat(remoteFacade.isInWorkspace(workspaceStub.child(FILE_NAME).getRemote())).isTrue();

            assertThat(remoteFacade.isInWorkspace(sourceFolderStub.getRemote())).isTrue();
            assertThat(remoteFacade.isInWorkspace(sourceFolderStub.child(FILE_NAME).getRemote())).isTrue();
        }
    }
}
