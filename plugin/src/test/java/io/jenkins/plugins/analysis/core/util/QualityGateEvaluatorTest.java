package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

import edu.hm.hafner.util.FilteredLog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.util.NullResultHandler;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

@SuppressWarnings("PMD.MoreThanOneLogger")
@DefaultLocale("en")
class QualityGateEvaluatorTest {
    @Test
    void shouldBeInactiveIfGatesAreEmpty() {
        var log = new FilteredLog();
        var result = evaluate(List.of(), new IssuesStatisticsBuilder(), log);

        assertThat(result.getOverallStatus())
                .isEqualTo(QualityGateStatus.INACTIVE);
        assertThat(log.getInfoMessages())
                .containsExactly("No quality gates have been set - skipping");
    }

    @Test
    void shouldHandleNegativeDeltaValues() {
        List<WarningsQualityGate> qualityGates = new ArrayList<>();
        qualityGates.add(addQualityGate(1, QualityGateType.DELTA, QualityGateCriticality.UNSTABLE));

        var builder = new IssuesStatisticsBuilder().setDeltaErrorSize(-1);

        var result = evaluate(qualityGates, builder, new FilteredLog());

        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.PASSED);
        assertThat(result.getMessages()).hasSize(1).first().asString()
                .contains("≪Success≫", QualityGateType.DELTA.getDisplayName(), "Actual value: -1", "Quality gate: 1.00");
    }

    @Test
    void shouldPassIfSizesAreZero() {
        List<WarningsQualityGate> qualityGates = new ArrayList<>();
        qualityGates.add(addQualityGate(1, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE));

        var builder = new IssuesStatisticsBuilder();

        var result = evaluate(qualityGates, builder, new FilteredLog());
        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.PASSED);
        assertThat(result.getMessages()).hasSize(1).first().asString()
                .contains("≪Success≫", QualityGateType.TOTAL.getDisplayName(), "Actual value: 0", "Quality gate: 1.00");

        qualityGates.add(addQualityGate(1, QualityGateType.NEW, QualityGateCriticality.UNSTABLE));

        result = evaluate(qualityGates, builder, new FilteredLog());
        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.PASSED);
        assertThat(result.getMessages()).hasSize(2).first().asString()
                .contains("≪Success≫", QualityGateType.TOTAL.getDisplayName(), "Actual value: 0", "Quality gate: 1.00");
        assertThat(result.getMessages()).hasSize(2).last().asString()
                .contains("≪Success≫", QualityGateType.NEW.getDisplayName(), "Actual value: 0", "Quality gate: 1.00");
    }

    @Test
    void shouldEvaluateAllProperties() {
        var builder = new IssuesStatisticsBuilder();

        evaluateQualityGateFor(builder, builder::setTotalNormalSize, QualityGateType.TOTAL);
        evaluateQualityGateFor(builder, builder::setTotalErrorSize, QualityGateType.TOTAL_ERROR);
        evaluateQualityGateFor(builder, builder::setTotalHighSize, QualityGateType.TOTAL_HIGH);
        evaluateQualityGateFor(builder, builder::setTotalNormalSize, QualityGateType.TOTAL_NORMAL);
        evaluateQualityGateFor(builder, builder::setTotalLowSize, QualityGateType.TOTAL_LOW);

        evaluateQualityGateFor(builder, builder::setDeltaNormalSize, QualityGateType.DELTA);
        evaluateQualityGateFor(builder, builder::setDeltaErrorSize, QualityGateType.DELTA_ERROR);
        evaluateQualityGateFor(builder, builder::setDeltaHighSize, QualityGateType.DELTA_HIGH);
        evaluateQualityGateFor(builder, builder::setDeltaNormalSize, QualityGateType.DELTA_NORMAL);
        evaluateQualityGateFor(builder, builder::setDeltaLowSize, QualityGateType.DELTA_LOW);

        evaluateQualityGateFor(builder, builder::setNewNormalSize, QualityGateType.NEW);
        evaluateQualityGateFor(builder, builder::setNewErrorSize, QualityGateType.NEW_ERROR);
        evaluateQualityGateFor(builder, builder::setNewHighSize, QualityGateType.NEW_HIGH);
        evaluateQualityGateFor(builder, builder::setNewNormalSize, QualityGateType.NEW_NORMAL);
        evaluateQualityGateFor(builder, builder::setNewLowSize, QualityGateType.NEW_LOW);
    }

    private void evaluateQualityGateFor(final IssuesStatisticsBuilder builder,
            final Function<Integer, IssuesStatisticsBuilder> setter,
            final QualityGateType type) {
        builder.clear();

        List<WarningsQualityGate> qualityGates = new ArrayList<>();
        qualityGates.add(addQualityGate(1, type, QualityGateCriticality.UNSTABLE));

        var result = evaluate(qualityGates, builder, new FilteredLog());
        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.PASSED);
        assertThat(result.getMessages()).hasSize(1).first().asString()
                .contains("≪Success≫", type.getDisplayName(), "Actual value: 0", "Quality gate: 1.00");

        setter.apply(1);
        result = evaluate(qualityGates, builder, new FilteredLog());
        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.WARNING);
        assertThat(result.getMessages()).hasSize(1).first().asString()
                .contains("≪Unstable≫", type.getDisplayName(), "Actual value: 1", "Quality gate: 1.00");
    }

    @Test
    void shouldFailIfSizeIsEqual() {
        List<WarningsQualityGate> qualityGates = new ArrayList<>();
        qualityGates.add(addQualityGate(1, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE));

        var builder = new IssuesStatisticsBuilder();
        builder.setTotalNormalSize(1);

        var result = evaluate(qualityGates, builder, new FilteredLog());

        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.WARNING);
        assertThat(result.getMessages()).hasSize(1).first().asString()
                .contains("≪Unstable≫", QualityGateType.TOTAL.getDisplayName(), "Actual value: 1", "Quality gate: 1.00");

        qualityGates.add(addQualityGate(1, QualityGateType.NEW, QualityGateCriticality.UNSTABLE));
        builder.setNewNormalSize(1);

        result = evaluate(qualityGates, builder, new FilteredLog());

        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.WARNING);
        assertThat(result.getMessages()).hasSize(2).first().asString()
                .contains("≪Unstable≫", QualityGateType.TOTAL.getDisplayName(), "Actual value: 1", "Quality gate: 1.00");
        assertThat(result.getMessages()).hasSize(2).last().asString()
                .contains("≪Unstable≫", QualityGateType.NEW.getDisplayName(), "Actual value: 1", "Quality gate: 1.00");
    }

    private QualityGateResult evaluate(final List<WarningsQualityGate> qualityGates,
            final IssuesStatisticsBuilder builder, final FilteredLog log) {
        return createEvaluator(qualityGates, builder).evaluate(new NullResultHandler(), log);
    }

    private WarningsQualityGate addQualityGate(final int threshold, final QualityGateType qualityGateType,
            final QualityGateCriticality qualityGateCriticality) {
        return new WarningsQualityGate(threshold, qualityGateType, qualityGateCriticality);
    }

    @Test
    void shouldIgnoreThresholdZero() {
        List<WarningsQualityGate> qualityGates = new ArrayList<>();
        qualityGates.add(addQualityGate(0, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE));

        var builder = new IssuesStatisticsBuilder();
        builder.setTotalNormalSize(1);

        var result = evaluate(qualityGates, builder, new FilteredLog());
        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.INACTIVE);
        assertThat(result.getMessages()).hasSize(1).first().asString()
                .contains("≪Not built≫", QualityGateType.TOTAL.getDisplayName(), "Actual value: Threshold too small: 0.0", "Quality gate: 0.00");

        qualityGates.add(addQualityGate(0, QualityGateType.NEW, QualityGateCriticality.UNSTABLE));
        builder.setNewNormalSize(1);

        result = evaluate(qualityGates, builder, new FilteredLog());

        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.INACTIVE);
        assertThat(result.getMessages()).hasSize(2).first().asString()
                .contains("≪Not built≫", QualityGateType.TOTAL.getDisplayName(), "Actual value: Threshold too small: 0.0", "Quality gate: 0.00");
        assertThat(result.getMessages()).hasSize(2).last().asString()
                .contains("≪Not built≫", QualityGateType.NEW.getDisplayName(), "Actual value: Threshold too small: 0.0", "Quality gate: 0.00");
    }

    @Test
    void shouldOverrideWarningWithFailure() {
        List<WarningsQualityGate> qualityGates = new ArrayList<>();
        qualityGates.add(addQualityGate(1, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE));
        qualityGates.add(addQualityGate(2, QualityGateType.TOTAL, QualityGateCriticality.FAILURE));

        var builder = new IssuesStatisticsBuilder();
        builder.setTotalNormalSize(1);

        var result = evaluate(qualityGates, builder, new FilteredLog());
        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.WARNING);
        assertThat(result.getMessages()).hasSize(2).first().asString()
                .contains("≪Unstable≫", QualityGateType.TOTAL.getDisplayName(), "Actual value: 1", "Quality gate: 1.00");
        assertThat(result.getMessages()).hasSize(2).last().asString()
                .contains("≪Success≫", QualityGateType.TOTAL.getDisplayName(), "Actual value: 1", "Quality gate: 2.00");

        builder.setTotalNormalSize(2);

        result = evaluate(qualityGates, builder, new FilteredLog());

        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.FAILED);
        assertThat(result.getMessages()).hasSize(2).first().asString()
                .contains("≪Unstable≫", QualityGateType.TOTAL.getDisplayName(), "Actual value: 2", "Quality gate: 1.00");
        assertThat(result.getMessages()).hasSize(2).last().asString()
                .contains("≪Failed≫", QualityGateType.TOTAL.getDisplayName(), "Actual value: 2", "Quality gate: 2.00");

        List<WarningsQualityGate> other = new ArrayList<>();
        other.add(addQualityGate(2, QualityGateType.TOTAL, QualityGateCriticality.FAILURE));
        other.add(addQualityGate(1, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE));

        builder.setTotalNormalSize(1);
        result = evaluate(other, builder, new FilteredLog());

        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.WARNING);

        builder.setTotalNormalSize(2);
        result = evaluate(other, builder, new FilteredLog());

        assertThat(result.getOverallStatus()).isEqualTo(QualityGateStatus.FAILED);
    }

    private WarningsQualityGateEvaluator createEvaluator(
            final List<WarningsQualityGate> qualityGates, final IssuesStatisticsBuilder builder) {
        return new WarningsQualityGateEvaluator(qualityGates, builder.build());
    }
}
