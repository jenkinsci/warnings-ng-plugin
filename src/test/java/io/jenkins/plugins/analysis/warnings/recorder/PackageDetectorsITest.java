package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.hm.hafner.analysis.Issue;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.FindBugs;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.PropertyTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.PropertyTable.PropertyRow;

import static io.jenkins.plugins.analysis.core.testutil.SoftAssertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * This class is an integration test for the classes associated with {@code edu.hm.hafner.analysis.PackageDetectors}.
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 * @author Ullrich Hafner
 */
public class PackageDetectorsITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String PACKAGE_FILE_PATH = "detectors/";
    private static final String PACKAGE_WITH_FILES_CSHARP = PACKAGE_FILE_PATH + "csharp/";
    private static final String PACKAGE_WITH_FILES_JAVA = PACKAGE_FILE_PATH + "java/";
    private static final String DEFAULT_ENTRY_PATH = "eclipse/";
    private static final String DEFAULT_TAB_TO_INVESTIGATE = "packageName";
    private static final String DEFAULT_DEBUG_LOG_LINE = "Resolving package names (or namespaces) by parsing the affected files";

    /**
     * Verifies that the output is correct if there exist various namespaces (C#) and packages (Java) at the same time
     * in the expected HTML output.
     */
    @Test
    public void shouldShowNamespacesAndPackagesAltogetherForJavaAndCSharpInTheHtmlOutput() {
        AnalysisResult result = buildProject(PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpVariousClasses.txt",
                PACKAGE_WITH_FILES_JAVA + "eclipseForJavaVariousClasses.txt",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithUnconventionalPackageNaming.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithBrokenPackageNaming.java",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespaceBetweenCode.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNestedAndNormalNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithoutNamespace.cs"
        );

        HtmlPage details = getWebPage(JavaScriptSupport.JS_DISABLED, result);

        verifyNamespaces(details,
                new PropertyRow("edu.hm.hafner.analysis._123.int.naming.structure", 1),
                new PropertyRow("SampleClassWithNamespace", 1),
                new PropertyRow("NestedNamespace", 1),
                new PropertyRow("SampleClassWithNestedAndNormalNamespace", 1),
                new PropertyRow("-", 6));
    }

    /**
     * Verifies that the output is correct if there are only packages (Java) in the expected HTML output.
     */
    @Test
    public void shouldShowPackagesForJavaOnly() {
        AnalysisResult result = buildProject(PACKAGE_WITH_FILES_JAVA + "eclipseForJavaVariousClasses.txt",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithUnconventionalPackageNaming.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithBrokenPackageNaming.java"
        );

        HtmlPage details = getWebPage(JavaScriptSupport.JS_DISABLED, result);

        verifyPackages(details,
                new PropertyRow("-", 5, 100),
                new PropertyRow("edu.hm.hafner.analysis._123.int.naming.structure", 1, 20));
    }

    /**
     * Verifies that the output is correct if there are only namespaces (C#) in the expected HTML output.
     */
    @Test
    public void shouldShowNamespacesForCSharpOnlyInTheHtmlOutput() {
        AnalysisResult result = buildProject(PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpVariousClasses.txt",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespaceBetweenCode.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNestedAndNormalNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithoutNamespace.cs");

        HtmlPage details = getWebPage(JavaScriptSupport.JS_DISABLED, result);

        verifyNamespaces(details,
                new PropertyRow("SampleClassWithNamespace", 1),
                new PropertyRow("NestedNamespace", 1),
                new PropertyRow("SampleClassWithNestedAndNormalNamespace", 1),
                new PropertyRow("-", 3));
    }

    private void verifyNamespaces(final HtmlPage details, final PropertyRow... packages) {
        PropertyTable propertyTable = new PropertyTable(details, "packageName");
        assertThat(propertyTable.getTitle()).isEqualTo("Namespaces");
        assertThat(propertyTable.getColumnName()).isEqualTo("Namespace");
        assertThat(propertyTable.getRows()).containsExactlyInAnyOrder(packages);

        // TODO: Click package links
    }

    private void verifyPackages(final HtmlPage details, final PropertyRow... packages) {
        PropertyTable propertyTable = new PropertyTable(details, "packageName");
        assertThat(propertyTable.getTitle()).isEqualTo("Packages");
        assertThat(propertyTable.getColumnName()).isEqualTo("Package");
        assertThat(propertyTable.getRows()).containsExactlyInAnyOrder(packages);

        // TODO: Click package links
    }

    /**
     * Verifies that the output of the HTML page is empty if the project is empty.
     */
    @Test
    public void shouldContainNoSpecificHtmlOutputForAnEmptyProject() {
        checkWebPageForExpectedEmptyResult(buildProject());
    }

    /**
     * Verifies that the output of the HTML page is empty if there is only one class without a package (Java).
     */
    @Test
    public void shouldContainNoHtmlOutputForNoPackageDefinedJava() {
        checkWebPageForExpectedEmptyResult(
                buildProject(PACKAGE_WITH_FILES_JAVA + "eclipseForJavaOneClassWithoutPackage.txt",
                        PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java"
                ));
    }

    /**
     * Verifies that the output of the HTML page is empty if there is only one class with a package (Java).
     */
    @Test
    public void shouldContainNoHtmlOutputForOnlyOnePackageDefinedJava() {
        checkWebPageForExpectedEmptyResult(
                buildProject(PACKAGE_WITH_FILES_JAVA + "eclipseForJavaOneClassWithPackage.txt",
                        PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java"
                ));
    }

    /**
     * Verifies that the output of the HTML page is empty if there is only one class without a namespace (C#).
     */
    @Test
    public void shouldContainNoHtmlOutputForNoNamespaceDefinedCSharp() {
        checkWebPageForExpectedEmptyResult(buildProject(
                PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpOneClassWithoutNamespace.txt",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithoutNamespace.cs"
        ));
    }

    /**
     * Verifies that the output of the HTML page is empty if there is only one class with a namespace (C#).
     */
    @Test
    public void shouldContainNoHtmlOutputForOnlyOneNamespaceDefinedCSharp() {
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
    public void shouldDetectVariousNamespacesAndPackagesForCombinedJavaAndCSharpFiles() {
        AnalysisResult result = buildProject(PACKAGE_WITH_FILES_JAVA + "eclipseForJavaVariousClasses.txt",
                PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpVariousClasses.txt",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithUnconventionalPackageNaming.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithBrokenPackageNaming.java",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespaceBetweenCode.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNestedAndNormalNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithoutNamespace.cs"
        );

        assertSoftly(softly -> {
            softly.assertThat(result.getIssues()).hasSize(10);
            softly.assertThat(result.getIssues().getPackages())
                    .containsExactly("edu.hm.hafner.analysis._123.int.naming.structure", "SampleClassWithNamespace",
                            "NestedNamespace", "SampleClassWithNestedAndNormalNamespace", "-");

            Map<String, Long> totalByPackageName = collectPackageNames(result);
            softly.assertThat(totalByPackageName).hasSize(5);
            softly.assertThat(totalByPackageName.get("edu.hm.hafner.analysis._123.int.naming.structure")).isEqualTo(1L);
            softly.assertThat(totalByPackageName.get("SampleClassWithNamespace")).isEqualTo(1L);
            softly.assertThat(totalByPackageName.get("NestedNamespace")).isEqualTo(1L);
            softly.assertThat(totalByPackageName.get("SampleClassWithNestedAndNormalNamespace")).isEqualTo(1L);
            softly.assertThat(totalByPackageName.get("-")).isEqualTo(6L);

            String logOutput = getConsoleLog(result);
            softly.assertThat(logOutput).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutput).contains(returnExpectedNumberOfResolvedPackageNames(10));
        });
    }

    /**
     * Verifies that if there are two builds with different parsers which set the package name at a different time are
     * handled correctly.
     */
    @Test
    public void shouldRunTwoIndependentBuildsWithTwoDifferentParsersAndCheckForCorrectPackageHandling() {
        FreeStyleProject jobWithFindBugsParser = createJobWithWorkspaceFile(
                PACKAGE_FILE_PATH + "various/findbugs-packages.xml");
        enableGenericWarnings(jobWithFindBugsParser, new FindBugs());
        AnalysisResult resultWithFindBugsParser = scheduleBuildAndAssertStatus(jobWithFindBugsParser, Result.SUCCESS);


        AnalysisResult resultWithEclipseParser = buildProject(
                PACKAGE_WITH_FILES_JAVA + "eclipseForJavaVariousClasses.txt",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithUnconventionalPackageNaming.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithBrokenPackageNaming.java"
        );

        assertSoftly(softly -> {
            softly.assertThat(resultWithFindBugsParser.getIssues()).hasSize(3);
            softly.assertThat(resultWithFindBugsParser.getIssues().getPackages())
                    .containsExactly("edu.hm.hafner.analysis.123",
                            "edu.hm.hafner.analysis._test",
                            "edu.hm.hafner.analysis.int.naming.structure");

            Map<String, Long> findBugsTotalByPackageName = collectPackageNames(resultWithFindBugsParser);
            softly.assertThat(findBugsTotalByPackageName).hasSize(3);
            softly.assertThat(findBugsTotalByPackageName.get("edu.hm.hafner.analysis.123")).isEqualTo(1L);
            softly.assertThat(findBugsTotalByPackageName.get("edu.hm.hafner.analysis._test")).isEqualTo(1L);
            softly.assertThat(findBugsTotalByPackageName.get("edu.hm.hafner.analysis.int.naming.structure")).isEqualTo(1L);

            String findBugsConsoleLog = getConsoleLog(resultWithFindBugsParser);
            softly.assertThat(findBugsConsoleLog).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(findBugsConsoleLog).contains("-> all affected files already have a valid package name");

            softly.assertThat(resultWithEclipseParser.getIssues()).hasSize(6);
            softly.assertThat(resultWithEclipseParser.getIssues().getPackages())
                    .containsExactly("edu.hm.hafner.analysis._123.int.naming.structure", "-");

            Map<String, Long> eclipseTotalByPackageName = collectPackageNames(resultWithEclipseParser);
            softly.assertThat(eclipseTotalByPackageName).hasSize(2);
            softly.assertThat(eclipseTotalByPackageName.get("edu.hm.hafner.analysis._123.int.naming.structure"))
                    .isEqualTo(1L);
            softly.assertThat(eclipseTotalByPackageName.get("-")).isEqualTo(5L);

            String eclipseConsoleLog = getConsoleLog(resultWithEclipseParser);
            softly.assertThat(eclipseConsoleLog).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(eclipseConsoleLog).contains(returnExpectedNumberOfResolvedPackageNames(6));
        });
    }

    /**
     * Verifies that various namespaces (C#) are handled correctly.
     */
    @Test
    public void shouldDetectVariousNamespacesForCSharpFiles() {
        AnalysisResult result = buildProject(
                PACKAGE_WITH_FILES_CSHARP + "eclipseForCSharpVariousClasses.txt",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNamespaceBetweenCode.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithNestedAndNormalNamespace.cs",
                PACKAGE_WITH_FILES_CSHARP + "SampleClassWithoutNamespace.cs");

        String logOutput = getConsoleLog(result);
        Map<String, Long> collect = collectPackageNames(result);

        assertSoftly(softly -> {
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
        });
    }

    /**
     * Verifies that various packages (Java) are handled correctly.
     */
    @Test
    public void shouldDetectVariousPackagesForJavaFiles() {
        AnalysisResult result = buildProject(
                PACKAGE_WITH_FILES_JAVA + "eclipseForJavaVariousClasses.txt",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithoutPackage.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithUnconventionalPackageNaming.java",
                PACKAGE_WITH_FILES_JAVA + "SampleClassWithBrokenPackageNaming.java"

        );

        assertSoftly(softly -> {
            softly.assertThat(result.getIssues()).hasSize(6);
            softly.assertThat(result.getIssues().getPackages()).containsExactly(
                    "edu.hm.hafner.analysis._123.int.naming.structure", "-");

            Map<String, Long> totalByPacakgeName = collectPackageNames(result);
            softly.assertThat(totalByPacakgeName).hasSize(2);
            softly.assertThat(totalByPacakgeName.get("edu.hm.hafner.analysis._123.int.naming.structure")).isEqualTo(1L);
            softly.assertThat(totalByPacakgeName.get("-")).isEqualTo(5L);

            String consoleLog = getConsoleLog(result);
            softly.assertThat(consoleLog).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(consoleLog).contains(returnExpectedNumberOfResolvedPackageNames(6));
        });
    }

    private String returnExpectedNumberOfResolvedPackageNames(final int expectedNumberOfResolvedPackageNames) {
        return "-> resolved package names of " + expectedNumberOfResolvedPackageNames + " affected files";
    }

    private WebClient createWebClient(final boolean javaScriptEnabled) {
        WebClient webClient = getJenkins().createWebClient();
        webClient.setJavaScriptEnabled(javaScriptEnabled);
        return webClient;
    }

    private Map<String, Long> collectPackageNames(final AnalysisResult result) {
        return result.getIssues().stream()
                .collect(Collectors.groupingBy(Issue::getPackageName, Collectors.counting()));
    }

    private void checkWebPageForExpectedEmptyResult(final AnalysisResult result) {
        try {
            WebClient webClient = createWebClient(false);
            WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            assertThat(getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE)).isEmpty();
        }
        catch (IOException | SAXException e) {
            throw new AssertionError(e);
        }
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
        enableGenericWarnings(project, new Eclipse());
        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
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
        FreeStyleProject job = createFreeStyleProject();
        copyMultipleFilesToWorkspaceWithSuffix(job, fileNames);
        return job;
    }

    /**
     * Skips .c and .java file names while renaming.
     *
     * @param fileName
     *         the filename
     */
    @Override
    protected String createWorkspaceFileName(final String fileName) {
        String[] genericFileNamesToKeep = new String[] {".cs", ".java"};

        List<Boolean> fileNamePrefixInList = Arrays.stream(genericFileNamesToKeep)
                .map(fileName::endsWith)
                .collect(Collectors.toList());
        if (fileNamePrefixInList.contains(true)) {
            return FilenameUtils.getName(fileName);
        }

        return super.createWorkspaceFileName(fileName);
    }
}
