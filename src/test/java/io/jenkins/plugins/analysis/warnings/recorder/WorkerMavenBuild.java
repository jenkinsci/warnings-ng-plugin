package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.ClassRule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerClassRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.slaves.DumbSlave;

/**
 * Tests a maven build on a worker.
 *
 * @author Andreas Neumeier
 * @author Tobias Redl
 */
public class WorkerMavenBuild extends WorkerBuildITest {
    /**
     * Docker java image rules.
     */
    @ClassRule
    public static final DockerClassRule<JavaContainer> DOCKER_JAVA = new DockerClassRule<>(JavaContainer.class);

    /**
     * Build maven project on worker.
     */
    @Test
    public void buildMavenOnWorker() {
        buildMavenOnWorker(createDumbSlave());
    }

    /**
     * Build maven project in docker container.
     */
    @Test
    public void buildMavenOnDocker() {
        buildMavenOnWorker(setUpDocker(DOCKER_JAVA));
    }
}