package io.jenkins.plugins.analysis.warnings.plugins;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.jvnet.hudson.test.CaptureEnvironmentBuilder;

import org.jenkinsci.plugins.envinject.EnvInjectBuildWrapper;
import org.jenkinsci.plugins.envinject.EnvInjectJobPropertyInfo;
import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.assertThat;

/**
 * This class tests the compatibility between the warnings-ng and the EnvInject plugins. It makes sure the basic
 * functionality of the envinject plugin works and that its features can be used to inject values into patterns.
 */
public class EnvInjectPluginITest extends IntegrationTestWithJenkinsPerTest {

    /**
     * Tests that the build still runs successfully and captures all warnings if used in combination with the envinject
     * plugin. In addition check if the variables where successfully injected.
     *
     * @throws IOException
     *         if environment injection failed to create its artifact.
     */
    @Test
    public void shouldRunWithEnvPlugin() throws IOException {
        FreeStyleProject project = createJavaWarningsFreestyleProject("**/*.txt");

        createFileWithJavaWarnings("javac.txt", project, 1, 2);

        CaptureEnvironmentBuilder capture = injectEnvironmentVariables(project, "HELLO_WORLD=hello_test",
                "MY_ENV_VAR=42");

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Assert that the injected env vars where available during the build
        EnvVars envVars = capture.getEnvVars();
        assertThat(envVars.get("HELLO_WORLD")).isEqualTo("hello_test");
        assertThat(envVars.get("MY_ENV_VAR")).isEqualTo("42");

        // Assert that the injectedEnvVars.txt was successfully created
        FreeStyleBuild lastBuild = project.getLastBuild();
        assertThat(lastBuild).isNotNull();
        Path envFile = Paths.get(lastBuild.getRootDir().getPath(), "injectedEnvVars.txt");
        List<String> lines = Files.readAllLines(envFile, Charset.forName("ISO-8859-1"));

        assertThat(lines.contains("HELLO_WORLD=hello_test"));
        assertThat(lines.contains("MY_ENV_VAR=42"));

        assertThat(analysisResult).hasTotalSize(2);
    }

    /**
     * Make sure that a file pattern containing environment variables correctly matches the expected files.
     */
    @Test
    public void shouldResolveEnvVariablesInPattern() {
        FreeStyleProject project = createJavaWarningsFreestyleProject( "**/*.${FILE_EXT}");

        injectEnvironmentVariables(project, "HELLO_WORLD=hello_test", "FILE_EXT=txt");

        // Create one file which matches the pattern and once which should not match
        createFileWithJavaWarnings("javac.txt", project, 1, 2, 3);
        createFileWithJavaWarnings("javac.csv", project, 1, 2);

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Assert that the expected amount of warnings were found with the pattern
        assertThat(analysisResult).hasTotalSize(3);
        analysisResult.getInfoMessages().contains("-> found 1 file");
    }

    /**
     * Make sure that a file pattern containing environment variables which in turn contain environment variables again
     * can be correctly resolved. The Environment variables should be injected with the EnvInject plugin.
     */
    @Test
    public void shouldResolveNestedEnvVariablesInPattern() {
        FreeStyleProject project = createJavaWarningsFreestyleProject("${FILE_PATTERN}");

        injectEnvironmentVariables(project, "FILE_PATTERN=${FILE_NAME}.${FILE_EXT}", "FILE_NAME=*_javac",
                "FILE_EXT=txt");

        // Create one file which matches the pattern and once which should not match
        createFileWithJavaWarnings("A_javac.txt", project, 1, 2);
        createFileWithJavaWarnings("B_javac.txt", project, 3, 4);
        createFileWithJavaWarnings("C_javac.csv", project, 11, 12, 13);
        createFileWithJavaWarnings("D_tmp.csv", project, 21, 22, 23);
        createFileWithJavaWarnings("E_tmp.txt", project, 31, 32, 33);

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Assert that the expected amount of warnings were found with the pattern
        assertThat(analysisResult).hasTotalSize(4);
        analysisResult.getInfoMessages().contains("-> found 2 files");
    }

    /**
     * Inject Environment variables into a given project and return a capture object.
     * @param project The project to inject the environment variables into.
     * @param properties The environment variables in format variable=value.
     * @return A capture object that can be used to retrieve the variables that where available during build.
     */
    private CaptureEnvironmentBuilder injectEnvironmentVariables(FreeStyleProject project, String... properties) {
        StringBuilder propertiesStringBuilder = new StringBuilder();
        for(String property : properties) {
            propertiesStringBuilder.append(property);
            propertiesStringBuilder.append('\n');
        }
        EnvInjectBuildWrapper envInjectBuildWrapper = new EnvInjectBuildWrapper(new EnvInjectJobPropertyInfo(
                null, propertiesStringBuilder.toString(), null, null,
                false, null
        ));
        project.getBuildWrappersList().add(envInjectBuildWrapper);

        CaptureEnvironmentBuilder capture = new CaptureEnvironmentBuilder();
        project.getBuildersList().add(capture);

        return capture;
    }

    /**
     * Create a Freestyle Project with enabled Java warnings.
     * @param pattern The pattern that is set for the warning files.
     * @return The created Freestyle Project.
     */
    private FreeStyleProject createJavaWarningsFreestyleProject(final String pattern) {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern(pattern);
        enableWarnings(project, java);
        return project;
    }

    /**
     * Create a file with some java warnings in the workspace of the project.
     *
     * @param fileName
     *         of the file to which the warnings will be written
     * @param project
     *         in which the file will be placed
     * @param linesWithWarning
     *         all lines in which a mocked warning should be placed
     */
    private void createFileWithJavaWarnings(final String fileName, final FreeStyleProject project,
            final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createJavaWarning(lineNumber)).append("\n");
        }

        createFileInWorkspace(project, fileName, warningText.toString());
    }

    /**
     * Builds a string representing a java deprecation warning.
     *
     * @param lineNumber
     *         line number in which the mock warning occurred
     *
     * @return a mocked warning string
     */
    private String createJavaWarning(final int lineNumber) {
        return String.format(
                "[WARNING] C:\\Path\\SourceFile.java:[%d,42] [deprecation] path.AClass in path has been deprecated\n",
                lineNumber);
    }
}
