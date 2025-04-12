package io.jenkins.plugins.analysis.core.model; // NOPMD

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.hm.hafner.echarts.BuildResult;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Optional;
import java.util.stream.Stream;

import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.model.AnalysisHistoryTest.ExpectedResult.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link AnalysisHistory}.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"ParameterNumber", "PMD.UnusedPrivateMethod", "unchecked"})
@SuppressFBWarnings("UPM")
class AnalysisHistoryTest {
    /**
     * Creates a sequence of three failing builds. Verifies that the history contains all of these builds.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-41598">Issue 41598</a>
     */
    @Test
    void issue41598() {
        Run<?, ?> last = createFailingBuild();

        ResultAction lastAction = mock(ResultAction.class);
        AnalysisResult lastResult = mock(AnalysisResult.class);
        when(lastAction.getResult()).thenReturn(lastResult);
        when(lastAction.getOwner()).thenAnswer(a -> last);

        Run<?, ?> middle = createFailingBuild();
        ResultAction middleAction = mock(ResultAction.class);
        AnalysisResult middleResult = mock(AnalysisResult.class);
        when(middleAction.getResult()).thenReturn(middleResult);
        when(middleAction.getOwner()).thenAnswer(a -> middle);

        Run<?, ?> first = createFailingBuild();
        ResultAction firstAction = mock(ResultAction.class);
        AnalysisResult firstResult = mock(AnalysisResult.class);
        when(firstAction.getResult()).thenReturn(firstResult);
        when(firstAction.getOwner()).thenAnswer(a -> first);

        when(last.getPreviousBuild()).thenAnswer(a -> middle);
        when(middle.getPreviousBuild()).thenAnswer(a -> first);

        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(last)).thenReturn(Optional.of(lastAction));
        when(resultSelector.get(middle)).thenReturn(Optional.of(middleAction));
        when(resultSelector.get(first)).thenReturn(Optional.of(firstAction));

        var history = new AnalysisHistory(last, resultSelector);

        assertThat(history.iterator()).toIterable().extracting(BuildResult::getResult).containsExactly(lastResult, middleResult, firstResult);
        assertThat(history.hasMultipleResults()).isTrue();
    }

    private Run<?, ?> createFailingBuild() {
        return createBuildWithResult(Result.FAILURE);
    }

    @Test
    void firstBaselineShouldHaveNoPreviousResult() {
        Run<?, ?> baseline = mock(Run.class);
        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(baseline)).thenReturn(Optional.empty());

        var history = new AnalysisHistory(baseline, resultSelector);

        assertThat(history.getBaselineResult()).isEmpty();
        assertThat(history.getResult()).isEmpty();
        assertThat(history.getBuild()).isEmpty();
        assertThat(history.hasMultipleResults()).isFalse();
    }

    /**
     * In practice, the current build is used as the baseline and so has not yet attached a result. If this baseline
     * gets its first result attached, then the result is the same as the baseline result.
     */
    @Test
    void baselineResultIsPreviousResultIfAlreadySet() {
        Run<?, ?> baseline = mock(Run.class);
        ResultSelector resultSelector = mock(ResultSelector.class);
        ResultAction baselineAction = mock(ResultAction.class);
        AnalysisResult baselineResult = mock(AnalysisResult.class);
        when(baselineAction.getResult()).thenReturn(baselineResult);
        when(baselineAction.getOwner()).thenAnswer(a -> baseline);
        when(resultSelector.get(baseline)).thenReturn(Optional.of(baselineAction));

        var history = new AnalysisHistory(baseline, resultSelector);

        assertThat(history.getBaselineResult()).contains(baselineResult);
        assertThat(history.getResult()).contains(baselineResult);
        assertThat(history.getBuild()).contains(baseline);
        assertThat(history.hasMultipleResults()).isFalse();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("createTestDataForIgnoredQualityGateAndIgnoredBuildResult")
    @DisplayName("Ignore job result + ignore quality gate -> history with one previous build")
    void shouldTestFirstIterationOfLoopIgnoreStatusAndResult(final String name,
            final ExpectedResult expectedResult, final QualityGateResult qualityGateStatus, final Result jobStatus) {
        runTest(qualityGateStatus, jobStatus, expectedResult);
    }

    private void runTest(final QualityGateResult qualityGateStatus, final Result jobStatus, final ExpectedResult expectedResult) {
        ResultSelector resultSelector = mock(ResultSelector.class);
        Run<?, ?> baseline = createBuild(qualityGateStatus, jobStatus, resultSelector);

        var history = new AnalysisHistory(baseline, resultSelector);

        if (expectedResult == NONE) {
            assertThat(history.getResult()).isEmpty();
            assertThat(history.getBuild()).isEmpty();
        }
        else {
            assertThat(history.getResult()).isNotEmpty();
            assertThat(history.getBuild()).contains(baseline);
        }
    }

    private Run<?, ?> createBuild(final QualityGateResult qualityGateStatus, final Result jobStatus,
            final ResultSelector resultSelector) {
        Run<?, ?> baseline = createBuildWithResult(jobStatus);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getOwner()).thenAnswer(a -> baseline);
        when(result.getQualityGateResult()).thenReturn(qualityGateStatus);

        ResultAction resultAction = mock(ResultAction.class);
        when(resultAction.getResult()).thenReturn(result);
        when(resultAction.getOwner()).thenAnswer(a -> baseline);
        when(resultAction.isSuccessful()).thenReturn(qualityGateStatus.isSuccessful());

        when(resultSelector.get(baseline)).thenReturn(Optional.of(resultAction));

        return baseline;
    }

    private Run<?, ?> createBuildWithResult(final Result jobStatus) {
        Run<?, ?> baseline = mock(Run.class);
        when(baseline.getResult()).thenReturn(jobStatus);
        return baseline;
    }

    /**
     * Method to provide test element that return an present optional.
     *
     * @return list of test data objects
     */
    private static Stream<Arguments> createTestDataForSuccessfulQualityGateAndNoFailedBuild() {
        return Stream.of(
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have analysis result (SUCCESS, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have analysis result (SUCCESS, quality gate has been passed)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate has been passed)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.FAILED))
                        .setTestName("Job should have no analysis result if quality gate has been missed (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.WARNING))
                        .setTestName("Job should have no analysis result if quality gate has a warning (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have no analysis result even if quality gate is not active (FAILED)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have no analysis result even if quality gate has been passed (FAILED)")
                        .build()
        );
    }

    private static QualityGateResult createResult(final QualityGateStatus qualityGateStatus) {
        var result = new QualityGateResult();
        result.add(new WarningsQualityGate(0, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE),
                qualityGateStatus, "message");
        return result;
    }

    /**
     * Method to provide test element that return an present optional.
     *
     * @return list of test data objects
     */
    private static Stream<Arguments> createTestDataForIgnoredQualityGateAndIgnoredBuildResult() {
        return Stream.of(
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have analysis result (SUCCESS, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have analysis result (SUCCESS, quality gate has been passed)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate has been passed)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.FAILED))
                        .setTestName("Job should have analysis result if quality gate has been missed (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.WARNING))
                        .setTestName("Job should have analysis result if quality gate has a warning (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have analysis result even if quality gate is not active (FAILED)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have analysis result even if quality gate has been passed (FAILED)")
                        .build()
        );
    }

    /**
     * Method to provide test element that return a present optional.
     *
     * @return list of test data objects
     */
    private static Stream<Arguments> createTestDataForSuccessfulQualityGateAndIgnoredBuildResult() {
        return Stream.of(
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have analysis result (SUCCESS, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have analysis result (SUCCESS, quality gate has been passed)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate has been passed)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.FAILED))
                        .setTestName("Job should have no analysis result if quality gate has been missed (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.WARNING))
                        .setTestName("Job should have no analysis result if quality gate has a warning (SUCCESS)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have analysis result even if quality gate is not active (FAILED)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have analysis result even if quality gate has been passed (FAILED)")
                        .build()
        );
    }

    /**
     * Method to provide test element that return an present optional.
     *
     * @return list of test data objects
     */
    private static Stream<Arguments> createTestDataForIgnoredQualityGateAndNoFailedBuild() {
        return Stream.of(
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have analysis result (SUCCESS, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have analysis result (SUCCESS, quality gate has been passed)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate has been passed)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.FAILED))
                        .setTestName("Job should have analysis result if quality gate has been missed (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateResult(createResult(QualityGateStatus.WARNING))
                        .setTestName("Job should have analysis result if quality gate has a warning (SUCCESS)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateResult(createResult(QualityGateStatus.INACTIVE))
                        .setTestName("Job should have no analysis result even if quality gate is not active (FAILED)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateResult(createResult(QualityGateStatus.PASSED))
                        .setTestName("Job should have no analysis result even if quality gate has been passed (FAILED)")
                        .build()
        );
    }

    /**
     * Determines which of the builds is the expected result.
     */
    enum ExpectedResult {
        NONE,
        FIRST
    }

    /**
     * Builder for the arguments of the parameterized tests.
     */
    private static class BuildHistoryBuilder {
        private String testName;
        private ExpectedResult expectedResult;
        private QualityGateResult qualityGateResult;
        private Result jobResult;

        BuildHistoryBuilder setTestName(final String testName) {
            this.testName = testName;
            return this;
        }

        BuildHistoryBuilder setExpectedResult(final ExpectedResult expectedResult) {
            this.expectedResult = expectedResult;
            return this;
        }

        BuildHistoryBuilder setQualityGateResult(final QualityGateResult result) {
            this.qualityGateResult = result;
            return this;
        }

        BuildHistoryBuilder setJobResult(final Result jobResult) {
            this.jobResult = jobResult;
            return this;
        }

        /**
         * Build the tests argument.
         *
         * @return test arg
         */
        Arguments build() {
            return Arguments.of(testName, expectedResult, qualityGateResult, jobResult);
        }
    }
}
