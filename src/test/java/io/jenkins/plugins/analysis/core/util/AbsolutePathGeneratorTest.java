package io.jenkins.plugins.analysis.core.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.jenkins.plugins.analysis.core.util.AbsolutePathGenerator.FileSystem;

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import static org.mockito.Mockito.*;

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

    /**
     * Ensures that illegal file names are processed without problems. Afterwards, the path name should be unchanged.
     */
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$&/&(", "\0 Null-Byte"})
    void shouldReturnFallbackOnError(final String fileName) {
        Report report = createIssuesSingleton(fileName, new IssueBuilder());

        new AbsolutePathGenerator().run(report, WORKSPACE);

        assertThat(report.iterator()).containsExactly(report.get(0));
        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("1 unresolved");
        assertThat(report.getErrorMessages()).hasSize(2).contains("- " + fileName);

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

        IssueBuilder builder = new IssueBuilder();

        Report report = createIssuesSingleton(fileName, builder.setOrigin(ID));

        AbsolutePathGenerator generator = new AbsolutePathGenerator(fileSystem);
        generator.run(report, WORKSPACE);

        assertThat(report.iterator()).containsExactly(builder.setFileName(absolutePath).build());
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
        Report report = new Report();
        URI resourceFolder = getResourceFolder();
        String workspace = resourceFolder.getPath();

        IssueBuilder builder1 = new IssueBuilder();

        report.add(builder1.setFileName("").build());
        report.add(builder1.setFileName("relative.txt").build());
        report.add(builder1.setDirectory(workspace).setFileName("relative.txt").build());
        report.add(builder1.setDirectory(workspace).setFileName("../../core/util/relative.txt").build());

        AbsolutePathGenerator generator = new AbsolutePathGenerator(new FileSystem());
        generator.run(report, Paths.get(resourceFolder));

        assertThat(report.get(0))
                .as("Issue with no file name").hasFileName("-");
        assertThat(report.get(1))
                .as("Issue with relative file name").hasFileName(workspace + RELATIVE_FILE);
        assertThat(report.get(2))
                .as("Issue with absolute file name (normalized)").hasFileName(workspace + RELATIVE_FILE);
        assertThat(report.get(3))
                .as("Issue with absolute file name (not normalized)").hasFileName(workspace + RELATIVE_FILE);

        assertThat(report.getInfoMessages()).hasSize(1);
        assertThat(report.getInfoMessages().get(0)).contains("2 resolved");
        assertThat(report.getInfoMessages().get(0)).contains("1 already resolved");
        assertThat(report.getErrorMessages()).isEmpty();
    }

    /**
     * Ensures that existing absolute paths are resolved.
     */
    @Test
    void shouldResolveAbsolutePath() {
        verifyNormalizedPath("../util/relative.txt");
        verifyNormalizedPath("relative.txt");
        verifyNormalizedPath("../../core/util/relative.txt");
    }

    private void verifyNormalizedPath(final String fileName) {
        Report report = new Report();
        URI resourceFolder = getResourceFolder();
        String workspace = resourceFolder.getPath();

        Issue issue = new IssueBuilder().setDirectory(workspace).setFileName(fileName).build();
        report.add(issue);

        AbsolutePathGenerator generator = new AbsolutePathGenerator(new FileSystem());
        generator.run(report, Paths.get(resourceFolder));

        assertThat(report.get(0).getFileName()).as("Resolving file '%s'", fileName).isEqualTo(workspace + RELATIVE_FILE);
        assertThat(report.getErrorMessages()).isEmpty();
    }

    @Test
    void shouldResolvePath() {
        verifyResolvedPath("relative.txt");
        verifyResolvedPath("../util/relative.txt");
        verifyResolvedPath("../../core/util/relative.txt");
    }

    private void verifyResolvedPath(final String fileName) {
        FileSystem fileSystem = new FileSystem();
        Optional<String> absolute = fileSystem.resolveAbsolutePath(Paths.get(getResourceFolder()), fileName);
        assertThat(absolute).isPresent();
        assertThat(absolute.get()).endsWith(PATH_TO_RESOURCE);
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