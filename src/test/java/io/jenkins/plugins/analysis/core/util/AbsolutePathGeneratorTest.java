package io.jenkins.plugins.analysis.core.util;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator.FileSystem;
import static org.mockito.Mockito.*;

import hudson.FilePath;

/**
 * Tests the class {@link AbsolutePathGenerator}.
 *
 * @author Ullrich Hafner
 */
class AbsolutePathGeneratorTest {
    private static final String WORKSPACE_PATH = "path";
    private static final FilePath WORKSPACE = new FilePath(new File(WORKSPACE_PATH));
    private static final IssueBuilder ISSUE_BUILDER = new IssueBuilder();
    private static final String ID = "ID";

    /**
     * Ensures that illegal file names are processed without problems. Afterwards, the path name should be unchanged.
     */
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$$&%/&(", "\0 Null-Byte"})
    void shouldReturnFallbackOnError(final String fileName) {
        Issues<Issue> issues = createIssuesSingleton(fileName, ISSUE_BUILDER);

        new AbsolutePathGenerator().run(issues, ISSUE_BUILDER, WORKSPACE);

        assertThat(issues.iterator()).containsExactly(issues.get(0));
        assertThat(issues).hasId(ID);
    }

    private Issues<Issue> createIssuesSingleton(final String fileName, final IssueBuilder issueBuilder) {
        Issues<Issue> issues = new Issues<>();
        Issue issue = issueBuilder.setFileName(fileName).build();
        issues.add(issue);
        issues.setId(ID);
        return issues;
    }

    @ParameterizedTest(name = "[{index}] File name = {0}")
    @ValueSource(strings = {"relative/file.txt", "../file.txt", "file.txt"})
    void shouldResolveRelativePath(final String fileName) {
        String absolutePath = WORKSPACE_PATH + "/" + fileName;

        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.resolveFile(fileName, WORKSPACE)).thenReturn(absolutePath);

        Issues<Issue> issues = createIssuesSingleton(fileName, ISSUE_BUILDER.setOrigin("Test"));

        AbsolutePathGenerator generator = new AbsolutePathGenerator(fileSystem);
        generator.run(issues, ISSUE_BUILDER, WORKSPACE);

        assertThat(issues.iterator()).containsExactly(ISSUE_BUILDER.setFileName(absolutePath).build());
        assertThat(issues).hasId(ID);
    }

    @Test
    void shouldDoNothingIfNoIssuesPresent() {
        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        Issues<Issue> issues = new Issues<>();
        issues.setId(ID);
        generator.run(issues, ISSUE_BUILDER, WORKSPACE);
        assertThat(issues).hasSize(0);
        assertThat(issues).hasId(ID);
    }

    /**
     * Ensures that absolute paths are not changed while relative paths are resolved.
     */
    @Test
    void shouldNotTouchAbsolutePath() {
        String relative = "relative.txt";
        String absolutePath = WORKSPACE_PATH + "/" + relative;

        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.resolveFile(relative, WORKSPACE)).thenReturn(absolutePath);

        Issues<Issue> issues = createIssuesSingleton(relative, ISSUE_BUILDER.setOrigin("Test"));
        Issue issueWithAbsolutePath = ISSUE_BUILDER.setFileName("/absolute/path.txt").build();
        issues.add(issueWithAbsolutePath);

        AbsolutePathGenerator generator = new AbsolutePathGenerator(fileSystem);
        generator.run(issues, ISSUE_BUILDER, WORKSPACE);

        assertThat(issues.iterator())
                .containsExactly(ISSUE_BUILDER.setFileName(absolutePath).build(), issueWithAbsolutePath);
        assertThat(issues).hasId(ID);
    }
}