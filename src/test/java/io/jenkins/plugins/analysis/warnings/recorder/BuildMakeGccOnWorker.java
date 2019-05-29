package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.ClassRule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerClassRule;
import hudson.slaves.DumbSlave;

/**
 * Tests a make/gcc build on a worker.
 *
 * @author Michael Schmid, Raphael Furch
 */
public class BuildMakeGccOnWorker extends BuildOnWorkerITest {
    /**
     * Rules for the used Gcc Docker image.
     */
    @ClassRule
    public static final DockerClassRule<GccContainer> GCC_DOCKER = new DockerClassRule<>(GccContainer.class);

    /**
     * Builds a make/gcc project on a dump slave..
     */
    @Test
    public void buildMakeOnDumpSlave() {
        DumbSlave worker = setupDumpSlave();
        buildMakeProjectOnWorker(worker);
    }

    /**
     * Builds a make/gcc project in a docker container.
     */
    @Test
    public void buildMakeOnDocker() {
        DumbSlave worker = setupDockerContainer(GCC_DOCKER);
        buildMakeProjectOnWorker(worker);
    }

}
