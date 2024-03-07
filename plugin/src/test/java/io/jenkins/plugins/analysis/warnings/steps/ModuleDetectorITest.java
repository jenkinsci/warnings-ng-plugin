package io.jenkins.plugins.analysis.warnings.steps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.ModuleDetector;

import hudson.FilePath;
import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Eclipse;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for the {@link ModuleDetector}.
 *
 * <p>
 * These tests work on several pom.xml, build.xml and MANIFEST.MF files that will be copied to the workspace for each
 * test. The following files are used:
 * </p>
 *
 * <b>Maven:</b>
 *
 * <dl>
 * <dt>pom.xml</dt>
 * <dd>a default pom.xml with a valid name tag</dd>
 * <dt>m1/pom.xml</dt>
 * <dd>a default pom.xml with a valid name tag which could be used to detect additional modules in addition to
 * the previous mentioned pom.xml</dd>
 * <dt>m2/pom.xml</dt>
 * <dd>a default pom.xml with a valid name tag which could be used to detect additional modules in addition to
 * the previous mentioned pom.xml</dd>
 * <dt>m3/pom.xml</dt>
 * <dd>a broken XML-structure breaks the correct parsing of this file</dd>
 * <dt>m4/pom.xml</dt>
 * <dd>a pom.xml with an artifactId tag and without a name tag</dd>
 * <dt>m5/pom.xml</dt>
 * <dd>a pom.xml without an artifactId tag and without a name tag</dd>
 * </dl>
 *
 * <p>
 * <b>Ant:</b>
 * </p>
 *
 * <dl>
 * <dt>build.xml</dt>
 * <dd>a default build.xml with a valid name tag</dd>
 * <dt>m1/build.xml</dt>
 * <dd>a default build.xml with a valid name tag which could be used to detect additional modules in addition
 * * to the previous mentioned build.xml</dd>
 * <dt>m2/build.xml</dt>
 * <dd>a broken XML-structure breaks the correct parsing of this file</dd>
 * <dt>m3/build.xml</dt>
 * <dd>a build file without the name tag</dd>
 * </dl>
 *
 * <b>OSGI:</b>
 *
 * <dl>
 * <dt>META-INF/MANIFEST.MF</dt>
 * <dd>a default MANIFEST.MF with a set Bundle-SymbolicName and a set Bundle-Vendor</dd>
 * <dt>m1/META-INF/MANIFEST.MF</dt>
 * <dd> a MANIFEST.MF with a wildcard Bundle-Name, a set Bundle-SymbolicName and a wildcard</dd>
 * <dt>m2/META-INF/MANIFEST.MF</dt>
 * <dd>a MANIFEST.MF with a set Bundle-Name and a wildcard Bundle-Vendor</dd>
 * <dt>m3/META-INF/MANIFEST.MF</dt>
 * <dd>an empty MANIFEST.MF</dd>
 * <dt>plugin.properties</dt>
 * <dd>a default plugin.properties file</dd>
 * </dl>
 *
 * <p>
 * All tests work the same way: first of all a set of module files will be copied to the workspace. Each module file
 * will be copied to a separate folder, the first module file is the top-level module. Into each of the modules, a
 * source code file will be placed. Finally,  Eclipse parser log file will be generated, that has exactly one warning
 * for each file.
 * </p>
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
class ModuleDetectorITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String BUILD_FILE_PATH = "detectors/buildfiles/";
    private static final String REPORT_FILE_NAME = "eclipse_prepared-issues.txt";
    private static final String MAVEN_BUILD_FILE_LOCATION = "maven/";
    private static final String ANT_BUILD_FILE_LOCATION = "ant/";
    private static final String OSGI_BUILD_FILE_LOCATION = "osgi/";
    private static final String DEFAULT_DEBUG_LOG_LINE = "Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)";
    private static final String EMPTY_MODULE_NAME = "-";
    private static final String PROPERTY = "moduleName";

    /**
     * Verifies that the HTML output is correct if there are OSGI, Maven, and Ant modules used within the build. This
     * test doesn't check for correct precedence in every possible case as this might fail.
     */
    @Test
    void shouldShowModulesForVariousModulesDetectedForOsgiMavenAndAntInTheHtmlOutput() {
        String[] workspaceFiles = {
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

        ResultAction result = createResult(
                workspaceFiles.length - 1,
                true,
                workspaceFiles);

        verifyModules(result,
                new PropertyRow(EMPTY_MODULE_NAME, 1),
                new PropertyRow("edu.hm.hafner.osgi.symbolicname", 1),
                new PropertyRow("edu.hm.hafner.osgi.symbolicname (TestVendor)", 7),
                new PropertyRow("Test-Bundle-Name", 1));
    }

    @Test
    void shouldSkipPostProcessing() {
        String[] workspaceFiles = {
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

        FreeStyleProject project = createFreeStyleProject();
        copyWorkspaceFiles(project, workspaceFiles, file -> file.replaceFirst("detectors/buildfiles/\\w*/", ""));
        var recorder = enableGenericWarnings(project, new Eclipse());
        recorder.setSkipPostProcessing(true);

        createEclipseWarningsReport(workspaceFiles.length - 1, true,
                getJenkins().jenkins.getWorkspaceFor(project));

        Run<?, ?> build = buildSuccessfully(project);
        ResultAction result = getResultAction(build);
        assertThat(result.getResult().getIssues().getModules()).containsExactly("-");

        assertThat(getConsoleLog(build)).doesNotContain("Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");
        assertThat(getConsoleLog(build)).contains("Skipping detection of missing package and module names");
    }

    /**
     * Verifies that the output is correct if there are only Maven modules in the expected HTML output.
     */
    @Test
    void shouldShowModulesForVariousMavenModulesInTheHtmlOutput() {
        String[] workspaceFiles = {
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml"};

        ResultAction result = createResult(
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
    void shouldShowModulesForVariousAntModulesInTheHtmlOutput() {
        String[] workspaceFiles = {
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m1/build.xml"};

        ResultAction result = createResult(workspaceFiles.length, false,
                workspaceFiles);

        verifyModules(result,
                new PropertyRow("TestModule", 1, 100),
                new PropertyRow("SecondTestModule", 1, 100));
    }

    /**
     * Verifies that the output is correct if there are only OSGI modules in the expected HTML output.
     */
    @Test
    void shouldShowModulesForVariousOsgiModulesInTheHtmlOutput() {
        String[] workspaceFiles = {
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m1/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m2/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m3/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"};

        ResultAction result = createResult(
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
    void shouldRunMavenAntAndOsgiAndCheckCorrectExecutionSequence() {
        String[] workspaceFiles = {
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

        ResultAction result = createResult(
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
    void shouldVerifyTheModuleDetectionBehaviorForVariousMavenPomFiles() {
        String[] workspaceFiles = {
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m1/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m2/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m3/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m4/pom.xml",
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "m5/pom.xml"};

        ResultAction result = createResult(
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
    void shouldVerifyTheModuleDetectionBehaviorForVariousAntBuildFiles() {
        String[] workspaceFiles = {
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m1/build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m2/build.xml",
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "m3/build.xml"
        };

        ResultAction result = createResult(
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
    void shouldVerifyTheModuleDetectionBehaviorForVariousOsgiMfFiles() {
        String[] workspaceFiles = {
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m1/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m2/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "m3/META-INF/MANIFEST.MF",
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "plugin.properties"};

        ResultAction result = createResult(
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
    void shouldContainNoSpecificHtmlOutputForAnEmptyProject() {
        checkWebPageForExpectedEmptyResult(createResult(0, false));
    }

    /**
     * Verifies that the output of the HTML page is empty if only one module is set by Maven.
     */
    @Test
    void shouldContainNoSpecificHtmlOutputForASingleModuleMavenProject() {
        verifyThatModulesTabIsNotShownForSingleModule(
                BUILD_FILE_PATH + MAVEN_BUILD_FILE_LOCATION + "pom.xml");
    }

    /**
     * Verifies that the output of the HTML page is empty if only one module is set by Ant.
     */
    @Test
    void shouldContainNoHtmlOutputForASingleModuleAntProject() {
        verifyThatModulesTabIsNotShownForSingleModule(
                BUILD_FILE_PATH + ANT_BUILD_FILE_LOCATION + "build.xml");
    }

    /**
     * Verifies that the output of the HTML page is empty if only one module is set by Ant.
     */
    @Test
    void shouldContainNoHtmlOutputForASingleModuleOsgiProject() {
        verifyThatModulesTabIsNotShownForSingleModule(
                BUILD_FILE_PATH + OSGI_BUILD_FILE_LOCATION + "META-INF/MANIFEST.MF");
    }

    private void verifyThatModulesTabIsNotShownForSingleModule(final String... workspaceFiles) {
        checkWebPageForExpectedEmptyResult(
                createResult(workspaceFiles.length, false, workspaceFiles));
    }

    private void verifyModules(final ResultAction result, final PropertyRow... modules) {
        assertThatModuleTableIsVisible(result, true);

        assertThat(PropertyRow.getRows(result, PROPERTY)).containsExactlyInAnyOrder(modules);

        verifyConsoleLog(result, Stream.of(modules).mapToLong(PropertyRow::getTotal).sum());
    }

    private void verifyConsoleLog(final ResultAction result, final long modulesSize) {
        String logOutput = getConsoleLog(result.getOwner());
        assertThat(logOutput).contains(DEFAULT_DEBUG_LOG_LINE);
        assertThat(logOutput).contains(String.format("-> resolved module names for %s issues", modulesSize));
    }

    /**
     * Creates a new eclipse report file that contains one warning for each of the specified modules. Each module is
     * part of a separate directory.
     *
     * @param modulePaths
     *         the number of module directories to create
     * @param appendNonExistingFile
     *         determines if one additional warning should be created that does not refer to a valid file
     * @param workspace
     *         the workspace to copy the files to
     */
    private void createEclipseWarningsReport(final int modulePaths,
            final boolean appendNonExistingFile, final FilePath workspace) {
        for (int i = 1; i <= modulePaths; i++) {
            String directory = workspace + "/m" + i;
            String affectedFile = directory + "/ClassWithWarnings.java";
            writeEclipseWarning(workspace,
                    "[javac] 1. WARNING in " + affectedFile + " (at line 42)",
                    "[javac] \tSample Message",
                    "[javac] \t^^^^^^^^^^^^^^^^^^",
                    "[javac] Sample Message",
                    "[javac] ----------");
            createAffectedFile(directory, affectedFile);
        }

        if (appendNonExistingFile) {
            writeEclipseWarning(workspace,
                    "[javac] NOT_EXISTING 99. WARNING in /NOT_EXISTING/PATH/NOT_EXISTING_FILE (at line 42)",
                    "[javac] \tSample Message",
                    "[javac] \t^^^^^^^^^^^^^^^^^^",
                    "[javac] Sample Message",
                    "[javac] ----------");
        }
    }

    private void createAffectedFile(final String directory, final String affectedFile) {
        try {
            Path path = Paths.get(affectedFile);
            if (!Files.exists(path)) {
                Path dirPath = Paths.get(directory);
                Files.createDirectories(dirPath);
                Files.createFile(path);
            }
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void writeEclipseWarning(final FilePath workspace, final String... lines) {
        try {
            Files.write(Paths.get(workspace.child(REPORT_FILE_NAME).getRemote()), Arrays.asList(lines),
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void checkWebPageForExpectedEmptyResult(final ResultAction result) {
        assertThatModuleTableIsVisible(result, false);
    }

    private void assertThatModuleTableIsVisible(final ResultAction result, final boolean isVisible) {
        assertThat(result.getResult().getIssues().getModules().size() > 1).isEqualTo(isVisible);
    }

    private ResultAction createResult(final int numberOfExpectedModules, final boolean appendNonExistingFile,
            final String... workspaceFiles) {
        FreeStyleProject project = createFreeStyleProject();
        copyWorkspaceFiles(project, workspaceFiles, file -> file.replaceFirst("detectors/buildfiles/\\w*/", ""));
        enableGenericWarnings(project, new Eclipse());

        createEclipseWarningsReport(numberOfExpectedModules, appendNonExistingFile,
                getJenkins().jenkins.getWorkspaceFor(project));

        return getResultAction(buildSuccessfully(project));
    }
}
