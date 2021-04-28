package io.jenkins.plugins.analysis.warnings.steps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.util.Strings;
import org.junit.Test;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.matrix.TextAxis;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Gcc4;
import io.jenkins.plugins.analysis.warnings.SpotBugs;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in matrix jobs.
 *
 * @author Ullrich Hafner
 */
public class MatrixJobITest extends IntegrationTestWithJenkinsPerSuite {
    /**
     * Build a matrix job with three configurations. For each configuration a different set of warnings will be parsed
     * with the same parser (GCC). After the successful build the total number of warnings at the root level should be
     * set to 12 (sum of all three configurations). Moreover, for each configuration the total number of warnings is
     * also verified (4, 6, and 2 warnings).
     *
     * @throws Exception in case of an error
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    public void shouldCreateIndividualAxisResults() throws Exception {
        MatrixProject project = createProject(MatrixProject.class);
        copySingleFileToWorkspace(project, "matrix-warnings-one.txt", "user_axis/one/warnings.txt");
        copySingleFileToWorkspace(project, "matrix-warnings-two.txt", "user_axis/two/warnings.txt");
        copySingleFileToWorkspace(project, "matrix-warnings-three.txt", "user_axis/three/warnings.txt");
        IssuesRecorder publisher = new IssuesRecorder();
        Gcc4 tool = new Gcc4();
        tool.setPattern("**/*.txt");
        publisher.setTools(tool);
        project.getPublishersList().add(publisher);

        AxisList axis = new AxisList();
        TextAxis userAxis = new TextAxis("user_axis", "one two three");
        axis.add(userAxis);
        project.setAxes(axis);

        Map<String, Integer> warningsPerAxis = new HashMap<>();
        warningsPerAxis.put("one", 4);
        warningsPerAxis.put("two", 6);
        warningsPerAxis.put("three", 2);

        MatrixBuild build = buildSuccessfully(project);
        for (MatrixRun run : build.getRuns()) {
            getJenkins().assertBuildStatus(Result.SUCCESS, run);

            AnalysisResult result = getAnalysisResult(run);

            String currentAxis = getAxisName(run);
            assertThat(result.getTotalSize()).as("Result of axis " + currentAxis).isEqualTo(warningsPerAxis.get(currentAxis));
        }
        AnalysisResult aggregation = getAnalysisResult(build);
        assertThat(aggregation.getTotalSize()).isEqualTo(12);
    }

    @Test
    public void shouldReportNoNewWarningsForSameAxisResults() throws Exception {
        MatrixProject project = createProject(MatrixProject.class);

        copySingleFileToWorkspace(project, "spotbugsXml.xml", "user_axis/JDK8/spotbugs-issues.txt");
        copySingleFileToWorkspace(project, "spotbugsXml.xml", "user_axis/JDK11/spotbugs-issues.txt");

        enableGenericWarnings(project, new SpotBugs());

        configureAxisLabels(project, "JDK8", "JDK11");

        MatrixBuild build = buildSuccessfully(project);
        verifyFirstBuild(build);

        verifySecondBuild(buildSuccessfully(project));
        verifySecondBuild(buildSuccessfully(project));
    }

    private void configureAxisLabels(final MatrixProject project, final String... axis) throws IOException {
        project.setAxes(new AxisList(new TextAxis("user_axis", String.join(" ", axis))));
    }

    private MatrixBuild buildSuccessfully(final MatrixProject project) throws Exception {
        MatrixBuild matrixBuild = project.scheduleBuild2(0).get();

        getJenkins().assertBuildStatus(Result.SUCCESS, matrixBuild);

        return matrixBuild;
    }

    private void verifyFirstBuild(final MatrixBuild build) throws Exception {
        for (MatrixRun run : build.getRuns()) {
            getJenkins().assertBuildStatus(Result.SUCCESS, run);

            AnalysisResult result = getAnalysisResult(run);

            String currentAxis = getAxisName(run);
            assertThat(result.getTotalSize()).as("Result of axis " + currentAxis).isEqualTo(2);
        }
        AnalysisResult aggregation = getAnalysisResult(build);
        assertThat(aggregation.getTotalSize()).isEqualTo(4);
    }

    private void verifySecondBuild(final MatrixBuild build) throws Exception {
        for (MatrixRun run : build.getRuns()) {
            getJenkins().assertBuildStatus(Result.SUCCESS, run);

            assertThat(getAnalysisResult(run)).as("Result of axis %s", getAxisName(run)).hasTotalSize(2).hasNewSize(0);
        }
        assertThat(getAnalysisResult(build)).hasTotalSize(4).hasNewSize(0);
    }

    private String getAxisName(final MatrixRun run) {
        return run.getBuildVariables().values().iterator().next();
    }
}
