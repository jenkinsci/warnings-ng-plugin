package io.jenkins.plugins.analysis.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.SerializableTest;
import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

import io.jenkins.plugins.analysis.core.model.DeltaReport;
import io.jenkins.plugins.analysis.core.util.QualityGate;
import io.jenkins.plugins.analysis.core.util.QualityGate.FormattedLogger;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateBuilder;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.core.util.ThresholdSet;
import io.jenkins.plugins.analysis.core.util.ThresholdSet.ThresholdSetBuilder;
import io.jenkins.plugins.analysis.core.util.Thresholds;

import static org.mockito.Mockito.*;

/**
 * Tests the class {@link QualityGate}.
 *
 * @author Michael Schmid
 * @author Alexandra Wenzel
 */
class QualityGateTest extends SerializableTest<QualityGate> {
    /** The message state. */
    private enum State {
        FAILURE,
        UNSTABLE
    }

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

        DeltaReport run = mock(DeltaReport.class);
        when(run.getTotalSize()).thenReturn(1);

        assertThat(qualityGate).isNotEnabled();

        FormattedLogger logger = mock(FormattedLogger.class);
        QualityGateStatus qualityGateStatus = qualityGate.evaluate(run, logger);
        assertThat(qualityGateStatus).isEqualTo(QualityGateStatus.PASSED);
        verifyZeroInteractions(logger);
    }
    
    @Test
    void shouldBeFailureWhenAllThresholdsReached() {
        ThresholdSet thresholdsDefined = new ThresholdSetBuilder()
                .setTotalThreshold(1)
                .setHighThreshold(1)
                .setNormalThreshold(1)
                .setLowThreshold(1).build();

        QualityGate qualityGate = new QualityGateBuilder()
                .setNewFailedThreshold(thresholdsDefined)
                .setNewUnstableThreshold(thresholdsDefined)
                .setTotalFailedThreshold(thresholdsDefined)
                .setTotalUnstableThreshold(thresholdsDefined)
                .build();

        DeltaReport run = mock(DeltaReport.class);
        when(run.getTotalSize()).thenReturn(1);
        when(run.getTotalSizeOf(any())).thenReturn(1);
        when(run.getNewSize()).thenReturn(1);
        when(run.getNewSizeOf(any())).thenReturn(1);

        assertThat(qualityGate).isEnabled();

        Logger logger = new Logger();
        QualityGateStatus qualityGateStatus = qualityGate.evaluate(run, logger);
        assertThat(qualityGateStatus).isEqualTo(QualityGateStatus.FAILED);
        
        assertThat(logger.getMessages()).containsExactly(
                "-> FAILED - Total number of issues: 1 - Quality Gate: 1",
                "-> FAILED - Total number of issues (Severity High): 1 - Quality Gate: 1",
                "-> FAILED - Total number of issues (Severity Normal): 1 - Quality Gate: 1",
                "-> FAILED - Total number of issues (Severity Low): 1 - Quality Gate: 1",
                "-> FAILED - Number of new issues: 1 - Quality Gate: 1",
                "-> FAILED - Number of new issues (Severity High): 1 - Quality Gate: 1",
                "-> FAILED - Number of new issues (Severity Normal): 1 - Quality Gate: 1",
                "-> FAILED - Number of new issues (Severity Low): 1 - Quality Gate: 1");
    }
    
    @Test
    void shouldBeSuccessWhenNoThresholdIsSet() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder();
        testThreshold(builder, 0, 0, 0, QualityGateStatus.PASSED);
    }

    @Test
    void shouldBeSuccessWhenNoIssuesPresentAndFailureQualityGateIsSet() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder()
                .setTotalFailedThreshold(s)
                .setTotalUnstableThreshold(s)
                .setNewFailedThreshold(s)
                .setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0, 0, QualityGateStatus.PASSED);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanFailedThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s);
        testThreshold(builder, 2, 1, 0, QualityGateStatus.PASSED);
    }

    @Test
    void shouldBeFailedWhenFailedThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s);
        testThreshold(builder, 2, 2, 0, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeFailedWhenFailedThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s);
        testThreshold(builder, 2, 3, 0, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalUnstableThreshold(
                s);
        testThreshold(builder, 2, 1, 0, QualityGateStatus.PASSED);
    }

    @Test
    void shouldBeUnstableWhenUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalUnstableThreshold(
                s);
        testThreshold(builder, 2, 2, 0, QualityGateStatus.WARNING);
    }

    @Test
    void shouldBeUnstableWhenUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalUnstableThreshold(
                s);
        testThreshold(builder, 2, 3, 0, QualityGateStatus.WARNING);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanFailedAndUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 1, 0, QualityGateStatus.PASSED);
    }

    @Test
    void shouldBeFailedWhenFailedAndUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 2, 0, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeFailedWhenFailedAndUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 3, 0, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanNewFailedThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s);
        testThreshold(builder, 2, 0, 1, QualityGateStatus.PASSED);
    }

    @Test
    void shouldBeFailedWhenNewFailedThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s);
        testThreshold(builder, 2, 0, 2, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeFailedWhenNewFailedThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s);
        testThreshold(builder, 2, 0, 3, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanNewUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewUnstableThreshold(
                s);
        testThreshold(builder, 2, 0, 1, QualityGateStatus.PASSED);
    }

    @Test
    void shouldBeFailedWhenNewUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewUnstableThreshold(
                s);
        testThreshold(builder, 2, 0, 2, QualityGateStatus.WARNING);
    }

    @Test
    void shouldBeFailedWhenNewUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewUnstableThreshold(
                s);
        testThreshold(builder, 2, 0, 3, QualityGateStatus.WARNING);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanNewFailedAndUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0, 1, QualityGateStatus.PASSED);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0, 2, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0, 3, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanNewFailedAndTotalUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 1, 1, QualityGateStatus.PASSED);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndTotalUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 2, 2, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndTotalUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(
                s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 2, 3, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeSuccessWhenWarningCountIsLowerThanTotalFailedAndNewUnstableThreshold() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 1, 1, QualityGateStatus.PASSED);
    }

    @Test
    void shouldBeFailedWhenTotalFailedAndNewUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 2, 2, QualityGateStatus.FAILED);
    }

    @Test
    void shouldBeFailedWhenTotalFailedAndNewUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(
                s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 2, 3, QualityGateStatus.FAILED);
    }

//    @Test
//    void shouldReturnAnEmptyEvaluation() {
//        QualityGateResult qualityGateResult = mock(QualityGateResult.class);
//        AnalysisResult analysisResult = mock(AnalysisResult.class);
//
//        assertThat(qualityGateResult.getEvaluations(analysisResult, createGateWithBuilder())).isEmpty();
//    }
//
//    @Test
//    void shouldReturnEvaluationForIsTotalReached() {
//        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);
//
//        when(totalThresholdResult.isTotalReached()).thenReturn(true);
//        when(totalUnstableThresholdResult.isTotalReached()).thenReturn(true);
//        when(newFailedThresholdResult.isTotalReached()).thenReturn(true);
//        when(newUnstableThresholdResult.isTotalReached()).thenReturn(true);
//
//        AnalysisResult analysisResult = mock(AnalysisResult.class);
//        when(analysisResult.getTotalSize()).thenReturn(10);
//
//        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
//                newFailedThresholdResult, newUnstableThresholdResult);
//
//        QualityGate qualityGate = createGateWithBuilder();
//
//        List<String> evaluations = qualityGateResult.getEvaluations(analysisResult,
//                qualityGate);
//
//        testEvaluationMessages(evaluations);
//    }
//
//    @Test
//    void shouldReturnEvaluationForIsHighReached() {
//        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);
//
//        when(totalThresholdResult.isHighReached()).thenReturn(true);
//        when(totalUnstableThresholdResult.isHighReached()).thenReturn(true);
//        when(newFailedThresholdResult.isHighReached()).thenReturn(true);
//        when(newUnstableThresholdResult.isHighReached()).thenReturn(true);
//
//        AnalysisResult analysisResult = mock(AnalysisResult.class);
//        when(analysisResult.getTotalSize()).thenReturn(10);
//
//        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
//                newFailedThresholdResult, newUnstableThresholdResult);
//
//        QualityGate qualityGate = createGateWithBuilder();
//
//        List<String> evaluations = qualityGateResult.getEvaluations(analysisResult,
//                qualityGate);
//
//        testEvaluationMessages(evaluations);
//
//    }
//
//    @Test
//    void shouldReturnEvaluationForIsNormalReached() {
//        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);
//
//        when(totalThresholdResult.isNormalReached()).thenReturn(true);
//        when(totalUnstableThresholdResult.isNormalReached()).thenReturn(true);
//        when(newFailedThresholdResult.isNormalReached()).thenReturn(true);
//        when(newUnstableThresholdResult.isNormalReached()).thenReturn(true);
//
//        AnalysisResult analysisResult = mock(AnalysisResult.class);
//        when(analysisResult.getTotalSize()).thenReturn(10);
//
//        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
//                newFailedThresholdResult, newUnstableThresholdResult);
//
//        QualityGate qualityGate = createGateWithBuilder();
//
//        List<String> evaluations = qualityGateResult.getEvaluations(analysisResult,
//                qualityGate);
//
//        testEvaluationMessages(evaluations);
//    }
//
//    @Test
//    void shouldReturnEvaluationForIsLowReached() {
//        ThresholdResult totalThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult totalUnstableThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult newFailedThresholdResult = mock(ThresholdResult.class);
//        ThresholdResult newUnstableThresholdResult = mock(ThresholdResult.class);
//
//        when(totalThresholdResult.isLowReached()).thenReturn(true);
//        when(totalUnstableThresholdResult.isLowReached()).thenReturn(true);
//        when(newFailedThresholdResult.isLowReached()).thenReturn(true);
//        when(newUnstableThresholdResult.isLowReached()).thenReturn(true);
//
//        AnalysisResult analysisResult = mock(AnalysisResult.class);
//        when(analysisResult.getTotalSize()).thenReturn(10);
//
//        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
//                newFailedThresholdResult, newUnstableThresholdResult);
//
//        QualityGate qualityGate = createGateWithBuilder();
//
//        List<String> evaluations = qualityGateResult.getEvaluations(analysisResult,
//                qualityGate);
//
//        testEvaluationMessages(evaluations);
//    }
//
//    @Test
//    void shouldReturnEvaluationsForAllReachedTrueAndFalse() {
//        ThresholdResult resultAllReachedTrue = mock(ThresholdResult.class);
//        ThresholdResult resultAllReachedFalse = mock(ThresholdResult.class);
//
//        when(resultAllReachedTrue.isTotalReached()).thenReturn(true);
//        when(resultAllReachedTrue.isHighReached()).thenReturn(true);
//        when(resultAllReachedTrue.isNormalReached()).thenReturn(true);
//        when(resultAllReachedTrue.isLowReached()).thenReturn(true);
//
//        when(resultAllReachedFalse.isTotalReached()).thenReturn(false);
//        when(resultAllReachedFalse.isHighReached()).thenReturn(false);
//        when(resultAllReachedFalse.isNormalReached()).thenReturn(false);
//        when(resultAllReachedFalse.isLowReached()).thenReturn(false);
//
//        AnalysisResult analysisResult = mock(AnalysisResult.class);
//        when(analysisResult.getTotalSize()).thenReturn(10);
//
//        testEvaluation(resultAllReachedFalse, resultAllReachedFalse, resultAllReachedTrue,
//                resultAllReachedFalse, analysisResult, State.FAILURE);
//        testEvaluation(resultAllReachedFalse, resultAllReachedTrue, resultAllReachedFalse,
//                resultAllReachedFalse, analysisResult, State.UNSTABLE);
//        testEvaluation(resultAllReachedTrue, resultAllReachedFalse, resultAllReachedFalse,
//                resultAllReachedFalse, analysisResult, State.FAILURE);
//        testEvaluation(resultAllReachedFalse, resultAllReachedFalse, resultAllReachedFalse,
//                resultAllReachedTrue, analysisResult, State.UNSTABLE);
//
//    }
//
//    /**
//     * Test the evaluation if all messages are the same type as message state.
//     *
//     * @param messages
//     *         collection of evaluation messages
//     * @param state
//     *         the message state
//     */
//    private void testEvaluationMessages(final List<String> messages, final State state) {
//        for (String message : messages) {
//            assertThat(message).contains(state.name());
//        }
//    }
//
//    /**
//     * Test the evaluation if the message contains either FAILURE or UNSTABLE based on the index of the evaluation
//     * list.
//     *
//     * @param messages
//     *         list of evaluation messages
//     */
//    private void testEvaluationMessages(final List<String> messages) {
//        assertThat(messages).hasSize(4);
//        int counter = 0;
//        for (String message : messages) {
//            if (counter % 2 == 0) {
//                assertThat(message).contains(State.FAILURE.name());
//            }
//            else {
//                assertThat(message).contains(State.UNSTABLE.name());
//            }
//            counter++;
//        }
//    }
//
//    /**
//     * Test the test setup defined by the parameter.
//     *
//     * @param totalThresholdResult
//     *         the threshold result for total
//     * @param totalUnstableThresholdResult
//     *         the threshold result for total unstable
//     * @param newFailedThresholdResult
//     *         the threshold result for new failed
//     * @param newUnstableThresholdResult
//     *         the threshold result for new unstable
//     * @param analysisResult
//     *         the analysis result
//     * @param state
//     *         the state of the evaluation messages
//     */
//    private void testEvaluation(final ThresholdResult totalThresholdResult, final ThresholdResult totalUnstableThresholdResult,
//            final ThresholdResult newFailedThresholdResult, final ThresholdResult newUnstableThresholdResult,
//            final AnalysisResult analysisResult, final State state) {
//        QualityGateResult qualityGateResult = new QualityGateResult(totalThresholdResult, totalUnstableThresholdResult,
//                newFailedThresholdResult, newUnstableThresholdResult);
//
//        QualityGate qualityGate = createGateWithBuilder();
//
//        List<String> evaluations = qualityGateResult.getEvaluations(analysisResult, qualityGate);
//        assertThat(evaluations).hasSize(4);
//        testEvaluationMessages(evaluations, state);
//    }
//
//   
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
     * @param qualityGateStatus
     *         expected result of the tests
     */
    private void testThreshold(final Function<ThresholdSet, QualityGateBuilder> builder,
            final int threshold, final int totalWarningCount, final int newWarningCount, final QualityGateStatus qualityGateStatus) {
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

        DeltaReport runTotal = mock(DeltaReport.class);
        when(runTotal.getTotalSize()).thenReturn(totalWarningCount);
        when(runTotal.getNewSize()).thenReturn(newWarningCount);

        DeltaReport runHigh = mock(DeltaReport.class);
        when(runHigh.getTotalSizeOf(Severity.WARNING_HIGH)).thenReturn(totalWarningCount);
        when(runHigh.getNewSizeOf(Severity.WARNING_HIGH)).thenReturn(newWarningCount);

        DeltaReport runNormal = mock(DeltaReport.class);
        when(runNormal.getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(totalWarningCount);
        when(runNormal.getNewSizeOf(Severity.WARNING_NORMAL)).thenReturn(newWarningCount);

        DeltaReport runLow = mock(DeltaReport.class);
        when(runLow.getTotalSizeOf(Severity.WARNING_LOW)).thenReturn(totalWarningCount);
        when(runLow.getNewSizeOf(Severity.WARNING_LOW)).thenReturn(newWarningCount);

        Logger logger = new Logger();
        assertThat(qualityGateTotal.evaluate(runTotal, logger))
                .as("Threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .isEqualTo(qualityGateStatus);
        assertThat(qualityGateTotal.isEnabled()).isEqualTo(threshold > 0);
        assertThatLogMessageIsCorrect(qualityGateStatus, logger);

        logger = new Logger();
        assertThat(qualityGateHigh.evaluate(runHigh, logger))
                .as("High priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .isEqualTo(qualityGateStatus);
        assertThat(qualityGateHigh.isEnabled()).isEqualTo(threshold > 0);
        assertThatLogMessageIsCorrect(qualityGateStatus, logger);

        logger = new Logger();
        assertThat(qualityGateNormal.evaluate(runNormal, logger))
                .as("Normal priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .isEqualTo(qualityGateStatus);
        assertThat(qualityGateNormal.isEnabled()).isEqualTo(threshold > 0);
        assertThatLogMessageIsCorrect(qualityGateStatus, logger);

        logger = new Logger();
        assertThat(qualityGateLow.evaluate(runLow, logger))
                .as("Low priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .isEqualTo(qualityGateStatus);
        assertThat(qualityGateLow.isEnabled()).isEqualTo(threshold > 0);
        assertThatLogMessageIsCorrect(qualityGateStatus, logger);
    }

    private void assertThatLogMessageIsCorrect(final QualityGateStatus qualityGateStatus, final Logger logger) {
        if (!qualityGateStatus.isSuccessful()) {
            assertThat(logger.getMessages()).anySatisfy(message -> assertThat(message).startsWith("-> " + qualityGateStatus.name()));
        }
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

        DeltaReport run = mock(DeltaReport.class);
        when(run.getTotalSize()).thenReturn(1);

        QualityGateStatus qualityGateResult = qualityGate.evaluate(run, mock(FormattedLogger.class));
        assertThat(qualityGateResult).as("Does not convert %s to zero threshold")
                .isEqualTo(QualityGateStatus.PASSED);
    }

    private static class Logger implements FormattedLogger {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void print(final String format, final Object... args) {
            messages.add(String.format(format, args));
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
