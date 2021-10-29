package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;

import static org.assertj.core.api.Assertions.*;

/**
 * Acceptance tests for the Warnings Issue Column.
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
@WithPlugins("warnings-ng")
public class IssuesColumnUiTest extends UiTest {
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

        IssuesColumn column = new IssuesColumn(build, job.name);

        String issueCount = column.getIssuesCountTextFromTable();
        assertThat(issueCount).isEqualTo("25");

        column.hoverIssueCount();

        assertHoverValues(column, 1, "CheckStyle Warnings", "3");
        assertHoverValues(column, 2, "FindBugs Warnings", "0");
        assertHoverValues(column, 3, "PMD Warnings", "2");
        assertHoverValues(column, 4, "CPD Duplications", "20");

        ListView view = createListView();

        IssuesTotalColumn totalColumn = view.addColumn(IssuesTotalColumn.class);
        totalColumn.setName("Hello World");
        totalColumn.filterByTool(CHECKSTYLE_TOOL);

        view.save();

        IssuesColumn checkstyleColumn = new IssuesColumn(build, job.name, 9);
        assertThat(checkstyleColumn.getIssuesCountTextFromTable()).isEqualTo("3");
        assertThat(checkstyleColumn.issuesCountFromTableHasLink()).isFalse();

        checkstyleColumn.hoverIssueCount();

        assertHoverValues(checkstyleColumn, 1, "CheckStyle Warnings", "3");

        view.configure(() -> {
            totalColumn.setType(StatisticProperties.TOTAL_HIGH);
            totalColumn.disableToolFilter();
        });

        IssuesColumn highColumn = new IssuesColumn(build, job.name, 9);
        assertThat(highColumn.getIssuesCountTextFromTable()).isEqualTo("5");
        assertThat(highColumn.issuesCountFromTableHasLink()).isFalse();

        highColumn.hoverIssueCount();

        assertHoverValues(highColumn, 1, "CheckStyle Warnings", "0");
        assertHoverValues(highColumn, 2, "FindBugs Warnings", "0");
        assertHoverValues(highColumn, 3, "PMD Warnings", "0");
        assertHoverValues(highColumn, 4, "CPD Duplications", "5");
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

        IssuesTotalColumn totalColumn = view.addColumn(IssuesTotalColumn.class);
        totalColumn.filterByTool(CHECKSTYLE_TOOL);

        view.save();

        IssuesColumn column = new IssuesColumn(build, job.name, 9);

        assertThat(column.getIssuesCountTextFromTable()).isEqualTo("3");
        assertThat(column.issuesCountFromTableHasLink()).isTrue();
    }

    private void addCheckStyle(final FreeStyleJob job) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool(UiTest.CHECKSTYLE_TOOL);
            recorder.setEnabledForFailure(true);
        });
    }

    private void assertHoverValues(final IssuesColumn column, final int rowNumber, final String toolName, final String issueCount) {
        assertThat(column.getToolNameFromHover(rowNumber)).isEqualTo(toolName);
        assertThat(column.getIssueCountFromHover(rowNumber)).isEqualTo(issueCount);
    }

    private ListView createListView() {
        ListView view = jenkins.getViews().create(ListView.class);
        view.configure();
        view.matchAllJobs();
        return view;
    }
}
