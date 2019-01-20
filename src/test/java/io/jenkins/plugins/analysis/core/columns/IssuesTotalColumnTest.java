package io.jenkins.plugins.analysis.core.columns;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import hudson.model.Job;

import io.jenkins.plugins.analysis.core.model.ToolSelection;

import static io.jenkins.plugins.analysis.core.testutil.JobStubs.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link IssuesTotalColumn}.
 *
 * @author Ullrich Hafner
 */
class IssuesTotalColumnTest {
    @Test
    void shouldShowNoResultIfNoAction() {
        IssuesTotalColumn column = new IssuesTotalColumn();
        column.setSelectTools(false);

        Job<?, ?> job = createJobWithActions();

        assertThat(column.getTotal(job)).isEmpty();
        assertThat(column.getUrl(job)).isEmpty();
    }

    @Test
    void shouldShowResultOfOneAction() {
        IssuesTotalColumn column = new IssuesTotalColumn();
        column.setSelectTools(false);

        Job<?, ?> job = createJob(CHECK_STYLE_ID, CHECK_STYLE_NAME, 1);

        assertThat(column.getTotal(job)).isNotEmpty();
        assertThat(column.getTotal(job)).hasValue(1);
        assertThat(column.getUrl(job)).isEqualTo(CHECK_STYLE_ID);
    }

    @Test
    void shouldShowTotalOfTwoActionsWhenSelectAllIsChecked() {
        IssuesTotalColumn column = new IssuesTotalColumn();
        column.setSelectTools(false);

        verifySumOfChecksStyleAndSpotBugs(column);
    }

    @Test
    void shouldShowTotalOfTwoActionsWhenSelectingIndividually() {
        IssuesTotalColumn column = new IssuesTotalColumn();
        column.setSelectTools(true);
        column.setTools(Arrays.asList(createTool(CHECK_STYLE_ID), createTool(SPOT_BUGS_ID)));

        verifySumOfChecksStyleAndSpotBugs(column);
    }

    @Test
    void shouldShowTotalOfSelectedTool() {
        IssuesTotalColumn column = new IssuesTotalColumn();
        column.setSelectTools(true);
        column.setTools(Collections.singletonList(createTool(CHECK_STYLE_ID)));

        Job<?, ?> job = createJobWithActions(
                createAction(1, CHECK_STYLE_ID, CHECK_STYLE_NAME),
                createAction(2, SPOT_BUGS_ID, SPOT_BUGS_NAME));

        assertThat(column.getTotal(job)).isNotEmpty();
        assertThat(column.getTotal(job)).hasValue(1);
        assertThat(column.getUrl(job)).isEqualTo(CHECK_STYLE_ID);

        column.setTools(Collections.singletonList(createTool(SPOT_BUGS_ID)));

        assertThat(column.getTotal(job)).isNotEmpty();
        assertThat(column.getTotal(job)).hasValue(2);
        assertThat(column.getUrl(job)).isEqualTo(SPOT_BUGS_ID);

        column.setTools(Collections.singletonList(createTool("unknown")));

        assertThat(column.getTotal(job)).isEmpty();
        assertThat(column.getUrl(job)).isEmpty();

        column.setTools(Collections.emptyList());

        assertThat(column.getTotal(job)).isEmpty();
        assertThat(column.getUrl(job)).isEmpty();
    }

    private ToolSelection createTool(final String id) {
        ToolSelection toolSelection = new ToolSelection();
        toolSelection.setId(id);
        return toolSelection;
    }

    private void verifySumOfChecksStyleAndSpotBugs(final IssuesTotalColumn column) {
        Job<?, ?> job = createJobWithActions(
                createAction(1, CHECK_STYLE_ID, CHECK_STYLE_NAME),
                createAction(2, SPOT_BUGS_ID, SPOT_BUGS_NAME));

        assertThat(column.getTotal(job)).isNotEmpty();
        assertThat(column.getTotal(job)).hasValue(1 + 2);
        assertThat(column.getUrl(job)).isEmpty();
    }
}