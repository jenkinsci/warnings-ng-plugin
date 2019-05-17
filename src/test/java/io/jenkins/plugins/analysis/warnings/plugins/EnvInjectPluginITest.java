package io.jenkins.plugins.analysis.warnings.plugins;

import org.junit.jupiter.api.Test;

import org.jenkinsci.plugins.envinject.EnvInjectBuildWrapper;
import org.jenkinsci.plugins.envinject.EnvInjectJobPropertyInfo;
import org.jenkinsci.plugins.envinject.EnvInjectPlugin;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

public class EnvInjectPluginITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    void shouldRunWithEnvPlugin() {
        // Set up the project and configure the java warnings
        FreeStyleProject project = createFreeStyleProject();
        EnvInjectPlugin envInjectPlugin = new EnvInjectPlugin();
        EnvInjectBuildWrapper envInjectBuildWrapper = new EnvInjectBuildWrapper(new EnvInjectJobPropertyInfo());
        project.getBuildWrappersList().add(envInjectBuildWrapper);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

    }
}
