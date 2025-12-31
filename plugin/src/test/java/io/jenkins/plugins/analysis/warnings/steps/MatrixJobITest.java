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
     * Build a matrix job with three configurations. For each configuration, a different set of warnings will be parsed
     * with the same parser (GCC). After the successful build, the total number of warnings at the root level should be
     * set to 12 (sum of all three configurations). Moreover, for each configuration the total number of warnings is
     * also verified (4, 6, and 2 warnings).
     */
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

    /**
     * Verifies that matrix builds produce consistent aggregated results regardless of axis declaration order.
     * This test ensures that the fix for JENKINS-71571 works correctly by creating two matrix projects with
     * identical warning files but different axis declaration orders and demonstrating that both produce the
     * same aggregated warning count.
     *
     * <p>Without the sorting fix in IssuesAggregator, the aggregated report structure would depend on the
     * axis declaration order, causing fingerprint mismatches and inconsistent warning counts between builds
     * with different axis orders. With the fix, reports are sorted alphabetically by axis name before
     * aggregation, ensuring deterministic results regardless of declaration order.</p>
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-71571">Issue 71571</a>
     */
    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-71571")
    void shouldProduceConsistentAggregatedResultsRegardlessOfAxisDeclarationOrder() {
        // First project: axis order JDK8, JDK11
        var projectOrder1 = createProject(MatrixProject.class);
        copySingleFileToWorkspace(projectOrder1, "matrix-warnings-one.txt", "user_axis/JDK8/issues.txt");
        copySingleFileToWorkspace(projectOrder1, "matrix-warnings-two.txt", "user_axis/JDK11/issues.txt");

        enableGenericWarnings(projectOrder1, new Gcc4());
        configureAxisLabels(projectOrder1, "JDK8", "JDK11");

        var buildOrder1 = buildSuccessfully(projectOrder1);
        var resultOrder1 = getAnalysisResult(buildOrder1);

        for (MatrixRun run : buildOrder1.getRuns()) {
            var axis = getAxisName(run);
            int expected = axis.equals("JDK8") ? 4 : 6;
            assertThat(getAnalysisResult(run).getTotalSize())
                    .as("First project: axis %s should have %d warnings", axis, expected)
                    .isEqualTo(expected);
        }

        // Second project: axis order JDK11, JDK8 (reversed)
        var projectOrder2 = createProject(MatrixProject.class);
        copySingleFileToWorkspace(projectOrder2, "matrix-warnings-one.txt", "user_axis/JDK8/issues.txt");
        copySingleFileToWorkspace(projectOrder2, "matrix-warnings-two.txt", "user_axis/JDK11/issues.txt");

        enableGenericWarnings(projectOrder2, new Gcc4());
        configureAxisLabels(projectOrder2, "JDK11", "JDK8");

        var buildOrder2 = buildSuccessfully(projectOrder2);
        var resultOrder2 = getAnalysisResult(buildOrder2);

        for (MatrixRun run : buildOrder2.getRuns()) {
            var axis = getAxisName(run);
            int expected = axis.equals("JDK8") ? 4 : 6;
            assertThat(getAnalysisResult(run).getTotalSize())
                    .as("Second project: axis %s should have %d warnings", axis, expected)
                    .isEqualTo(expected);
        }

        assertThat(resultOrder1.getTotalSize())
                .as("Aggregated result must be independent of axis declaration order")
                .isEqualTo(resultOrder2.getTotalSize())
                .isEqualTo(10);
    }

    private String getAxisName(final MatrixRun run) {
        return run.getBuildVariables().values().iterator().next();
    }
}
