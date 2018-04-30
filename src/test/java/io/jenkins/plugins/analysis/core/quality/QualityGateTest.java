package io.jenkins.plugins.analysis.core.quality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.util.SerializableTest;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.quality.QualityGate.QualityGateBuilder;
import io.jenkins.plugins.analysis.core.quality.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.quality.ThresholdSet.ThresholdResult;
import io.jenkins.plugins.analysis.core.quality.ThresholdSet.ThresholdSetBuilder;
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

        AnalysisResult run = mock(AnalysisResult.class);
        when(run.getTotalSize()).thenReturn(1);

        assertThat(qualityGate).isNotEnabled();

        QualityGateResult qualityGateResult = qualityGate.evaluate(run);
        assertThat(qualityGateResult).hasOverallResult(Result.SUCCESS);
        assertThat(qualityGateResult.getEvaluations(mock(AnalysisResult.class), qualityGate)).isEmpty();
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

    @Test
    void shouldReturnAnEmptyEvaluation() {
        QualityGateResult qualityGateResult = mock(QualityGateResult.class);
        AnalysisResult analysisResult = mock(AnalysisResult.class);

        assertThat(qualityGateResult.getEvaluations(analysisResult, this.createGateWithBuilder())).isEmpty();
    }

    @Test
    void shouldReturnEvaluationForIsTotalReached() {
        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);

        when(totalThresholdResult.isTotalReached()).thenReturn(true);
        when(totalUnstableThresholdResult.isTotalReached()).thenReturn(true);
        when(newFailedThresholdResult.isTotalReached()).thenReturn(true);
        when(newUnstableThresholdResult.isTotalReached()).thenReturn(true);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getTotalSize()).thenReturn(10);

        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
                newFailedThresholdResult, newUnstableThresholdResult);

        QualityGate qualityGate = this.createGateWithBuilder();

        ArrayList<String> evaluations = (ArrayList<String>) qualityGateResult.getEvaluations(analysisResult,
                qualityGate);
        assertThat(evaluations).hasSize(4);

        assertThat(evaluations.get(0)).contains("FAILURE");
        assertThat(evaluations.get(1)).contains("UNSTABLE");
        assertThat(evaluations.get(2)).contains("FAILURE");
        assertThat(evaluations.get(3)).contains("UNSTABLE");
    }

    @Test
    void shouldReturnEvaluationForIsHighReached() {
        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);

        when(totalThresholdResult.isHighReached()).thenReturn(true);
        when(totalUnstableThresholdResult.isHighReached()).thenReturn(true);
        when(newFailedThresholdResult.isHighReached()).thenReturn(true);
        when(newUnstableThresholdResult.isHighReached()).thenReturn(true);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getTotalSize()).thenReturn(10);

        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
                newFailedThresholdResult, newUnstableThresholdResult);

        QualityGate qualityGate = this.createGateWithBuilder();

        ArrayList<String> evaluations = (ArrayList<String>) qualityGateResult.getEvaluations(analysisResult,
                qualityGate);
        assertThat(evaluations).hasSize(4);

        assertThat(evaluations.get(0)).contains("FAILURE");
        assertThat(evaluations.get(1)).contains("UNSTABLE");
        assertThat(evaluations.get(2)).contains("FAILURE");
        assertThat(evaluations.get(3)).contains("UNSTABLE");

    }

    @Test
    void shouldReturnEvaluationForIsNormalReached() {
        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);

        when(totalThresholdResult.isNormalReached()).thenReturn(true);
        when(totalUnstableThresholdResult.isNormalReached()).thenReturn(true);
        when(newFailedThresholdResult.isNormalReached()).thenReturn(true);
        when(newUnstableThresholdResult.isNormalReached()).thenReturn(true);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getTotalSize()).thenReturn(10);

        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
                newFailedThresholdResult, newUnstableThresholdResult);

        QualityGate qualityGate = this.createGateWithBuilder();

        ArrayList<String> evaluations = (ArrayList<String>) qualityGateResult.getEvaluations(analysisResult,
                qualityGate);
        assertThat(evaluations).hasSize(4);

        assertThat(evaluations.get(0)).contains("FAILURE");
        assertThat(evaluations.get(1)).contains("UNSTABLE");
        assertThat(evaluations.get(2)).contains("FAILURE");
        assertThat(evaluations.get(3)).contains("UNSTABLE");
    }

    @Test
    void shouldReturnEvaluationForIsLowReached() {
        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);

        when(totalThresholdResult.isLowReached()).thenReturn(true);
        when(totalUnstableThresholdResult.isLowReached()).thenReturn(true);
        when(newFailedThresholdResult.isLowReached()).thenReturn(true);
        when(newUnstableThresholdResult.isLowReached()).thenReturn(true);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getTotalSize()).thenReturn(10);

        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
                newFailedThresholdResult, newUnstableThresholdResult);

        QualityGate qualityGate = this.createGateWithBuilder();

        ArrayList<String> evaluations = (ArrayList<String>) qualityGateResult.getEvaluations(analysisResult,
                qualityGate);
        assertThat(evaluations).hasSize(4);

        assertThat(evaluations.get(0)).contains("FAILURE");
        assertThat(evaluations.get(1)).contains("UNSTABLE");
        assertThat(evaluations.get(2)).contains("FAILURE");
        assertThat(evaluations.get(3)).contains("UNSTABLE");
    }

    @Test
    void shouldReturnEvaluationForTotalFailed() {
        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);

        when(totalThresholdResult.isTotalReached()).thenReturn(true);
        when(totalThresholdResult.isHighReached()).thenReturn(true);
        when(totalThresholdResult.isNormalReached()).thenReturn(true);
        when(totalThresholdResult.isLowReached()).thenReturn(true);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getTotalSize()).thenReturn(15);

        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
                newFailedThresholdResult, newUnstableThresholdResult);

        QualityGate qualityGate = this.createGateWithBuilder();

        Collection<String> evaluations = qualityGateResult.getEvaluations(analysisResult, qualityGate);
        assertThat(evaluations).hasSize(4);

        testEvaluationMessageAllFailure(evaluations);
    }

    @Test
    void shouldReturnEvaluationForNewFailed() {
        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);

        when(newFailedThresholdResult.isTotalReached()).thenReturn(true);
        when(newFailedThresholdResult.isHighReached()).thenReturn(true);
        when(newFailedThresholdResult.isNormalReached()).thenReturn(true);
        when(newFailedThresholdResult.isLowReached()).thenReturn(true);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getTotalSize()).thenReturn(10);

        testEvaluation(totalThresholdResult, totalUnstableThresholdResult, newFailedThresholdResult,
                newUnstableThresholdResult, analysisResult, false);
    }

    @Test
    void shouldReturnEvaluationForNewUnstable() {
        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);

        when(newUnstableThresholdResult.isTotalReached()).thenReturn(true);
        when(newUnstableThresholdResult.isHighReached()).thenReturn(true);
        when(newUnstableThresholdResult.isNormalReached()).thenReturn(true);
        when(newUnstableThresholdResult.isLowReached()).thenReturn(true);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getTotalSize()).thenReturn(10);

        testEvaluation(totalThresholdResult, totalUnstableThresholdResult, newFailedThresholdResult,
                newUnstableThresholdResult, analysisResult, true);
    }

    /**
     * Test the evaluation if all messages are failure messages.
     *
     * @param messages
     *         collection of evaluation messages
     */
    private void testEvaluationMessageAllFailure(Collection<String> messages) {
        ArrayList<String> evaluations = (ArrayList<String>) messages;
        for (String message : evaluations) {
            assertThat(message).contains("FAILURE");
        }
    }

    /**
     * Test the evaluation if all messages are unstable messages.
     *
     * @param messages
     *         collection of evaluation messages
     */
    private void testEvaluationMessageAllUnstable(Collection<String> messages) {
        ArrayList<String> evaluations = (ArrayList<String>) messages;
        for (String message : evaluations) {
            assertThat(message).contains("UNSTABLE");
        }
    }

    /**
     * Test the test setup defined by the parameter.
     *
     * @param totalThresholdResult
     *         the threshold result for total
     * @param totalUnstableThresholdResult
     *         the threshold result for total unstable
     * @param newFailedThresholdResult
     *         the threshold result for new failed
     * @param newUnstableThresholdResult
     *         the threshold result for new unstable
     * @param analysisResult
     *         the analysis result
     * @param flag
     *         true if  test should test if all unstable messages otherwise if all failure messages
     */
    private void testEvaluation(ThresholdResult totalThresholdResult, ThresholdResult totalUnstableThresholdResult,
            ThresholdResult newFailedThresholdResult, ThresholdResult newUnstableThresholdResult,
            AnalysisResult analysisResult, boolean flag) {
        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
                newFailedThresholdResult, newUnstableThresholdResult);

        QualityGate qualityGate = this.createGateWithBuilder();

        Collection<String> evaluations = qualityGateResult.getEvaluations(analysisResult, qualityGate);
        assertThat(evaluations).hasSize(4);

        if (flag) {
            testEvaluationMessageAllUnstable(evaluations);
        }
        else {
            testEvaluationMessageAllFailure(evaluations);
        }

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

        AnalysisResult runTotal = mock(AnalysisResult.class);
        when(runTotal.getTotalSize()).thenReturn(totalWarningCount);
        when(runTotal.getNewSize()).thenReturn(newWarningCount);

        AnalysisResult runHigh = mock(AnalysisResult.class);
        when(runHigh.getTotalHighPrioritySize()).thenReturn(totalWarningCount);
        when(runHigh.getNewHighPrioritySize()).thenReturn(newWarningCount);

        AnalysisResult runNormal = mock(AnalysisResult.class);
        when(runNormal.getTotalNormalPrioritySize()).thenReturn(totalWarningCount);
        when(runNormal.getNewNormalPrioritySize()).thenReturn(newWarningCount);

        AnalysisResult runLow = mock(AnalysisResult.class);
        when(runLow.getTotalLowPrioritySize()).thenReturn(totalWarningCount);
        when(runLow.getNewLowPrioritySize()).thenReturn(newWarningCount);

        assertThat(qualityGateTotal.evaluate(runTotal))
                .as("Threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .hasOverallResult(result);
        assertThat(qualityGateTotal.isEnabled()).isEqualTo(threshold > 0);

        assertThat(qualityGateHigh.evaluate(runHigh))
                .as("High priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .hasOverallResult(result);
        assertThat(qualityGateHigh.isEnabled()).isEqualTo(threshold > 0);

        assertThat(qualityGateNormal.evaluate(runNormal))
                .as("Normal priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .hasOverallResult(result);
        assertThat(qualityGateNormal.isEnabled()).isEqualTo(threshold > 0);

        assertThat(qualityGateLow.evaluate(runLow))
                .as("Low priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .hasOverallResult(result);
        assertThat(qualityGateLow.isEnabled()).isEqualTo(threshold > 0);
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

        AnalysisResult run = mock(AnalysisResult.class);
        when(run.getTotalSize()).thenReturn(1);

        QualityGateResult qualityGateResult = qualityGate.evaluate(run);
        assertThat(qualityGateResult).as("Does not convert %s to zero threshold")
                .hasOverallResult(Result.SUCCESS);
    }

}
