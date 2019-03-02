package io.jenkins.plugins.analysis.core.model; // NOPMD

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.core.model.AnalysisHistory.JobResultEvaluationMode;
import io.jenkins.plugins.analysis.core.model.AnalysisHistory.QualityGateEvaluationMode;

import static io.jenkins.plugins.analysis.core.model.AnalysisHistory.JobResultEvaluationMode.*;
import static io.jenkins.plugins.analysis.core.model.AnalysisHistory.QualityGateEvaluationMode.*;
import static io.jenkins.plugins.analysis.core.model.AnalysisHistoryTest.ExpectedResult.*;
import static java.util.Arrays.*;
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
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-41598">Issue 41598</a>
     */
    @Test
    void issue41598() {
        Run last = createFailingBuild();

        ResultAction lastAction = mock(ResultAction.class);
        AnalysisResult lastResult = mock(AnalysisResult.class);
        when(lastAction.getResult()).thenReturn(lastResult);
        when(lastAction.getOwner()).thenReturn(last);

        Run middle = createFailingBuild();
        ResultAction middleAction = mock(ResultAction.class);
        AnalysisResult middleResult = mock(AnalysisResult.class);
        when(middleAction.getResult()).thenReturn(middleResult);
        when(middleAction.getOwner()).thenReturn(middle);

        Run first = createFailingBuild();
        ResultAction firstAction = mock(ResultAction.class);
        AnalysisResult firstResult = mock(AnalysisResult.class);
        when(firstAction.getResult()).thenReturn(firstResult);
        when(firstAction.getOwner()).thenReturn(first);

        when(last.getPreviousBuild()).thenReturn(middle);
        when(middle.getPreviousBuild()).thenReturn(first);

        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(last)).thenReturn(Optional.of(lastAction));
        when(resultSelector.get(middle)).thenReturn(Optional.of(middleAction));
        when(resultSelector.get(first)).thenReturn(Optional.of(firstAction));

        AnalysisHistory history = new AnalysisHistory(last, resultSelector);

        assertThat(history.iterator()).toIterable().containsExactly(lastResult, middleResult, firstResult);
    }

    private Run createFailingBuild() {
        return createBuildWithResult(Result.FAILURE);
    }

    @Test
    void firstBaselineShouldHaveNoPreviousResult() {
        Run baseline = mock(Run.class);
        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(baseline)).thenReturn(Optional.empty());

        AnalysisHistory history = new AnalysisHistory(baseline, resultSelector);

        assertThat(history.getBaselineResult()).isEmpty();
        assertThat(history.getResult()).isEmpty();
        assertThat(history.getBuild()).isEmpty();
    }

    /**
     * In practice, the current build is used as the baseline and so has not yet attached a result. If this baseline
     * gets its first result attached, then the result is the same as the baseline result.
     */
    @Test
    void baselineResultIsPreviousResultIfAlreadySet() {
        Run baseline = mock(Run.class);
        ResultSelector resultSelector = mock(ResultSelector.class);
        ResultAction baselineAction = mock(ResultAction.class);
        AnalysisResult baselineResult = mock(AnalysisResult.class);
        when(baselineAction.getResult()).thenReturn(baselineResult);
        when(baselineAction.getOwner()).thenReturn(baseline);
        when(resultSelector.get(baseline)).thenReturn(Optional.of(baselineAction));

        AnalysisHistory history = new AnalysisHistory(baseline, resultSelector);

        assertThat(history.getBaselineResult()).contains(baselineResult);
        assertThat(history.getResult()).contains(baselineResult);
        assertThat(history.getBuild()).contains(baseline);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("createTestDataForIgnoredQualityGateAndIgnoredBuildResult")
    @DisplayName("Ignore job result + ignore quality gate -> history with one previous build")
    void shouldTestFirstIterationOfLoopIgnoreStatusAndResult(final String name,
            final ExpectedResult expectedResult, final QualityGateStatus qualityGateStatus, final Result jobStatus) {
        runTest(IGNORE_QUALITY_GATE, IGNORE_JOB_RESULT, qualityGateStatus, jobStatus, expectedResult);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("createTestDataForIgnoredQualityGateAndNoFailedBuild")
    @DisplayName("No job failure + ignore quality gate -> history with one previous build")
    void shouldTestFirstIterationOfLoopIgnoreStatus(final String name,
            final ExpectedResult expectedResult, final QualityGateStatus qualityGateStatus, final Result jobStatus) {
        runTest(IGNORE_QUALITY_GATE, NO_JOB_FAILURE, qualityGateStatus, jobStatus, expectedResult);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("createTestDataForSuccessfulQualityGateAndIgnoredBuildResult")
    @DisplayName("Ignore job result + successful quality gate -> history with one previous build")
    void shouldTestFirstIterationOfLoopIgnoreResult(final String name,
            final ExpectedResult expectedResult, final QualityGateStatus qualityGateStatus, final Result jobStatus) {
        runTest(SUCCESSFUL_QUALITY_GATE, IGNORE_JOB_RESULT, qualityGateStatus, jobStatus, expectedResult);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("createTestDataForSuccessfulQualityGateAndNoFailedBuild")
    @DisplayName("No job failure + successful quality gate -> history with one previous build")
    void shouldTestFirstIterationOfLoop(final String name,
            final ExpectedResult expectedResult, final QualityGateStatus qualityGateStatus, final Result jobStatus) {
        runTest(SUCCESSFUL_QUALITY_GATE, NO_JOB_FAILURE, qualityGateStatus, jobStatus, expectedResult);
    }

    private void runTest(final QualityGateEvaluationMode qualityGateEvaluationMode,
            final JobResultEvaluationMode jobResultEvaluationMode,
            final QualityGateStatus qualityGateStatus, final Result jobStatus, final ExpectedResult expectedResult) {
        ResultSelector resultSelector = mock(ResultSelector.class);
        Run baseline = createBuild(qualityGateStatus, jobStatus, resultSelector);

        AnalysisHistory history = new AnalysisHistory(baseline, resultSelector, qualityGateEvaluationMode,
                jobResultEvaluationMode);

        if (expectedResult == NONE) {
            assertThat(history.getResult()).isEmpty();
            assertThat(history.getBuild()).isEmpty();
        }
        else {
            assertThat(history.getResult()).isNotEmpty();
            assertThat(history.getBuild()).contains(baseline);
        }
    }

    private Run createBuild(final QualityGateStatus qualityGateStatus, final Result jobStatus,
            final ResultSelector resultSelector) {
        Run baseline = createBuildWithResult(jobStatus);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getOwner()).thenReturn(baseline);
        when(result.getQualityGateStatus()).thenReturn(qualityGateStatus);
        
        ResultAction resultAction = mock(ResultAction.class);
        when(resultAction.getResult()).thenReturn(result);
        when(resultAction.getOwner()).thenReturn(baseline);
        when(resultAction.isSuccessful()).thenReturn(qualityGateStatus.isSuccessful());
        
        when(resultSelector.get(baseline)).thenReturn(Optional.of(resultAction));
        
        return baseline;
    }

    private Run createBuildWithResult(final Result jobStatus) {
        Run baseline = mock(Run.class);
        when(baseline.getResult()).thenReturn(jobStatus);
        return baseline;
    }

    /**
     * Method to provide test element that return an present optional.
     *
     * @return list of test data objects
     */
    private static Iterable<Object> createTestDataForSuccessfulQualityGateAndNoFailedBuild() {
        return asList(
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have analysis result (SUCCESS, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have analysis result (SUCCESS, quality gate has been passed)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate has been passed)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.FAILED)
                        .setTestName("Job should have no analysis result if quality gate has been missed (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.WARNING)
                        .setTestName("Job should have no analysis result if quality gate has a warning (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have no analysis result even if quality gate is not active (FAILED)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have no analysis result even if quality gate has been passed (FAILED)")
                        .build()
        );
    }

    /**
     * Method to provide test element that return an present optional.
     *
     * @return list of test data objects
     */
    private static Iterable<Object> createTestDataForIgnoredQualityGateAndIgnoredBuildResult() {
        return asList(
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have analysis result (SUCCESS, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have analysis result (SUCCESS, quality gate has been passed)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate has been passed)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.FAILED)
                        .setTestName("Job should have analysis result if quality gate has been missed (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.WARNING)
                        .setTestName("Job should have analysis result if quality gate has a warning (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have analysis result even if quality gate is not active (FAILED)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have analysis result even if quality gate has been passed (FAILED)")
                        .build()
        );
    }

    /**
     * Method to provide test element that return an present optional.
     *
     * @return list of test data objects
     */
    private static Iterable<Object> createTestDataForSuccessfulQualityGateAndIgnoredBuildResult() {
        return asList(
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have analysis result (SUCCESS, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have analysis result (SUCCESS, quality gate has been passed)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate has been passed)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.FAILED)
                        .setTestName("Job should have no analysis result if quality gate has been missed (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.WARNING)
                        .setTestName("Job should have no analysis result if quality gate has a warning (SUCCESS)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have analysis result even if quality gate is not active (FAILED)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have analysis result even if quality gate has been passed (FAILED)")
                        .build()
        );
    }

    /**
     * Method to provide test element that return an present optional.
     *
     * @return list of test data objects
     */
    private static Iterable<Object> createTestDataForIgnoredQualityGateAndNoFailedBuild() {
        return asList(
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have analysis result (SUCCESS, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have analysis result (SUCCESS, quality gate has been passed)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate is not active)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.UNSTABLE)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
                        .setTestName("Job should have analysis result (UNSTABLE, quality gate has been passed)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.FAILED)
                        .setTestName("Job should have analysis result if quality gate has been missed (SUCCESS)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(FIRST)
                        .setJobResult(Result.SUCCESS)
                        .setQualityGateStatus(QualityGateStatus.WARNING)
                        .setTestName("Job should have analysis result if quality gate has a warning (SUCCESS)")
                        .build(),

                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateStatus(QualityGateStatus.INACTIVE)
                        .setTestName("Job should have no analysis result even if quality gate is not active (FAILED)")
                        .build(),
                new BuildHistoryBuilder().setExpectedResult(NONE)
                        .setJobResult(Result.FAILURE)
                        .setQualityGateStatus(QualityGateStatus.PASSED)
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
        private QualityGateStatus qualityGateStatus;
        private Result jobResult;

        public BuildHistoryBuilder setTestName(final String testName) {
            this.testName = testName;
            return this;
        }

        public BuildHistoryBuilder setExpectedResult(final ExpectedResult expectedResult) {
            this.expectedResult = expectedResult;
            return this;
        }

        public BuildHistoryBuilder setQualityGateStatus(final QualityGateStatus qualityGateStatus) {
            this.qualityGateStatus = qualityGateStatus;
            return this;
        }

        public BuildHistoryBuilder setJobResult(final Result jobResult) {
            this.jobResult = jobResult;
            return this;
        }

        /**
         * Build the tests argument.
         *
         * @return test arg
         */
        public Object build() {
            return Arguments.of(testName, expectedResult, qualityGateStatus, jobResult);
        }
    }
}