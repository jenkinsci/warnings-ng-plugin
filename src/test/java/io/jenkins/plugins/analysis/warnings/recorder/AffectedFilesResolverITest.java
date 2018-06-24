package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

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

import hudson.Functions;
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
    private static final String WINDOWS_FILE_ACCESS_READ_ONLY = "RX";
    private static final String WINDOWS_FILE_DENY = "/deny";
    private static final String PACKAGE_FOR_ECLIPSE_TXT = "affected-files";
    private static final String SOURCE_FILE = PACKAGE_FOR_ECLIPSE_TXT + "/Main.java";
    private static final String ECLIPSE_REPORT =
            PACKAGE_FOR_ECLIPSE_TXT + "/eclipseOneAffectedAndThreeNotExistingFiles.txt";
    private static final String ECLIPSE_TXT_ONE_AFFECTED_FILE = PACKAGE_FOR_ECLIPSE_TXT + "/eclipseOneAffectedFile.txt";
    private static final String WHITESPACE = "\\s";
    private static final String DEFAULT_ENTRY_PATH = "eclipseResult/";

    /**
     * Verifies that the copied class file is not available and cannot be obtained by HTML scraping.
     */
    @Test
    public void shouldVerifyHtmlOutputBehaviourForAffectedFileWhichShouldNotHaveBeenCopied()
            throws IOException, SAXException {
        FreeStyleProject project = createEclipseParserProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        deleteWorkspaceAndBuildFolderFiles(project, result);

        WebClient webClient = createWebClient(false);
        HtmlPage defaultEntryPage = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH);
        String firstPartOfLink = getPartOfLink(defaultEntryPage, "fileName", SOURCE_FILE);
        String secondPartOfLink = getSourceLink(result);
        String link = DEFAULT_ENTRY_PATH + firstPartOfLink + secondPartOfLink;
        HtmlPage contentPage = webClient.getPage(result.getOwner(), link);
        checkIfContentExists(contentPage);
    }

    private void deleteWorkspaceAndBuildFolderFiles(final FreeStyleProject project, final AnalysisResult result) {
        Report issues = result.getIssues();
        issues.forEach(issue -> AffectedFilesResolver.getFile(result.getOwner(), issue).delete());
        assertThat(new File(j.jenkins.getWorkspaceFor(project) + "/" + SOURCE_FILE).delete()).isTrue();
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
    public void shouldVerifyHtmlOutputBehaviourForAffectedFileWhichShouldNotHaveBeenCopiedCausedByAccessDeniedToOpenAffectedFile()
            throws IOException, SAXException, InterruptedException {
        FreeStyleProject project = createEclipseParserProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        WebClient webClient = createWebClient(false);
        HtmlPage defaultEntryPage = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH);
        String firstPartOfLink = getPartOfLink(defaultEntryPage, "fileName", SOURCE_FILE);

        Report issues = result.getIssues();
        for (Issue issue : issues) {
            if (issue.getFileName().endsWith(SOURCE_FILE)) {
                String pathOfFileInWorkspace = Objects.requireNonNull(j.jenkins.getWorkspaceFor(project))
                        .child(SOURCE_FILE)
                        .toURI()
                        .getPath();
                File fileInTmp = AffectedFilesResolver.getFile(result.getOwner(), issue);
                if (Functions.isWindows()) {
                    pathOfFileInWorkspace = pathOfFileInWorkspace.replaceFirst("/", "");
                    execWindowsCommandIcacls(pathOfFileInWorkspace, WINDOWS_FILE_DENY, WINDOWS_FILE_ACCESS_READ_ONLY);
                    execWindowsCommandIcacls(fileInTmp.getAbsolutePath(), WINDOWS_FILE_DENY,
                            WINDOWS_FILE_ACCESS_READ_ONLY);
                }
                else {
                    File file = new File(pathOfFileInWorkspace);
                    assertThat(file.setReadable(false, false)).isTrue();
                    assertThat(fileInTmp.setReadable(false, false)).isTrue();
                }
            }
        }
        String secondPartOfLink = getSourceLink(result);
        HtmlPage contentPage = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH + firstPartOfLink + secondPartOfLink);
        checkIfContentExists(contentPage);
    }

    private FreeStyleProject getJobWithWorkspaceFiles() {
        FreeStyleProject job = createFreeStyleProject();
        copyMultipleFilesToWorkspace(job, ECLIPSE_REPORT, SOURCE_FILE);
        return job;
    }

    /**
     * Verifies that the copied class file is available and can be obtained by HTML scraping.
     */
    @Test
    public void shouldVerifyHtmlOutputBehaviourForAffectedFileWhichShouldFindFile()
            throws IOException, SAXException {
        FreeStyleProject project = createEclipseParserProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        WebClient webClient = createWebClient(false);
        HtmlPage defaultEntryPage = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH);
        String firstPartOfLink = getPartOfLink(defaultEntryPage, "fileName", SOURCE_FILE);
        String secondPartOfLink = getSourceLink(result);
        HtmlPage contentPage = webClient.getPage(result.getOwner(),
                DEFAULT_ENTRY_PATH + firstPartOfLink + secondPartOfLink);
        DomElement domElement = contentPage.getElementById("main-panel");
        DomNodeList<HtmlElement> list = domElement.getElementsByTagName("a");
        removeElementsByTagName(list);

        String content = toString(new File(j.jenkins.getWorkspaceFor(project) + "/" + SOURCE_FILE));

        String actual = contentPage.getElementById("main-panel").asText().replaceAll(WHITESPACE, "");
        assertThat(actual).contains(content.replaceAll(WHITESPACE, ""));
    }

    /**
     * Verifies that a given file which are copied in a tmp folder can be found by the {@link AffectedFilesResolver}.
     */
    @Test
    public void shouldFindTempFile() {
        FreeStyleProject project = createEclipseParserProject();
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        Report issues = result.getIssues();
        issues.forEach(issue -> assertThatFileExistsInBuildFolder(issue, project, result.getOwner()));
    }

    private void assertThatFileExistsInBuildFolder(final Issue issue, final FreeStyleProject project, final Run<?, ?> owner) {
        File buildFolderCopy = AffectedFilesResolver.getFile(owner, issue);
        if (issue.getFileName().contains(SOURCE_FILE)) {
            assertThat(buildFolderCopy).exists();
            
            File file = new File(j.jenkins.getWorkspaceFor(project) + "/" + SOURCE_FILE);
            assertThat(toString(buildFolderCopy)).isEqualTo(toString(file));
        }
        else {
            assertThat(buildFolderCopy).doesNotExist();
        }
    }

    /**
     * Verifies that the AffectedFilesResolver produces an I/O error, when the affected files cannot copied.
     */
    @Test
    public void shouldGetIOErrorBySearchingForAffectedFiles() {
        FreeStyleProject project = createEclipseParserProject();

        denyFileAccess(j.jenkins.getWorkspaceFor(project) + "/" + SOURCE_FILE);

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
        AnalysisResult result = buildEclipseProject(ECLIPSE_TXT_ONE_AFFECTED_FILE, SOURCE_FILE);
        
        assertThatLogContains(result.getOwner(), "1 copied");
        assertThatLogContains(result.getOwner(), "0 not-found");
        assertThatLogContains(result.getOwner(), "0 with I/O error");
    }

    private void denyFileAccess(final String pathToFile) {
        if (Functions.isWindows()) {
            execWindowsCommandIcacls(pathToFile, WINDOWS_FILE_DENY, WINDOWS_FILE_ACCESS_READ_ONLY);
        }
        else {
            assertThat(new File(pathToFile).setReadable(false, false)).isTrue();
        }
    }

    private void checkIfContentExists(final HtmlPage contentPage) {
        assertThat(contentPage.getElementById("main-panel").asText()).contains(("Can't read file: "));
        boolean findTmp = contentPage.getElementById("main-panel").asText().contains(".tmp");
        if (findTmp) {
            assertThat(contentPage.getElementById("main-panel").asText()).contains((".tmp"));
        }
        else {
            assertThat(contentPage.getElementById("main-panel").asText()).contains((SOURCE_FILE));
        }
    }

    private String getPartOfLink(final HtmlPage page, final String elementsByIdAndOrName, final String fileName) {
        List<DomElement> htmlElements = page.getElementsByIdAndOrName(elementsByIdAndOrName);
        for (DomElement domElement : htmlElements) {
            DomNodeList<HtmlElement> domNodeList = domElement.getElementsByTagName("a");
            for (HtmlElement htmlElement : domNodeList) {
                if (htmlElement.getTextContent().contains(fileName)) {
                    return htmlElement.getAttribute("href");
                }
            }
        }
        return "";
    }

    private String getSourceLink(final AnalysisResult result) {
        Report issues = result.getIssues();
        for (Issue issue : issues) {
            if (issue.getFileName().endsWith(SOURCE_FILE)) {
                return "/source." + issue.getId() + "/#" + result.getBuild().getNumber();
            }
        }
        return "";
    }

    private void removeElementsByTagName(final DomNodeList<HtmlElement> domNodeList) {
        ListIterator<HtmlElement> listIterator = domNodeList.listIterator();
        while (listIterator.hasNext()) {
            listIterator.next().remove();
            listIterator = domNodeList.listIterator();
        }
    }

    private void execWindowsCommandIcacls(final String path, final String command, final String accessMode) {
        try {
            Process process = Runtime.getRuntime().exec("icacls " + path + " " + command + " *S-1-1-0:" + accessMode);
            process.waitFor();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private WebClient createWebClient(final boolean javaScriptEnabled) {
        WebClient webClient = j.createWebClient();
        webClient.setJavaScriptEnabled(javaScriptEnabled);
        return webClient;
    }

    private AnalysisResult buildEclipseProject(final String... files) {
        FreeStyleProject project = createFreeStyleProject();
        copyMultipleFilesToWorkspace(project, files);
        enableEclipseWarnings(project);

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }

}
