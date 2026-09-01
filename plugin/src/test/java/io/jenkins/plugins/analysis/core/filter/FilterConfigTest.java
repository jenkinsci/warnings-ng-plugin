package io.jenkins.plugins.analysis.core.filter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.util.FilteredLog;

import hudson.FilePath;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link FilterConfig}.
 *
 * @author Michael Trimarchi
 */
class FilterConfigTest {
    @TempDir
    private Path workspace;

    @Test
    void shouldReadConfiguredFileNames() throws IOException {
        write("include.txt", "src/File.ts\nsrc/Other.ts\n");

        var config = new FilterConfig(List.of(), "include.txt");
        var log = new FilteredLog("test");

        var filter = config.readFileNameFilter(new FilePath(workspace.toFile()), log);

        assertThat(filter).isInstanceOf(FileNameFilter.class).isNotInstanceOf(NullFileNameFilter.class);
        assertThat(filter.test(issue("/home/jenkins/workspace/job/src/File.ts"))).isTrue();
        assertThat(filter.test(issue("/home/jenkins/workspace/job/src/Other.ts"))).isTrue();
        assertThat(filter.test(issue("/home/jenkins/workspace/job/src/NotListed.ts"))).isFalse();
        assertThat(log.getErrorMessages()).isEmpty();
        assertThat(log.getInfoMessages()).anyMatch(message -> message.contains("Restricting issues to the 2 files listed"));
    }

    @Test
    void shouldIgnoreBlankLinesInFilterFile() throws IOException {
        write("include.txt", "src/File.ts\n\n   \nsrc/Other.ts\n");

        var config = new FilterConfig(List.of(), "include.txt");
        var log = new FilteredLog("test");

        var filter = config.readFileNameFilter(new FilePath(workspace.toFile()), log);

        assertThat(filter).isInstanceOf(FileNameFilter.class).isNotInstanceOf(NullFileNameFilter.class);
        assertThat(filter.test(issue("/home/jenkins/workspace/job/src/File.ts"))).isTrue();
        assertThat(filter.test(issue("/home/jenkins/workspace/job/src/Other.ts"))).isTrue();
        assertThat(filter.test(issue("/home/jenkins/workspace/job/src/NotListed.ts"))).isFalse();
        assertThat(log.getInfoMessages())
                .anyMatch(message -> message.contains("Restricting issues to the 2 files listed"));
    }

    @Test
    void shouldReturnNullFilterWhenNotConfigured() {
        for (var config : List.of(new FilterConfig(List.of(), null), new FilterConfig(List.of(), "   "))) {
            var log = new FilteredLog("test");

            assertThat(config.readFileNameFilter(new FilePath(workspace.toFile()), log))
                    .isInstanceOf(NullFileNameFilter.class);
            assertThat(log.getErrorMessages()).isEmpty();
        }
    }

    @Test
    void shouldReportMissingWorkspace() {
        var config = new FilterConfig(List.of(), "include.txt");
        var log = new FilteredLog("test");

        assertThat(config.readFileNameFilter(null, log)).isInstanceOf(NullFileNameFilter.class);
        assertThat(log.getErrorMessages()).anyMatch(message -> message.contains("no workspace available"));
    }

    @Test
    void shouldRejectAbsolutePaths() {
        var config = new FilterConfig(List.of(), "/etc/passwd");
        var log = new FilteredLog("test");

        assertThat(config.readFileNameFilter(new FilePath(workspace.toFile()), log))
                .isInstanceOf(NullFileNameFilter.class);
        assertThat(log.getErrorMessages()).anyMatch(message -> message.contains("only relative paths"));
    }

    @Test
    void shouldRejectTraversal() {
        var config = new FilterConfig(List.of(), "../outside.txt");
        var log = new FilteredLog("test");

        assertThat(config.readFileNameFilter(new FilePath(workspace.toFile()), log))
                .isInstanceOf(NullFileNameFilter.class);
        assertThat(log.getErrorMessages()).anyMatch(message -> message.contains("only relative paths"));
    }

    @Test
    void shouldRejectWindowsDriveAndUncPaths() {
        for (var path : List.of("C:\\outside.txt", "\\\\server\\share\\list.txt")) {
            var config = new FilterConfig(List.of(), path);
            var log = new FilteredLog("test");

            assertThat(config.readFileNameFilter(new FilePath(workspace.toFile()), log))
                    .isInstanceOf(NullFileNameFilter.class);
            assertThat(log.getErrorMessages()).anyMatch(message -> message.contains("only relative paths"));
        }
    }

    @Test
    void shouldNormalizeBackslashesAndTraversalWithinWorkspace() throws IOException {
        Files.createDirectories(workspace.resolve("dir"));
        Files.writeString(workspace.resolve("dir").resolve("list.txt"), "src/File.ts\n");

        var config = new FilterConfig(List.of(), "dir\\..\\dir\\list.txt");
        var log = new FilteredLog("test");

        var filter = config.readFileNameFilter(new FilePath(workspace.toFile()), log);

        assertThat(filter).isInstanceOf(FileNameFilter.class).isNotInstanceOf(NullFileNameFilter.class);
        assertThat(filter.test(issue("/home/jenkins/workspace/job/src/File.ts"))).isTrue();
    }

    @Test
    void shouldReportMissingFile() {
        var config = new FilterConfig(List.of(), "missing.txt");
        var log = new FilteredLog("test");

        assertThat(config.readFileNameFilter(new FilePath(workspace.toFile()), log))
                .isInstanceOf(NullFileNameFilter.class);
        assertThat(log.getErrorMessages()).anyMatch(message -> message.contains("Cannot read the filter file"));
    }

    @Test
    void shouldReportEmptyFile() throws IOException {
        write("empty.txt", "\n");

        var config = new FilterConfig(List.of(), "empty.txt");
        var log = new FilteredLog("test");

        assertThat(config.readFileNameFilter(new FilePath(workspace.toFile()), log))
                .isInstanceOf(NullFileNameFilter.class);
        assertThat(log.getInfoMessages()).anyMatch(message -> message.contains("does not list any file name"));
    }

    @Test
    void shouldBlockSymlinkEscapingWorkspace() throws IOException {
        var outside = Files.createTempDirectory("outside");
        try {
            Files.createSymbolicLink(workspace.resolve("link"), outside);

            var config = new FilterConfig(List.of(), "link");
            var log = new FilteredLog("test");

            assertThat(config.readFileNameFilter(new FilePath(workspace.toFile()), log))
                    .isInstanceOf(NullFileNameFilter.class);
            assertThat(log.getErrorMessages()).anyMatch(message -> message.contains("outside of the workspace"));
        }
        finally {
            Files.deleteIfExists(outside);
        }
    }

    private void write(final String name, final String content) throws IOException {
        Files.writeString(workspace.resolve(name), content);
    }

    private Issue issue(final String fileName) {
        return new IssueBuilder().setFileName(fileName).build();
    }
}
