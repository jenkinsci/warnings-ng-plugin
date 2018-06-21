package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.FilePath;
import hudson.FilePath.TarCompression;
import hudson.Functions;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.Maven;

/**
 * This class is an integration test for the class {@link AffectedFilesResolver}.
 * <p>
 * The files below are necessary for the integration test of {@link io.jenkins.plugins.analysis.core.util.AffectedFilesResolver}}.
 * A short description of the purpose of every file should be given here.
 * <b>Main.zip:</b>
 * Main.java a sample Java class for the test cases
 * <p>
 *
 * @author Deniz Mardin
 * @author Frank Christian Geyer
 */
public class AffectedFilesResolverITest extends IntegrationTest {

    private static final String WINDOWS_FILE_ACCESS_READ_ONLY = "RX";
    private static final String WINDOWS_FILE_DENY = "/deny";
    private static final String PACKAGE_FOR_ECLIPSE_TXT = "AffectedFilesResolverTestFiles";
    private static final String ZIP_FILE = PACKAGE_FOR_ECLIPSE_TXT + "/Main.zip";
    private static final String ECLIPSE_TXT =
            PACKAGE_FOR_ECLIPSE_TXT + "/eclipseOneAffectedAndThreeNotExistingFiles.txt";
    private static final String ECLIPSE_TXT_ONE_AFFECTED_FILE = PACKAGE_FOR_ECLIPSE_TXT + "/eclipseOneAffectedFile.txt";
    private static final String WHITESPACE = "\\s";
    private static final String DEFAULT_ENTRY_PATH = "eclipseResult/";
    private static final String EXTRACTED_FILE = "Main.java";

    private static final boolean IS_MAVEN_PROJECT = false;

    /**
     * Verifies that the copied class file is not available and cannot be obtained by HTML scraping.
     */
    @Test
    public void shouldVerifyHtmlOutputBehaviourForAffectedFileWhichShouldNotHaveBeenCopied()
            throws IOException, SAXException {
        FreeStyleProject project = createJobWithWorkspaceFile(ECLIPSE_TXT, ZIP_FILE);
        enableWarnings(project);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        Issues<?> issues = result.getIssues();
        issues.forEach(issue -> AffectedFilesResolver.getTempFile(result.getOwner(), issue).delete());
        assertThat(new File(j.jenkins.getWorkspaceFor(project) + "/" + EXTRACTED_FILE).delete()).isTrue();

        WebClient webClient = createWebClient(false);
        HtmlPage defaultEntryPage = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH);
        String firstPartOfLink = getPartOfLink(defaultEntryPage, "fileName", EXTRACTED_FILE);
        String secondPartOfLink = getSourceLink(result);
        HtmlPage contentPage = webClient.getPage(result.getOwner(),
                DEFAULT_ENTRY_PATH + firstPartOfLink + secondPartOfLink);
        checkIfContentExists(contentPage);
    }

    /**
     * Verifies that the copied class file cannot be obtained by HTML scraping because the permissions to open the file
     * are denied.
     */
    @Test
    public void shouldVerifyHtmlOutputBehaviourForAffectedFileWhichShouldNotHaveBeenCopiedCausedByAccessDeniedToOpenAffectedFile()
            throws IOException, SAXException, InterruptedException {
        FreeStyleProject project = createJobWithWorkspaceFile(ECLIPSE_TXT, ZIP_FILE);
        enableWarnings(project);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        WebClient webClient = createWebClient(false);
        HtmlPage defaultEntryPage = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH);
        String firstPartOfLink = getPartOfLink(defaultEntryPage, "fileName", EXTRACTED_FILE);

        Issues<?> issues = result.getIssues();
        for (Issue issue : issues) {
            if (issue.getFileName().endsWith(EXTRACTED_FILE)) {
                String pathOfFileInWorkspace = Objects.requireNonNull(j.jenkins.getWorkspaceFor(project))
                        .child(EXTRACTED_FILE)
                        .toURI()
                        .getPath();
                File fileInTmp = AffectedFilesResolver.getTempFile(result.getOwner(), issue);
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
        HtmlPage contentPage = webClient.getPage(result.getOwner(),
                DEFAULT_ENTRY_PATH + firstPartOfLink + secondPartOfLink);
        checkIfContentExists(contentPage);
    }

    /**
     * Verifies that the copied class file is available and can be obtained by HTML scraping.
     */
    @Test
    public void shouldVerifyHtmlOutputBehaviourForAffectedFileWhichShouldFindFile()
            throws IOException, SAXException {
        FreeStyleProject project = createJobWithWorkspaceFile(ECLIPSE_TXT, ZIP_FILE);
        enableWarnings(project);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        WebClient webClient = createWebClient(false);
        HtmlPage defaultEntryPage = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH);
        String firstPartOfLink = getPartOfLink(defaultEntryPage, "fileName", EXTRACTED_FILE);
        String secondPartOfLink = getSourceLink(result);
        HtmlPage contentPage = webClient.getPage(result.getOwner(),
                DEFAULT_ENTRY_PATH + firstPartOfLink + secondPartOfLink);
        DomElement domElement = contentPage.getElementById("main-panel");
        DomNodeList<HtmlElement> list = domElement.getElementsByTagName("a");
        removeElementsByTagName(list);

        String content = convertFileToString(new File(j.jenkins.getWorkspaceFor(project) + "/" + EXTRACTED_FILE));

        assertThat(contentPage.getElementById("main-panel").asText().replaceAll(WHITESPACE, "")).contains(
                content.replaceAll(WHITESPACE, ""));
    }

    /**
     * Verifies that a given file which are copied in a tmp folder can be found by the {@link AffectedFilesResolver}.
     */
    @Test
    public void shouldFindTempFile() {
        FreeStyleProject project = createJobWithWorkspaceFile(ECLIPSE_TXT, ZIP_FILE);
        enableWarnings(project);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        Issues<?> issues = result.getIssues();
        issues.forEach(issue -> checkIfTmpExists(issue, project, result.getOwner()));
    }

    /**
     * Verifies that the AffectedFilesResolver produces an I/O error, when the affected files cannot copied.
     */
    @Test
    public void shouldGetIOErrorBySearchingForAffectedFiles() {
        FreeStyleProject project = createJobWithWorkspaceFile(ECLIPSE_TXT, ZIP_FILE);
        enableWarnings(project);

        String pathToExtractedFile = j.jenkins.getWorkspaceFor(project) + "/" + EXTRACTED_FILE;
        denyFileAccess(pathToExtractedFile);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        File logFile = project.getBuildByNumber(result.getBuild().getNumber()).getLogFile();
        String consoleOutput = convertFileToString(logFile);
        assertThat(consoleOutput).contains("0 copied, 3 not-found, 1 with I/O error");
    }

    private void denyFileAccess(final String pathToFile) {
        if (Functions.isWindows()) {
            execWindowsCommandIcacls(pathToFile, WINDOWS_FILE_DENY, WINDOWS_FILE_ACCESS_READ_ONLY);
        }
        else {
            assertThat(new File(pathToFile).setReadable(false, false)).isTrue();
        }
    }

    /**
     * Verifies that the {@link AffectedFilesResolver} can find one existing file and cannot find 3 other files.
     */
    @Test
    public void shouldFindAffectedFilesWhereasThreeFilesAreNotFound() {
        AnalysisResult result = buildProject(ECLIPSE_TXT, ZIP_FILE);
        assertThat(convertFileToString(result.getOwner().getLogFile())).contains(
                "1 copied, 3 not-found, 0 with I/O error");
    }

    /**
     * Verifies that the {@link AffectedFilesResolver} can find one existing file.
     */
    @Test
    public void shouldFindOneAffectedFile() {
        AnalysisResult result = buildProject(ECLIPSE_TXT_ONE_AFFECTED_FILE, ZIP_FILE);
        assertThat(convertFileToString(result.getOwner().getLogFile())).contains(
                "1 copied, 0 not-found, 0 with I/O error");
    }

    /**
     * Verifies that the {@link AffectedFilesResolver} cannot find in a empty Project.
     */
    @Test
    public void shouldFindNothingInAEmptyProject() {
        assertThat(convertFileToString(buildProject().getOwner().getLogFile())).contains(
                "0 copied, 0 not-found, 0 with I/O error");
    }

    /**
     * Creates a pre-defined filename for a workspace file.
     *
     * @param fileNamePrefix
     *         prefix of the filename
     */
    @Override
    protected String createWorkspaceFileName(final String fileNamePrefix) {
        String modifiedFileName = String.format("%s-issues.txt", FilenameUtils.getBaseName(fileNamePrefix));

        String[] genericFileNamesToKeep = new String[]{
                ".zip", ".tar", ".gz"
        };

        List<Boolean> fileNamePrefixInList = Arrays.stream(genericFileNamesToKeep)
                .map(fileNamePrefix::endsWith)
                .collect(Collectors.toList());
        return fileNamePrefixInList.contains(true) ? FilenameUtils.getName(fileNamePrefix) : modifiedFileName;
    }


    private void checkIfContentExists(final HtmlPage contentPage) {
        assertThat(contentPage.getElementById("main-panel").asText()).contains(("Can't read file: "));
        boolean findTmp = contentPage.getElementById("main-panel").asText().contains(".tmp");
        if (findTmp) {
            assertThat(contentPage.getElementById("main-panel").asText()).contains((".tmp"));
        }
        else {
            assertThat(contentPage.getElementById("main-panel").asText()).contains((EXTRACTED_FILE));
        }
    }

    private void checkIfTmpExists(final Issue issue, final FreeStyleProject project, final Run<?, ?> owner) {
        File tmpFile = AffectedFilesResolver.getTempFile(owner, issue);
        if (issue.getFileName().contains(EXTRACTED_FILE)) {
            assertThat(tmpFile).exists();
            File file = new File(j.jenkins.getWorkspaceFor(project) + "/" + EXTRACTED_FILE);
            assertThat(convertFileToString(tmpFile)).isEqualTo(convertFileToString(file));
        }
        else {
            assertThat(tmpFile).doesNotExist();
        }
    }

    private void extractInWorkspace(final FreeStyleProject project, final String... archiveFile) {
        try {
            for (String file : archiveFile) {
                Path source = new File(
                        Objects.requireNonNull(j.jenkins.getWorkspaceFor(project)).toURI().getPath() + new File(
                                file).getName()).toPath();
                Path destination = new File(
                        Objects.requireNonNull(j.jenkins.getWorkspaceFor(project)).toURI().getPath()).toPath();
                extract(source, destination);
            }
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private void extract(final Path source, final Path destination) {
        FilePath pathOfZip = new FilePath(source.toFile());
        FilePath filePathUnZip = new FilePath(destination.toFile());
        try {
            if (source.toString().contains(".zip")) {
                pathOfZip.unzip(filePathUnZip);
            }
            else if (source.toString().contains(".gz")) {
                pathOfZip.untar(filePathUnZip, TarCompression.GZIP);
            }
            else if (source.toString().contains(".tar")) {
                pathOfZip.untar(filePathUnZip, TarCompression.NONE);
            }
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
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
        Issues<?> issues = result.getIssues();
        for (Issue issue : issues) {
            if (issue.getFileName().endsWith(EXTRACTED_FILE)) {
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

    private AnalysisResult buildProject(final String... files) {
        FreeStyleProject project = createJobWithWorkspaceFile(files);
        enableWarnings(project);

        extractInWorkspace(project, files);

        if (IS_MAVEN_PROJECT) {
            project.getBuildersList().add(new Maven("package", "MavenTestBuild",
                    "pom.xml"
                    , "", "", true));
        }

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }

    private String convertFileToString(final File file) {
        try {
            return FileUtils.readFileToString(file, Charset.defaultCharset());
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job}. The job will get a generated name.
     *
     * @return the created job
     */
    private FreeStyleProject createJob() {
        try {
            return j.createFreeStyleProject();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job} and copies the specified resources to the workspace folder.
     * The job will get a generated name.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the created job
     */
    private FreeStyleProject createJobWithWorkspaceFile(final String... fileNames) {
        FreeStyleProject job = createJob();
        copyFilesToWorkspace(job, fileNames);
        extractInWorkspace(job, fileNames);
        return job;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Collections.singletonList(new ToolConfiguration("**/*issues.txt", new Eclipse())));
        job.getPublishersList().add(publisher);
        return publisher;
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link AnalysisResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result for the build
     *
     * @return the created {@link ResultAction}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private AnalysisResult scheduleBuildAndAssertStatus(final FreeStyleProject job, final Result status) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));

            ResultAction action = build.getAction(ResultAction.class);

            assertThat(action).isNotNull();

            return action.getResult();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
