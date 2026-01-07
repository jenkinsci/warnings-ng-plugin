package io.jenkins.plugins.analysis.warnings.axivion;

import org.junit.jupiter.api.Test;

import hudson.util.FormValidation;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the descriptor validation for {@link AxivionSuite}.
 *
 * @author Akash Manna 
 */
class AxivionSuiteDescriptorTest extends IntegrationTestWithJenkinsPerSuite {
    @Test
    void shouldAcceptValidUrls() {
        var project = createFreeStyleProject();
        var descriptor = new AxivionSuite.AxivionSuiteToolDescriptor();

        assertThat(descriptor.doCheckProjectUrl(project, "http://localhost:9090/axivion/projects/test").kind)
                .isEqualTo(FormValidation.Kind.OK);

        assertThat(descriptor.doCheckProjectUrl(project, "https://axivion.com/projects/myproject").kind)
                .isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    void shouldAcceptUrlsWithEnvironmentVariables() {
        var project = createFreeStyleProject();
        var descriptor = new AxivionSuite.AxivionSuiteToolDescriptor();

        // Test ${VAR} syntax
        assertThat(descriptor.doCheckProjectUrl(project, "http://localhost:9090/axivion/projects/${BUILD_VARIANT}").kind)
                .isEqualTo(FormValidation.Kind.OK);

        // Test $VAR syntax
        assertThat(descriptor.doCheckProjectUrl(project, "http://localhost:9090/axivion/projects/$BUILD_VARIANT").kind)
                .isEqualTo(FormValidation.Kind.OK);

        // Test multiple environment variables
        assertThat(descriptor.doCheckProjectUrl(project, "http://${HOST}:${PORT}/axivion/projects/${PROJECT}").kind)
                .isEqualTo(FormValidation.Kind.OK);

        // Test environment variable in different parts of URL
        assertThat(descriptor.doCheckProjectUrl(project, "https://${DOMAIN}/projects/${PROJECT_NAME}/dashboard").kind)
                .isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    void shouldRejectInvalidUrls() {
        var project = createFreeStyleProject();
        var descriptor = new AxivionSuite.AxivionSuiteToolDescriptor();

        assertThat(descriptor.doCheckProjectUrl(project, "not-a-url").kind)
                .isEqualTo(FormValidation.Kind.ERROR);

        assertThat(descriptor.doCheckProjectUrl(project, "invalid url with spaces").kind)
                .isEqualTo(FormValidation.Kind.ERROR);
    }

    @Test
    void shouldAcceptBasedirWithEnvironmentVariables() {
        var project = createFreeStyleProject();
        var descriptor = new AxivionSuite.AxivionSuiteToolDescriptor();

        // Basedir already supports environment variables
        assertThat(descriptor.doCheckBasedir(project, "$WORKSPACE").kind)
                .isEqualTo(FormValidation.Kind.OK);

        assertThat(descriptor.doCheckBasedir(project, "${BUILD_DIR}").kind)
                .isEqualTo(FormValidation.Kind.OK);
    }
}
