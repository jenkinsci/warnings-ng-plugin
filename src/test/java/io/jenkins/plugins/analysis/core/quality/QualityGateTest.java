package io.jenkins.plugins.analysis.core.quality;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static edu.hm.hafner.analysis.assertj.Assertions.assertThat;
import edu.hm.hafner.analysis.assertj.SoftAssertions;
import edu.hm.hafner.util.SerializableTest;
import io.jenkins.plugins.analysis.core.quality.QualityGate.QualityGateBuilder;
import io.jenkins.plugins.analysis.core.quality.ThresholdSet.ThresholdSetBuilder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import hudson.model.Result;

/**
 * Tests the class {@link QualityGate QualityGate}. These Tests were created while developing the class QualityGate.
 *
 * @author Michael Schmid
 */
class QualityGateTest extends SerializableTest<QualityGate> {
    @Override
    protected QualityGate createSerializable() {
        return new QualityGateBuilder().build();
    }

    @Test
    void shouldCreateInstanceFromStringThresholds() {
        hudson.plugins.analysis.core.Thresholds thresholds = new hudson.plugins.analysis.core.Thresholds();

        thresholds.unstableTotalAll = "1";
        thresholds.unstableTotalHigh = "2";
        thresholds.unstableTotalNormal = "3";
        thresholds.unstableTotalLow = "4";

        thresholds.failedTotalAll = "5";
        thresholds.failedTotalHigh = "6";
        thresholds.failedTotalNormal = "7";
        thresholds.failedTotalLow = "8";

        thresholds.unstableNewAll = "11";
        thresholds.unstableNewHigh = "12";
        thresholds.unstableNewNormal = "13";
        thresholds.unstableNewLow = "14";

        thresholds.failedNewAll = "15";
        thresholds.failedNewHigh = "16";
        thresholds.failedNewNormal = "17";
        thresholds.failedNewLow = "18";

        QualityGate expected = createGateWithBuilder();
        assertThat(expected.isEnabled()).isTrue();

        assertThat(new QualityGate(thresholds)).isEqualTo(expected);
    }

    @Test
    void shouldCreateInstanceFromIntegerThresholds() {
        Thresholds thresholds = new Thresholds();

        thresholds.unstableTotalAll = 1;
        thresholds.unstableTotalHigh = 2;
        thresholds.unstableTotalNormal = 3;
        thresholds.unstableTotalLow = 4;

        thresholds.failedTotalAll = 5;
        thresholds.failedTotalHigh = 6;
        thresholds.failedTotalNormal = 7;
        thresholds.failedTotalLow = 8;

        thresholds.unstableNewAll = 11;
        thresholds.unstableNewHigh = 12;
        thresholds.unstableNewNormal = 13;
        thresholds.unstableNewLow = 14;

        thresholds.failedNewAll = 15;
        thresholds.failedNewHigh = 16;
        thresholds.failedNewNormal = 17;
        thresholds.failedNewLow = 18;

        QualityGate expected = createGateWithBuilder();
        assertThat(expected.isEnabled()).isTrue();

        assertThat(new QualityGate(thresholds)).isEqualTo(expected);
    }

    private QualityGate createGateWithBuilder() {
        QualityGateBuilder qBuilder = new QualityGateBuilder();
        ThresholdSetBuilder tBuilder = new ThresholdSetBuilder();

        tBuilder.setTotalThreshold(1).setHighThreshold(2).setNormalThreshold(3).setLowThreshold(4);
        qBuilder.setTotalUnstableThreshold(tBuilder.build());
        tBuilder.setTotalThreshold(5).setHighThreshold(6).setNormalThreshold(7).setLowThreshold(8);
        qBuilder.setTotalFailedThreshold(tBuilder.build());
        tBuilder.setTotalThreshold(11).setHighThreshold(12).setNormalThreshold(13).setLowThreshold(14);
        qBuilder.setNewUnstableThreshold(tBuilder.build());
        tBuilder.setTotalThreshold(15).setHighThreshold(16).setNormalThreshold(17).setLowThreshold(18);
        qBuilder.setNewFailedThreshold(tBuilder.build());

        return qBuilder.build();
    }

    @Test
    void shouldBeSuccessWhenNoThresholdIsSetApiDemonstration() {
        ThresholdSet noThresholdsDefined = new ThresholdSetBuilder()
                .setTotalThreshold(0)
                .setHighThreshold(0)
                .setNormalThreshold(0)
                .setLowThreshold(0).build();

        QualityGate qualityGate = new QualityGateBuilder()
                .setNewFailedThreshold(noThresholdsDefined)
                .setNewUnstableThreshold(noThresholdsDefined)
                .setTotalFailedThreshold(noThresholdsDefined)
                .setTotalUnstableThreshold(noThresholdsDefined)
                .build();

        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        when(run.getTotalSize()).thenReturn(1);

        assertThat(qualityGate.isEnabled()).isFalse();

        Result qualityGateResult = qualityGate.evaluate(run);
        assertThat(qualityGateResult).isEqualTo(Result.SUCCESS);
    }

    @Test
    void shouldBeSuccessWhenNoThresholdIsSet() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder();
        testThreshold(builder, 0, 0, 0, Result.SUCCESS);
    }

    @Test
    void shouldBeSuccessWhenNoIssuesPresentAndFailureQualityGateIsSet() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder()
                .setTotalFailedThreshold(s)
                .setTotalUnstableThreshold(s)
                .setNewFailedThreshold(s)
                .setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0, 0, Result.SUCCESS);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanFailedThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s);
        testThreshold(builder, 2, 1, 0, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenFailedThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s);
        testThreshold(builder, 2, 2, 0, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenFailedThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s);
        testThreshold(builder, 2, 3, 0, Result.FAILURE);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalUnstableThreshold(
                s);
        testThreshold(builder, 2, 1, 0, Result.SUCCESS);
    }

    @Test
    void shouldBeUnstableWhenUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalUnstableThreshold(
                s);
        testThreshold(builder, 2, 2, 0, Result.UNSTABLE);
    }

    @Test
    void shouldBeUnstableWhenUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalUnstableThreshold(
                s);
        testThreshold(builder, 2, 3, 0, Result.UNSTABLE);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanFailedAndUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 1, 0, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenFailedAndUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 2, 0, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenFailedAndUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 3, 0, Result.FAILURE);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanNewFailedThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s);
        testThreshold(builder, 2, 0, 1, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenNewFailedThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s);
        testThreshold(builder, 2, 0, 2, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenNewFailedThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s);
        testThreshold(builder, 2, 0, 3, Result.FAILURE);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanNewUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewUnstableThreshold(
                s);
        testThreshold(builder, 2, 0, 1, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenNewUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewUnstableThreshold(
                s);
        testThreshold(builder, 2, 0, 2, Result.UNSTABLE);
    }

    @Test
    void shouldBeFailedWhenNewUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewUnstableThreshold(
                s);
        testThreshold(builder, 2, 0, 3, Result.UNSTABLE);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanNewFailedAndUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0, 1, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0, 2, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0, 3, Result.FAILURE);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanNewFailedAndTotalUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 1, 1, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndTotalUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 2, 2, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndTotalUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 2, 3, Result.FAILURE);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanTotalFailedAndNewUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 1, 1, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenTotalFailedAndNewUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 2, 2, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenTotalFailedAndNewUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 2, 3, Result.FAILURE);
    }

    /**
     * Test the test set defined by the parameters against all four thresholds (total, high, normal, low). The test
     * method implements soft assertions. All assertions are executed even a assertion fails.
     *
     * @param builder
     *         function which sets the thresholds to test
     * @param threshold
     *         to set
     * @param totalWarningCount
     *         count of all warnings
     * @param newWarningCount
     *         count of new warnings
     * @param result
     *         expected result of the tests
     */
    private void testThreshold(Function<ThresholdSet, QualityGateBuilder> builder,
            final int threshold, final int totalWarningCount, final int newWarningCount, final Result result) {
        QualityGate qualityGateTotal = builder.apply(
                new ThresholdSetBuilder().setTotalThreshold(threshold).build()).build();
        QualityGate qualityGateHigh = builder.apply(
                new ThresholdSetBuilder().setTotalThreshold(Integer.MAX_VALUE).setHighThreshold(threshold).build())
                .build();
        QualityGate qualityGateNormal = builder.apply(
                new ThresholdSetBuilder().setTotalThreshold(Integer.MAX_VALUE).setNormalThreshold(threshold).build())
                .build();
        QualityGate qualityGateLow = builder.apply(
                new ThresholdSetBuilder().setTotalThreshold(Integer.MAX_VALUE).setLowThreshold(threshold).build())
                .build();

        StaticAnalysisRun runTotal = mock(StaticAnalysisRun.class);
        when(runTotal.getTotalSize()).thenReturn(totalWarningCount);
        when(runTotal.getNewSize()).thenReturn(newWarningCount);

        StaticAnalysisRun runHigh = mock(StaticAnalysisRun.class);
        when(runHigh.getTotalHighPrioritySize()).thenReturn(totalWarningCount);
        when(runHigh.getNewHighPrioritySize()).thenReturn(newWarningCount);

        StaticAnalysisRun runNormal = mock(StaticAnalysisRun.class);
        when(runNormal.getTotalNormalPrioritySize()).thenReturn(totalWarningCount);
        when(runNormal.getNewNormalPrioritySize()).thenReturn(newWarningCount);

        StaticAnalysisRun runLow = mock(StaticAnalysisRun.class);
        when(runLow.getTotalLowPrioritySize()).thenReturn(totalWarningCount);
        when(runLow.getNewLowPrioritySize()).thenReturn(newWarningCount);

        SoftAssertions.assertSoftly((softly) -> {
            softly.assertThat(qualityGateTotal.evaluate(runTotal))
                    .as("Threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                    .isEqualTo(result);
            softly.assertThat(qualityGateTotal.isEnabled()).isEqualTo(threshold > 0);

            softly.assertThat(qualityGateHigh.evaluate(runHigh))
                    .as("High priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                    .isEqualTo(result);
            softly.assertThat(qualityGateHigh.isEnabled()).isEqualTo(threshold > 0);

            softly.assertThat(qualityGateNormal.evaluate(runNormal))
                    .as("Normal priority threshold was <" + threshold + "> warning count was <" + totalWarningCount
                            + ">")
                    .isEqualTo(result);
            softly.assertThat(qualityGateNormal.isEnabled()).isEqualTo(threshold > 0);

            softly.assertThat(qualityGateLow.evaluate(runLow))
                    .as("Low priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                    .isEqualTo(result);
            softly.assertThat(qualityGateLow.isEnabled()).isEqualTo(threshold > 0);
        });
    }

    /** Verifies that a String input parameter is validated. */
    @ParameterizedTest
    @ValueSource(strings = {"Nothing", "2V3", "-1", "-2"})
    void shouldNotAcceptIllegalThresholdValues(final String threshold) {
        assertThatThrownBy(() -> new ThresholdSetBuilder().setTotalThreshold(threshold))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "[{index}] ''{0}''")
    @ValueSource(strings = {"", " ", "\t", "    "})
    void shouldConvertEmptyStringToZero(final String threshold) {
        ThresholdSet noThresholdsDefined = new ThresholdSetBuilder()
                .setTotalThreshold(threshold)
                .setHighThreshold(threshold)
                .setNormalThreshold(threshold)
                .setLowThreshold(threshold).build();

        QualityGate qualityGate = new QualityGateBuilder()
                .setNewFailedThreshold(noThresholdsDefined)
                .setNewUnstableThreshold(noThresholdsDefined)
                .setTotalFailedThreshold(noThresholdsDefined)
                .setTotalUnstableThreshold(noThresholdsDefined)
                .build();

        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        when(run.getTotalSize()).thenReturn(1);

        Result qualityGateResult = qualityGate.evaluate(run);
        assertThat(qualityGateResult).as("Does not convert %s to zero threshold").isEqualTo(Result.SUCCESS);
    }

}
