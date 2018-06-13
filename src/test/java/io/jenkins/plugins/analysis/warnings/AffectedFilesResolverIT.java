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
import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.FilePath;
import hudson.FilePath.TarCompression;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.tasks.Maven;

/**
 * This class is an integration test for the class {@link AffectedFilesResolver}.
 *
 * @author Deniz Mardin
 * @author Frank Christian Geyer
 */
public class AffectedFilesResolverIT extends IntegrationTest {
    /**
     * These are the module files that are necessary for the integration test of {@link
     * edu.hm.hafner.analysis.ModuleDetector}}. A short description of the purpose of every file should be given here.
     *
     * <b>Maven:</b>
     * pom.xml a default pom.xml with a valid name tag
     * <p>
     * m1/pom.xml a default pom.xml with a valid name tag which could be used to detect additional modules in addition
     * to the previous mentioned pom.xml
     * <p>
     * m2/pom.xml a default pom.xml with a valid name tag which could be used to detect additional modules in addition
     * to the first mentioned pom.xml
     * <p>
     * m3/pom.xml a broken XML-structure breaks the correct parsing of this file
     * <p>
     * m4/pom.xml a pom.xml with a substitutional artifactId tag and without a name tag
     * <p>
     * m5/pom.xml a pom.xml without a substitutional artifactId tag and without a name tag
     *
     * <b>Ant:</b>
     * build.xml a default build.xml with a valid name tag
     * <p>
     * m1/build.xml a default build.xml with a valid name tag which could be used to detect additional modules in
     * addition to the previous mentioned build.xml
     * <p>
     * m2/build.xml a broken XML-structure breaks the correct parsing of this file
     * <p>
     * m3/build.xml a build file without the name tag
     *
     * <b>OSGI:</b>
     * META-INF/MANIFEST.MF a default MANIFEST.MF with a set Bundle-SymbolicName and a set Bundle-Vendor
     * <p>
     * m1/META-INF/MANIFEST.MF a MANIFEST.MF with a wildcard Bundle-Name, a set Bundle-SymbolicName and a wildcard
     * Bundle-Vendor
     * <p>
     * m2/META-INF/MANIFEST.MF a MANIFEST.MF with a set Bundle-Name and a wildcard Bundle-Vendor
     * <p>
     * m3/META-INF/MANIFEST.MF an empty MANIFEST.MF
     * <p>
     * plugin.properties a default plugin.properties file
     */
    private static final String[] MODULE_FILE_NAMES_TO_KEEP = new String[]{
            "m1/pom.xml", "m2/pom.xml", "m3/pom.xml", "m4/pom.xml", "m5/pom.xml", "pom.xml",
            "m1/build.xml", "m2/build.xml", "m3/build.xml", "build.xml",
            "m1/META-INF/MANIFEST.MF", "m2/META-INF/MANIFEST.MF", "m3/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", "plugin.properties"
    };

    private static final String[] GENERIC_FILE_NAMES_TO_KEEP = new String[]{
            ".cs", ".java", ".zip", ".tar", ".gz"
    };

    /**
     * Creates a pre-defined filename for a workspace file.
     *
     * @param fileNamePrefix
     *         prefix of the filename
     */
    protected String createWorkspaceFileName(final String fileNamePrefix) {
        String modifiedFileName = String.format("%s-issues.txt", FilenameUtils.getBaseName(fileNamePrefix));

        String fileNamePrefixInModuleList = Arrays.stream(MODULE_FILE_NAMES_TO_KEEP)
                .filter(fileNamePrefix::endsWith)
                .findFirst()
                .orElse("");

        if ("".equals(fileNamePrefixInModuleList)) {
            List<Boolean> fileNamePrefixInList = Arrays.stream(GENERIC_FILE_NAMES_TO_KEEP)
                    .map(fileNamePrefix::endsWith)
                    .collect(Collectors.toList());
            return fileNamePrefixInList.contains(true) ? FilenameUtils.getName(fileNamePrefix) : modifiedFileName;
        }
        return fileNamePrefixInModuleList;
    }

    /**
     * Copies the specified files to the workspace using a generated file name.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     */
    protected void copyFilesToWorkspace(final TopLevelItem job, final String... fileNames) {
        try {
            FilePath workspace = j.jenkins.getWorkspaceFor(job);
            assertThat(workspace).isNotNull();
            for (String fileName : fileNames) {
                workspace.child(createWorkspaceFileName(fileName)).copyFrom(asInputStream(fileName));
            }
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private static final String WINDOWS_FILE_ACCESS_READ_ONLY = "RX";
    private static final String WINDOWS_FILE_DENY = "/deny";
    private static final String PACKAGE_FOR_ECLIPSE_TXT = "/edu/hm/hafner/analysis/AffectedFilesResolverTestFiles";
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

        Report issues = result.getIssues();
        issues.forEach(issue -> AffectedFilesResolver.getFile(result.getOwner(), issue).delete());
        assertThat(new File(j.jenkins.getWorkspaceFor(project) + "/" + EXTRACTED_FILE).delete()).isTrue();

        WebClient webClient = createWebClient(false);
        HtmlPage defaultEntryPage = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH);
        String firstPartOfLink = getPartOfLink(defaultEntryPage, "fileName", EXTRACTED_FILE);
        String secondPartOfLink = getSourceLink(result);
        String link = DEFAULT_ENTRY_PATH + firstPartOfLink + secondPartOfLink;
        HtmlPage contentPage = webClient.getPage(result.getOwner(),
                link);
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

        Report issues = result.getIssues();
        for (Issue issue : issues) {
            if (issue.getFileName().endsWith(EXTRACTED_FILE)) {
                String pathOfFileInWorkspace = Objects.requireNonNull(j.jenkins.getWorkspaceFor(project))
                        .child(EXTRACTED_FILE)
                        .toURI()
                        .getPath();
                File fileInTmp = AffectedFilesResolver.getFile(result.getOwner(), issue);
                if (System.getProperty("os.name").contains("Windows")) {
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

        Report issues = result.getIssues();
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
        if (System.getProperty("os.name").contains("Windows")) {
            execWindowsCommandIcacls(pathToExtractedFile, WINDOWS_FILE_DENY, WINDOWS_FILE_ACCESS_READ_ONLY);
        }
        else {
            assertThat(new File(pathToExtractedFile).setReadable(false, false)).isTrue();
        }

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        File logFile = project.getBuildByNumber(result.getBuild().getNumber()).getLogFile();
        String consoleOutput = convertFileToString(logFile);
        assertThat(consoleOutput).contains("0 copied, 3 not-found, 1 with I/O error");
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
                "Skipping post processing due to errors.");
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
        File tmpFile = AffectedFilesResolver.getFile(owner, issue);
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
        Report issues = result.getIssues();
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
        publisher.setTools(Collections.singletonList(new ToolConfiguration(new Eclipse(), "**/*issues.txt")));
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
