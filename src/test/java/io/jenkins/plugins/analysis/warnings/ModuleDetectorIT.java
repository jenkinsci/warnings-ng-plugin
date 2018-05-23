package io.jenkins.plugins.analysis.warnings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
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
 * This class is an integration test for the classes associated with {@link edu.hm.hafner.analysis.ModuleDetector}.
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
public class ModuleDetectorIT extends IntegrationTest {

    private static final String BUILD_FILE_PATH = "/edu/hm/hafner/analysis/moduleandpackagedetectorfiles/";
    private static final String DEFAULT_ECLIPSE_TEST_FILE_PATH = "/eclipse_prepared-issues.txt";
    private static final String MAVEN_BUILD_FILE_LOCATION = "buildfiles/maven/";
    private static final String ANT_BUILD_FILE_LOCATION = "buildfiles/ant/";
    private static final String OSGI_BUILD_FILE_LOCATION = "buildfiles/osgi/";
    private static final String DEFAULT_ENTRY_PATH = "eclipseResult/";
    private static final String DEFAULT_TAB_TO_INVESTIGATE = "moduleName";
    private static final String DEFAULT_DEBUG_LOG_LINE = "Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)";
    private static final String EMPTY_MODULE_NAME = "";
    private static final short NO_MODULE_PATHS = 0;

    private static final boolean IS_MAVEN_PROJECT = false;

    /**
     * Verifies that the HTML output is correct if there are OSGI, Maven and Ant modules used within the build. This
     * test doesn't check for correct precedence in every possible case as this might fail.
     */
    @Test
    public void shouldShowModulesForVariousModulesDetectedForOsgiMavenAndAntInTheHtmlOutput()
            throws IOException, SAXException {

        String[] filesWithModuleConfiguration = new String[]{BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m1/build.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m1/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m2/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m3/META-INF/MANIFEST.MF"
        };

        String[] filesWithModuleConfigurationAndProperties = (String[]) ArrayUtils
                .add(filesWithModuleConfiguration, (BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"));

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, true,
                filesWithModuleConfigurationAndProperties
        );

        WebClient webClient = createWebClient(false);
        WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
        String webResponseContentAsString = webResponse.getContentAsString();

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlHeader());
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput(EMPTY_MODULE_NAME, 1));
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput("edu.hm.hafner.osgi.symbolicname \\(TestVendor\\)", 7));
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput("edu.hm.hafner.osgi.symbolicname", 1));
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(
                            returnPreparedHtmlOutput("Test-Bundle-Name", 1) + returnPreparedTotalHtmlOutput(10));

            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            List<String> moduleLinks = getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE);

            softly.assertThat(moduleLinks).hasSize(4);

            crawlAllSubPagesOfPackagesAndAssertTheyAreNotLinkingToFurtherModules(moduleLinks, result, webClient,
                    softly);
        }
    }

    /**
     * Verifies that the output is correct if there are only Maven modules in the expected HTML output.
     */
    @Test
    public void shouldShowModulesForVariousMavenModulesInTheHtmlOutput() throws IOException, SAXException {

        String[] filesWithModuleConfiguration = new String[]{BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml"};

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, false,
                filesWithModuleConfiguration);

        WebClient webClient = createWebClient(false);
        WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
        String webResponseContentAsString = webResponse.getContentAsString();

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(webResponseContentAsString).containsPattern(returnPreparedHtmlHeader());
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput("SubModuleOne", 1));
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput("SubModuleTwo", 1));
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(returnPreparedHtmlOutput("MainModule", 1)
                            + returnPreparedTotalHtmlOutput(3));

            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            List<String> moduleLinks = getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE);

            softly.assertThat(moduleLinks).hasSize(3);

            crawlAllSubPagesOfPackagesAndAssertTheyAreNotLinkingToFurtherModules(moduleLinks, result, webClient,
                    softly);
        }
    }

    /**
     * Verifies that the output is correct if there are only Ant modules in the expected HTML output.
     */
    @Test
    public void shouldShowModulesForVariousAntModulesInTheHtmlOutput() throws IOException, SAXException {

        String[] filesWithModuleConfiguration = new String[]{BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m1/build.xml"};

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, false,
                filesWithModuleConfiguration);

        WebClient webClient = createWebClient(false);
        WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
        String webResponseContentAsString = webResponse.getContentAsString();

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(webResponseContentAsString).containsPattern(returnPreparedHtmlHeader());
            softly.assertThat(webResponseContentAsString).containsPattern(
                    returnPreparedHtmlOutput("SecondTestModule", 1));
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(returnPreparedHtmlOutput("SecondTestModule", 1) +
                            returnPreparedTotalHtmlOutput(2));

            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            List<String> moduleLinks = getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE);

            softly.assertThat(moduleLinks).hasSize(2);

            crawlAllSubPagesOfPackagesAndAssertTheyAreNotLinkingToFurtherModules(moduleLinks, result, webClient,
                    softly);
        }
    }

    /**
     * Verifies that the output is correct if there are only OSGI modules in the expected HTML output.
     */
    @Test
    public void shouldShowModulesForVariousOsgiModulesInTheHtmlOutput() throws IOException, SAXException {

        String[] filesWithModuleConfiguration = new String[]{
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m1/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m2/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m3/META-INF/MANIFEST.MF"
        };

        String[] filesWithModuleConfigurationAndProperties = (String[]) ArrayUtils
                .add(filesWithModuleConfiguration, (BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"));

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, false,
                filesWithModuleConfigurationAndProperties);

        WebClient webClient = createWebClient(false);
        WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
        String webResponseContentAsString = webResponse.getContentAsString();

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(webResponseContentAsString).containsPattern(returnPreparedHtmlHeader());
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(returnPreparedHtmlOutput("edu.hm.hafner.osgi.symbolicname \\(TestVendor\\)", 2));
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(returnPreparedHtmlOutput("edu.hm.hafner.osgi.symbolicname", 1));
            softly.assertThat(webResponseContentAsString)
                    .containsPattern(returnPreparedHtmlOutput("Test-Bundle-Name", 1) + returnPreparedTotalHtmlOutput(4)
                    );

            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            List<String> moduleLinks = getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE);

            softly.assertThat(moduleLinks).hasSize(3);

            crawlAllSubPagesOfPackagesAndAssertTheyAreNotLinkingToFurtherModules(moduleLinks, result, webClient,
                    softly);
        }
    }

    /**
     * Verifies that the output of the HTML page is empty if the project is empty.
     */
    @Test
    public void shouldContainNoSpecificHtmlOutputForAnEmptyProject() throws IOException, SAXException {
        checkWebPageForExpectedEmptyResult(buildProjectWithFilesAndReturnResult(NO_MODULE_PATHS, false));
    }

    /**
     * Verifies that the output of the HTML page is empty if only one module is set by Maven.
     */
    @Test
    public void shouldContainNoSpecificHtmlOutputForASingleModuleMavenProject() throws IOException, SAXException {
        String[] filesWithModuleConfiguration = new String[]{BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml"};
        checkWebPageForExpectedEmptyResult(
                buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, false,
                        filesWithModuleConfiguration));
    }

    /**
     * Verifies that the output of the HTML page is empty if only one module is set by Ant.
     */
    @Test
    public void shouldContainNoHtmlOutputForASingleModuleAntProject() throws IOException, SAXException {
        String[] filesWithModuleConfiguration = new String[]{BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml"};
        checkWebPageForExpectedEmptyResult(
                buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, false,
                        filesWithModuleConfiguration));
    }

    /**
     * Verifies that the output of the HTML page is empty if only one module is set by Ant.
     */
    @Test
    public void shouldContainNoHtmlOutputForASingleModuleOsgiProject() throws IOException, SAXException {
        String[] filesWithModuleConfiguration = new String[]{
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/Manifest.MF"};
        checkWebPageForExpectedEmptyResult(
                buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, false,
                        filesWithModuleConfiguration));
    }

    /**
     * Verifies that if there are different usages of Maven, Ant and OSGI, OSGI should have precedence. This test
     * doesn't check for correct precedence in every possible case as this might fail.
     */
    @Test
    public void shouldRunMavenAntAndOsgiAndCheckCorrectExecutionSequence() throws IOException {

        String[] filesWithModuleConfiguration = new String[]{BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m1/build.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m1/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m2/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m3/META-INF/MANIFEST.MF"
        };

        String[] filesWithModuleConfigurationAndProperties = (String[]) ArrayUtils
                .add(filesWithModuleConfiguration, (BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"));

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, true,
                filesWithModuleConfigurationAndProperties);

        String logOutput = FileUtils.readFileToString(result.getOwner().getLogFile(), StandardCharsets.UTF_8);
        Map<String, Long> collect = collectModuleNames(result);

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(result.getIssues()).hasSize(10);
            softly.assertThat(result.getIssues().getModules())
                    .containsExactly(
                            EMPTY_MODULE_NAME,
                            "edu.hm.hafner.osgi.symbolicname (TestVendor)",
                            "edu.hm.hafner.osgi.symbolicname",
                            "Test-Bundle-Name");
            softly.assertThat(collect).hasSize(4);
            softly.assertThat(collect.get(EMPTY_MODULE_NAME)).isEqualTo(1L);
            softly.assertThat(collect.get("Test-Bundle-Name")).isEqualTo(1L);
            softly.assertThat(collect.get("edu.hm.hafner.osgi.symbolicname")).isEqualTo(1L);
            softly.assertThat(collect.get("edu.hm.hafner.osgi.symbolicname (TestVendor)")).isEqualTo(7L);
            softly.assertThat(logOutput).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutput).contains(returnExpectedNumberOfResolvedModuleNames(10));
        }
    }

    /**
     * Verifies that if there are two builds with different parsers which set the module name at a different time are
     * handled correctly.
     */
    @Test
    public void shouldRunTwoIndependentBuildsWithTwoDifferentParsersAndCheckForCorrectModuleHandling()
            throws IOException {

        FreeStyleProject project = createJobWithWorkspaceFile(BUILD_FILE_PATH + "various/findbugs-modules.xml");
        enableWarnings(project, new FindBugs());
        AnalysisResult resultFindBugs = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        String logOutputFindBugs = FileUtils.readFileToString(resultFindBugs.getOwner().getLogFile(),
                StandardCharsets.UTF_8);
        Map<String, Long> collectFindBugs = collectModuleNames(resultFindBugs);

        AnalysisResult resultEclipse = buildProjectWithFilesAndReturnResult(NO_MODULE_PATHS, false);

        String logOutputEclipse = FileUtils.readFileToString(resultEclipse.getOwner().getLogFile(),
                StandardCharsets.UTF_8);
        Map<String, Long> collectEclipse = collectModuleNames(resultEclipse);

        String expectedLogOutputDefaultLine = "All issues already have a valid module name";

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(resultFindBugs.getIssues()).hasSize(1);
            softly.assertThat(resultFindBugs.getIssues().getModules()).containsExactly("sampleFindBugsParserModule");
            softly.assertThat(collectFindBugs).hasSize(1);
            softly.assertThat(collectFindBugs.get("sampleFindBugsParserModule")).isEqualTo(1L);
            softly.assertThat(logOutputFindBugs).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutputFindBugs).contains(expectedLogOutputDefaultLine);

            softly.assertThat(resultEclipse.getIssues()).isEmpty();
            softly.assertThat(resultEclipse.getIssues().getModules()).isEmpty();
            softly.assertThat(collectEclipse).isEmpty();
            softly.assertThat(logOutputEclipse).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutputEclipse).contains(expectedLogOutputDefaultLine);
        }
    }

    /**
     * Verifies that various Maven .pom files are handled correctly.
     */
    @Test
    public void shouldVerifyTheModuleDetectionBehaviorForVariousMavenPomFiles() throws IOException {

        String[] filesWithModuleConfiguration = new String[]{BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m3/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m4/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m5/pom.xml"};

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, true,
                filesWithModuleConfiguration
        );

        String logOutput = FileUtils.readFileToString(result.getOwner().getLogFile(), StandardCharsets.UTF_8);
        Map<String, Long> collect = collectModuleNames(result);

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(result.getIssues()).hasSize(7);
            softly.assertThat(result.getIssues().getModules())
                    .containsExactly(
                            "SubModuleOne",
                            EMPTY_MODULE_NAME,
                            "module.read.from.artifact.id",
                            "SubModuleTwo",
                            "MainModule");
            softly.assertThat(collect).hasSize(5);
            softly.assertThat(collect.get(EMPTY_MODULE_NAME)).isEqualTo(1L);
            softly.assertThat(collect.get("SubModuleOne")).isEqualTo(1L);
            softly.assertThat(collect.get("module.read.from.artifact.id")).isEqualTo(1L);
            softly.assertThat(collect.get("SubModuleTwo")).isEqualTo(1L);
            softly.assertThat(collect.get("MainModule")).isEqualTo(3L);
            softly.assertThat(logOutput).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutput).contains(returnExpectedNumberOfResolvedModuleNames(7));
        }
    }

    /**
     * Verifies that various Ant .build files are handled correctly.
     */
    @Test
    public void shouldVerifyTheModuleDetectionBehaviorForVariousAntBuildFiles() throws IOException {

        String[] filesWithModuleConfiguration = new String[]{
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m1/build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m2/build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m3/build.xml"
        };

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length,
                true, filesWithModuleConfiguration);

        String logOutput = FileUtils.readFileToString(result.getOwner().getLogFile(), StandardCharsets.UTF_8);
        Map<String, Long> collect = collectModuleNames(result);

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(result.getIssues()).hasSize(5);
            softly.assertThat(result.getIssues().getModules())
                    .containsExactly(EMPTY_MODULE_NAME,
                            "SecondTestModule",
                            "TestModule");
            softly.assertThat(collect).hasSize(3);
            softly.assertThat(collect.get(EMPTY_MODULE_NAME)).isEqualTo(1L);
            softly.assertThat(collect.get("SecondTestModule")).isEqualTo(1L);
            softly.assertThat(collect.get("TestModule")).isEqualTo(3L);
            softly.assertThat(logOutput).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutput).contains(returnExpectedNumberOfResolvedModuleNames(5));
        }
    }

    /**
     * Verifies that various OSGI .MF files are handled correctly.
     */
    @Test
    public void shouldVerifyTheModuleDetectionBehaviorForVariousOsgiMfFiles() throws IOException {

        String[] filesWithModuleConfiguration = new String[]{
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m1/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m2/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m3/META-INF/MANIFEST.MF"};

        String[] filesWithModuleConfigurationAndProperties = (String[]) ArrayUtils
                .add(filesWithModuleConfiguration, (BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"));

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, true,
                filesWithModuleConfigurationAndProperties);

        Map<String, Long> collect = collectModuleNames(result);
        String logOutput = FileUtils.readFileToString(result.getOwner().getLogFile(),
                StandardCharsets.UTF_8);

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(result.getIssues()).hasSize(5);
            softly.assertThat(result.getIssues().getModules())
                    .containsExactly(EMPTY_MODULE_NAME,
                            "edu.hm.hafner.osgi.symbolicname (TestVendor)",
                            "edu.hm.hafner.osgi.symbolicname",
                            "Test-Bundle-Name");
            softly.assertThat(collect).hasSize(4);
            softly.assertThat(collect.get(EMPTY_MODULE_NAME)).isEqualTo(1L);
            softly.assertThat(collect.get("Test-Bundle-Name")).isEqualTo(1L);
            softly.assertThat(collect.get("edu.hm.hafner.osgi.symbolicname (TestVendor)")).isEqualTo(2L);
            softly.assertThat(collect.get("edu.hm.hafner.osgi.symbolicname")).isEqualTo(1L);
            softly.assertThat(logOutput).contains(DEFAULT_DEBUG_LOG_LINE);
            softly.assertThat(logOutput).contains(returnExpectedNumberOfResolvedModuleNames(5));
        }
    }

    private String returnExpectedNumberOfResolvedModuleNames(final int expectedNumberOfResolvedModuleNames) {
        return "Resolved module names for " + expectedNumberOfResolvedModuleNames + " issues";
    }

    private String returnPreparedHtmlHeader() {
        return "<a data-toggle=\"tab\" role=\"tab\" href=\"#moduleNameContent\" class=\"nav-link\">Modules</a>.*<th>Module</th><th>Total</th><th class=\"no-sort\">Distribution</th></tr></thead><tbody><tr><td>";
    }

    private String returnPreparedHtmlOutput(final String moduleName,
            final int numberOfModules) {
        return "<a href=\"moduleName..*\">" + moduleName + "</a></td><td>" + numberOfModules
                + "</td><td><div><span style=\"width:.*%\" class=\"bar-graph priority-normal priority-normal--hover\">.</span></div></td></tr>";
    }

    private String returnPreparedTotalHtmlOutput(final int numberOfTotalPackagesOrNamespaces) {
        return "<tfoot><tr><td>Total</td><td>" + numberOfTotalPackagesOrNamespaces + "</td><td>";
    }

    private void writeDynamicFile(final FreeStyleProject project, final int modulePaths,
            final boolean appendNonExistingFile, final String path)
            throws IOException {
        for (int i = 1; i <= modulePaths; i++) {
            String sampleClassDummyName = j.jenkins.getWorkspaceFor(project) + "/m" + i + "/SampleClass-issues.txt";
            PrintWriter writer = new PrintWriter(
                    new FileOutputStream((j.jenkins.getWorkspaceFor(project) + path), true));
            writer.println("[javac] " + i + ". WARNING in " + sampleClassDummyName + " (at line 42)");
            writer.println("[javac] Sample Message");
            writer.println("[javac] ^^^^^^^^^^^^^^^^^^");
            writer.println("[javac] Sample Message" + i);
            writer.close();
        }

        if (appendNonExistingFile) {
            PrintWriter writer = new PrintWriter(
                    new FileOutputStream((j.jenkins.getWorkspaceFor(project) + path), true));
            writer.println("[javac] NOT_EXISTING X. WARNING in /NOT_EXISTING/PATH/NOT_EXISTING_FILE (at line 42)");
            writer.println("[javac] Sample Message");
            writer.println("[javac] ^^^^^^^^^^^^^^^^^^");
            writer.println("[javac] Sample Message");
            writer.close();
        }
    }

    private WebClient createWebClient(final boolean javaScriptEnabled) {
        WebClient webClient = j.createWebClient();
        webClient.setJavaScriptEnabled(javaScriptEnabled);
        return webClient;
    }

    private Map<String, Long> collectModuleNames(final AnalysisResult result) {
        return result.getIssues().stream()
                .collect(Collectors.groupingBy(Issue::getModuleName,
                        Collectors.counting()));
    }

    private void crawlAllSubPagesOfPackagesAndAssertTheyAreNotLinkingToFurtherModules(final List<String> packageLinks,
            final AnalysisResult result, final WebClient webClient, final AutoCloseableSoftAssertions softly)
            throws IOException, SAXException {
        for (String link : packageLinks) {
            WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH + link).getWebResponse();
            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            softly.assertThat(getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE)).hasSize(0);
        }
    }

    private void checkWebPageForExpectedEmptyResult(final AnalysisResult result) throws IOException, SAXException {
        WebClient webClient = createWebClient(false);
        WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
        HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
        List<String> packageLinks = getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE);

        assertThat(packageLinks).isEmpty();
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

    private AnalysisResult buildProjectWithFilesAndReturnResult(final int numberOfExpectedModules,
            final boolean appendNonExistingFile, final String... files) throws IOException {
        FreeStyleProject project = buildProject(files);
        writeDynamicFile(project, numberOfExpectedModules, appendNonExistingFile,
                DEFAULT_ECLIPSE_TEST_FILE_PATH);
        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }

    private FreeStyleProject buildProject(final String... files) {
        FreeStyleProject project = createJobWithWorkspaceFile(files);
        enableWarnings(project, new Eclipse());

        if (IS_MAVEN_PROJECT) {
            project.getBuildersList().add(new Maven("package", "MavenTestBuild",
                    "pom.xml"
                    , "", "", true));
        }

        return project;
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
