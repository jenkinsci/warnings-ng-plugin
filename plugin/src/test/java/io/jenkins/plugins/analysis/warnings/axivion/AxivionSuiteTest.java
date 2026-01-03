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
}
