package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.ClassRule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerClassRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.slaves.DumbSlave;

/**
 * Tests a maven build on a worker.
 *
 * @author Michael Schmid, Raphael Furch
 */
public class BuildMavenOnWorker extends BuildOnWorkerITest {
    /**
     * Rules for the used Java Docker image.
     */
    @ClassRule
    public static final DockerClassRule<JavaContainer> JAVA_DOCKER = new DockerClassRule<>(JavaContainer.class);

    /**
     * Builds a maven project on a dump slave..
     */
    @Test
    public void buildMavenOnDumpSlave() {
        DumbSlave worker = setupDumpSlave();
        buildMavenProjectOnWorker(worker);
    }

    /**
     * Builds a maven project in a docker container.
     */
    @Test
    public void buildMavenOnDocker() {
        DumbSlave worker = setupDockerContainer(JAVA_DOCKER);
        buildMavenProjectOnWorker(worker);
    }
}
