package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.ClassRule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerClassRule;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;

import hudson.model.Descriptor.FormException;
import hudson.model.FreeStyleProject;
import hudson.model.Slave;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;
import hudson.tasks.Maven;
import hudson.tasks.Shell;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Gcc3;
import io.jenkins.plugins.analysis.warnings.MavenConsole;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests a build on a worker.
 *
 * @author Andreas Neumeier
 * @author Tobias Redl
 */
abstract class WorkerBuildITest extends IntegrationTestWithJenkinsPerSuite {

    private FreeStyleProject createFreeStyleProjectWithWorker(final Slave worker) {
        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(worker);
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        buildSuccessfully(project);

        return project;
    }

    void buildMavenOnWorker(final Slave worker) {
        FreeStyleProject project = createFreeStyleProjectWithWorker(worker);

        project.getBuildersList().add(new Maven("verify", null));
        copySingleFileToAgentWorkspace(worker, project, "affected-files/Main.java",
                "src/main/java/hm/edu/hafner/analysis/Main.java");
        copySingleFileToAgentWorkspace(worker, project, "pom.xml", "pom.xml");
        enableWarnings(project, createTool(new MavenConsole(), ""));

        scheduleSuccessfulBuild(project);

        List<AnalysisResult> result = getAnalysisResults(Objects.requireNonNull(project.getLastBuild()));
        assertThat(result.size()).isEqualTo(1);
        assertThat(Objects.requireNonNull(project.getLastBuild().getBuiltOn()).getLabelString()).isEqualTo(
                worker.getLabelString());
    }

    void buildMakeOnWorker(final Slave worker) {
        FreeStyleProject project = createFreeStyleProjectWithWorker(worker);

        project.getBuildersList().add(new Shell("make"));
        copySingleFileToAgentWorkspace(worker, project, "make-gcc/Makefile", "Makefile");
        copySingleFileToAgentWorkspace(worker, project, "make-gcc/main.c", "main.c");
        enableWarnings(project, createTool(new Gcc3(), ""));

        buildSuccessfully(project);

        List<AnalysisResult> results = getAnalysisResults(Objects.requireNonNull(project.getLastBuild()));
        assertThat(results.size()).isEqualTo(1);
        assertThat(Objects.requireNonNull(project.getLastBuild().getBuiltOn()).getLabelString()).isEqualTo(
                worker.getLabelString());
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