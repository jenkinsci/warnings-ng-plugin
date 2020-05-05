package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import hudson.FilePath;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.SourceDirectory;
import io.jenkins.plugins.analysis.core.model.WarningsPluginConfiguration;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.Gcc4;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the class {@link AffectedFilesResolver}.
 *
 * @author Deniz Mardin
 * @author Frank Christian Geyer
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class AffectedFilesResolverITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String FOLDER = "affected-files";
    private static final String SOURCE_AFFECTED_FILE = FOLDER + "/Main.java";
    private static final String ECLIPSE_REPORT = FOLDER + "/eclipseOneAffectedAndThreeNotExistingFiles.txt";
    private static final String ECLIPSE_REPORT_ONE_AFFECTED_AFFECTED_FILE = FOLDER + "/eclipseOneAffectedFile.txt";

    private FreeStyleProject createEclipseProject() {
        FreeStyleProject project = getJobWithWorkspaceFiles();
        enableEclipseWarnings(project);
        return project;
    }

    private void enableEclipseWarnings(final FreeStyleProject project) {
        enableWarnings(project, createTool(new Eclipse(), "**/*.txt"));
    }

    private FreeStyleProject getJobWithWorkspaceFiles() {
        FreeStyleProject job = createFreeStyleProject();
        copyMultipleFilesToWorkspace(job, ECLIPSE_REPORT, SOURCE_AFFECTED_FILE);
        return job;
    }

    /**
     * Verifies that all copied affected files are found by the {@link AffectedFilesResolver#getFile(Run, String)}.
     */
    @Test
    public void shouldRetrieveAffectedFilesInBuildFolder() {
        FreeStyleProject project = createEclipseProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        Report issues = result.getIssues();
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
        return Paths.get(getSourceAbsolutePath(project));
    }

    private String getSourceAbsolutePath(final FreeStyleProject project) {
        return getWorkspace(project) + "/" + getBaseName();
    }

    private String getBaseName() {
        return Paths.get(SOURCE_AFFECTED_FILE).getFileName().toString();
    }

    /**
     * Verifies that the AffectedFilesResolver produces an I/O error, when the affected files could not be copied.
     */
    @Test
    public void shouldGetIoErrorBySearchingForAffectedFiles() {
        FreeStyleProject project = createEclipseProject();

        makeFileUnreadable(getSourceAbsolutePath(project));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        String consoleLog = getConsoleLog(result);
        assertThat(consoleLog).contains("0 copied");
        if (isWindows()) { // In Windows a file does not exist if it is unreadable
            assertThat(consoleLog).contains("4 not-found", "0 with I/O error");
        }
        else {
            assertThat(consoleLog).contains("3 not-found", "1 with I/O error");
        }
    }

    /**
     * Verifies that the {@link AffectedFilesResolver} finds only one file in a report with 4 files.
     */
    @Test
    public void shouldFindAffectedFilesWhereasThreeFilesAreNotFound() {
        AnalysisResult result = buildEclipseProject(ECLIPSE_REPORT, SOURCE_AFFECTED_FILE);

        assertThat(getConsoleLog(result)).contains("1 copied", "3 not-found", "0 with I/O error");
    }

    /**
     * Verifies that a source code file cannot be shown if the file is not in the workspace.
     */
    @Test
    public void shouldShowNoFilesOutsideWorkspace() {
        FreeStyleProject job = createFreeStyleProject();
        prepareGccLog(job);
        enableWarnings(job, createTool(new Gcc4(), "**/gcc.log"));

        buildAndVerifyFilesResolving(job, "0 copied", "1 not in workspace", "0 not-found", "0 with I/O error");
    }

    /**
     * Verifies that a source code file will be copied from outside the workspace if configured correspondingly.
     */
    @Test
    @org.jvnet.hudson.test.Issue("JENKINS-55998")
    public void shouldShowFileOutsideWorkspaceIfConfigured() {
        FreeStyleProject job = createFreeStyleProject();
        prepareGccLog(job);

        IssuesRecorder recorder = enableWarnings(job, createTool(new Gcc4(), "**/gcc.log"));
        String buildsFolder = job.getRootDir().getAbsolutePath();
        recorder.setSourceDirectory(buildsFolder);

        // First build: copying the affected file is forbidden
        buildAndVerifyFilesResolving(job,"0 copied", "1 not in workspace", "0 not-found", "0 with I/O error");

        AnalysisResult result = getAnalysisResult(job.getLastCompletedBuild());
        assertThat(result.getErrorMessages()).contains(
                String.format("Additional source directory '%s' must be registered in Jenkins system configuration",
                        buildsFolder));


        WarningsPluginConfiguration.getInstance().setSourceDirectories(
                Collections.singletonList(new SourceDirectory(buildsFolder)));

        // Second build: copying the affected file is permitted
        buildAndVerifyFilesResolving(job,"1 copied", "0 not in workspace", "0 not-found", "0 with I/O error");
    }

    private void buildAndVerifyFilesResolving(final FreeStyleProject job,
            final String... resolveMessages) {
        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        assertThat(getConsoleLog(result)).contains(resolveMessages);

        assertThat(result.getIssues()).hasSize(1);

        Issue issue = result.getIssues().get(0);
        String filename = issue.getFileName().substring(issue.getFileName().lastIndexOf("/") + 1);
        assertThat(filename).isEqualTo("config.xml");
        assertThat(issue.getLineStart()).isEqualTo(451);
        assertThat(issue.getMessage()).isEqualTo("foo defined but not used");
        assertThat(issue.getSeverity()).isEqualTo(Severity.WARNING_NORMAL);
    }

    private void prepareGccLog(final FreeStyleProject job) {
        try {
            FilePath workspace = getWorkspace(job);
            workspace.mkdirs();
            String logMessage = String.format("%s/config.xml:451: warning: foo defined but not used%n",
                    job.getRootDir());
            Files.write(Paths.get(workspace.child("gcc.log").getRemote()), logMessage.getBytes());
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Verifies that the {@link AffectedFilesResolver} can find one existing file.
     */
    @Test
    public void shouldFindOneAffectedFile() {
        AnalysisResult result = buildEclipseProject(ECLIPSE_REPORT_ONE_AFFECTED_AFFECTED_FILE, SOURCE_AFFECTED_FILE);

        assertThat(getConsoleLog(result)).contains("1 copied", "0 not-found", "0 with I/O error");
    }

    private AnalysisResult buildEclipseProject(final String... files) {
        FreeStyleProject project = createFreeStyleProject();
        copyMultipleFilesToWorkspace(project, files);
        enableEclipseWarnings(project);

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }
}
