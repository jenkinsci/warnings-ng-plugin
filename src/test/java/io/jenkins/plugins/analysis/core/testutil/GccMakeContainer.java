package io.jenkins.plugins.analysis.core.testutil;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

@DockerFixture(id = "gccmake", ports = {22, 8080, 50724})
public class GccMakeContainer extends DockerContainer {
}
