package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.ClassRule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerClassRule;
import hudson.slaves.DumbSlave;

/**
 * Tests a make/gcc build on a worker.
 *
 * @author Andreas Neumeier
 * @author Tobias Redl
 */
public class WorkerMakeGccBuild extends WorkerBuildITest {
    /**
     * Rules for the used Gcc Docker image.
     */
    @ClassRule
    public static final DockerClassRule<DockerContainerGcc> DOCKER_GCC = new DockerClassRule<>(DockerContainerGcc.class);

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
        DumbSlave worker = setupDockerContainer(DOCKER_GCC);
        buildMakeProjectOnWorker(worker);
    }

}