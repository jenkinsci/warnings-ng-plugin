package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.Collections;

import org.jenkinsci.test.acceptance.docker.DockerClassRule;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import hudson.model.FreeStyleProject;
import hudson.model.Slave;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

/**
 * Holds logic for WorkerXBuildITests.
 *
 * @author Andreas Neumeier
 * @author Tobias Redl
 */
abstract class WorkerBuildABC extends IntegrationTestWithJenkinsPerSuite {

    FreeStyleProject createFreeStyleProjectWithWorker(final Slave worker) {
        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(worker);
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
        buildSuccessfully(project);

        return project;
    }

    //createSlave could throw exception of type Exception, therefore it is necessary to catch it
    @SuppressWarnings("checkstyle:IllegalCatch")
    DumbSlave createDumbSlave() {
        try {
            return JENKINS_PER_SUITE.createSlave();
        }
        catch (Exception e) {
            throw new RuntimeException("Error while creating dumb slave.", e);
        }
    }

    //waitOnline could throw exception of type Exception, therefore it is necessary to catch it
    @SuppressWarnings("checkstyle:IllegalCatch")
    <T extends SshdContainer> DumbSlave setUpDocker(final DockerClassRule<T> docker) {
        DumbSlave worker;
        try {
            T container = docker.create();
            worker = new DumbSlave("docker", "/home/test",
                    new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "",
                            "-Dfile.encoding=ISO-8859-1"));
            worker.setNodeProperties(Collections.singletonList(new EnvironmentVariablesNodeProperty(
                    new Entry("JAVA_HOME", "/usr/lib/jvm/java-8-openjdk-amd64/jre"))));
            getJenkins().jenkins.addNode(worker);
            getJenkins().waitOnline(worker);
        }
        catch (Exception e) {
            throw new RuntimeException("Error while setting up docker.", e);
        }

        return worker;
    }
}