package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
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

import edu.hm.hafner.analysis.Issue;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.PropertyTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.PropertyTable.PropertyRow;
import static org.assertj.core.api.Assertions.*;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

/**
 * Integration test for the classes associated with {@link edu.hm.hafner.analysis.ModuleDetector}.
 * <p>
 * These are the module files that are necessary for this integration test:
 * <b>Maven:</b>
 * pom.xml a default pom.xml with a valid name tag
 * <p>
 * m1/pom.xml a default pom.xml with a valid name tag which could be used to detect additional modules in addition to
 * the previous mentioned pom.xml
 * <p>
 * m2/pom.xml a default pom.xml with a valid name tag which could be used to detect additional modules in addition to
 * the first mentioned pom.xml
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
 * m1/build.xml a default build.xml with a valid name tag which could be used to detect additional modules in addition
 * to the previous mentioned build.xml
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
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
public class ModuleDetectorITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String BUILD_FILE_PATH = "detectors/";
    private static final String DEFAULT_ECLIPSE_TEST_FILE_PATH = "/eclipse_prepared-issues.txt";
    private static final String MAVEN_BUILD_FILE_LOCATION = "buildfiles/maven/";
    private static final String ANT_BUILD_FILE_LOCATION = "buildfiles/ant/";
    private static final String OSGI_BUILD_FILE_LOCATION = "buildfiles/osgi/";
    private static final String DEFAULT_ENTRY_PATH = "eclipse/";
    private static final String DEFAULT_TAB_TO_INVESTIGATE = "moduleName";
    private static final String DEFAULT_DEBUG_LOG_LINE = "Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)";
    private static final String EMPTY_MODULE_NAME = "";
    private static final short NO_MODULE_PATHS = 0;

    /**
     * Verifies that the HTML output is correct if there are OSGI, Maven and Ant modules used within the build. This
     * test doesn't check for correct precedence in every possible case as this might fail.
     */
    @Test
    public void shouldShowModulesForVariousModulesDetectedForOsgiMavenAndAntInTheHtmlOutput() {
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

        String[] filesWithModuleConfigurationAndProperties = ArrayUtils
                .add(filesWithModuleConfiguration, BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties");

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, true,
                filesWithModuleConfigurationAndProperties
        );

        verifyModules(result,
                new PropertyRow(EMPTY_MODULE_NAME, 1),
                new PropertyRow("edu.hm.hafner.osgi.symbolicname", 1),
                new PropertyRow("edu.hm.hafner.osgi.symbolicname (TestVendor)", 7),
                new PropertyRow("Test-Bundle-Name", 1));
    }

    /**
     * Verifies that the output is correct if there are only Maven modules in the expected HTML output.
     */
    @Test
    public void shouldShowModulesForVariousMavenModulesInTheHtmlOutput() {
        String[] filesWithModuleConfiguration = new String[]{BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml"};

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, false,
                filesWithModuleConfiguration);

        verifyModules(result,
                new PropertyRow("MainModule", 1, 100),
                new PropertyRow("SubModuleOne", 1, 100),
                new PropertyRow("SubModuleTwo", 1, 100));
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

        verifyModules(result,
                new PropertyRow("TestModule", 1, 100),
                new PropertyRow("SecondTestModule", 1, 100));
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

        String[] filesWithModuleConfigurationAndProperties = ArrayUtils
                .add(filesWithModuleConfiguration, (BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"));

        AnalysisResult result = buildProjectWithFilesAndReturnResult(filesWithModuleConfiguration.length, false,
                filesWithModuleConfigurationAndProperties);

        verifyModules(result,
                new PropertyRow("edu.hm.hafner.osgi.symbolicname", 1),
                new PropertyRow("edu.hm.hafner.osgi.symbolicname (TestVendor)", 2),
                new PropertyRow("Test-Bundle-Name", 1));
    }

    private void verifyModules(final AnalysisResult result, final PropertyRow... modules) {
        HtmlPage details = getWebPage(result);
        PropertyTable propertyTable = new PropertyTable(details, "moduleName");
        assertThat(propertyTable.getTitle()).isEqualTo("Modules");
        assertThat(propertyTable.getColumnName()).isEqualTo("Module");
        assertThat(propertyTable.getRows()).containsExactlyInAnyOrder(modules);

        // TODO: Click module links
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
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF"};
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

        String[] filesWithModuleConfigurationAndProperties = ArrayUtils
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

        String[] filesWithModuleConfigurationAndProperties = ArrayUtils
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
        return "-> resolved module names for " + expectedNumberOfResolvedModuleNames + " issues";
    }

    private void writeDynamicFile(final FreeStyleProject project, final int modulePaths,
            final boolean appendNonExistingFile, final String path) {
        try {
            for (int i = 1; i <= modulePaths; i++) {
                String sampleClassDummyName = getJenkins().jenkins.getWorkspaceFor(project) + "/m" + i + "/SampleClass-issues.txt";
                PrintWriter writer = new PrintWriter(
                        new FileOutputStream((getJenkins().jenkins.getWorkspaceFor(project) + path), true));
                writer.println("[javac] " + i + ". WARNING in " + sampleClassDummyName + " (at line 42)");
                writer.println("[javac] \tSample Message");
                writer.println("[javac] \t^^^^^^^^^^^^^^^^^^");
                writer.println("[javac] Sample Message" + i);
                writer.close();
            }

            if (appendNonExistingFile) {
                PrintWriter writer = new PrintWriter(
                        new FileOutputStream((getJenkins().jenkins.getWorkspaceFor(project) + path), true));
                writer.println("[javac] NOT_EXISTING X. WARNING in /NOT_EXISTING/PATH/NOT_EXISTING_FILE (at line 42)");
                writer.println("[javac] \tSample Message");
                writer.println("[javac] \t^^^^^^^^^^^^^^^^^^");
                writer.println("[javac] Sample Message");
                writer.close();
            }
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private WebClient createWebClient(final boolean javaScriptEnabled) {
        WebClient webClient = getJenkins().createWebClient();
        webClient.setJavaScriptEnabled(javaScriptEnabled);
        return webClient;
    }

    private Map<String, Long> collectModuleNames(final AnalysisResult result) {
        return result.getIssues().stream()
                .collect(Collectors.groupingBy(Issue::getModuleName,
                        Collectors.counting()));
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
            final boolean appendNonExistingFile, final String... files) {
        FreeStyleProject project = buildProject(files);
        writeDynamicFile(project, numberOfExpectedModules, appendNonExistingFile,
                DEFAULT_ECLIPSE_TEST_FILE_PATH);
        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }

    private FreeStyleProject buildProject(final String... files) {
        FreeStyleProject project = createJobWithWorkspaceFile(files);
        enableWarnings(project, new Eclipse());
        return project;
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
     * Creates a pre-defined filename for a workspace file.
     *
     * @param fileName
     *         the filename
     */
    @Override
    protected String createWorkspaceFileName(final String fileName) {
        String modifiedFileName = String.format("%s-issues.txt", FilenameUtils.getBaseName(fileName));

        String[] moduleFileNamesToKeep = new String[]{
                "m1/pom.xml", "m2/pom.xml", "m3/pom.xml", "m4/pom.xml", "m5/pom.xml", "pom.xml",
                "m1/build.xml", "m2/build.xml", "m3/build.xml", "build.xml",
                "m1/META-INF/MANIFEST.MF", "m2/META-INF/MANIFEST.MF", "m3/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", "plugin.properties"
        };

        return Arrays.stream(moduleFileNamesToKeep)
                .filter(fileName::endsWith)
                .findFirst()
                .orElse(modifiedFileName);
    }
}
