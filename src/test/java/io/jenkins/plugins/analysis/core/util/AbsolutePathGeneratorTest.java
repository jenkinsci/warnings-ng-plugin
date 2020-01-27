package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.PathUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static edu.hm.hafner.analysis.assertions.Assertions.*;
import static io.jenkins.plugins.analysis.core.util.ConsoleLogHandler.*;

/**
 * Tests the class {@link AbsolutePathGenerator}.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings("DMI")
class AbsolutePathGeneratorTest {
    private static final URI RESOURCE_FOLDER = getResourceFolder();
    private static final Path RESOURCE_FOLDER_PATH = Paths.get(RESOURCE_FOLDER);
    private static final String RESOURCE_FOLDER_STRING = getUriPath(RESOURCE_FOLDER);

    private static final String ID = "ID";
    private static final String RELATIVE_FILE = "relative.txt";
    private static final char SLASH = '/';

    /**
     * Ensures that illegal file names are processed without problems. Afterwards, the path name should be unchanged.
     */
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$&/&(", "\0 Null-Byte", "C:/!<>$&/&( \0", "/!<>$&/&( \0"})
    @DisplayName("Should not change path on errors")
    @org.jvnet.hudson.test.Issue("JENKINS-59986")
    void shouldReturnFallbackOnError(final String fileName) {
        Report report = createIssuesSingleton(fileName, new IssueBuilder());

        resolvePaths(report, RESOURCE_FOLDER_PATH);

        assertThat(report.iterator()).toIterable().containsExactly(report.get(0));
        assertThatOneFileIsUnresolved(fileName, report);
    }

    private AbsolutePathGenerator resolvePaths(final Report report, final Path workspace) {
        AbsolutePathGenerator absolutePathGenerator = new AbsolutePathGenerator();
        absolutePathGenerator.run(report, Collections.singleton(workspace.toString()));
        return absolutePathGenerator;
    }

    @Test
    @DisplayName("Should skip processing if there are no issues")
    void shouldDoNothingIfNoIssuesPresent() {
        Report report = new Report();

        resolvePaths(report, RESOURCE_FOLDER_PATH);

        assertThat(report).hasSize(0);
        assertThat(report.getInfoMessages()).containsExactly(AbsolutePathGenerator.NOTHING_TO_DO);
    }

    @Test
    @DisplayName("Should skip existing absolute paths")
    void shouldNotTouchAbsolutePathOrEmptyPath() {
        Report report = new Report();

        IssueBuilder builder = new IssueBuilder();

        report.add(builder.setFileName("").build());
        report.add(builder.setFileName(JENKINS_CONSOLE_LOG_FILE_NAME_ID).build());
        report.add(builder.setFileName("relative.txt").build());
        report.add(builder.setDirectory(RESOURCE_FOLDER_STRING).setFileName("relative.txt").build());
        report.add(builder.setDirectory(RESOURCE_FOLDER_STRING).setFileName(normalize("../../core/util/normalized.txt")).build());

        resolvePaths(report, RESOURCE_FOLDER_PATH);

        assertThat(report).hasSize(5);
        assertThat(report.get(0)).as("Issue with no file name")
                .hasFileName("-");
        assertThat(report.get(1)).as("Issue in console log")
                .hasFileName(JENKINS_CONSOLE_LOG_FILE_NAME_ID);
        assertThat(report.get(2)).as("Issue with relative file name")
                .hasFileName(getAbsolutePath(RELATIVE_FILE));
        assertThat(report.get(3)).as("Issue with absolute file name (normalized)")
                .hasFileName(getAbsolutePath(RELATIVE_FILE));
        assertThat(report.get(4)).as("Issue with absolute file name (not normalized)")
                .hasFileName(getAbsolutePath("normalized.txt"));

        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).as(report.stream().map(Issue::getFileName).collect(Collectors.joining()))
                .contains("1 resolved");
        assertThat(report.getInfoMessages().get(0)).contains("2 already resolved");
        assertThat(report.getErrorMessages()).isEmpty();
    }

    private String getAbsolutePath(final String fileName) {
        return new PathUtil().getAbsolutePath(RESOURCE_FOLDER_PATH.resolve(fileName));
    }

    @ParameterizedTest(name = "[{index}] Relative file name = {0}")
    @ValueSource(strings = {"../util/relative.txt", "relative.txt", "../../core/util/relative.txt"})
    @DisplayName("Should replace relative issue path with absolute path")
    void shouldResolveRelativePath(final String fileName) {
        IssueBuilder builder = new IssueBuilder();

        Report report = createIssuesSingleton(fileName, builder.setOrigin(ID));

        resolvePaths(report, RESOURCE_FOLDER_PATH);

        assertThat(report.get(0).getFileName()).as("Resolving file '%s'", normalize(fileName))
                .isEqualTo(getAbsolutePath(RELATIVE_FILE));
        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("1 resolved");
    }

    @Test
    @DisplayName("Should replace relative issue path with absolute path in child folder")
    void shouldResolveRelativePathInOtherFolder() {
        IssueBuilder builder = new IssueBuilder();

        String fileName = "child.txt";
        Report report = createIssuesSingleton(fileName, builder.setOrigin(ID));

        AbsolutePathGenerator generator = resolvePaths(report, RESOURCE_FOLDER_PATH);

        assertThatOneFileIsUnresolved(fileName, report);

        report = createIssuesSingleton(fileName, builder.setOrigin(ID));
        generator.run(report,  Collections.singleton(RESOURCE_FOLDER_PATH.resolve("child").toString()));

        assertThatChildIsResolved(report);

        report = createIssuesSingleton(fileName, builder.setOrigin(ID));
        generator.run(report, RESOURCE_FOLDER_PATH, RESOURCE_FOLDER_PATH.resolve("child"));

        assertThatChildIsResolved(report);

        report = createIssuesSingleton(fileName, builder.setOrigin(ID));
        generator.run(report, RESOURCE_FOLDER_PATH.resolve("child"), RESOURCE_FOLDER_PATH);

        assertThatChildIsResolved(report);
    }

    @Test
    @DisplayName("Should replace relative issue path with absolute path in relative path of workspace")
    void shouldResolveRelativePathInWorkspaceSubFolder() {
        IssueBuilder builder = new IssueBuilder();

        String fileName = "child.txt";
        Report report = createIssuesSingleton(fileName, builder.setOrigin(ID));

        AbsolutePathGenerator generator = resolvePaths(report, RESOURCE_FOLDER_PATH);

        assertThatOneFileIsUnresolved(fileName, report);

        report = createIssuesSingleton(fileName, builder.setOrigin(ID));
        generator.run(report, createSubFolder());

        assertThatChildIsResolved(report);

        assertThatChildIsResolved(report);

        report = createIssuesSingleton(fileName, builder.setOrigin(ID));
        generator.run(report, RESOURCE_FOLDER_PATH.resolve("child"), RESOURCE_FOLDER_PATH);

        assertThatChildIsResolved(report);
    }

    private Set<String> createSubFolder() {
        return Collections.singleton(RESOURCE_FOLDER_PATH.resolve("child").toString());
    }

    private void assertThatChildIsResolved(final Report report) {
        assertThat(report.get(0).getFileName()).isEqualTo(getAbsolutePath("child/child.txt"));
        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("1 resolved");
    }

    private void assertThatOneFileIsUnresolved(final String fileName, final Report report) {
        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("1 unresolved");
        assertThat(report.getErrorMessages()).hasSize(2).contains("- " + fileName);
    }

    @ParameterizedTest(name = "[{index}] Relative filename = {0}")
    @ValueSource(strings = {"../util/relative.txt", "relative.txt", "../../core/util/relative.txt"})
    @DisplayName("Should let existing absolute path unchanged")
    void shouldResolveAbsolutePath(final String fileName) {
        Report report = new Report();

        Issue issue = new IssueBuilder()
                .setDirectory(RESOURCE_FOLDER_STRING)
                .setFileName(normalize(fileName))
                .build();
        report.add(issue);

        resolvePaths(report, Paths.get(RESOURCE_FOLDER));

        String description = String.format("Resolving file '%s'", normalize(fileName));
        assertThat(report.get(0).getFileName()).as(description)
                .isEqualTo(getAbsolutePath(RELATIVE_FILE));
        assertThat(report.getErrorMessages()).as(description).isEmpty();
        assertThat(report.getInfoMessages()).as(description).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).as(description).contains("1 already resolved");
    }

    private static String getUriPath(final URI folder) {
        String workspace = folder.getPath();
        if (isWindows() && workspace.charAt(0) == SLASH) {
            workspace = workspace.substring(1);
        }
        return new PathUtil().getAbsolutePath(workspace);
    }

    /**
     * Returns whether the OS under test is Windows or Unix.
     *
     * @return {@code true} if the OS is Windows, {@code false} otherwise
     */
    private static boolean isWindows() {
        return File.pathSeparatorChar == ';';
    }

    private Report createIssuesSingleton(final String fileName, final IssueBuilder issueBuilder) {
        Report report = new Report();
        Issue issue = issueBuilder.setFileName(fileName).build();
        report.add(issue);
        return report;
    }

    private String normalize(final String fileName) {
        return fileName.replace("/", File.separator);
    }

    private static URI getResourceFolder() {
        try {
            URL resource = AbsolutePathGeneratorTest.class.getResource(RELATIVE_FILE);
            String fileName = resource.toExternalForm();
            return new URL(fileName.replace(RELATIVE_FILE, "")).toURI();
        }
        catch (MalformedURLException | URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
