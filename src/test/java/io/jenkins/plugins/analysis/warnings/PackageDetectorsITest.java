package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import edu.hm.hafner.analysis.Issue;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static org.assertj.core.api.Assertions.*;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Maven;

/**
 * This class is an integration test for the classes associated with {@link edu.hm.hafner.analysis.PackageDetectors}.
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
public class PackageDetectorsITest extends IntegrationTest {

    private static final String PACKAGE_FILE_PATH = "moduleandpackagedetectorfiles/";
    private static final String PACKAGE_WITH_FILES_CSHARP = PACKAGE_FILE_PATH + "csharp/";
    private static final String PACKAGE_WITH_FILES_JAVA = PACKAGE_FILE_PATH + "java/";
    private static final String DEFAULT_ENTRY_PATH = "eclipseResult/";
    private static final String DEFAULT_TAB_TO_INVESTIGATE = "packageName";
    private static final String DEFAULT_DEBUG_LOG_LINE = "to resolve package names (or namespaces)";

    private static final boolean IS_MAVEN_PROJECT = false;

    /**
     * Verifies that the output is correct if there exist various namespaces (C#) and packages (Java) at the same time
     * in the expected HTML output.
     */
    @Test
    public void shouldShowNamespacesAndPackagesAltogetherForJavaAndCSharpInTheHtmlOutput()
            throws IOException, SAXException {

        AnalysisResult result = buildProject(PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpVariousClasses.txt",
                PACKAGE_WITH_FILES_JAVA + "eclipseForJavaVariousClasses.txt"
                , PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithUnconventionalPackageNaming.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithBrokenPackageNaming.java"
                , PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespaceBetweenCode.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNestedAndNormalNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithoutNamespace.cs"
        );

        WebClient webClient = createWebClient(false);
        WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
        String webResponseContentAsString = webResponse.getContentAsString();

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlHeader(false));
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(returnPreparedHtmlOutput("edu.hm.hafner.analysis._123.int.naming.structure", 1));
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput("SampleClassWithNamespace", 1) + "<tr><td>");
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput("NestedNamespace", 1) + "<tr><td>");
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput("SampleClassWithNestedAndNormalNamespace", 1) + "<tr><td>");
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput("-", 6) + returnPreparedTotalHtmlOutput(10));

            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            List<String> packageLinks = getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE);

            softly.assertThat(packageLinks).hasSize(5);

            crawlAllSubPagesOfPackagesAndAssertTheyAreNotLinkingToFurtherPackages(packageLinks, result, webClient,
                    softly);
        }
    }

    /**
     * Verifies that the output is correct if there are only packages (Java) in the expected HTML output.
     */
    @Test
    public void shouldShowPackagesForJavaOnlyInTheHtmlOutput() throws IOException, SAXException {

        AnalysisResult result = buildProject(PACKAGE_WITH_FILES_JAVA + "eclipseForJavaVariousClasses.txt"
                , PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithUnconventionalPackageNaming.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithBrokenPackageNaming.java"
        );

        WebClient webClient = createWebClient(false);
        WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
        String webResponseContentAsString = webResponse.getContentAsString();

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlHeader(true));
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(returnPreparedHtmlOutput("edu.hm.hafner.analysis._123.int.naming.structure", 1)
                            + "<tr><td>");
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput("-", 5) + returnPreparedTotalHtmlOutput(6));

            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            List<String> packageLinks = getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE);

            softly.assertThat(packageLinks).hasSize(2);

            crawlAllSubPagesOfPackagesAndAssertTheyAreNotLinkingToFurtherPackages(packageLinks, result, webClient,
                    softly);
        }
    }

    /**
     * Verifies that the output is correct if there are only namespaces (C#) in the expected HTML output.
     */
    @Test
    public void shouldShowNamespacesForCSharpOnlyInTheHtmlOutput() throws IOException, SAXException {

        AnalysisResult result = buildProject(PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpVariousClasses.txt"
                , PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespaceBetweenCode.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNestedAndNormalNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithoutNamespace.cs");

        WebClient webClient = createWebClient(false);
        WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
        String webResponseContentAsString = webResponse.getContentAsString();

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(webResponseContentAsString).containsPattern(returnPreparedHtmlHeader(false));
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(returnPreparedHtmlOutput("SampleClassWithNamespace", 1) + "<tr><td>");
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(returnPreparedHtmlOutput("NestedNamespace", 1) + "<tr><td>");
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(
                            returnPreparedHtmlOutput("SampleClassWithNestedAndNormalNamespace", 1) + "<tr><td>");
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(returnPreparedHtmlOutput("-", 3) + returnPreparedTotalHtmlOutput(6));

            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            List<String> packageLinks = getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE);

            softly.assertThat(packageLinks).hasSize(4);

            crawlAllSubPagesOfPackagesAndAssertTheyAreNotLinkingToFurtherPackages(packageLinks, result, webClient,
                    softly);
        }
    }

    /**
     * Verifies that the output of the HTML page is empty if the project is empty.
     */
    @Test
    public void shouldContainNoSpecificHtmlOutputForAnEmptyProject() throws IOException, SAXException {
        checkWebPageForExpectedEmptyResult(buildProject());
    }

    /**
     * Verifies that the output of the HTML page is empty if there is only one class without a package (Java).
     */
    @Test
    public void shouldContainNoHtmlOutputForNoPackageDefinedJava() throws IOException, SAXException {
        checkWebPageForExpectedEmptyResult(
                buildProject(PACKAGE_WITH_FILES_JAVA + "eclipseForJavaOneClassWithoutPackage.txt"

                        , PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java"
                ));
    }

    /**
     * Verifies that the output of the HTML page is empty if there is only one class with a package (Java).
     */
    @Test
    public void shouldContainNoHtmlOutputForOnlyOnePackageDefinedJava() throws IOException, SAXException {
        checkWebPageForExpectedEmptyResult(
                buildProject(PACKAGE_WITH_FILES_JAVA + "eclipseForJavaOneClassWithPackage.txt"
                        , PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java"
                ));
    }

    /**
     * Verifies that the output of the HTML page is empty if there is only one class without a namespace (C#).
     */
    @Test
    public void shouldContainNoHtmlOutputForNoNamespaceDefinedCSharp() throws IOException, SAXException {
        checkWebPageForExpectedEmptyResult(buildProject(
                PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpOneClassWithoutNamespace.txt"
                , PACKAGE_WITH_FILES_CSHARP + "SampleClassWithoutNamespace.cs"
        ));
    }

    /**
     * Verifies that the output of the HTML page is empty if there is only one class with a namespace (C#).
     */
    @Test
    public void shouldContainNoHtmlOutputForOnlyOneNamespaceDefinedCSharp() throws IOException, SAXException {
        checkWebPageForExpectedEmptyResult(
                buildProject(PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpOneClassWithNamespace.txt",
                        PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespace.cs"
                ));
    }

    /**
     * Verifies that the packages of Java files and namespaces of C# files are handled correctly when they are used
     * together in a build.
     */
    @Test
    public void shouldDetectVariousNamespacesAndPackagesForCombinedJavaAndCSharpFiles() throws IOException {

        AnalysisResult result = buildProject(PACKAGE_WITH_FILES_JAVA + "eclipseForJavaVariousClasses.txt",
                PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpVariousClasses.txt"

                , PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithUnconventionalPackageNaming.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithBrokenPackageNaming.java"
                , PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespaceBetweenCode.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNestedAndNormalNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithoutNamespace.cs"
        );

        String logOutput = FileUtils.readFileToString(result.getOwner().getLogFile(), StandardCharsets.UTF_8);
        Map<String, Long> collect = collectPackageNames(result);

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(result.getIssues()).hasSize(10);
            softly.assertThat(result.getIssues().getPackages())
                    .containsExactly("edu.hm.hafner.analysis._123.int.naming.structure", "SampleClassWithNamespace",
                            "NestedNamespace",
                            "SampleClassWithNestedAndNormalNamespace", "-");
            softly.assertThat(collect).hasSize(5);
            softly.assertThat(collect.get("edu.hm.hafner.analysis._123.int.naming.structure")).isEqualTo(1L);
            softly.assertThat(collect.get("SampleClassWithNamespace")).isEqualTo(1L);
            softly.assertThat(collect.get("NestedNamespace")).isEqualTo(1L);
            softly.assertThat(collect.get("SampleClassWithNestedAndNormalNamespace")).isEqualTo(1L);
            softly.assertThat(collect.get("-")).isEqualTo(6L);
            softly.assertThat(logOutput).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutput).contains(returnExpectedNumberOfResolvedPackageNames(10));
        }
    }

    /**
     * Verifies that if there are two builds with different parsers which set the package name at a different time are
     * handled correctly.
     */
    @Test
    public void shouldRunTwoIndependentBuildsWithTwoDifferentParsersAndCheckForCorrectPackageHandling()
            throws IOException {

        FreeStyleProject jobWithFindBugsParser = createJobWithWorkspaceFile(
                PACKAGE_FILE_PATH + "various/findbugs-packages.xml");
        enableWarnings(jobWithFindBugsParser, new FindBugs());
        AnalysisResult resultWithFindBugsParser = scheduleBuildAndAssertStatus(jobWithFindBugsParser, Result.SUCCESS);

        String logOutputForFindBugs = FileUtils.readFileToString(resultWithFindBugsParser.getOwner().getLogFile(),
                StandardCharsets.UTF_8);
        Map<String, Long> collectFindBugsPackages = collectPackageNames(resultWithFindBugsParser);

        AnalysisResult resultWithEclipseParser = buildProject(
                PACKAGE_WITH_FILES_JAVA + "eclipseForJavaVariousClasses.txt",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithUnconventionalPackageNaming.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithBrokenPackageNaming.java"
        );

        String logOutputForEclipse = FileUtils.readFileToString(resultWithEclipseParser.getOwner().getLogFile(),
                StandardCharsets.UTF_8);
        Map<String, Long> collectEclipsePackages = collectPackageNames(resultWithEclipseParser);

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(resultWithFindBugsParser.getIssues()).hasSize(3);
            softly.assertThat(resultWithFindBugsParser.getIssues().getPackages())
                    .containsExactly("edu.hm.hafner.analysis.123",
                            "edu.hm.hafner.analysis._test",
                            "edu.hm.hafner.analysis.int.naming.structure");
            softly.assertThat(collectFindBugsPackages).hasSize(3);
            softly.assertThat(collectFindBugsPackages.get("edu.hm.hafner.analysis.123")).isEqualTo(1L);
            softly.assertThat(collectFindBugsPackages.get("edu.hm.hafner.analysis._test")).isEqualTo(1L);
            softly.assertThat(collectFindBugsPackages.get("edu.hm.hafner.analysis.int.naming.structure")).isEqualTo(1L);
            softly.assertThat(logOutputForFindBugs).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutputForFindBugs).contains(returnExpectedNumberOfResolvedPackageNames(0));

            softly.assertThat(resultWithEclipseParser.getIssues()).hasSize(6);
            softly.assertThat(resultWithEclipseParser.getIssues().getPackages())
                    .containsExactly("edu.hm.hafner.analysis._123.int.naming.structure", "-");
            softly.assertThat(collectEclipsePackages).hasSize(2);
            softly.assertThat(collectEclipsePackages.get("edu.hm.hafner.analysis._123.int.naming.structure"))
                    .isEqualTo(1L);
            softly.assertThat(collectEclipsePackages.get("-")).isEqualTo(5L);
            softly.assertThat(logOutputForEclipse).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutputForEclipse).contains(returnExpectedNumberOfResolvedPackageNames(6));
        }
    }

    /**
     * Verifies that various namespaces (C#) are handled correctly.
     */
    @Test
    public void shouldDetectVariousNamespacesForCSharpFiles() throws IOException {

        AnalysisResult result = buildProject(
                PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpVariousClasses.txt"
                , PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespaceBetweenCode.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNestedAndNormalNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithoutNamespace.cs");

        String logOutput = FileUtils.readFileToString(result.getOwner().getLogFile(), StandardCharsets.UTF_8);
        Map<String, Long> collect = collectPackageNames(result);

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(result.getIssues()).hasSize(6);
            softly.assertThat(result.getIssues().getPackages())
                    .containsExactly("SampleClassWithNamespace", "NestedNamespace",
                            "SampleClassWithNestedAndNormalNamespace", "-");
            softly.assertThat(collect).hasSize(4);
            softly.assertThat(collect.get("SampleClassWithNamespace")).isEqualTo(1L);
            softly.assertThat(collect.get("NestedNamespace")).isEqualTo(1L);
            softly.assertThat(collect.get("SampleClassWithNestedAndNormalNamespace")).isEqualTo(1L);
            softly.assertThat(collect.get("-")).isEqualTo(3L);
            softly.assertThat(logOutput).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutput).contains(returnExpectedNumberOfResolvedPackageNames(6));
        }
    }

    /**
     * Verifies that various packages (Java) are handled correctly.
     */
    @Test
    public void shouldDetectVariousPackagesForJavaFiles() throws IOException {

        AnalysisResult result = buildProject(
                PACKAGE_WITH_FILES_JAVA + "eclipseForJavaVariousClasses.txt"
                , PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithUnconventionalPackageNaming.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithBrokenPackageNaming.java"

        );

        String logOutput = FileUtils.readFileToString(result.getOwner().getLogFile(), StandardCharsets.UTF_8);
        Map<String, Long> collect = collectPackageNames(result);

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(result.getIssues()).hasSize(6);
            softly.assertThat(result.getIssues().getPackages()).containsExactly(
                    "edu.hm.hafner.analysis._123.int.naming.structure",
                    "-");
            softly.assertThat(collect).hasSize(2);
            softly.assertThat(collect.get("edu.hm.hafner.analysis._123.int.naming.structure")).isEqualTo(1L);
            softly.assertThat(collect.get("-")).isEqualTo(5L);
            softly.assertThat(logOutput).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutput).contains(returnExpectedNumberOfResolvedPackageNames(6));
        }
    }

    private String returnExpectedNumberOfResolvedPackageNames(final int expectedNumberOfResolvedPackageNames) {
        return "Resolved package names of " + expectedNumberOfResolvedPackageNames + " affected files";
    }

    private String returnPreparedHtmlHeader(final boolean isPackage) {
        String determinedName = isPackage ? "Package" : "Namespace";
        return "<a data-toggle=\"tab\" role=\"tab\" href=\"#packageNameContent\" class=\"nav-link\">" + determinedName
                + "s</a>.*<th>" + determinedName
                + "</th><th>Total</th><th class=\"no-sort\">Distribution</th></tr></thead><tbody><tr><td>";
    }

    private String returnPreparedHtmlOutput(final String packageOrNamespaceName,
            final int numberOfPackagesOrNamespaces) {
        return "<a href=\"packageName..*>" + packageOrNamespaceName + "</a></td><td>" + numberOfPackagesOrNamespaces
                + "</td><td><div><span style=\"width:.*%\" class=\"bar-graph priority-normal priority-normal--hover\">.</span></div></td></tr>";
    }

    private String returnPreparedTotalHtmlOutput(final int numberOfTotalPackagesOrNamespaces) {
        return "<tfoot><tr><td>Total</td><td>" + numberOfTotalPackagesOrNamespaces + "</td><td>";
    }

    private WebClient createWebClient(final boolean javaScriptEnabled) {
        WebClient webClient = j.createWebClient();
        webClient.setJavaScriptEnabled(javaScriptEnabled);
        return webClient;
    }

    private Map<String, Long> collectPackageNames(final AnalysisResult result) {
        return result.getIssues().stream()
                .collect(Collectors.groupingBy(Issue::getPackageName,
                        Collectors.counting()));
    }

    private void crawlAllSubPagesOfPackagesAndAssertTheyAreNotLinkingToFurtherPackages(final List<String> packageLinks,
            final AnalysisResult result, final WebClient webClient, final AutoCloseableSoftAssertions softly)
            throws IOException, SAXException {
        for (String link : packageLinks) {
            WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH + link).getWebResponse();
            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            softly.assertThat(getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE)).isEmpty();
        }
    }

    private void checkWebPageForExpectedEmptyResult(final AnalysisResult result) throws IOException, SAXException {
        WebClient webClient = createWebClient(false);
        WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
        HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
        assertThat(getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE)).isEmpty();
    }

    private List<String> getLinksWithGivenTargetName(final HtmlPage page, final String targetName) {
        List<DomElement> htmlElement = page.getElementsByIdAndOrName(targetName);
        ArrayList<String> links = new ArrayList<>();
        for (DomElement element : htmlElement) {
            DomNodeList<HtmlElement> domNodeList = element.getElementsByTagName("a");
            for (HtmlElement htmlElementHref : domNodeList) {
                links.add(htmlElementHref.getAttribute("href"));
            }
        }
        return links;
    }

    private AnalysisResult buildProject(final String... files) {
        FreeStyleProject project = createJobWithWorkspaceFile(files);
        enableWarnings(project, new Eclipse());

        if (IS_MAVEN_PROJECT) {
            project.getBuildersList().add(new Maven("package", "MavenTestBuild",
                    "pom.xml"
                    , "", "", true));
        }

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
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
        return job;
    }

    /**
     * Creates a pre-defined filename for a workspace file.
     *
     * @param fileName
     *         the filename
     */
    @Override
    protected String createWorkspaceFileName(final String fileName) {
        String modifiedFileName = String.format("%s-issues.txt", FilenameUtils.getBaseName(fileName));

        String[] genericFileNamesToKeep = new String[]{
                ".cs", ".java"
        };

        List<Boolean> fileNamePrefixInList = Arrays.stream(genericFileNamesToKeep)
                .map(fileName::endsWith)
                .collect(Collectors.toList());
        return fileNamePrefixInList.contains(true) ? FilenameUtils.getName(fileName) : modifiedFileName;
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
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param staticAnalysisTool
     *         the static analysis tool to use
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job, final StaticAnalysisTool staticAnalysisTool) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Collections.singletonList(new ToolConfiguration("**/*issues.txt", staticAnalysisTool)));
        job.getPublishersList().add(publisher);
        return publisher;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param configuration
     *         configuration of the recorder
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job, final Consumer<IssuesRecorder> configuration) {
        IssuesRecorder publisher = enableWarnings(job);
        configuration.accept(publisher);
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
