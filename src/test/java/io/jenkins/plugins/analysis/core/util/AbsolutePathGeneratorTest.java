package io.jenkins.plugins.analysis.core.util;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator.FileSystem;

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.FilePath;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Issues;

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

        new AbsolutePathGenerator().run(issues, WORKSPACE);

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

        Issues<Issue> issues = createIssuesSingleton(fileName, ISSUE_BUILDER.setOrigin(ID));

        AbsolutePathGenerator generator = new AbsolutePathGenerator(fileSystem);
        generator.run(issues, WORKSPACE);

        assertThat(issues.iterator()).containsExactly(ISSUE_BUILDER.setFileName(absolutePath).build());
        assertThat(issues).hasId(ID);
        assertThat(issues.getInfoMessages()).hasSize(1);
        assertThat(issues.getInfoMessages().get(0)).contains("1 resolved");
    }

    @Test
    void shouldDoNothingIfNoIssuesPresent() {
        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        Issues<Issue> issues = new Issues<>();
        issues.setId(ID);
        generator.run(issues, WORKSPACE);
        assertThat(issues).hasSize(0);
        assertThat(issues).hasId(ID);
        assertThat(issues.getInfoMessages()).containsExactly(AbsolutePathGenerator.NOTHING_TO_DO);
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

        Issues<Issue> issues = createIssuesSingleton(relative, ISSUE_BUILDER.setOrigin(ID));
        Issue issueWithAbsolutePath = ISSUE_BUILDER.setFileName("/absolute/path.txt").build();
        issues.add(issueWithAbsolutePath);
        Issue issueWithSelfReference = ISSUE_BUILDER.setFileName(IssueParser.SELF).build();
        issues.add(issueWithSelfReference);

        AbsolutePathGenerator generator = new AbsolutePathGenerator(fileSystem);
        generator.run(issues, WORKSPACE);

        assertThat(issues.iterator())
                .containsExactly(ISSUE_BUILDER.setFileName(absolutePath).build(),
                        issueWithAbsolutePath, issueWithSelfReference);
        assertThat(issues).hasId(ID);
        assertThat(issues.getInfoMessages()).hasSize(1);
        assertThat(issues.getInfoMessages().get(0)).contains("2 already absolute");
    }
}