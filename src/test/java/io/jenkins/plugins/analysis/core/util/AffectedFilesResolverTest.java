package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

import hudson.FilePath;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link AffectedFilesResolver}.
 *
 * @author Ullrich Hafner
 */
class AffectedFilesResolverTest {
    private static final FilePath BUILD_ROOT = new FilePath(new File("builds"));

    /** Ensures that illegal file names are processed without problems. */
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$$&%/&(", "\0 Null-Byte"})
    void shouldReturnFallbackOnError(final String fileName) throws IOException, InterruptedException {
        Report report = new Report().add(new IssueBuilder().setFileName(fileName).build());
        
        new AffectedFilesResolver().copyAffectedFilesToBuildFolder(report, BUILD_ROOT, createWorkspaceStub());

        assertThat(report.getInfoMessages()).hasSize(1);
        String message = report.getInfoMessages().get(0);
        assertThat(message).contains("0 copied");
        assertThat(message).contains("0 not in workspace");
        assertThat(message).contains("1 not-found");
        assertThat(message).contains("0 with I/O error");
    }

    private FilePath createWorkspaceStub() throws IOException {
        return new FilePath(Files.createTempFile("prefix", "suffix").toFile());
    }
}
