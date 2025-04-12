package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.matrix.TextAxis;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Gcc4;
import io.jenkins.plugins.analysis.warnings.SpotBugs;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in matrix jobs.
 *
 * @author Ullrich Hafner
 */
class MatrixJobITest extends IntegrationTestWithJenkinsPerSuite {
    /**
     * Build a matrix job with three configurations. For each configuration a different set of warnings will be parsed
     * with the same parser (GCC). After the successful build the total number of warnings at the root level should be
     * set to 12 (sum of all three configurations). Moreover, for each configuration the total number of warnings is
     * also verified (4, 6, and 2 warnings).
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    void shouldCreateIndividualAxisResults() {
        var project = createProject(MatrixProject.class);
        copySingleFileToWorkspace(project, "matrix-warnings-one.txt", "user_axis/one/issues.txt");
        copySingleFileToWorkspace(project, "matrix-warnings-two.txt", "user_axis/two/issues.txt");
        copySingleFileToWorkspace(project, "matrix-warnings-three.txt", "user_axis/three/issues.txt");

        enableGenericWarnings(project, new Gcc4());
        configureAxisLabels(project, "one", "two", "three");

        Map<String, Integer> warningsPerAxis = new HashMap<>();
        warningsPerAxis.put("one", 4);
        warningsPerAxis.put("two", 6);
        warningsPerAxis.put("three", 2);

        var build = buildSuccessfully(project);
        for (MatrixRun run : build.getRuns()) {
            assertSuccessfulBuild(run);

            var result = getAnalysisResult(run);
            var currentAxis = getAxisName(run);

            assertThat(result.getTotalSize())
                    .as("Result of axis " + currentAxis)
                    .isEqualTo(warningsPerAxis.get(currentAxis));
        }
        var aggregation = getAnalysisResult(build);
        assertThat(aggregation.getTotalSize()).isEqualTo(12);
    }

    /**
     * Verifies that in a matrix build that produces the same results for each axis no new warnings are shown.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-65482">Issue 65482</a>
     */
    @Test
    void shouldReportNoNewWarningsForSameAxisResults() {
        var project = createProject(MatrixProject.class);

        copySingleFileToWorkspace(project, "spotbugsXml.xml", "user_axis/JDK8/spotbugs-issues.txt");
        copySingleFileToWorkspace(project, "spotbugsXml.xml", "user_axis/JDK11/spotbugs-issues.txt");

        enableGenericWarnings(project, new SpotBugs());

        configureAxisLabels(project, "JDK8", "JDK11");

        var build = buildSuccessfully(project);
        verifyFirstBuild(build);

        verifySecondBuild(buildSuccessfully(project));
        verifySecondBuild(buildSuccessfully(project));
    }

    private void configureAxisLabels(final MatrixProject project, final String... axis) {
        try {
            project.setAxes(new AxisList(new TextAxis("user_axis", String.join(" ", axis))));
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private void verifyFirstBuild(final MatrixBuild build) {
        for (MatrixRun run : build.getRuns()) {
            assertSuccessfulBuild(run);

            var result = getAnalysisResult(run);
            var currentAxis = getAxisName(run);

            assertThat(result.getTotalSize())
                    .as("Result of axis " + currentAxis)
                    .isEqualTo(2);
        }

        assertThat(getAnalysisResult(build).getTotalSize()).isEqualTo(2);
    }

    private void verifySecondBuild(final MatrixBuild build) {
        for (MatrixRun run : build.getRuns()) {
            assertSuccessfulBuild(run);

            assertThat(getAnalysisResult(run)).as("Result of axis %s", getAxisName(run)).hasTotalSize(2).hasNewSize(0);
        }
        assertThat(getAnalysisResult(build)).hasTotalSize(2).hasNewSize(0);
    }

    private String getAxisName(final MatrixRun run) {
        return run.getBuildVariables().values().iterator().next();
    }
}
