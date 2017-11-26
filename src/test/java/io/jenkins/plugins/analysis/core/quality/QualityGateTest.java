package io.jenkins.plugins.analysis.core.quality;

import java.util.function.Function;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.quality.QualityGate.QualityGateBuilder;
import io.jenkins.plugins.analysis.core.quality.QualityGate.ThresholdSet;
import static org.mockito.Mockito.*;

import hudson.model.Result;

/**
 * Tests the class {@link QualityGate QualityGate}. These Tests are generated while developing.
 *
 * @author Michael Schmid
 */
class QualityGateTest {

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
    void shouldBeSuccessWhenFailedThresholdIsHigherThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(s);
        testThreshold(builder, 2, 1, 0, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenFailedThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(s);
        testThreshold(builder, 2, 2, 0, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenFailedThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(s);
        testThreshold(builder, 2, 3, 0, Result.FAILURE);
    }

    @Test
    void shouldBeSuccessWhenUnstableThresholdIsHigherThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 1, 0, Result.SUCCESS);
    }

    @Test
    void shouldBeUnstableWhenUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 2, 0, Result.UNSTABLE);
    }

    @Test
    void shouldBeUnstableWhenUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 3, 0, Result.UNSTABLE);
    }

    @Test
    void shouldBeSuccessWhenFailedAndUnstableThresholdIsHigherThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 1, 0, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenFailedAndUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 2, 0, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenFailedAndUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 3, 0, Result.FAILURE);
    }

    @Test
    void shouldBeSuccessWhenNewFailedThresholdIsHigherThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(s);
        testThreshold(builder, 2, 0,1, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenNewFailedThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(s);
        testThreshold(builder, 2, 0, 2, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenNewFailedThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(s);
        testThreshold(builder, 2, 0, 3, Result.FAILURE);
    }

    @Test
    void shouldBeSuccessWhenNewUnstableThresholdIsHigherThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0, 1, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenNewUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewUnstableThreshold(s);
        testThreshold(builder, 2,  0,2, Result.UNSTABLE);
    }

    @Test
    void shouldBeFailedWhenNewUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0,3, Result.UNSTABLE);
    }

    @Test
    void shouldBeSuccessWhenNewFailedAndUnstableThresholdIsHigherThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0,1, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0,2, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 0,3, Result.FAILURE);
    }

    @Test
    void shouldBeSuccessWhenNewFailedAndTotalUnstableThresholdIsHigherThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 1,1, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndTotalUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 2, 2, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenNewFailedAndTotalUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setNewFailedThreshold(s).setTotalUnstableThreshold(s);
        testThreshold(builder, 2, 2, 3, Result.FAILURE);
    }

    @Test
    void shouldBeSuccessWhenTotalFailedAndNewUnstableThresholdIsHigherThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 1,1, Result.SUCCESS);
    }

    @Test
    void shouldBeFailedWhenTotalFailedAndNewUnstableThresholdIsEqualThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 2, 2, Result.FAILURE);
    }

    @Test
    void shouldBeFailedWhenTotalFailedAndNewUnstableThresholdIsLowerThanWarningCount() {
        Function<ThresholdSet, QualityGateBuilder> builder = (ThresholdSet s) -> new QualityGateBuilder().setTotalFailedThreshold(s).setNewUnstableThreshold(s);
        testThreshold(builder, 2, 2, 3, Result.FAILURE);
    }


    /**
     * Test the test set defined by the parameters against all four thresholds (total, high, normal, low).
     * The test method implements soft assertions. All assertions are executed even a assertion fails.
     * @param builder function which sets the thresholds to test
     * @param threshold to set
     * @param totalWarningCount count of all warnings
     * @param newWarningCount count of new warnings
     * @param result expected result of the tests
     */
    private void testThreshold(Function<ThresholdSet, QualityGateBuilder> builder, final int threshold, final int totalWarningCount, final int newWarningCount, final Result result) {
        QualityGate qualityGateTotal = builder.apply(new ThresholdSet(threshold, 0, 0, 0)).build();
        QualityGate qualityGateHigh = builder.apply(new ThresholdSet(Integer.MAX_VALUE, threshold, 0, 0)).build();
        QualityGate qualityGateNormal = builder.apply(new ThresholdSet(Integer.MAX_VALUE, 0, threshold, 0)).build();
        QualityGate qualityGateLow = builder.apply(new ThresholdSet(Integer.MAX_VALUE, 0, 0, threshold)).build();

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

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(qualityGateTotal.evaluate(runTotal))
                .as("Threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .isEqualTo(result);
        softly.assertThat(qualityGateHigh.evaluate(runHigh))
                .as("High priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .isEqualTo(result);
        softly.assertThat(qualityGateNormal.evaluate(runNormal))
                .as("Normal priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .isEqualTo(result);
        softly.assertThat(qualityGateLow.evaluate(runLow))
                .as("Low priority threshold was <" + threshold + "> warning count was <" + totalWarningCount + ">")
                .isEqualTo(result);
        softly.assertAll();
    }


}
