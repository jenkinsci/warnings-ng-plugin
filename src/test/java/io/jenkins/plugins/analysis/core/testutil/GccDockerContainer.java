package io.jenkins.plugins.analysis.core.testutil;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

@DockerFixture( id = "gccDocker", ports = {22, 8080})
public class GccDockerContainer extends DockerContainer {
}
