package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Severity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.IssuesModel.IssuesRow;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.Gcc4;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.forensics.reference.SimpleReferenceRecorder;
import io.jenkins.plugins.prism.PermittedSourceCodeDirectory;
import io.jenkins.plugins.prism.PrismConfiguration;
import io.jenkins.plugins.prism.SourceCodeDirectory;
import io.jenkins.plugins.prism.SourceCodeRetention;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the class {@link AffectedFilesResolver}.
 *
 * @author Deniz Mardin
 * @author Frank Christian Geyer
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
class AffectedFilesResolverITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String FOLDER = "affected-files";
    private static final String SOURCE_AFFECTED_FILE = FOLDER + "/Main.java";
    private static final String ECLIPSE_REPORT = FOLDER + "/eclipseOneAffectedAndThreeNotExistingFiles.txt";
    private static final String ECLIPSE_REPORT_ONE_AFFECTED_AFFECTED_FILE = FOLDER + "/eclipseOneAffectedFile.txt";
    private static final int ROW_NUMBER_ACTUAL_AFFECTED_FILE = 0;
    private static final String COPY_FILES = "Copying affected files to Jenkins' build folder";
    private static final String INITIAL_JAVA_REPORT = FOLDER + "/javalog-1.txt";
    private static final String MODIFIED_JAVA_REPORT = FOLDER + "/javalog-2.txt";
    private static final String INITIAL_JAVA_FINGERPRINT_SOURCE_FILE =  FOLDER + "/FingerprintTestWithoutModification.java";
    private static final String MODIFIED_JAVA_FINGERPRINT_SOURCE_FILE = FOLDER + "/FingerprintTestWithModification.java";

    /**
     * Verifies that the affected source code is copied and shown in the source code view. If the file is deleted in the
     * build folder, then the link to open the file disappears.
     */
    @Test
    void shouldShowNoLinkIfSourceCodeHasBeenDeleted() {
        var project = createEclipseProject();
        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        var row = getIssuesModel(result, ROW_NUMBER_ACTUAL_AFFECTED_FILE);
        assertThat(row.getFileName().getDisplay()).contains("<a href=").contains("Main.java:3");

        deleteAffectedFilesInBuildFolder(result);

        row = getIssuesModel(result, ROW_NUMBER_ACTUAL_AFFECTED_FILE);
        assertThat(row.getFileName().getDisplay()).isEqualTo("Main.java:3");
    }

    private FreeStyleProject createEclipseProject() {
        var project = getJobWithWorkspaceFiles();
        enableEclipseWarnings(project);
        return project;
    }

    private IssuesRecorder enableEclipseWarnings(final FreeStyleProject project) {
        return enableWarnings(project, createTool(new Eclipse(), "**/*.txt"));
    }

    private FreeStyleProject getJobWithWorkspaceFiles() {
        var job = createFreeStyleProject();
        copyMultipleFilesToWorkspace(job, ECLIPSE_REPORT, SOURCE_AFFECTED_FILE);
        return job;
    }

    /**
     * Verifies that the affected source code is copied and shown in the source code view. If the file is made
     * unreadable in the build folder, then the link to open the file disappears.
     */
    @Test
    void shouldShowNoLinkIfSourceCodeHasBeenMadeUnreadable() {
        var project = createEclipseProject();
        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        var row = getIssuesModel(result, ROW_NUMBER_ACTUAL_AFFECTED_FILE);
        assertThat(row.getFileName().getDisplay()).contains("<a href=").contains("Main.java:3");

        makeAffectedFilesInBuildFolderUnreadable(result);

        row = getIssuesModel(result, ROW_NUMBER_ACTUAL_AFFECTED_FILE);
        assertThat(row.getFileName().getDisplay()).isEqualTo("Main.java:3");
    }

    private void makeAffectedFilesInBuildFolderUnreadable(final AnalysisResult result) {
        makeFileUnreadable(AffectedFilesResolver.getZipFile(result.getOwner(),
                getIssueWithSource(result).getFileName()));
    }

    private Issue getIssueWithSource(final AnalysisResult result) {
        return result.getIssues()
                .stream()
                .filter(issue -> issue.getFileName().endsWith("Main.java"))
                .findFirst().orElseThrow(NoSuchElementException::new);
    }

    private void deleteAffectedFilesInBuildFolder(final AnalysisResult result) {
        Set<String> files = result.getIssues().getFiles();
        for (String fileName : files) {
            Path file = AffectedFilesResolver.getZipFile(result.getOwner(), fileName);
            try {
                Files.delete(file);
            }
            catch (IOException ignore) {
                // ignore
            }
        }
    }

    /**
     * Verifies that all copied affected files are found by the {@link AffectedFilesResolver#getFile(Run, String)}.
     */
    @Test
    void shouldRetrieveAffectedFilesInBuildFolder() {
        var project = createEclipseProject();
        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        var issues = result.getIssues();
        issues.forEach(issue -> assertThatFileExistsInBuildFolder(issue, project, result.getOwner()));
    }

    private void assertThatFileExistsInBuildFolder(final Issue issue, final FreeStyleProject project,
            final Run<?, ?> owner) {
        Path buildFolderCopy = AffectedFilesResolver.getFile(owner, issue.getFileName());
        if (issue.getFileName().contains(SOURCE_AFFECTED_FILE)) {
            assertThat(buildFolderCopy).exists();
            assertThat(buildFolderCopy).hasSameTextualContentAs(getSourceInWorkspace(project));
        }
    }

    private Path getSourceInWorkspace(final FreeStyleProject project) {
        return Path.of(getSourceAbsolutePath(project));
    }

    private String getSourceAbsolutePath(final FreeStyleProject project) {
        return getWorkspace(project) + "/" + getBaseName();
    }

    private String getBaseName() {
        return Path.of(SOURCE_AFFECTED_FILE).getFileName().toString();
    }

    /**
     * Verifies that the AffectedFilesResolver produces an I/O error, when the affected files could not be copied.
     */
    @Test
    void shouldGetIoErrorBySearchingForAffectedFiles() {
        var project = createEclipseProject();

        makeFileUnreadable(getSourceAbsolutePath(project));

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        var consoleLog = getConsoleLog(result);
        assertThat(consoleLog).contains("0 copied");
        assertThat(consoleLog).contains("3 not-found", "1 with I/O error");
    }

    /**
     * Verifies that the {@link AffectedFilesResolver} finds only one file in a report with 4 files.
     */
    @Test
    void shouldFindAffectedFilesWhereasThreeFilesAreNotFound() {
        var result = buildEclipseProject(ECLIPSE_REPORT, SOURCE_AFFECTED_FILE);

        assertThat(getConsoleLog(result)).contains("1 copied", "3 not-found", "0 with I/O error");
    }

    /**
     * Verifies that a source code file cannot be shown if the file is not in the workspace.
     */
    @Test
    void shouldShowNoFilesOutsideWorkspace() {
        var job = createFreeStyleProject();
        prepareGccLog(job);
        enableWarnings(job, createTool(new Gcc4(), "**/gcc.log"));

        buildAndVerifyFilesResolving(job, ColumnLink.SHOULD_NOT_HAVE_LINK, "0 copied", "1 not in workspace", "0 not-found", "0 with I/O error");
    }

    /**
     * Verifies that a source code file will be copied from outside the workspace if configured correspondingly.
     */
    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-55998")
    void shouldShowFileOutsideWorkspaceIfConfigured() {
        var job = createFreeStyleProject();
        prepareGccLog(job);

        var recorder = enableWarnings(job, createTool(new Gcc4(), "**/gcc.log"));
        var buildsFolder = job.getRootDir().getAbsolutePath();
        recorder.setSourceDirectories(Arrays.asList(new SourceCodeDirectory(buildsFolder), new SourceCodeDirectory("relative")));

        // First build: copying the affected file is forbidden
        buildAndVerifyFilesResolving(job, ColumnLink.SHOULD_NOT_HAVE_LINK, "0 copied", "1 not in workspace", "0 not-found", "0 with I/O error");

        // Use source directories of old Warnings plugin configuration
        PrismConfiguration.getInstance().setSourceDirectories(
                Collections.singletonList(new PermittedSourceCodeDirectory(buildsFolder)));

        // Second build: copying the affected file is permitted
        buildAndVerifyFilesResolving(job, ColumnLink.SHOULD_HAVE_LINK, "1 copied", "0 not in workspace", "0 not-found", "0 with I/O error");

        PrismConfiguration.getInstance().setSourceDirectories(new ArrayList<>());

        // Third build: copying the affected file is forbidden again
        buildAndVerifyFilesResolving(job, ColumnLink.SHOULD_NOT_HAVE_LINK, "0 copied", "1 not in workspace", "0 not-found", "0 with I/O error");

        PrismConfiguration.getInstance().setSourceDirectories(
                Collections.singletonList(new PermittedSourceCodeDirectory(buildsFolder)));

        // Fourth build: copying the affected file is permitted again
        buildAndVerifyFilesResolving(job, ColumnLink.SHOULD_HAVE_LINK, "1 copied", "0 not in workspace", "0 not-found", "0 with I/O error");
    }

    @Test
    void shouldDeleteSourceCodeFilesOfPreviousBuilds() {
        var job = createFreeStyleProject();
        prepareGccLog(job);

        var recorder = enableWarnings(job, createTool(new Gcc4(), "**/gcc.log"));
        recorder.setSourceCodeRetention(SourceCodeRetention.LAST_BUILD);

        var buildsFolder = job.getRootDir().getAbsolutePath();

        PrismConfiguration.getInstance().setSourceDirectories(
                Collections.singletonList(new PermittedSourceCodeDirectory(buildsFolder)));

        Run<?, ?> first = buildAndVerifyFilesResolving(job, ColumnLink.SHOULD_HAVE_LINK,
                "1 copied", "0 not in workspace", "0 not-found", "0 with I/O error");
        Run<?, ?> second = buildAndVerifyFilesResolving(job, ColumnLink.SHOULD_HAVE_LINK,
                "1 copied", "0 not in workspace", "0 not-found", "0 with I/O error");

        verifyResolving(ColumnLink.SHOULD_NOT_HAVE_LINK, getAnalysisResult(first));
        assertThat(getConsoleLog(second)).contains("Deleting source code files of build #1");
    }

    private Run<?, ?> buildAndVerifyFilesResolving(final FreeStyleProject job, final ColumnLink columnLink,
            final String... expectedResolveMessages) {
        var result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        assertThat(getConsoleLog(result)).contains(expectedResolveMessages);

        verifyResolving(columnLink, result);

        return result.getOwner();
    }

    private void verifyResolving(final ColumnLink columnLink, final AnalysisResult result) {
        assertThat(result.getIssues()).hasSize(1);

        var firstRow = getIssuesModel(result, 0);
        assertThat(firstRow.getSeverity()).contains(Severity.WARNING_NORMAL.getName());
        if (columnLink == ColumnLink.SHOULD_HAVE_LINK) {
            assertThat(firstRow.getFileName().getDisplay()).startsWith("<a href=\"").contains("config.xml:451");
        }
        else {
            assertThat(firstRow.getFileName().getDisplay()).isEqualTo("config.xml:451");
        }

        var issue = result.getIssues().get(0);
        assertThat(issue.getBaseName()).isEqualTo("config.xml");
        assertThat(issue.getLineStart()).isEqualTo(451);
        assertThat(issue.getMessage()).isEqualTo("foo defined but not used");
        assertThat(issue.getSeverity()).isEqualTo(Severity.WARNING_NORMAL);
    }

    private IssuesRow getIssuesModel(final AnalysisResult result, final int rowNumber) {
        var issuesDetail = result.getOwner().getAction(ResultAction.class).getTarget();
        return (IssuesRow) issuesDetail.getTableModel("issues").getRows().get(rowNumber);
    }

    private void prepareGccLog(final FreeStyleProject job) {
        try {
            var workspace = getWorkspace(job);
            workspace.mkdirs();
            var logMessage = "%s/config.xml:451: warning: foo defined but not used%n".formatted(
                    job.getRootDir());
            Files.write(Path.of(workspace.child("gcc.log").getRemote()), logMessage.getBytes());
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void shouldFindOneAffectedFile() {
        var result = buildEclipseProject(ECLIPSE_REPORT_ONE_AFFECTED_AFFECTED_FILE, SOURCE_AFFECTED_FILE);

        assertThat(getConsoleLog(result))
                .contains(COPY_FILES, "1 copied", "0 not-found", "0 with I/O error");
    }

    @Test
    void shouldSkipStoringOfAffectedFiles() {
        var project = createFreeStyleProject();
        copyMultipleFilesToWorkspace(project, ECLIPSE_REPORT_ONE_AFFECTED_AFFECTED_FILE, SOURCE_AFFECTED_FILE);
        var recorder = enableEclipseWarnings(project);
        recorder.setSourceCodeRetention(SourceCodeRetention.NEVER);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(getConsoleLog(result))
                .doesNotContain(COPY_FILES, " copied", " not-found", " with I/O error");
    }

    @Test
    void shouldProduceDifferentFingerprints() {
        var project = createFreeStyleProject();

        var firstBuildResult = configureBuildForFingerprintTests(project, INITIAL_JAVA_REPORT,
                INITIAL_JAVA_FINGERPRINT_SOURCE_FILE, -1, false);

        var secondBuildResult = configureBuildForFingerprintTests(project, MODIFIED_JAVA_REPORT,
                MODIFIED_JAVA_FINGERPRINT_SOURCE_FILE, -1, true);

        assertThat(secondBuildResult.getNewIssues().size()).isEqualTo(1);
        assertThat(firstBuildResult.getIssues().get(0).getFingerprint())
                .isNotEqualTo(secondBuildResult.getIssues().get(0).getFingerprint());
    }

    @Test
    void shouldProduceSameFingerprints() {
        var project = createFreeStyleProject();

        var firstBuildResult = configureBuildForFingerprintTests(project, INITIAL_JAVA_REPORT,
                INITIAL_JAVA_FINGERPRINT_SOURCE_FILE, 2, false);

        var secondBuildResult = configureBuildForFingerprintTests(project, MODIFIED_JAVA_REPORT,
                MODIFIED_JAVA_FINGERPRINT_SOURCE_FILE, 2, true);

        assertThat(secondBuildResult.getNewIssues().size()).isEqualTo(0);
        assertThat(firstBuildResult.getIssues().get(0).getFingerprint())
                .isEqualTo(secondBuildResult.getIssues().get(0).getFingerprint());
    }

    private AnalysisResult configureBuildForFingerprintTests(final FreeStyleProject project, final String logFile,
                                                             final String srcFile, final int linesLookAhead, final boolean warningEnabled) {
        copyFileToWorkspace(project, logFile, "log-java.txt");
        copyFileToWorkspace(project, srcFile, "FingerprintITest.java");

        var tool = createTool(new Java(), "**/*.txt");

        if (linesLookAhead != -1) {
            tool.setLinesLookAhead(linesLookAhead);
        }

        // else linesLookAhead defaults to 3

        if (!warningEnabled) {
            project.getPublishersList().add(new SimpleReferenceRecorder());
            enableWarnings(project, tool);
        }

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }

    private AnalysisResult buildEclipseProject(final String... files) {
        var project = createFreeStyleProject();
        copyMultipleFilesToWorkspace(project, files);
        enableEclipseWarnings(project);

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }

    private enum ColumnLink {
        SHOULD_HAVE_LINK, SHOULD_NOT_HAVE_LINK
    }
}
