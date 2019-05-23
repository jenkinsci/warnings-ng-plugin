package io.jenkins.plugins.analysis.warnings.plugins;

import org.junit.Test;

import org.jenkinsci.plugins.envinject.EnvInjectBuildWrapper;
import org.jenkinsci.plugins.envinject.EnvInjectJobPropertyInfo;
import org.jenkinsci.plugins.envinject.EnvInjectPlugin;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.Java;

public class EnvInjectPluginITest extends IntegrationTestWithJenkinsPerTest {

    @Test
    public void shouldRunWithEnvPlugin() {
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
