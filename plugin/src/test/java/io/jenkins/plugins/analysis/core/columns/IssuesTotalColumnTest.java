package io.jenkins.plugins.analysis.core.columns;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import java.util.Arrays;
import java.util.Collections;

import hudson.DescriptorExtensionList;
import hudson.model.Job;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.columns.IssuesTotalColumn.AnalysisResultDescription;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;
import io.jenkins.plugins.util.GlobalConfigurationFacade;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.testutil.JobStubs.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link IssuesTotalColumn}.
 *
 * @author Ullrich Hafner
 */
class IssuesTotalColumnTest {
    private static final String NAME = "My Column";

    @Test
    void shouldShowNoResultIfBuild() {
        var column = createColumn();
        column.setSelectTools(false);

        Job<?, ?> job = mock(Job.class);

        assertThat(column.getTotal(job)).isEmpty();
        assertThat(column.getUrl(job)).isEmpty();
        assertThat(column.getDetails(job)).isEmpty();
        assertThat(column.getTools()).isEmpty();
        assertThat(column.getSelectTools()).isFalse();
        assertThat(column.getName()).isEqualTo(NAME);
    }

    @Test
    void shouldShowNoResultIfNoAction() {
        var column = createColumn();
        column.setSelectTools(false);

        Job<?, ?> job = createJobWithActions();

        assertThat(column.getTotal(job)).isEmpty();
        assertThat(column.getUrl(job)).isEmpty();
        assertThat(column.getDetails(job)).isEmpty();
        assertThat(column.getTools()).isEmpty();
        assertThat(column.getSelectTools()).isFalse();
        assertThat(column.getName()).isEqualTo(NAME);
    }

    @Test
    void shouldShowResultOfOneAction() {
        var column = createColumn();
        column.setSelectTools(false);

        Job<?, ?> job = createJob(CHECK_STYLE_ID, CHECK_STYLE_NAME, 1);

        assertThat(column.getTotal(job)).isNotEmpty();
        assertThat(column.getTotal(job)).hasValue(1);
        assertThat(column.getUrl(job)).isEqualTo("0/" + CHECK_STYLE_ID);
    }

    @Test @Issue("JENKINS-57312, JENKINS-59591")
    void shouldShowResultAndDetailsInToolTipOfNewWarnings() {
        var column = createColumn();
        column.setType(StatisticProperties.NEW);
        column.setSelectTools(false);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getNewSize()).thenReturn(1);

        int newSize = 2;
        int fixedSize = 4;
        Job<?, ?> job = createJobWithActions(
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 3, newSize, fixedSize));

        assertThat(column.getTotal(job)).isNotEmpty().hasValue(newSize);
        var newIssuesUrl = "0/" + CHECK_STYLE_ID + "/new";
        assertThat(column.getUrl(job)).isEqualTo(newIssuesUrl);
        assertThat(column.getDetails(job)).hasSize(1).element(0)
                .as("Value of new column")
                .isEqualTo(new AnalysisResultDescription("checkstyle.png", CHECK_STYLE_NAME, newSize, newIssuesUrl));

        column.setType(StatisticProperties.FIXED);
        assertThat(column.getTotal(job)).isNotEmpty().hasValue(fixedSize);
        var fixedIssuesUrl = "0/" + CHECK_STYLE_ID + "/fixed";
        assertThat(column.getUrl(job)).isEqualTo(fixedIssuesUrl);
        assertThat(column.getDetails(job)).hasSize(1).element(0)
                .as("Value of fixed column")
                .isEqualTo(new AnalysisResultDescription("checkstyle.png", CHECK_STYLE_NAME,
                        fixedSize, fixedIssuesUrl));
    }

    @Test
    void shouldShowTotalOfTwoActionsWhenSelectAllIsChecked() {
        var column = createColumn();
        column.setSelectTools(false);

        verifySumOfChecksStyleAndSpotBugs(column);
    }

    @Test
    void shouldShowTotalOfTwoActionsWhenSelectingIndividually() {
        var column = createColumn();
        column.setSelectTools(true);
        column.setTools(Arrays.asList(createTool(CHECK_STYLE_ID), createTool(SPOT_BUGS_ID)));

        verifySumOfChecksStyleAndSpotBugs(column);
    }

    @Test
    void shouldShowTotalOfSelectedTool() {
        var column = createColumn();
        column.setSelectTools(true);
        column.setTools(Collections.singletonList(createTool(CHECK_STYLE_ID)));

        Job<?, ?> job = createJobWithActions(
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 1),
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 2));

        assertThat(column.getTotal(job)).isNotEmpty().hasValue(1);
        assertThat(column.getUrl(job)).isEqualTo("0/" + CHECK_STYLE_ID);

        column.setTools(Collections.singletonList(createTool(SPOT_BUGS_ID)));

        assertThat(column.getTotal(job)).isNotEmpty().hasValue(2);
        assertThat(column.getUrl(job)).isEqualTo("0/" + SPOT_BUGS_ID);

        column.setSelectTools(false);

        assertThat(column.getTotal(job)).isNotEmpty().hasValue(3);
        assertThat(column.getUrl(job)).isEmpty();

        column.setSelectTools(true);

        column.setTools(Collections.singletonList(createTool("unknown")));

        assertThat(column.getTotal(job)).isEmpty();
        assertThat(column.getUrl(job)).isEmpty();

        column.setTools(Collections.emptyList());

        assertThat(column.getTotal(job)).isEmpty();
        assertThat(column.getUrl(job)).isEmpty();
    }

    @Test
    void shouldLinkToAllWhenSelectingTotalIssues() {
        var column = createColumn();
        column.setSelectTools(false);

        column.setType(StatisticProperties.TOTAL);
        verifyUrlOfChecksStyleAndSpotBugs(column, "");
        column.setType(StatisticProperties.TOTAL_ERROR);
        verifyUrlOfChecksStyleAndSpotBugs(column, "/error");
    }

    @Test
    void shouldLinkToNewWhenSelectingNewIssues() {
        var column = createColumn();
        column.setSelectTools(false);

        column.setType(StatisticProperties.NEW);
        verifyUrlOfChecksStyleAndSpotBugs(column, "/new");
        column.setType(StatisticProperties.NEW_ERROR);
        verifyUrlOfChecksStyleAndSpotBugs(column, "/new/error");
    }

    @Test
    void shouldLinkToOverallWhenSelectingDeltaIssues() {
        var column = createColumn();
        column.setSelectTools(false);

        column.setType(StatisticProperties.DELTA);
        verifyUrlOfChecksStyleAndSpotBugs(column, "");
        column.setType(StatisticProperties.DELTA_ERROR);
        verifyUrlOfChecksStyleAndSpotBugs(column, "");
    }

    @Test
    void shouldLinkToFixedWhenSelectingFixedIssues() {
        var column = createColumn();
        column.setSelectTools(false);

        column.setType(StatisticProperties.FIXED);
        verifyUrlOfChecksStyleAndSpotBugs(column, "/fixed");
    }

    private IssuesTotalColumn createColumn() {
        var jenkins = mock(JenkinsFacade.class);
        var descriptorList = DescriptorExtensionList.createDescriptorList((Jenkins) null, GlobalConfiguration.class);
        descriptorList.add(new WarningsAppearanceConfiguration(mock(GlobalConfigurationFacade.class), jenkins));
        when(jenkins.getDescriptorsFor(GlobalConfiguration.class)).thenReturn(descriptorList);
        var column = new IssuesTotalColumn(jenkins);
        column.setName(NAME);
        LabelProviderFactory labelProviderFactory = mock(LabelProviderFactory.class);
        registerTool(labelProviderFactory, CHECK_STYLE_ID, CHECK_STYLE_NAME);
        registerTool(labelProviderFactory, SPOT_BUGS_ID, SPOT_BUGS_NAME);
        column.setLabelProviderFactory(labelProviderFactory);
        return column;
    }

    private void verifySumOfChecksStyleAndSpotBugs(final IssuesTotalColumn column) {
        Job<?, ?> job = createJobWithActions(
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 1),
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 2));

        assertThat(column.getTotal(job)).isNotEmpty();
        assertThat(column.getTotal(job)).hasValue(1 + 2);
        assertThat(column.getUrl(job)).isEmpty();

        assertThat(column.getDetails(job)).containsExactly(
                new AnalysisResultDescription("checkstyle.png", CHECK_STYLE_NAME, 1,
                        "0/" + CHECK_STYLE_ID),
                new AnalysisResultDescription("spotbugs.png", SPOT_BUGS_NAME, 2,
                        "0/" + SPOT_BUGS_ID));
    }

    private void verifyUrlOfChecksStyleAndSpotBugs(final IssuesTotalColumn column, final String url) {
        Job<?, ?> job = createJobWithActions(
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 0),
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 0));

        assertThat(column.getDetails(job)).containsExactly(
                new AnalysisResultDescription("checkstyle.png", CHECK_STYLE_NAME, 0,
                        "0/" + CHECK_STYLE_ID + url),
                new AnalysisResultDescription("spotbugs.png", SPOT_BUGS_NAME, 0,
                        "0/" + SPOT_BUGS_ID + url));
    }
}
