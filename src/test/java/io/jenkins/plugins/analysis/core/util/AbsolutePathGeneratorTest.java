package io.jenkins.plugins.analysis.core.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator.FileSystem;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link AbsolutePathGenerator}.
 *
 * @author Ullrich Hafner
 */
class AbsolutePathGeneratorTest {
    private static final String WORKSPACE_PATH = "path";
    private static final Path WORKSPACE = Paths.get(WORKSPACE_PATH);
    private static final IssueBuilder ISSUE_BUILDER = new IssueBuilder();
    private static final String ID = "ID";

    /**
     * Ensures that illegal file names are processed without problems. Afterwards, the path name should be unchanged.
     */
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$&/&(", "\0 Null-Byte"})
    void shouldReturnFallbackOnError(final String fileName) {
        Report report = createIssuesSingleton(fileName, ISSUE_BUILDER);

        new AbsolutePathGenerator().run(report, WORKSPACE);

        assertThat(report.iterator()).containsExactly(report.get(0));
    }

    private Report createIssuesSingleton(final String fileName, final IssueBuilder issueBuilder) {
        Report report = new Report();
        Issue issue = issueBuilder.setFileName(fileName).build();
        report.add(issue);
        return report;
    }

    @ParameterizedTest(name = "[{index}] File name = {0}")
    @ValueSource(strings = {"relative/file.txt", "../file.txt", "file.txt"})
    void shouldResolveRelativePath(final String fileName) {
        String absolutePath = WORKSPACE_PATH + "/" + fileName;

        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.resolveAbsolutePath(WORKSPACE, fileName)).thenReturn(Optional.of(absolutePath));
        when(fileSystem.isRelative(fileName)).thenReturn(true);

        Report report = createIssuesSingleton(fileName, ISSUE_BUILDER.setOrigin(ID));

        AbsolutePathGenerator generator = new AbsolutePathGenerator(fileSystem);
        generator.run(report, WORKSPACE);

        assertThat(report.iterator()).containsExactly(ISSUE_BUILDER.setFileName(absolutePath).build());
        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("1 resolved");
    }

    @Test
    void shouldDoNothingIfNoIssuesPresent() {
        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        Report report = new Report();
        generator.run(report, WORKSPACE);
        assertThat(report).hasSize(0);
        assertThat(report.getInfoMessages()).containsExactly(AbsolutePathGenerator.NOTHING_TO_DO);
    }

    /**
     * Ensures that absolute paths are not changed while relative paths are resolved.
     */
    @Test
    void shouldNotTouchAbsolutePathOrEmptyPath() {
        String relative = "relative.txt";
        String absolutePath = WORKSPACE_PATH + "/" + relative;

        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.resolveAbsolutePath(WORKSPACE, relative)).thenReturn(Optional.of(absolutePath));
        when(fileSystem.isRelative(absolutePath)).thenReturn(false);
        when(fileSystem.isRelative(relative)).thenReturn(true);
        when(fileSystem.isRelative("-")).thenReturn(true);

        Report report = createIssuesSingleton(relative, ISSUE_BUILDER.setOrigin(ID));
        Issue issueWithAbsolutePath = ISSUE_BUILDER.setFileName("/absolute/path.txt").build();
        report.add(issueWithAbsolutePath);
        Issue issueWithoutFileName = ISSUE_BUILDER.setFileName("-").build();
        report.add(issueWithoutFileName);

        AbsolutePathGenerator generator = new AbsolutePathGenerator(fileSystem);
        generator.run(report, WORKSPACE);

        assertThat(report.iterator())
                .containsExactly(ISSUE_BUILDER.setFileName(absolutePath).build(),
                        issueWithAbsolutePath, issueWithoutFileName);
        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("2 already absolute");
        assertThat(report.getErrorMessages()).isEmpty();
    }
}