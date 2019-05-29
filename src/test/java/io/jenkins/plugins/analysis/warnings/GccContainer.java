package io.jenkins.plugins.analysis.warnings;

import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;

/**
 * Docker container for make/gcc.
 */
@DockerFixture(
        id = "gcc",
        ports = {22}
)
public class GccContainer extends SshdContainer {
}