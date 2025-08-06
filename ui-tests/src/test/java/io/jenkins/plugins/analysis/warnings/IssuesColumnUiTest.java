package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;
import static io.jenkins.plugins.analysis.warnings.IssuesColumnConfiguration.*;

/**
 * Acceptance tests for the Warnings Issue Column.
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
@WithPlugins("warnings-ng")
public class IssuesColumnUiTest extends UiTest {
    private static final String CUSTOM_ISSUES_COLUMN_NAME = "Hello World";

    /**
     * Configure a job with multiple recorders: Should display a table when hovering over the issue column.
     */
    @Test
    public void shouldDisplayIssueCount() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_02");
        addAllRecorders(job);
        job.save();

        Build build = job.startBuild().waitUntilFinished();

        jenkins.open();

        IssuesColumn column = new IssuesColumn(jenkins, DEFAULT_ISSUES_COLUMN_NAME);
        assertThat(column).hasTotalCount("25");

        ListView view = createListView();

        IssuesColumnConfiguration totalColumn = view.addColumn(IssuesColumnConfiguration.class);
        totalColumn.setName(CUSTOM_ISSUES_COLUMN_NAME);
        totalColumn.filterByTool(CHECKSTYLE_ID);

        view.save();

        IssuesColumn checkstyleColumn = new IssuesColumn(build, CUSTOM_ISSUES_COLUMN_NAME);
        assertThat(checkstyleColumn).hasTotalCount("3");
        assertThat(checkstyleColumn).hasLinkToResults();
        assertThat(checkstyleColumn.getResultUrl()).endsWith("1/" + CHECKSTYLE_ID);

        view.configure(() -> {
            totalColumn.setType(StatisticProperties.TOTAL_HIGH);
            totalColumn.disableToolFilter();
        });

        IssuesColumn highColumn = new IssuesColumn(build, CUSTOM_ISSUES_COLUMN_NAME);
        assertThat(highColumn).hasTotalCount("5");
        assertThat(highColumn).doesNotHaveLinkToResults();
    }

    /**
     * Configure a job with only one recorder, also create a ListView and configure the issue column to display only this
     * tool results. Should have a link in issue column.
     */
    @Test
    public void shouldShowConfiguredToolOnlyWithLink() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_02");
        addCheckStyle(job);
        job.save();
        Build build = job.startBuild().waitUntilFinished();

        ListView view = createListView();

        IssuesColumnConfiguration totalColumn = view.addColumn(IssuesColumnConfiguration.class);
        totalColumn.filterByTool(CHECKSTYLE_TOOL);
        totalColumn.setName(CUSTOM_ISSUES_COLUMN_NAME);

        view.save();

        IssuesColumn column = new IssuesColumn(build, CUSTOM_ISSUES_COLUMN_NAME);

        assertThat(column).hasTotalCount("3");
        assertThat(column).hasLinkToResults();
        assertThat(column.getResultUrl()).endsWith("1/" + CHECKSTYLE_ID);
    }

    private void addCheckStyle(final FreeStyleJob job) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool(CHECKSTYLE_TOOL).setPattern("**/checkstyle-report.xml");
            recorder.setEnabledForFailure(true);
        });
    }

    private ListView createListView() {
        ListView view = jenkins.getViews().create(ListView.class);
        view.configure();
        view.matchAllJobs();
        return view;
    }
}
