package io.jenkins.plugins.analysis.core.util;

import java.io.File;

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
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$$&%/&(", "\0 Null-Byte"})
    void shouldReturnFallbackOnError(final String fileName) {
        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        IssueBuilder builder = new IssueBuilder();

        Issues<Issue> issues = new Issues<>();
        Issue issue = builder.setOrigin("Test").setFileName(fileName).build();
        issues.add(issue);

        Issues<Issue> resolved = generator.run(issues, builder, new FilePath(new File("path")));

        assertThat(resolved.iterator()).containsExactly(issue);
    }

    @ParameterizedTest(name = "[{index}] File name = {0}")
    @ValueSource(strings = {"relative/file.txt", "../file.txt", "file.txt"})
    void shouldResolveRelativePath(final String fileName) {
        String prefix = "path";
        FilePath workspace = new FilePath(new File(prefix));
        String absolutePath = prefix + "/" + fileName;

        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.resolveFile(fileName, workspace)).thenReturn(absolutePath);

        IssueBuilder builder = new IssueBuilder();

        Issues<Issue> issues = new Issues<>();
        Issue issue = builder.setOrigin("Test").setFileName(fileName).build();
        issues.add(issue);

        AbsolutePathGenerator generator = new AbsolutePathGenerator(fileSystem);

        Issues<Issue> resolved = generator.run(issues, builder, workspace);

        assertThat(resolved.iterator()).containsExactly(builder.setFileName(absolutePath).build());
    }
}