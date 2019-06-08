package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.Arrays;
import java.util.Collections;

import org.jenkinsci.test.acceptance.docker.DockerClassRule;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;

import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

/**
 * Abstract test of a build on a worker.
 *
 * @author Michael Schmid, Raphael Furch
 */
@SuppressWarnings("IllegalCatch")
abstract class BuildOnWorkerITest extends IntegrationTestWithJenkinsPerSuite {

    DumbSlave setupDumpSlave() {
        try {
            return JENKINS_PER_SUITE.createSlave();
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    <T extends SshdContainer> DumbSlave setupDockerContainer(final DockerClassRule<T> docker) {
        DumbSlave worker;
        try {
            T container = docker.create();
            worker = new DumbSlave("docker", "/home/test",
                    new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "",
                            "-Dfile.encoding=ISO-8859-1"));
            worker.setNodeProperties(Collections.singletonList(new EnvironmentVariablesNodeProperty(
                    new Entry("JAVA_HOME", "/usr/lib/jvm/java-8-openjdk-amd64/jre"))));

            worker = new DumbSlave("docker", "/home/test", new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "", "-Dfile.encoding=ISO-8859-1"));
            worker.setNodeProperties(Arrays.asList(new EnvironmentVariablesNodeProperty(new EnvironmentVariablesNodeProperty.Entry("JAVA_HOME", "/usr/lib/jvm/java-8-openjdk-amd64/jre"))));
            getJenkins().jenkins.addNode(worker);
            getJenkins().waitOnline(worker);
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
        return worker;
    }

    /**
     * Docker Container which supplies make and gcc.
     */
    @DockerFixture(
            id = "gcc",
            ports = {22, 8080}
    )
    public static class GccContainer extends JavaContainer {

    }
}
