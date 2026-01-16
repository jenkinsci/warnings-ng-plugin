package io.jenkins.plugins.analysis.warnings.axivion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AxivionSuiteTest {
    private static AxivionSuite newTool(final String projectUrl) {
        return new AxivionSuite(projectUrl, "", "");
    }

    @Test
    void projectUrlsAreEncoded() {
        assertThat(newTool("http://localhost:9090/axivion/projects/vscode plugin").getProjectUrl())
                .isEqualTo("http://localhost:9090/axivion/projects/vscode%20plugin");

        assertThat(newTool("https://axivion.com/projects/vscode plugin").getProjectUrl())
                .isEqualTo("https://axivion.com/projects/vscode%20plugin");
    }

    @Test
    void projectUrlWithEnvironmentVariablesArePreserved() {
        // Test with ${VAR} syntax
        assertThat(newTool("http://localhost:9090/axivion/projects/${BUILD_VARIANT}").getProjectUrl())
                .isEqualTo("http://localhost:9090/axivion/projects/${BUILD_VARIANT}");

        // Test with $VAR syntax
        assertThat(newTool("http://localhost:9090/axivion/projects/$BUILD_VARIANT").getProjectUrl())
                .isEqualTo("http://localhost:9090/axivion/projects/$BUILD_VARIANT");

        // Test with multiple environment variables
        assertThat(newTool("http://${HOST}:${PORT}/axivion/projects/${PROJECT}").getProjectUrl())
                .isEqualTo("http://${HOST}:${PORT}/axivion/projects/${PROJECT}");

        // Test with environment variable in path
        assertThat(newTool("https://axivion.com/projects/${PROJECT_NAME}/dashboard").getProjectUrl())
                .isEqualTo("https://axivion.com/projects/${PROJECT_NAME}/dashboard");
    }

    @Test
    void projectUrlWithMixedEnvironmentVariablesAndSpaces() {
        // URL with both environment variable and spaces should preserve the environment variable
        assertThat(newTool("http://localhost:9090/axivion/projects/${BUILD_VARIANT} name").getProjectUrl())
                .isEqualTo("http://localhost:9090/axivion/projects/${BUILD_VARIANT} name");
    }
}
