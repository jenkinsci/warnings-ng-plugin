package io.jenkins.plugins.analysis.warnings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jenkins_ci.plugins.flexible_publish.ConditionalPublisher;
import org.jenkins_ci.plugins.flexible_publish.FlexiblePublisher;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner.Run;
import org.jenkins_ci.plugins.run_condition.core.AlwaysRun;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Item;
import hudson.tasks.BuildStep;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssueRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssuesTable;

import static org.assertj.core.api.Assertions.*;

/**
 * Test the flexible publish plugin in combination with the warnings-ng-plugin.
 *
 * @author Tobias Redl
 * @author Andreas Neumeier
 */
public class FlexiblePublishITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JAVA_FILE = "java2Warnings.txt";
    private static final String JAVA_FILE2 = "java-start.txt";
    private static final String JAVA_PATTERN = "**/*.txt";

    /**
     * Test that the same tool can be used twice with different configuration.
     */
    @Test
    public void shouldAnalyseJavaTwice() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, JAVA_FILE, JAVA_FILE);
        copySingleFileToWorkspace(project, JAVA_FILE2, JAVA_FILE2);

        final String toolId = "hm-edu1";
        final String toolId2 = "hm-edu2";

        IssuesRecorder publisher = constructJavaIssuesRecorder(JAVA_FILE, toolId, false, 1, 9);
        IssuesRecorder publisher2 = constructJavaIssuesRecorder(JAVA_PATTERN, toolId2, false, 1, 3);

        project.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                constructConditionalPublisher(publisher),
                constructConditionalPublisher(publisher2)
        )));

        AnalysisResult analysisResult = scheduleSuccessfulBuild(project);
        assertThat(analysisResult.isSuccessful()).isEqualTo(true);
        HealthReport healthReport = project.getBuildHealth();
        assertThat(healthReport.getScore()).isEqualTo(0);
        checkDetailsViewForIssues(project, analysisResult, toolId, 2);
        checkDetailsViewForIssues(project, analysisResult, toolId2, 4);
    }

    /**
     * Test that the same tool can be used twice with different configuration and is aggregated afterwards.
     */
    @Test
    public void shouldAnalyseJavaWithAggregation() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, JAVA_FILE, JAVA_FILE);
        copySingleFileToWorkspace(project, JAVA_FILE2, JAVA_FILE2);

        final String toolId = "hm-edu1";
        final String toolId2 = "hm-edu2";

        IssuesRecorder publisher = constructJavaIssuesRecorder(JAVA_FILE, toolId, true);
        IssuesRecorder publisher2 = constructJavaIssuesRecorder(JAVA_PATTERN, toolId2, true);

        project.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                constructConditionalPublisher(publisher),
                constructConditionalPublisher(publisher2)
        )));

        AnalysisResult analysisResult = scheduleSuccessfulBuild(project);
        assertThat(analysisResult.isSuccessful()).isEqualTo(true);
        //is there a bug with the aggregation?
        checkDetailsViewForIssues(project, analysisResult, toolId, 6);
    }

    private IssuesRecorder constructJavaIssuesRecorder(final String patter, final String id,
            final boolean setAggregation) {
        return constructJavaIssuesRecorder(patter, id, setAggregation, 0, 0);
    }

    private IssuesRecorder constructJavaIssuesRecorder(final String patter, final String id,
            final boolean setAggregation,
            final int healthy, final int unhealthy) {
        Java java = new Java();
        java.setPattern(patter);
        java.setId(id);
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(java);
        if (healthy != 0 && unhealthy != 0) {
            publisher.setHealthy(healthy);
            publisher.setUnhealthy(unhealthy);
        }
        publisher.setBlameDisabled(true);
        publisher.setAggregatingResults(setAggregation);

        return publisher;
    }

    private ConditionalPublisher constructConditionalPublisher(final BuildStep publisher) {
        return new ConditionalPublisher(
                new AlwaysRun(),
                Collections.singletonList(
                        publisher
                ),
                new Run(),
                false,
                null,
                null,
                null
        );
    }

    private void checkDetailsViewForIssues(final Item project, final AnalysisResult analysisResult,
            final String publisherId, final int warnings) {
        HtmlPage detailsPage = getDetailsWebPage(project, analysisResult, publisherId);
        IssuesTable issuesTable = new IssuesTable(detailsPage);
        List<IssueRow> issuesTableRows = issuesTable.getRows();

        assertThat(issuesTableRows.size()).isEqualTo(warnings);
    }

    private HtmlPage getDetailsWebPage(final Item project, final AnalysisResult result, final String publisherId) {
        int buildNumber = result.getBuild().getNumber();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, String.format("%d/%s", buildNumber, publisherId));
    }
}
