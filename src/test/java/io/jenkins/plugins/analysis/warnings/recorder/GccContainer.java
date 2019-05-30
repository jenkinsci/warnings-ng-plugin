package io.jenkins.plugins.analysis.warnings.recorder;

import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;

/**
 * Docker Container which sets up make + gcc.
 */
@DockerFixture(
        id = "gcc",
        ports = {22, 8080}
)
public class GccContainer extends SshdContainer {
    /**
     * Default constructor to get no warning.
     */
    public GccContainer() {
    }
}