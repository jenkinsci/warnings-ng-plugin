package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import hudson.FilePath;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.SourceDirectory;
import io.jenkins.plugins.analysis.core.model.WarningsPluginConfiguration;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.Gcc4;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.TabType;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceCodeView;
import io.jenkins.plugins.datatables.TablePageObject;
import io.jenkins.plugins.datatables.TableRowPageObject;

import static io.jenkins.plugins.analysis.warnings.recorder.MiscIssuesRecorderITest.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the class {@link AffectedFilesResolver}.
 *
 * @author Deniz Mardin
 * @author Frank Christian Geyer
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class AffectedFilesResolverITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String FOLDER = "affected-files";
    private static final String SOURCE_AFFECTED_FILE = FOLDER + "/Main.java";
    private static final String ECLIPSE_REPORT = FOLDER + "/eclipseOneAffectedAndThreeNotExistingFiles.txt";
    private static final String ECLIPSE_REPORT_ONE_AFFECTED_AFFECTED_FILE = FOLDER + "/eclipseOneAffectedFile.txt";
    private static final int ROW_NUMBER_ACTUAL_AFFECTED_FILE = 1;

    /**
     * Verifies that the affected source code is copied and shown in the source code view. If the file is deleted in the
     * build folder, then the link to open the file disappears.
     */
    @Test
    public void shouldShowNoLinkIfSourceCodeHasBeenDeleted() {
        FreeStyleProject project = createEclipseProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        TableRowPageObject row = getIssuesTableRow(result, ROW_NUMBER_ACTUAL_AFFECTED_FILE);
        SourceCodeView sourceCodeView = new SourceCodeView(getSourceCodePage(result));

        assertThat(row.hasLink(AFFECTED_FILE)).isTrue();
        assertThat(sourceCodeView.getSourceCode()).isEqualToIgnoringWhitespace(readSourceCode(project));

        deleteAffectedFilesInBuildFolder(result);

        row = getIssuesTableRow(result, ROW_NUMBER_ACTUAL_AFFECTED_FILE);
        assertThat(row.hasLink(AFFECTED_FILE)).isFalse();
    }

    /**
     * Verifies that the affected source code is copied and shown in the source code view. If the file is made
     * unreadable in the build folder, then the link to open the file disappears.
     */
    @Test
    public void shouldShowNoLinkIfSourceCodeHasBeenMadeUnreadable() {
        FreeStyleProject project = createEclipseProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        TableRowPageObject row = getIssuesTableRow(result, ROW_NUMBER_ACTUAL_AFFECTED_FILE);
        assertThat(row.hasLink(AFFECTED_FILE)).isTrue();

        makeAffectedFilesInBuildFolderUnreadable(result);

        row = getIssuesTableRow(result, ROW_NUMBER_ACTUAL_AFFECTED_FILE);
        assertThat(row.hasLink(AFFECTED_FILE)).isFalse();
    }

    private void makeAffectedFilesInBuildFolderUnreadable(final AnalysisResult result) {
        makeFileUnreadable(AffectedFilesResolver.getFile(result.getOwner(), getIssueWithSource(result).getFileName()));
    }

    private void deleteAffectedFilesInBuildFolder(final AnalysisResult result) {
        Set<String> files = result.getIssues().getFiles();
        for (String fileName : files) {
            Path file = AffectedFilesResolver.getFile(result.getOwner(), fileName);
            try {
                Files.delete(file);
            }
            catch (IOException ignore) {
                // ignore
            }
        }
    }

    private TableRowPageObject getIssuesTableRow(final AnalysisResult result, final int rowNumber) {
        return getIssuesTable(result).getRow(rowNumber);
    }

    private String readSourceCode(final FreeStyleProject project) {
        return toString(getSourceInWorkspace(project));
    }

    // TODO: Navigate to source code from details page 
    private HtmlPage getSourceCodePage(final AnalysisResult result) {
        return getWebPage(JavaScriptSupport.JS_DISABLED, result,
                new FileNameRenderer(result.getOwner()).getSourceCodeUrl(getIssueWithSource(result))
        );
    }

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

        TableRowPageObject row = buildAndVerifyFilesResolving(job,
                "0 copied", "1 not in workspace", "0 not-found", "0 with I/O error");

        assertThat(row.hasLink(AFFECTED_FILE)).isFalse();
    }

    /**
     * Verifies that a source code file will be copied from outside the workspace if configured correspondingly.
     */
    @Test @org.jvnet.hudson.test.Issue("JENKINS-55998")
    public void shouldShowFileOutsideWorkspaceIfConfigured() {
        FreeStyleProject job = createFreeStyleProject();
        prepareGccLog(job);

        IssuesRecorder recorder = enableWarnings(job, createTool(new Gcc4(), "**/gcc.log"));
        String buildsFolder = job.getRootDir().getAbsolutePath();
        recorder.setSourceDirectory(buildsFolder);

        // First build: copying the affected file is forbidden
        TableRowPageObject unresolvedRow = buildAndVerifyFilesResolving(job,
                "0 copied", "1 not in workspace", "0 not-found", "0 with I/O error");

        AnalysisResult result = getAnalysisResult(job.getLastCompletedBuild());
        assertThat(result.getErrorMessages()).contains(
                String.format("Additional source directory '%s' must be registered in Jenkins system configuration",
                        buildsFolder));

        assertThat(unresolvedRow.hasLink(AFFECTED_FILE)).isFalse();

        WarningsPluginConfiguration.getInstance().setSourceDirectories(
                Collections.singletonList(new SourceDirectory(buildsFolder)));

        // Second build: copying the affected file is permitted
        TableRowPageObject resolvedRow = buildAndVerifyFilesResolving(job,
                "1 copied", "0 not in workspace", "0 not-found", "0 with I/O error");

        assertThat(resolvedRow.hasLink(AFFECTED_FILE)).isTrue();

        SourceCodeView sourceCodeView = openSourceCode(resolvedRow);
        assertThat(sourceCodeView.getSourceCode()).contains("<io.jenkins.plugins.analysis.core.steps.IssuesRecorder>");
    }

    private SourceCodeView openSourceCode(final TableRowPageObject resolvedRow) {
        return new SourceCodeView(resolvedRow.clickColumnLink(AFFECTED_FILE));
    }

    private TableRowPageObject buildAndVerifyFilesResolving(final FreeStyleProject job, final String... resolveMessages) {
        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        assertThat(getConsoleLog(result)).contains(resolveMessages);

        TablePageObject issues = getIssuesTable(result);
        assertThat(issues.getColumnHeaders()).containsExactly(
                DETAILS, AFFECTED_FILE, SEVERITY, AGE);
        List<TableRowPageObject> rows = issues.getRows();
        assertThat(rows).hasSize(1);
        TableRowPageObject row = rows.get(0);
        assertThat(row.getValuesByColumnLabel()).contains(
                entry(DETAILS, "foo defined but not used"),
                entry(AFFECTED_FILE, "config.xml:451"),
                entry(SEVERITY, "Normal"));
        return row;
    }

    private TablePageObject getIssuesTable(final AnalysisResult result) {
        HtmlPage details = getWebPage(JavaScriptSupport.JS_ENABLED, result);
        DetailsTab detailsTab = new DetailsTab(details);
        return detailsTab.select(TabType.ISSUES);
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

    private Issue getIssueWithSource(final AnalysisResult result) {
        return result.getIssues()
                .stream()
                .filter(issue -> issue.getFileName().endsWith("Main.java"))
                .findFirst().orElseThrow(NoSuchElementException::new);
    }

    private AnalysisResult buildEclipseProject(final String... files) {
        FreeStyleProject project = createFreeStyleProject();
        copyMultipleFilesToWorkspace(project, files);
        enableEclipseWarnings(project);

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }
}
