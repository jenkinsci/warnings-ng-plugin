package io.jenkins.plugins.analysis.warnings;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link IssuesRecorder}.
 *
 * @author Raphael Furch
 */

@RunWith(Parameterized.class)
class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"javac.txt", 2, 80 },
                {"javac_0_warnings.txt", 0, 100 },
                {"javac_1_warnings.txt", 1, 90 },
                {"javac_9_warnings.txt", 9, 10 },
                {"javac_10_warnings.txt", 10, 0 }

        });
    }


    @SuppressWarnings("checkstyle:visibilitymodifier")
    @Parameter
    public /* NOT private */ String javacSrcFile;

    @SuppressWarnings("checkstyle:visibilitymodifier")
    @Parameter(1)
    public /* NOT private */ int resultTotalSize;

    @SuppressWarnings("checkstyle:visibilitymodifier")
    @Parameter(2)
    public /* NOT private */ int healthReportScore;


    @Test
    public void checkHealthReport() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, javacSrcFile, "javac.txt");

        Java java = new Java();
        java.setPattern("javac.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);
        recorder.setMinimumSeverity("LOW");

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        HealthReport healthReport = project.getBuildHealth();

        assertThat(result.getTotalSize()).isEqualTo(resultTotalSize);
        assertThat(healthReport.getScore()).isEqualTo(healthReportScore);
    }
}