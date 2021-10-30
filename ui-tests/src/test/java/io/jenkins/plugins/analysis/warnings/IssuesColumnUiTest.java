package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Acceptance tests for the Warnings Issue Column.
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
@WithPlugins("warnings-ng")
public class IssuesColumnUiTest extends UiTest {
    private static final String DEFAULT_ISSUES_COLUMN_NAME = "# Issues";
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

        assertHoverValues(column, 1, "CheckStyle Warnings", "3");
        assertHoverValues(column, 2, "FindBugs Warnings", "0");
        assertHoverValues(column, 3, "PMD Warnings", "2");
        assertHoverValues(column, 4, "CPD Duplications", "20");

        ListView view = createListView();

        IssuesColumnConfiguration totalColumn = view.addColumn(IssuesColumnConfiguration.class);
        totalColumn.setName(CUSTOM_ISSUES_COLUMN_NAME);
        totalColumn.filterByTool(CHECKSTYLE_TOOL);

        view.save();

        IssuesColumn checkstyleColumn = new IssuesColumn(build, CUSTOM_ISSUES_COLUMN_NAME);
        assertThat(checkstyleColumn.getTotalCount()).isEqualTo("3");
        assertThat(checkstyleColumn.hasLinkToResults()).isFalse();

        assertHoverValues(checkstyleColumn, 1, "CheckStyle Warnings", "3");

        view.configure(() -> {
            totalColumn.setType(StatisticProperties.TOTAL_HIGH);
            totalColumn.disableToolFilter();
        });

        IssuesColumn highColumn = new IssuesColumn(build, CUSTOM_ISSUES_COLUMN_NAME);
        assertThat(highColumn.getTotalCount()).isEqualTo("5");
        assertThat(highColumn.hasLinkToResults()).isFalse();

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

        IssuesColumnConfiguration totalColumn = view.addColumn(IssuesColumnConfiguration.class);
        totalColumn.filterByTool(CHECKSTYLE_TOOL);
        totalColumn.setName(CUSTOM_ISSUES_COLUMN_NAME);

        view.save();

        IssuesColumn column = new IssuesColumn(build, CUSTOM_ISSUES_COLUMN_NAME);

        assertThat(column.getTotalCount()).isEqualTo("3");
        assertThat(column.hasLinkToResults()).isTrue();
    }

    private void addCheckStyle(final FreeStyleJob job) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool(UiTest.CHECKSTYLE_TOOL);
            recorder.setEnabledForFailure(true);
        });
    }

    private void assertHoverValues(final IssuesColumn column, final int rowNumber, final String toolName, final String issueCount) {
        assertThat(column.getToolFromTooltip(rowNumber)).isEqualTo(toolName);
        assertThat(column.getTotalFromTooltip(rowNumber)).isEqualTo(issueCount);
    }

    private ListView createListView() {
        ListView view = jenkins.getViews().create(ListView.class);
        view.configure();
        view.matchAllJobs();
        return view;
    }
}
