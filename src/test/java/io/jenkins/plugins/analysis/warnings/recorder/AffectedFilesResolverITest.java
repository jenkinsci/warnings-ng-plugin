package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.File;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.warnings.Eclipse;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Integration tests for the class {@link AffectedFilesResolver}.
 *
 * @author Deniz Mardin
 * @author Frank Christian Geyer
 */
public class AffectedFilesResolverITest extends IssuesRecorderITest {
    private static final String FOLDER = "affected-files";
    private static final String SOURCE_FILE = FOLDER + "/Main.java";
    private static final String ECLIPSE_REPORT = FOLDER + "/eclipseOneAffectedAndThreeNotExistingFiles.txt";
    private static final String ECLIPSE_REPORT_ONE_AFFECTED_FILE = FOLDER + "/eclipseOneAffectedFile.txt";
    private static final String ECLIPSE_RESULTS = "eclipseResult/";

    /** Verifies that the affected source code is copied and shown in the source code view. */
    @Test
    public void shouldShowAffectedSourceCode() {
        FreeStyleProject project = createEclipseParserProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(extractSourceCodeFromHtml(getSourceCodePage(result))).contains(readSourceCode(project));
    }

    /**
     * Verifies that the workspace file is shown as fallback if the affected file copy in the build folder has been
     * deleted.
     */
    @Test
    public void shouldShowAffectedSourceCodeEvenIfDeletedInBuildFolder() {
        FreeStyleProject project = createEclipseParserProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        deleteAffectedFilesInBuildFolder(result);

        assertThat(extractSourceCodeFromHtml(getSourceCodePage(result))).contains(readSourceCode(project));
    }

    /**
     * Verifies that the workspace file is shown as fallback if the affected file copy in the build folder has been
     * deleted.
     */
    @Test
    public void shouldShowAffectedSourceCodeEvenIfMadeUnreadableInBuildFolder() {
        FreeStyleProject project = createEclipseParserProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        makeFileUnreadable(AffectedFilesResolver.getFile(result.getOwner(), getIssueWithSource(result)));

        assertThat(extractSourceCodeFromHtml(getSourceCodePage(result))).contains(readSourceCode(project));
    }

    private void deleteAffectedFilesInBuildFolder(final AnalysisResult result) {
        result.getIssues().forEach(issue -> AffectedFilesResolver.getFile(result.getOwner(), issue).delete());
    }

    private String extractSourceCodeFromHtml(final HtmlPage contentPage) {
        DomElement domElement = contentPage.getElementById("main-panel");
        DomNodeList<HtmlElement> list = domElement.getElementsByTagName("a");
        removeElementsByTagName(list);
        return replaceWhitespace(contentPage.getElementById("main-panel").asText());
    }

    private String replaceWhitespace(final String s) {
        return s.replaceAll("\\s", "");
    }

    private String readSourceCode(final FreeStyleProject project) {
        return replaceWhitespace(toString(getSourceInWorkspace(project)));
    }

    /**
     * Verifies that an error message is shown if both the workspace file and the copy in the build folder have been
     * deleted.
     */
    @Test
    public void shouldShowErrorMessageIfAffectedFileHasBeenDeletedInWorkspaceAndBuildFolder() {
        FreeStyleProject project = createEclipseParserProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        deleteAffectedFilesInBuildFolder(result);
        deleteSourcesInWorkspace(project);

        HtmlPage contentPage = getSourceCodePage(result);
        assertThatPageShowsErrorMessage(contentPage);
    }

    private void deleteSourcesInWorkspace(final FreeStyleProject project) {
        assertThat(getSourceInWorkspace(project).delete()).isTrue();
    }

    private HtmlPage getSourceCodePage(final AnalysisResult result) {
        return getWebPage(result, ECLIPSE_RESULTS + getSourceLink(result));
    }

    private FreeStyleProject createEclipseParserProject() {
        FreeStyleProject project = getJobWithWorkspaceFiles();
        enableEclipseWarnings(project);
        return project;
    }

    private void enableEclipseWarnings(final FreeStyleProject project) {
        enableWarnings(project, new ToolConfiguration(new Eclipse(), "**/*.txt"));
    }

    /**
     * Verifies that the copied class file cannot be obtained by HTML scraping because the permissions to open the file
     * are denied.
     */
    @Test
    public void shouldShowErrorMessageIfAffectedFileHasBeenMadeUnreadableInWorkspaceAndBuildFolder() {
        FreeStyleProject project = createEclipseParserProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        makeFileUnreadable(getWorkspaceFor(project).child(SOURCE_FILE).getRemote());
        makeFileUnreadable(AffectedFilesResolver.getFile(result.getOwner(), getIssueWithSource(result)));

        HtmlPage contentPage = getSourceCodePage(result);
        assertThatPageShowsErrorMessage(contentPage);
    }

    private FreeStyleProject getJobWithWorkspaceFiles() {
        FreeStyleProject job = createFreeStyleProject();
        copyMultipleFilesToWorkspace(job, ECLIPSE_REPORT, SOURCE_FILE);
        return job;
    }

    /**
     * Verifies that all copied affected files are found by the {@link AffectedFilesResolver#getFile(Run, Issue)}.
     */
    @Test
    public void shouldRetrieveAffectedFilesInBuildFolder() {
        FreeStyleProject project = createEclipseParserProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        Report issues = result.getIssues();
        issues.forEach(issue -> assertThatFileExistsInBuildFolder(issue, project, result.getOwner()));
    }

    private void assertThatFileExistsInBuildFolder(final Issue issue, final FreeStyleProject project,
            final Run<?, ?> owner) {
        File buildFolderCopy = AffectedFilesResolver.getFile(owner, issue);
        if (issue.getFileName().contains(SOURCE_FILE)) {
            assertThat(buildFolderCopy).exists();
            assertThat(buildFolderCopy).hasSameContentAs(getSourceInWorkspace(project));
        }
        else {
            assertThat(buildFolderCopy).doesNotExist(); // these files have not been copied, since they were not present
        }
    }

    private File getSourceInWorkspace(final FreeStyleProject project) {
        return new File(getSourceAbsolutePath(project));
    }

    private String getSourceAbsolutePath(final FreeStyleProject project) {
        return getWorkspaceFor(project) + "/" + SOURCE_FILE;
    }

    /**
     * Verifies that the AffectedFilesResolver produces an I/O error, when the affected files cannot copied.
     */
    @Test
    public void shouldGetIoErrorBySearchingForAffectedFiles() {
        FreeStyleProject project = createEclipseParserProject();

        makeFileUnreadable(getSourceAbsolutePath(project));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThatLogContains(result.getOwner(), "0 copied");
        assertThatLogContains(result.getOwner(), "3 not-found");
        assertThatLogContains(result.getOwner(), "1 with I/O error");
    }

    /**
     * Verifies that the {@link AffectedFilesResolver} finds only one file in a report with 4 files.
     */
    @Test
    public void shouldFindAffectedFilesWhereasThreeFilesAreNotFound() {
        AnalysisResult result = buildEclipseProject(ECLIPSE_REPORT, SOURCE_FILE);

        assertThatLogContains(result.getOwner(), "1 copied");
        assertThatLogContains(result.getOwner(), "3 not-found");
        assertThatLogContains(result.getOwner(), "0 with I/O error");
    }

    /**
     * Verifies that the {@link AffectedFilesResolver} can find one existing file.
     */
    @Test
    public void shouldFindOneAffectedFile() {
        AnalysisResult result = buildEclipseProject(ECLIPSE_REPORT_ONE_AFFECTED_FILE, SOURCE_FILE);

        assertThatLogContains(result.getOwner(), "1 copied");
        assertThatLogContains(result.getOwner(), "0 not-found");
        assertThatLogContains(result.getOwner(), "0 with I/O error");
    }

    private void assertThatPageShowsErrorMessage(final HtmlPage contentPage) {
        assertThat(contentPage.getElementById("main-panel").asText()).contains("Can't read file: ");
        assertThat(contentPage.getElementById("main-panel").asText()).contains(".tmp");
    }

    private String getSourceLink(final AnalysisResult result) {
        return String.format("/source.%s/#%s", getIssueWithSource(result).getId(), result.getBuild().getNumber());
    }

    private Issue getIssueWithSource(final AnalysisResult result) {
        return result.getIssues()
                .stream()
                .filter(issue -> issue.getFileName().endsWith(SOURCE_FILE))
                .findFirst().orElseThrow(NoSuchElementException::new);
    }

    private void removeElementsByTagName(final DomNodeList<HtmlElement> domNodeList) {
        ListIterator<HtmlElement> listIterator = domNodeList.listIterator();
        while (listIterator.hasNext()) {
            listIterator.next().remove();
            listIterator = domNodeList.listIterator();
        }
    }

    private AnalysisResult buildEclipseProject(final String... files) {
        FreeStyleProject project = createFreeStyleProject();
        copyMultipleFilesToWorkspace(project, files);
        enableEclipseWarnings(project);

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }
}
