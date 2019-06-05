package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.List;
import java.util.Objects;

import org.junit.ClassRule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerClassRule;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import hudson.model.FreeStyleProject;
import hudson.model.Slave;
import hudson.slaves.DumbSlave;
import hudson.tasks.Shell;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.warnings.Gcc3;

import static org.assertj.core.api.Assertions.*;

/**
 * Test gcc make build on worker and in docker container.
 *
 * @author Andreas Neumeier
 * @author Tobias Redl
 */
public class WorkerMakeGccBuildITest extends WorkerBuildABC {
    /**
     * Gcc Docker image rules.
     */
    @ClassRule
    public static final DockerClassRule<GccContainer> DOCKER_GCC = new DockerClassRule<>(GccContainer.class);

    /**
     * Build gcc make project worker.
     */
    @Test
    public void buildMakeOnDumpSlave() {
        buildMakeOnWorker(createDumbSlave());
    }

    /**
     * Build gcc make project in docker container.
     */
    @Test
    public void buildMakeOnDocker() {
        buildMakeOnWorker(setUpDocker(DOCKER_GCC));
    }

    private void buildMakeOnWorker(final Slave worker) {
        FreeStyleProject project = createFreeStyleProjectWithWorker(worker);
        project.getBuildersList().add(new Shell("make"));
        copySingleFileToAgentWorkspace(worker, project, "gcc-src/Makefile", "Makefile");
        copySingleFileToAgentWorkspace(worker, project, "gcc-src/main.c", "main.c");
        enableWarnings(project, createTool(new Gcc3(), ""));

        scheduleSuccessfulBuild(project);

        List<AnalysisResult> results = getAnalysisResults(Objects.requireNonNull(project.getLastBuild()));
        assertThat(results.size()).isEqualTo(1);
        assertThat(Objects.requireNonNull(project.getLastBuild().getBuiltOn()).getLabelString()).isEqualTo(
                worker.getLabelString());
    }
}