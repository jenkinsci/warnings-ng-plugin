package io.jenkins.plugins.analysis.core.testutil;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * Docker Container with SSH and Java runtime to support Jenkins agent start. Additionally gcc and make are installed
 * for c/c++ projects.
 *
 * @author Florian Hageneder
 */
@DockerFixture(id = "gccmake", ports = {22, 8_080, 50_724})
public class GccMakeContainer extends DockerContainer {
}
