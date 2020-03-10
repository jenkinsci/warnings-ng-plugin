package io.jenkins.plugins.analysis.core.testutil;

import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;

/**
 * Docker Container for an jenkins agent to build c++ projects with makefiles.
 *
 * @author Andreas Reiser
 * @author Andreas Moser
 */
@DockerFixture(id = "gcc", ports = {22, 8080})
public class GccDockerContainer extends SshdContainer {
}
