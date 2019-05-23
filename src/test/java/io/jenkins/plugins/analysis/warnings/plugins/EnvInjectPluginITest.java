package io.jenkins.plugins.analysis.warnings.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

import org.jenkinsci.plugins.envinject.EnvInjectBuildWrapper;
import org.jenkinsci.plugins.envinject.EnvInjectJobPropertyInfo;
import org.jenkinsci.plugins.envinject.EnvInjectPlugin;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.Java;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;


public class EnvInjectPluginITest extends IntegrationTestWithJenkinsPerTest {

    @Test
    public void shouldRunWithEnvPlugin() throws IOException {
        // Set up the project and configure the java warnings
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        createFileWithJavaWarnings(project, 1, 2);

        EnvInjectBuildWrapper envInjectBuildWrapper = new EnvInjectBuildWrapper(new EnvInjectJobPropertyInfo(
                null, "HELLO_WORLD=hello_test\nMY_ENV_VAR=42",
                null, null, false, null
        ));

        // Set the env inject plugin to run and add a script step which will print out all set env vars
        project.getBuildWrappersList().add(envInjectBuildWrapper);
        addScriptStep(project, "printenv");

        // Do the actual build
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Assert that the printenv command displayed both of our set variables
        // Each env var will be printed out by the plugin itself once, so we need to search for two occurrences
        assertThatLogContainsNTimes(project, "HELLO_WORLD=hello_test", 2);
        assertThatLogContainsNTimes(project, "MY_ENV_VAR=42", 2);

        assertThat(analysisResult).hasTotalSize(2);
    }


    @Test
    public void shouldStoreEnvironmentVariablesInFile() throws IOException {
        // Set up the project and configure the java warnings
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        createFileWithJavaWarnings(project, 1, 2);

        EnvInjectPlugin envInjectPlugin = new EnvInjectPlugin();
        EnvInjectBuildWrapper envInjectBuildWrapper = new EnvInjectBuildWrapper(new EnvInjectJobPropertyInfo());
        project.getBuildWrappersList().add(envInjectBuildWrapper);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        Path envFile =  Paths.get(project.getLastBuild().getRootDir().getPath(), "injectedEnvVars.txt");
        List<String> lines = Files.readAllLines(envFile, Charset.forName("ISO-8859-1"));
        for (String line: lines)
        {
            System.out.println(line);
        }
    }


    private void assertThatLogContainsNTimes(final FreeStyleProject project, final String element, final int times) throws IOException {
        FreeStyleBuild lastBuild = project.getLastBuild();
        assertThat(lastBuild).isNotNull();

        InputStream inputStream = lastBuild.getLogInputStream();
        BufferedReader logReader = new BufferedReader(new InputStreamReader(inputStream));

        int foundOccurences = 0;
        String logLine = logReader.readLine();
        while (logLine != null) {
            if (logLine.equals(element)) {
                foundOccurences++;
            }
            logLine = logReader.readLine();
        }
        assertThat(foundOccurences).withFailMessage("Element was not found as often as expected in log")
                .isEqualTo(times);
    }


    /**
     * Create a file with some java warnings in the workspace of the project.
     *
     * @param project
     *         in which the file will be placed
     * @param linesWithWarning
     *         all lines in which a mocked warning should be placed
     */
    private void createFileWithJavaWarnings(final FreeStyleProject project,
            final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createJavaWarning(lineNumber)).append("\n");
        }

        createFileInWorkspace(project, "javac_warnings.txt", warningText.toString());
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
