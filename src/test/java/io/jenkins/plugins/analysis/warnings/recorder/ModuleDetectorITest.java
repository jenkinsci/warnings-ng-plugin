package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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

import edu.hm.hafner.analysis.ModuleDetector;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.PropertyTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.PropertyTable.PropertyRow;
import static org.assertj.core.api.Assertions.*;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

/**
 * Integration test for the {@link ModuleDetector}.
 *
 * <p>
 * These tests work on several pom.xml, build.xml and MANIFEST.MF files that will be copied to the workspace for each
 * test. The following files are used:
 *
 * <b>Maven:</b>
 *
 * <dl>
 * <dt>pom.xml<dt/>
 * <dd>a default pom.xml with a valid name tag<dd/>
 * <dt>m1/pom.xml<dt/>
 * <dd>a default pom.xml with a valid name tag which could be used to detect additional modules in addition to
 * the previous mentioned pom.xml<dd/>
 * <dt>m2/pom.xml<dt/>
 * <dd>a default pom.xml with a valid name tag which could be used to detect additional modules in addition to
 * the previous mentioned pom.xml<dd/>
 * <dt>m3/pom.xml<dt/>
 * <dd>a broken XML-structure breaks the correct parsing of this file<dd/>
 * <dt>m4/pom.xml<dt/>
 * <dd>a pom.xml with an artifactId tag and without a name tag<dd/>
 * <dt>m5/pom.xml<dt/>
 * <dd>a pom.xml without an artifactId tag and without a name tag<dd/>
 * </dl>
 * <p>
 *
 * <b>Ant:</b>
 *
 * <dl>
 * <dt>build.xml<dt/>
 * <dd>a default build.xml with a valid name tag<dd/>
 * <dt>m1/build.xml<dt/>
 * <dd>a default build.xml with a valid name tag which could be used to detect additional modules in addition
 * * to the previous mentioned build.xml<dd/>
 * <dt>m2/build.xml<dt/>
 * <dd>a broken XML-structure breaks the correct parsing of this file<dd/>
 * <dt>m3/build.xml<dt/>
 * <dd>a build file without the name tag<dd/>
 * </dl>
 *
 * <b>OSGI:</b>
 *
 * <dl>
 * <dt>META-INF/MANIFEST.MF<dt/>
 * <dd>a default MANIFEST.MF with a set Bundle-SymbolicName and a set Bundle-Vendor<dd/>
 * <dt>m1/META-INF/MANIFEST.MF<dt/>
 * <dd> a MANIFEST.MF with a wildcard Bundle-Name, a set Bundle-SymbolicName and a wildcard<dd/>
 * <dt>m2/META-INF/MANIFEST.MF<dt/>
 * <dd>a MANIFEST.MF with a set Bundle-Name and a wildcard Bundle-Vendor<dd/>
 * <dt>m3/META-INF/MANIFEST.MF<dt/>
 * <dd>an empty MANIFEST.MF<dd/>
 * <dt>plugin.properties<dt/>
 * <dd>a default plugin.properties file<dd/>
 * </dl>
 * <p>
 * All tests work the same way: first of all a set of module files will be copied to the workspace. Each module file
 * will be copied to a separate folder, the first module file is the top-level module. Into each of the modules, a
 * source code file will be placed. Finally,  Eclipse parser log file will be generated, that has exactly one warning
 * for each file.
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
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m1/build.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m1/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m2/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m3/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"};

        AnalysisResult result = createResult(
                workspaceFiles.length - 1, 
                true, 
                workspaceFiles);

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
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml"};

        AnalysisResult result = createResult(
                workspaceFiles.length, 
                false,
                workspaceFiles);

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
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m1/build.xml"};

        AnalysisResult result = createResult(workspaceFiles.length, false,
                workspaceFiles);

        verifyModules(result,
                new PropertyRow("TestModule", 1, 100),
                new PropertyRow("SecondTestModule", 1, 100));
    }

    /**
     * Verifies that the output is correct if there are only OSGI modules in the expected HTML output.
     */
    @Test
    public void shouldShowModulesForVariousOsgiModulesInTheHtmlOutput() throws IOException, SAXException {
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m1/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m2/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m3/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"};

        AnalysisResult result = createResult(
                workspaceFiles.length - 1, 
                false,
                workspaceFiles);

        verifyModules(result,
                new PropertyRow("edu.hm.hafner.osgi.symbolicname", 1),
                new PropertyRow("edu.hm.hafner.osgi.symbolicname (TestVendor)", 2),
                new PropertyRow("Test-Bundle-Name", 1));
    }

    /**
     * Verifies that if there are different usages of Maven, Ant and OSGI, OSGI should have precedence. This test
     * doesn't check for correct precedence in every possible case as this might fail.
     */
    @Test
    public void shouldRunMavenAntAndOsgiAndCheckCorrectExecutionSequence() throws IOException {
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m1/build.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m1/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m2/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m3/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"};

        AnalysisResult result = createResult(
                workspaceFiles.length - 1,
                true,
                workspaceFiles);

        verifyModules(result,
                new PropertyRow(EMPTY_MODULE_NAME, 1),
                new PropertyRow("edu.hm.hafner.osgi.symbolicname", 1),
                new PropertyRow("edu.hm.hafner.osgi.symbolicname (TestVendor)", 7),
                new PropertyRow("Test-Bundle-Name", 1));
    }

    /**
     * Verifies that various Maven .pom files are handled correctly.
     */
    @Test
    public void shouldVerifyTheModuleDetectionBehaviorForVariousMavenPomFiles() throws IOException {
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m3/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m4/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m5/pom.xml"};

        AnalysisResult result = createResult(
                workspaceFiles.length,
                true,
                workspaceFiles);

        verifyModules(result,
                new PropertyRow(EMPTY_MODULE_NAME, 1),
                new PropertyRow("SubModuleOne", 1),
                new PropertyRow("module.read.from.artifact.id", 1),
                new PropertyRow("MainModule", 3),
                new PropertyRow("SubModuleTwo", 1));
    }

    /**
     * Verifies that various Ant .build files are handled correctly.
     */
    @Test
    public void shouldVerifyTheModuleDetectionBehaviorForVariousAntBuildFiles() throws IOException {
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m1/build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m2/build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m3/build.xml"
        };

        AnalysisResult result = createResult(
                workspaceFiles.length,
                true,
                workspaceFiles);

        verifyModules(result,
                new PropertyRow(EMPTY_MODULE_NAME, 1),
                new PropertyRow("SecondTestModule", 1),
                new PropertyRow("TestModule", 3));
    }

    /**
     * Verifies that various OSGI .MF files are handled correctly.
     */
    @Test
    public void shouldVerifyTheModuleDetectionBehaviorForVariousOsgiMfFiles() throws IOException {
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m1/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m2/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m3/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"};

        AnalysisResult result = createResult(
                workspaceFiles.length - 1,
                true,
                workspaceFiles);

        verifyModules(result,
                new PropertyRow(EMPTY_MODULE_NAME, 1),
                new PropertyRow("edu.hm.hafner.osgi.symbolicname (TestVendor)", 2),
                new PropertyRow("edu.hm.hafner.osgi.symbolicname", 1),
                new PropertyRow("Test-Bundle-Name", 1));
    }

    /**
     * Verifies that the output of the HTML page is empty if the project is empty.
     */
    @Test
    public void shouldContainNoSpecificHtmlOutputForAnEmptyProject() {
        checkWebPageForExpectedEmptyResult(createResult(NO_MODULE_PATHS, false));
    }

    /**
     * Verifies that the output of the HTML page is empty if only one module is set by Maven.
     */
    @Test
    public void shouldContainNoSpecificHtmlOutputForASingleModuleMavenProject() {
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml"};
        verifyThatModulesTabIsNotShownForSingleModule(workspaceFiles);
    }

    /**
     * Verifies that the output of the HTML page is empty if only one module is set by Ant.
     */
    @Test
    public void shouldContainNoHtmlOutputForASingleModuleAntProject() {
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml"};
        verifyThatModulesTabIsNotShownForSingleModule(workspaceFiles);
    }

    /**
     * Verifies that the output of the HTML page is empty if only one module is set by Ant.
     */
    @Test
    public void shouldContainNoHtmlOutputForASingleModuleOsgiProject() {
        String[] workspaceFiles = new String[] {
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF"};
        verifyThatModulesTabIsNotShownForSingleModule(workspaceFiles);
    }

    private void verifyThatModulesTabIsNotShownForSingleModule(final String[] workspaceFiles) {
        checkWebPageForExpectedEmptyResult(
                createResult(workspaceFiles.length, false, workspaceFiles));
    }

    private void verifyModules(final AnalysisResult result, final PropertyRow... modules) {
        HtmlPage details = getWebPage(result);
        PropertyTable propertyTable = new PropertyTable(details, "moduleName");
        assertThat(propertyTable.getTitle()).isEqualTo("Modules");
        assertThat(propertyTable.getColumnName()).isEqualTo("Module");
        assertThat(propertyTable.getRows()).containsExactlyInAnyOrder(modules);

        verifyConsoleLog(result, Stream.of(modules).mapToInt(PropertyRow::size).sum());

        // TODO: Click module links
    }

    private void verifyConsoleLog(final AnalysisResult result, final int modulesSize) {
        String logOutput = getConsoleLog(result);
        assertThat(logOutput).contains(DEFAULT_DEBUG_LOG_LINE);
        assertThat(logOutput).contains(getModulesResolvedMessage(modulesSize));
    }

    private String getModulesResolvedMessage(final int modulesSize) {
        return String.format("-> resolved module names for %s issues", modulesSize);
    }

    private void writeDynamicFile(final FreeStyleProject project, final int modulePaths,
            final boolean appendNonExistingFile, final String file) {
        try {
            for (int i = 1; i <= modulePaths; i++) {
                String directory = getJenkins().jenkins.getWorkspaceFor(project) + "/m" + i;
                String sampleClassDummyName = directory + "/ClassWithWarnings.java";
                PrintWriter writer = new PrintWriter(
                        new FileOutputStream(getJenkins().jenkins.getWorkspaceFor(project) + file, true));
                writer.println("[javac] " + i + ". WARNING in " + sampleClassDummyName + " (at line 42)");
                writer.println("[javac] \tSample Message");
                writer.println("[javac] \t^^^^^^^^^^^^^^^^^^");
                writer.println("[javac] Sample Message" + i);
                writer.close();
                Path path = Paths.get(sampleClassDummyName);
                if (!Files.exists(path)) {
                    Path dirPath = Paths.get(directory);
                    Files.createDirectories(dirPath);
                    Files.createFile(path);
                }
            }

            if (appendNonExistingFile) {
                PrintWriter writer = new PrintWriter(
                        new FileOutputStream(getJenkins().jenkins.getWorkspaceFor(project) + file, true));
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

    private void checkWebPageForExpectedEmptyResult(final AnalysisResult result) {
        try {
            WebClient webClient = createWebClient(false);
            WebResponse webResponse = webClient.getPage(result.getOwner(), DEFAULT_ENTRY_PATH).getWebResponse();
            HtmlPage htmlPage = HTMLParser.parseHtml(webResponse, webClient.getCurrentWindow());
            List<String> packageLinks = getLinksWithGivenTargetName(htmlPage, DEFAULT_TAB_TO_INVESTIGATE);

            assertThat(packageLinks).isEmpty();
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

    private AnalysisResult createResult(final int numberOfExpectedModules,
            final boolean appendNonExistingFile, final String... files) {
        FreeStyleProject project = buildProject(files);
        writeDynamicFile(project, numberOfExpectedModules, appendNonExistingFile, DEFAULT_ECLIPSE_TEST_FILE_PATH);
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

        String[] moduleFileNamesToKeep = new String[] {
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
