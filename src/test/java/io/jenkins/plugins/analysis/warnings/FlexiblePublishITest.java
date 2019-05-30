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

        Java java = new Java();
        java.setPattern(JAVA_FILE);
        java.setId("hm-edu1");
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(java);
        publisher.setHealthy(1);
        publisher.setUnhealthy(9);
        publisher.setBlameDisabled(true);

        Java java2 = new Java();
        java2.setPattern(JAVA_PATTERN);
        java2.setId("hm-edu2");
        IssuesRecorder publisher2 = new IssuesRecorder();
        publisher2.setTools(java2);
        publisher2.setHealthy(1);
        publisher2.setUnhealthy(3);
        publisher2.setBlameDisabled(true);

        project.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                new ConditionalPublisher(
                        new AlwaysRun(),
                        Collections.singletonList(
                                publisher
                        ),
                        new Run(),
                        false,
                        null,
                        null,
                        null
                ),
                new ConditionalPublisher(
                        new AlwaysRun(),
                        Collections.singletonList(
                                publisher2
                        ),
                        new Run(),
                        false,
                        null,
                        null,
                        null
                )
        )));

        AnalysisResult analysisResult = scheduleSuccessfulBuild(project);
        assertThat(analysisResult.isSuccessful()).isEqualTo(true);
        HealthReport healthReport = project.getBuildHealth();
        assertThat(healthReport.getScore()).isEqualTo(0);
        checkDetailsViewForIssues(project, analysisResult, java.getId(), 2);
        checkDetailsViewForIssues(project, analysisResult, java2.getId(), 4);
    }

    /**
     * Test that the same tool can be used twice with different configuration.
     */
    @Test
    public void shouldAnalyseJavaTwice2() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, JAVA_FILE, JAVA_FILE);
        copySingleFileToWorkspace(project, JAVA_FILE2, JAVA_FILE2);

        Java java = new Java();
        java.setPattern(JAVA_FILE);
        java.setId("hm-edu1");
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(java);
        publisher.setHealthy(1);
        publisher.setUnhealthy(9);
        publisher.setBlameDisabled(true);

        Java java2 = new Java();
        java2.setPattern(JAVA_PATTERN);
        java2.setId("hm-edu2");
        IssuesRecorder publisher2 = new IssuesRecorder();
        publisher2.setTools(java2);
        publisher2.setHealthy(1);
        publisher2.setUnhealthy(3);
        publisher2.setBlameDisabled(true);

        project.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                new ConditionalPublisher(
                        new AlwaysRun(),
                        Collections.singletonList(
                                publisher
                        ),
                        new Run(),
                        true,
                        null,
                        null,
                        null
                ),
                new ConditionalPublisher(
                        new AlwaysRun(),
                        Collections.singletonList(
                                publisher2
                        ),
                        new Run(),
                        true,
                        null,
                        null,
                        null
                )
        )));

        AnalysisResult analysisResult = scheduleSuccessfulBuild(project);
        assertThat(analysisResult.isSuccessful()).isEqualTo(true);
        HealthReport healthReport = project.getBuildHealth();
        assertThat(healthReport.getScore()).isEqualTo(0);
        checkDetailsViewForIssues(project, analysisResult, java.getId(), 2);
        checkDetailsViewForIssues(project, analysisResult, java2.getId(), 4);
    }

    /**
     * Checks if the Issue Table contains the given amount of issues.
     * @param project
     * @param analysisResult
     * @param publisherId
     * @param warnings
     */
    private void checkDetailsViewForIssues(final Item project, final AnalysisResult analysisResult, final String publisherId, final int warnings) {
        HtmlPage detailsPage = getDetailsWebPage(project, analysisResult, publisherId);
        IssuesTable issuesTable = new IssuesTable(detailsPage);
        List<IssueRow> issuesTableRows = issuesTable.getRows();

        assertThat(issuesTableRows.size()).isEqualTo(warnings);
    }


    /**
     * Returns the details page of a job.
     *
     * @param project
     *         The project containing the job.
     * @param result
     *         The result to use.
     *
     * @return The web page containing build details.
     */
    private HtmlPage getDetailsWebPage(final Item project, final AnalysisResult result, String publisherId) {
        int buildNumber = result.getBuild().getNumber();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, String.format("%d/%s", buildNumber, publisherId));
    }
}
