package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.junit.ClassRule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerClassRule;
import hudson.model.FreeStyleProject;
import hudson.slaves.DumbSlave;
import hudson.tasks.Shell;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.warnings.Gcc3;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests a make/gcc build on a worker.
 *
 * @author Michael Schmid, Raphael Furch
 */
public class BuildMakeGccOnWorkerITest extends BuildOnWorkerITest {
    /**
     * Rules for the used Gcc Docker image.
     */
    @ClassRule
    public static final DockerClassRule<GccContainer> GCC_DOCKER = new DockerClassRule<>(GccContainer.class);

    /**
     * Builds a make/gcc project in a docker container.
     */
    @Test
    public void buildMakeOnDocker() {
        DumbSlave worker = setupDockerContainer(GCC_DOCKER);
        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(worker);
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        buildSuccessfully(project);

        project.getBuildersList().add(new Shell("make"));
        copySingleFileToAgentWorkspace(worker, project, "make-gcc/Makefile", "Makefile");
        copySingleFileToAgentWorkspace(worker, project, "make-gcc/main.c", "main.c");
        enableWarnings(project, createTool(new Gcc3(), ""));

        buildSuccessfully(project);

        List<AnalysisResult> results = getAnalysisResults(Objects.requireNonNull(project.getLastBuild()));
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).getTotalSize()).isEqualTo(3);
        assertThat(Objects.requireNonNull(project.getLastBuild().getBuiltOn()).getLabelString()).isEqualTo(
                worker.getLabelString());
    }

}
