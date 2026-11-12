package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Severity;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests the class {@link OverviewModel}.
 *
 * @author Ullrich Hafner
 */
class OverviewModelTest {
    /**
     * Verifies that the sizes of all results are summed up and that only severities with warnings are shown.
     */
    @Test
    void shouldSumUpSizesOfAllResults() {
        var checkstyle = createAction("checkstyle", 5, 2, 1, QualityGateStatus.INACTIVE);
        var pmd = createAction("pmd", 3, 0, 4, QualityGateStatus.INACTIVE);
        when(checkstyle.getResult().getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(5);
        when(pmd.getResult().getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(1);
        when(pmd.getResult().getTotalSizeOf(Severity.ERROR)).thenReturn(2);

        var model = createModel(checkstyle, pmd);

        assertThat(model)
                .hasDisplayName("Overview")
                .hasUrlName("overview")
                .hasTotalSize(8)
                .hasNewSize(2)
                .hasFixedSize(5)
                .hasActions(checkstyle, pmd);
        assertThat(model.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(6);
        assertThat(model.getTotalSizeOf(Severity.WARNING_LOW)).isZero();
        assertThat(model.getSeverities()).containsExactly(Severity.ERROR, Severity.WARNING_NORMAL);
        assertThat(model.getSeverityLabel(Severity.ERROR)).isEqualTo("Error");

        var segments = model.getTotalSegments();
        assertThat(segments).hasSize(2);
        assertThat(segments.get(0)).hasCount(2).hasLabel("Error").hasModifier("error");
        assertThat(segments.get(1)).hasCount(6).hasLabel("Normal").hasModifier("normal");
    }

    /**
     * Verifies that the distribution of the new warnings by severity is computed from the new warnings only.
     */
    @Test
    void shouldCreateSegmentsForNewWarnings() {
        var checkstyle = createAction("checkstyle", 5, 3, 0, QualityGateStatus.INACTIVE);
        var pmd = createAction("pmd", 3, 1, 0, QualityGateStatus.INACTIVE);
        when(checkstyle.getResult().getNewSizeOf(Severity.WARNING_HIGH)).thenReturn(1);
        when(checkstyle.getResult().getNewSizeOf(Severity.WARNING_LOW)).thenReturn(2);
        when(pmd.getResult().getNewSizeOf(Severity.WARNING_LOW)).thenReturn(1);
        when(pmd.getResult().getTotalSizeOf(Severity.ERROR)).thenReturn(3);

        var model = createModel(checkstyle, pmd);

        assertThat(model.getNewSizeOf(Severity.WARNING_LOW)).isEqualTo(3);
        assertThat(model.getNewSizeOf(Severity.ERROR)).isZero();

        var segments = model.getNewSegments();
        assertThat(segments).hasSize(2);
        assertThat(segments.get(0)).hasCount(1).hasLabel("High").hasModifier("high");
        assertThat(segments.get(1)).hasCount(3).hasLabel("Low").hasModifier("low");
    }

    /**
     * Verifies that an overview without results shows no values and no quality gates.
     */
    @Test
    void shouldHandleRunWithoutResults() {
        var model = createModel();

        assertThat(model)
                .hasTotalSize(0)
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasNoActions()
                .hasNoSeverities()
                .hasNoTotalSegments()
                .hasNoNewSegments()
                .hasNoQualityGateActions()
                .doesNotHaveQualityGates()
                .hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    /**
     * Verifies that only results with active quality gates are shown and that the worst status determines the
     * overall status.
     */
    @Test
    void shouldReportWorstQualityGateStatus() {
        var checkstyle = createAction("checkstyle", 1, 0, 0, QualityGateStatus.PASSED);
        var pmd = createAction("pmd", 1, 0, 0, QualityGateStatus.INACTIVE);
        var spotbugs = createAction("spotbugs", 1, 0, 0, QualityGateStatus.WARNING);

        var model = createModel(checkstyle, pmd, spotbugs);

        assertThat(model)
                .hasQualityGates()
                .hasQualityGateActions(checkstyle, spotbugs)
                .hasQualityGateStatus(QualityGateStatus.WARNING);
    }

    /**
     * Verifies that a passed quality gate is reported as passed, even if other results have no quality gates.
     */
    @Test
    void shouldReportPassedQualityGate() {
        var checkstyle = createAction("checkstyle", 1, 0, 0, QualityGateStatus.PASSED);
        var pmd = createAction("pmd", 1, 0, 0, QualityGateStatus.INACTIVE);

        assertThat(createModel(checkstyle, pmd))
                .hasQualityGates()
                .hasQualityGateActions(checkstyle)
                .hasQualityGateStatus(QualityGateStatus.PASSED);
    }

    /**
     * Verifies that the criticality of a quality gate is visualized with an error icon (if the step or build fails)
     * or a warning icon (if the step or build is marked as unstable).
     *
     * @param criticality
     *         the criticality of the quality gate
     * @param icon
     *         the expected icon
     * @param color
     *         the expected color
     * @param description
     *         the expected description
     */
    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "NOTE,     symbol-warning-outline plugin-ionicons-api,      jenkins-!-warning-color, Mark the step as unstable",
        "UNSTABLE, symbol-warning-outline plugin-ionicons-api,      jenkins-!-warning-color, Mark the step and the build as unstable",
        "ERROR,    symbol-close-circle-outline plugin-ionicons-api, jenkins-!-error-color,   Fail the step but not the build",
        "FAILURE,  symbol-close-circle-outline plugin-ionicons-api, jenkins-!-error-color,   Fail the step and the build"
    })
    void shouldVisualizeCriticality(
            final QualityGateCriticality criticality, final String icon, final String color, final String description) {
        var qualityGate = new WarningsQualityGate(10, QualityGateType.TOTAL, criticality);

        var model = createModel();

        assertThat(model.getCriticalityIcon(qualityGate)).isEqualTo(icon);
        assertThat(model.getCriticalityColor(qualityGate)).isEqualTo(color);
        assertThat(model.getCriticalityDescription(qualityGate)).isEqualTo(description);
    }

    /**
     * Creates a model for a run with the specified results.
     *
     * @param actions
     *         the results of the run
     *
     * @return the model
     */
    private OverviewModel createModel(final ResultAction... actions) {
        return new OverviewModel(mock(Run.class), List.of(actions));
    }

    /**
     * Creates a result action stub with the specified values.
     *
     * @param id
     *         the ID of the action
     * @param totalSize
     *         the total number of warnings
     * @param newSize
     *         the number of new warnings
     * @param fixedSize
     *         the number of fixed warnings
     * @param status
     *         the overall quality gate status
     *
     * @return the action
     */
    private ResultAction createAction(
            final String id,
            final int totalSize,
            final int newSize,
            final int fixedSize,
            final QualityGateStatus status) {
        var action = mock(ResultAction.class);
        var result = mock(AnalysisResult.class);

        when(action.getId()).thenReturn(id);
        when(action.getUrlName()).thenReturn(id);
        when(action.getResult()).thenReturn(result);
        when(action.getQualityGateResult()).thenReturn(new QualityGateResult(status));
        when(result.getTotalSize()).thenReturn(totalSize);
        when(result.getNewSize()).thenReturn(newSize);
        when(result.getFixedSize()).thenReturn(fixedSize);

        return action;
    }
}
