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

import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Item;
import hudson.tasks.BuildStep;

import io.jenkins.plugins.analysis.core.filter.ExcludeFile;
import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
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
    private static final String JAVA_FILE_2_WARNINGS = "java2Warnings.txt";
    private static final String JAVA_FILE_2_WARNINGS_2 = "java-start.txt";
    private static final String JAVA_PATTERN = "**/*.txt";
    private static final String TOOL_ID = "sampleId1";
    private static final String TOOL_ID2 = "sampleId2";

    /**
     * Test that the same tool can be used twice with different configuration.
     */
    @Test
    public void shouldAnalyseJavaTwiceWithHealthReport() {
        FreeStyleProject project = setUpFreeStyleProjectWithFlexiblePublisher(true);

        buildProjectAndAssertResults(project);
        List<HealthReport> healthReports = project.getBuildHealthReports();
        assertThat(healthReports.size()).isEqualTo(3);
        assertThat(healthReports.stream().map(HealthReport::getScore).max(Integer::compareTo).get()).isEqualTo(100);
    }

    /**
     * Test that two java issue recorder can run with different configuration (one with issue filter, one without).
     */
    @Test
    public void shouldAnalyseJavaTwiceWithOneIssueFilter() {
        FreeStyleProject project = setUpFreeStyleProjectWithFlexiblePublisher(false);
        FlexiblePublisher flex = (FlexiblePublisher) project.getPublishersList().get(0);
        IssuesRecorder publisher1 = (IssuesRecorder) flex.getPublishers().get(0).getPublisherList().get(0);

        publisher1.setFilters(createFileExcludeFilter(".*File.java"));

        buildProjectAndAssertResults(project);
    }

    private List<RegexpFilter> createFileExcludeFilter(final String pattern) {
        RegexpFilter filter = new ExcludeFile(pattern);
        List<RegexpFilter> filterList = new ArrayList<>();
        filterList.add(filter);
        return filterList;
    }

    private void buildProjectAndAssertResults(final FreeStyleProject project) {
        hudson.model.Run<?, ?> run = buildSuccessfully(project);
        List<AnalysisResult> results = getAnalysisResults(run);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(TOOL_ID);
        assertThat(results.get(1).getId()).isEqualTo(TOOL_ID2);
        checkDetailsViewForIssues(project, results.get(0), results.get(0).getId(), 2);
        checkDetailsViewForIssues(project, results.get(1), results.get(1).getId(), 4);
    }

    private FreeStyleProject setUpFreeStyleProjectWithFlexiblePublisher(final boolean health) {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, JAVA_FILE_2_WARNINGS, JAVA_FILE_2_WARNINGS);
        copySingleFileToWorkspace(project, JAVA_FILE_2_WARNINGS_2, JAVA_FILE_2_WARNINGS_2);

        IssuesRecorder publisher = constructJavaIssuesRecorder(JAVA_PATTERN, TOOL_ID);
        IssuesRecorder publisher2 = constructJavaIssuesRecorder(JAVA_PATTERN, TOOL_ID2);
        if (health) {
            publisher = constructJavaIssuesRecorder(JAVA_FILE_2_WARNINGS, TOOL_ID, 1, 9);
            publisher2 = constructJavaIssuesRecorder(JAVA_PATTERN, TOOL_ID2, 1, 3);
        }

        project.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                constructConditionalPublisher(publisher),
                constructConditionalPublisher(publisher2)
        )));
        return project;
    }

    private IssuesRecorder constructJavaIssuesRecorder(final String patter, final String id) {
        return constructJavaIssuesRecorder(patter, id, 0, 0);
    }

    private IssuesRecorder constructJavaIssuesRecorder(final String patter, final String id,
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
