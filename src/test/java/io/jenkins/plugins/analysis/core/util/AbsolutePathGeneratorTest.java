package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import static io.jenkins.plugins.analysis.core.util.ConsoleLogHandler.*;

/**
 * Tests the class {@link AbsolutePathGenerator}.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings("DMI")
class AbsolutePathGeneratorTest {
    private static final String WORKSPACE_PATH = "path";
    private static final Path WORKSPACE = Paths.get(WORKSPACE_PATH);
    private static final String ID = "ID";
    private static final String RELATIVE_FILE = "relative.txt";
    private static final String PATH_TO_RESOURCE = "io/jenkins/plugins/analysis/core/util/relative.txt";
    private static final char SLASH = '/';

    /**
     * Ensures that illegal file names are processed without problems. Afterwards, the path name should be unchanged.
     */
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$&/&(", "\0 Null-Byte"})
    @DisplayName("Should not change path on errors")
    void shouldReturnFallbackOnError(final String fileName) {
        Report report = createIssuesSingleton(fileName, new IssueBuilder());

        new AbsolutePathGenerator().run(report, WORKSPACE);

        assertThat(report.iterator()).toIterable().containsExactly(report.get(0));
        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("1 unresolved");
        assertThat(report.getErrorMessages()).hasSize(2).contains("- " + fileName);

    }

    @Test
    @DisplayName("Should skip processing if there are no issues")
    void shouldDoNothingIfNoIssuesPresent() {
        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        Report report = new Report();
        generator.run(report, WORKSPACE);
        assertThat(report).hasSize(0);
        assertThat(report.getInfoMessages()).containsExactly(AbsolutePathGenerator.NOTHING_TO_DO);
    }

    /**
     * Returns whether the OS under test is Windows or Unix.
     *
     * @return {@code true} if the OS is Windows, {@code false} otherwise
     */
    private boolean isWindows() {
        return File.pathSeparatorChar == ';';
    }

    @Test
    @DisplayName("Should skip existing absolute paths")
    void shouldNotTouchAbsolutePathOrEmptyPath() {
        Report report = new Report();
        URI resourceFolder = getResourceFolder();
        String workspace = getUriPath(resourceFolder);

        IssueBuilder builder = new IssueBuilder();

        report.add(builder.setFileName("").build());
        report.add(builder.setFileName(JENKINS_CONSOLE_LOG_FILE_NAME_ID).build());
        report.add(builder.setFileName("relative.txt").build());
        report.add(builder.setDirectory(workspace).setFileName("relative.txt").build());
        report.add(builder.setDirectory(workspace).setFileName(normalize("../../core/util/normalized.txt")).build());

        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        generator.run(report, Paths.get(resourceFolder));

        assertThat(report).hasSize(5);
        assertThat(report.get(0))
                .as("Issue with no file name").hasFileName("-");
        assertThat(report.get(1))
                .as("Issue in console log").hasFileName(JENKINS_CONSOLE_LOG_FILE_NAME_ID);
        assertThat(report.get(2))
                .as("Issue with relative file name").hasFileName(workspace + RELATIVE_FILE);
        assertThat(report.get(3))
                .as("Issue with absolute file name (normalized)").hasFileName(workspace + RELATIVE_FILE);
        assertThat(report.get(4))
                .as("Issue with absolute file name (not normalized)").hasFileName(workspace + "normalized.txt");

        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("1 resolved");
        assertThat(report.getInfoMessages().get(0)).contains("2 already resolved");
        assertThat(report.getErrorMessages()).isEmpty();
    }

    @ParameterizedTest(name = "[{index}] Relative file name = {0}")
    @ValueSource(strings = {"../util/relative.txt", "relative.txt", "../../core/util/relative.txt"})
    @DisplayName("Should replace relative issue path with absolute path in stubbed FileSystem facade")
    void shouldResolveRelativePath(final String fileName) {
        URI resourceFolder = getResourceFolder();
        String workspace = getUriPath(resourceFolder);

        IssueBuilder builder = new IssueBuilder();

        Report report = createIssuesSingleton(fileName, builder.setOrigin(ID));

        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        generator.run(report, Paths.get(resourceFolder));

        assertThat(report.get(0).getFileName())
                .as("Resolving file '%s'", normalize(fileName))
                .isEqualTo(workspace + RELATIVE_FILE);
        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("1 resolved");
    }

    @ParameterizedTest(name = "[{index}] Relative filename = {0}")
    @ValueSource(strings = {"../util/relative.txt", "relative.txt", "../../core/util/relative.txt"})
    @DisplayName("Should leave existing absolute path unchanged with real FileSystem in resource folder")
    void shouldResolveAbsolutePath(final String fileName) {
        Report report = new Report();
        URI resourceFolder = getResourceFolder();
        String workspace = getUriPath(resourceFolder);

        Issue issue = new IssueBuilder().setDirectory(workspace).setFileName(normalize(fileName)).build();
        report.add(issue);

        AbsolutePathGenerator generator = new AbsolutePathGenerator();
        generator.run(report, Paths.get(resourceFolder));

        assertThat(report.get(0).getFileName())
                .as("Resolving file '%s'", normalize(fileName))
                .isEqualTo(workspace + RELATIVE_FILE);
        assertThat(report.getErrorMessages()).isEmpty();
        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("1 already resolved");
    }

    private String getUriPath(final URI resourceFolder) {
        String workspace = resourceFolder.getPath();
        if (isWindows() && workspace.charAt(0) == SLASH) {
            workspace = workspace.substring(1);
        }
        return workspace;
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

    private URI getResourceFolder() {
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
