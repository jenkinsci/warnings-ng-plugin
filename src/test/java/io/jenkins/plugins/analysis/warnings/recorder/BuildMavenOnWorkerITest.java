package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.junit.ClassRule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerClassRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.model.FreeStyleProject;
import hudson.model.Slave;
import hudson.slaves.DumbSlave;
import hudson.tasks.Maven;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.warnings.MavenConsole;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests a maven build on a worker.
 *
 * @author Michael Schmid, Raphael Furch
 */
public class BuildMavenOnWorkerITest extends BuildOnWorkerITest {
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

    private void buildMavenProjectOnWorker(final Slave worker) {
        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(worker);
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
        buildSuccessfully(project);

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
}
