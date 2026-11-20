package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Run;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;
import java.util.List;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link Widget}.
 */
class WidgetTest {
    @Test
    void shouldShowWarningSummaryAndResultsWithWarnings() {
        var clean = createAction("clean", 0, 0, 0);
        var checkstyle = createAction("checkstyle", 2, 1, 0);
        var pmd = createAction("pmd", 1, 0, 3);

        var widget = createWidget(clean, pmd, checkstyle);

        assertThat(widget)
                .hasSymbol("symbol-warning-outline plugin-ionicons-api")
                .hasLines(Messages.Widget_WarningsForThisBuild(3))
                .hasTotalSize(3)
                .hasNewSize(1)
                .hasFixedSize(3)
                .doesNotHaveQualityGates()
                .hasPills()
                .hasErrorMessagesSize(0)
                .hasLogUrl(LogModel.URL);
        assertThat(widget.getResults()).containsExactly(checkstyle, pmd);
    }

    @Test
    void shouldShowAllClearIfBuildHasNoWarnings() {
        var checkstyle = createAction("checkstyle", 0, 0, 2);
        var pmd = createAction("pmd", 0, 0, 0);

        var widget = createWidget(checkstyle, pmd);

        assertThat(widget)
                .hasSymbol("symbol-status-blue")
                .hasLines(Messages.Widget_AllClear(), Messages.Widget_NoWarningsForThisBuild())
                .hasTotalSize(0)
                .hasFixedSize(2)
                .hasNoResults()
                .doesNotHavePills();
    }

    @Test
    void shouldSortResultsByQualityGatesAndNewWarnings() {
        var passed = createAction("passed", 10, 0, 0, QualityGateStatus.PASSED);
        var unstable = createAction("unstable", 1, 0, 0, QualityGateStatus.WARNING, QualityGateStatus.PASSED);
        var fresh = createAction("fresh", 2, 2, 0);
        var failed = createAction("failed", 0, 0, 0, QualityGateStatus.FAILED);
        var clean = createAction("clean", 0, 0, 0);

        var widget = createWidget(passed, unstable, fresh, failed, clean);

        assertThat(widget)
                .hasQualityGates()
                .hasQualityGateStatus(QualityGateStatus.FAILED)
                .hasQualityGateIcon("symbol-status-red")
                .hasQualityGateSummary(Messages.Widget_QualityGates_Missed(2, 4));
        assertThat(widget.getResults()).containsExactly(failed, unstable, fresh, passed);
        assertThat(widget.getStatusModifier(failed)).isEqualTo("error");
        assertThat(widget.getStatusModifier(unstable)).isEqualTo("warning");
        assertThat(widget.getStatusModifier(passed)).isEqualTo("none");
        assertThat(widget.getStatusModifier(fresh)).isEqualTo("none");
        assertThat(widget.getNewSizeOf(fresh)).isEqualTo(2);
    }

    @Test
    void shouldSummarizePassedQualityGates() {
        var checkstyle = createAction("checkstyle", 1, 0, 0, QualityGateStatus.PASSED, QualityGateStatus.PASSED);

        assertThat(createWidget(checkstyle))
                .hasQualityGateStatus(QualityGateStatus.PASSED)
                .hasQualityGateIcon("symbol-status-blue")
                .hasQualityGateSummary(Messages.Widget_QualityGates_Passed(2));
    }

    @Test
    void shouldNotShowPillsForSingleResult() {
        var checkstyle = createAction("checkstyle", 5, 1, 0);

        assertThat(createWidget(checkstyle)).hasResults(checkstyle).doesNotHavePills();
    }

    @Test
    void shouldLinkToFirstResultWithErrors() {
        var checkstyle = createAction("checkstyle", 1, 0, 0);
        var pmd = createAction("pmd", 1, 0, 0);
        when(pmd.getResult().getErrorMessages()).thenReturn(Lists.immutable.of("missing file", "parse error"));

        assertThat(createWidget(checkstyle, pmd)).hasErrorMessagesSize(2).hasLogUrl("log#tool=pmd");
    }

    /**
     * Creates a widget for the specified results.
     *
     * @param actions
     *         the results of the run
     *
     * @return the widget
     */
    private Widget createWidget(final ResultAction... actions) {
        return new Widget(new OverviewModel(mock(Run.class), List.of(actions)));
    }

    /**
     * Creates a result action stub with the specified values.
     *
     * @param id
     *         the ID (and name) of the action
     * @param totalSize
     *         the total number of warnings
     * @param newSize
     *         the number of new warnings
     * @param fixedSize
     *         the number of fixed warnings
     * @param qualityGates
     *         the status of each quality gate of the action
     *
     * @return the action
     */
    private ResultAction createAction(
            final String id,
            final int totalSize,
            final int newSize,
            final int fixedSize,
            final QualityGateStatus... qualityGates) {
        var action = mock(ResultAction.class);
        var result = mock(AnalysisResult.class);

        when(action.getId()).thenReturn(id);
        when(action.getDisplayName()).thenReturn(id);
        when(action.getResult()).thenReturn(result);
        when(result.getTotalSize()).thenReturn(totalSize);
        when(result.getNewSize()).thenReturn(newSize);
        when(result.getFixedSize()).thenReturn(fixedSize);
        when(result.getErrorMessages()).thenReturn(Lists.immutable.empty());

        var qualityGateResult = new QualityGateResult();
        for (QualityGateStatus status : qualityGates) {
            qualityGateResult.add(
                    new WarningsQualityGate(0, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE),
                    status,
                    String.valueOf(totalSize));
        }
        when(action.getQualityGateResult()).thenReturn(qualityGateResult);

        return action;
    }
}
