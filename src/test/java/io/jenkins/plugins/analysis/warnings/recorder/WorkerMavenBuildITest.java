package io.jenkins.plugins.analysis.warnings.recorder;

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
 * Test maven build on worker and in docker container.
 *
 * @author Andreas Neumeier
 * @author Tobias Redl
 */
public class WorkerMavenBuildITest extends WorkerBuildABC {
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

    private void buildMavenOnWorker(final Slave worker) {
        FreeStyleProject project = createFreeStyleProjectWithWorker(worker);
        project.getBuildersList().add(new Maven("verify", null));
        copySingleFileToAgentWorkspace(worker, project, "affected-files/Main.java",
                "src/main/java/hm/edu/hafner/analysis/Main.java");
        copySingleFileToAgentWorkspace(worker, project, "pom.xml", "pom.xml");
        enableWarnings(project, createTool(new MavenConsole(), ""));

        scheduleSuccessfulBuild(project);

        List<AnalysisResult> results = getAnalysisResults(Objects.requireNonNull(project.getLastBuild()));
        assertThat(results.size()).isEqualTo(1);
        assertThat(Objects.requireNonNull(project.getLastBuild().getBuiltOn()).getLabelString()).isEqualTo(
                worker.getLabelString());
    }
}