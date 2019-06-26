package io.jenkins.plugins.analysis.warnings;

import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;

/**
 * Creates a docker container with gcc/make installed.
 */
@DockerFixture(
        id = "gcc",
        ports = {22, 8080}
)
public class GccContainer extends SshdContainer {
    public GccContainer() {
    }
}