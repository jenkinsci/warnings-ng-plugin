package io.jenkins.plugins.analysis.warnings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jenkins_ci.plugins.flexible_publish.ConditionalPublisher;
import org.jenkins_ci.plugins.flexible_publish.FlexiblePublisher;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner.Run;
import org.jenkins_ci.plugins.run_condition.core.AlwaysRun;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.hm.hafner.analysis.Report.IssueFilterBuilder;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Item;
import hudson.tasks.BuildStep;

import io.jenkins.plugins.analysis.core.filter.ExcludeFile;
import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.TabType;
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
    private static final String TOOL_ID = "sampleId1";
    private static final String TOOL_ID2 = "sampleId2";

    /**
     * Test that the same tool can be used twice with different configuration.
     */
    @Test
    public void shouldAnalyseJavaTwiceWithHealthReport() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, JAVA_FILE, JAVA_FILE);
        copySingleFileToWorkspace(project, JAVA_FILE2, JAVA_FILE2);

        IssuesRecorder publisher = constructJavaIssuesRecorder(JAVA_FILE, TOOL_ID, false, 1, 9);
        IssuesRecorder publisher2 = constructJavaIssuesRecorder(JAVA_PATTERN, TOOL_ID2, false, 1, 3);

        project.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                constructConditionalPublisher(publisher),
                constructConditionalPublisher(publisher2)
        )));

        hudson.model.Run<?, ?> run = buildSuccessfully(project);
        List<AnalysisResult> results = getAnalysisResults(run);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(TOOL_ID);
        assertThat(results.get(1).getId()).isEqualTo(TOOL_ID2);
        List<HealthReport> healthReports = project.getBuildHealthReports();
        assertThat(healthReports.get(0).getScore()).isEqualTo(0);
        assertThat(healthReports.get(1).getScore()).isEqualTo(80);
        assertThat(healthReports.get(2).getScore()).isEqualTo(100);
        checkDetailsViewForIssues(project, results.get(0), results.get(0).getId(), 2);
        checkDetailsViewForIssues(project, results.get(1), results.get(1).getId(), 4);
    }

    /**
     * Test that the same tool can be used twice with different configuration and aggregation, but is not aggregated
     * afterwards.
     */
    @Test
    public void shouldAnalyseJavaTwiceWithAggregationNotAggregated() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, JAVA_FILE, JAVA_FILE);
        copySingleFileToWorkspace(project, JAVA_FILE2, JAVA_FILE2);

        IssuesRecorder publisher = constructJavaIssuesRecorder(JAVA_FILE, TOOL_ID, true);
        IssuesRecorder publisher2 = constructJavaIssuesRecorder(JAVA_PATTERN, TOOL_ID2, true);

        project.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                constructConditionalPublisher(publisher),
                constructConditionalPublisher(publisher2)
        )));

        hudson.model.Run<?, ?> run = buildSuccessfully(project);
        List<AnalysisResult> results = getAnalysisResults(run);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(TOOL_ID);
        assertThat(results.get(1).getId()).isEqualTo(TOOL_ID2);
        checkDetailsViewForIssues(project, results.get(0), results.get(0).getId(), 2);
        checkDetailsViewForIssues(project, results.get(1), results.get(1).getId(), 4);
    }

    /**
     * Test that two java issue recorder can run with different configuration (one with issue filter, one without).
     */
    @Test
    public void shouldAnalyseJavaTwiceWithOneIssueFilter() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, JAVA_FILE, JAVA_FILE);
        copySingleFileToWorkspace(project, JAVA_FILE2, JAVA_FILE2);

        IssuesRecorder publisher = constructJavaIssuesRecorder(JAVA_PATTERN, TOOL_ID, false);

        RegexpFilter filter = new ExcludeFile(".*File.java");
        List<RegexpFilter> filterList = new ArrayList<>();
        filterList.add(filter);
        publisher.setFilters(filterList);
        IssuesRecorder publisher2 = constructJavaIssuesRecorder(JAVA_PATTERN, TOOL_ID2, false);

        project.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                constructConditionalPublisher(publisher),
                constructConditionalPublisher(publisher2)
        )));

        hudson.model.Run<?, ?> run = buildSuccessfully(project);
        List<AnalysisResult> results = getAnalysisResults(run);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(TOOL_ID);
        assertThat(results.get(1).getId()).isEqualTo(TOOL_ID2);
        checkDetailsViewForIssues(project, results.get(0), results.get(0).getId(), 2);
        checkDetailsViewForIssues(project, results.get(1), results.get(1).getId(), 4);
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
        DetailsTab detailsTab = new DetailsTab(detailsPage);
        IssuesTable issuesTable = detailsTab.select(TabType.ISSUES);
        List<IssueRow> issuesTableRows = issuesTable.getRows();

        assertThat(issuesTableRows.size()).isEqualTo(warnings);
    }

    private HtmlPage getDetailsWebPage(final Item project, final AnalysisResult result, final String publisherId) {
        int buildNumber = result.getBuild().getNumber();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, String.format("%d/%s", buildNumber, publisherId));
    }
}
