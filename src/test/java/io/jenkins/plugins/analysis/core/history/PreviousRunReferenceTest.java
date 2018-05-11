package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.quality.Status;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import hudson.model.Result;
import hudson.model.Run;

/**
 * Tests the class {@link PreviousRunReference}.
 *
 * @author Alexander Praegla
 */
class PreviousRunReferenceTest {
    /**
     * Method to provide test element that return an empty optional.
     *
     * @return list of test data objects
     */
    private static Iterable<Object> testDataOptionalIsEmpty() {
        return asList(
                new TestArgumentsBuilder()
                        .setTestName("isEmpty when result of previous run is null")
                        .setStatus(Status.PASSED)
                        .setRunResult(Result.SUCCESS)
                        .setPreviousRunResult(null)
                        .setOverallMustBeSuccess(true)
                        .setEnableAssertThatResultIsPresent(false)
                        .setEnableAssertThatResultIsEmpty(true)
                        .build(),
                new TestArgumentsBuilder()
                        .setTestName("isEmpty when result of previous run is FAILURE")
                        .setStatus(Status.PASSED)
                        .setRunResult(Result.SUCCESS)
                        .setPreviousRunResult(Result.FAILURE)
                        .setOverallMustBeSuccess(true)
                        .setEnableAssertThatResultIsPresent(false)
                        .setEnableAssertThatResultIsEmpty(true)
                        .build(),
                new TestArgumentsBuilder()
                        .setTestName(
                                "isEmpty when analysisResult#overallResult is SUCCESS and result of previous run is NOT_BUILT")
                        .setStatus(Status.PASSED)
                        .setRunResult(Result.SUCCESS)
                        .setPreviousRunResult(Result.NOT_BUILT)
                        .setOverallMustBeSuccess(false)
                        .setEnableAssertThatResultIsPresent(false)
                        .setEnableAssertThatResultIsEmpty(true)
                        .build()
        );
    }

    /**
     * Method to provide test element that return an present optional.
     *
     * @return list of test data objects
     */
    private static Iterable<Object> testDataOptionalIsPresent() {
        return asList(
                new TestArgumentsBuilder()
                        .setTestName(
                                "isPresent when analysisResult#overallResult is SUCCESS and result of previous run is SUCCESS")
                        .setStatus(Status.PASSED)
                        .setRunResult(Result.SUCCESS)
                        .setPreviousRunResult(Result.SUCCESS)
                        .setOverallMustBeSuccess(true)
                        .setEnableAssertThatResultIsPresent(true)
                        .setEnableAssertThatResultIsEmpty(false)
                        .build(),
                new TestArgumentsBuilder()
                        .setTestName(
                                "isPresent when analysisResult#overallResult is SUCCESS and result of previous run is SUCCESS")
                        .setStatus(Status.PASSED)
                        .setRunResult(Result.SUCCESS)
                        .setPreviousRunResult(Result.SUCCESS)
                        .setOverallMustBeSuccess(false)
                        .setEnableAssertThatResultIsPresent(true)
                        .setEnableAssertThatResultIsEmpty(false)
                        .build(),
                new TestArgumentsBuilder()
                        .setTestName(
                                "isPresent when analysisResult#overallResult is FAILURE and result of previous run is SUCCESS")
                        .setStatus(Status.ERROR)
                        .setRunResult(Result.SUCCESS)
                        .setPreviousRunResult(Result.SUCCESS)
                        .setOverallMustBeSuccess(false)
                        .setEnableAssertThatResultIsPresent(true)
                        .setEnableAssertThatResultIsEmpty(false)
                        .build(),
                new TestArgumentsBuilder()
                        .setTestName(
                                "isPresent when analysisResult#overallResult is FAILURE and result of previous run is FAILURE")
                        .setStatus(Status.ERROR)
                        .setRunResult(Result.SUCCESS)
                        .setPreviousRunResult(Result.FAILURE)
                        .setOverallMustBeSuccess(false)
                        .setEnableAssertThatResultIsPresent(true)
                        .setEnableAssertThatResultIsEmpty(false)
                        .build()
        );
    }

    /**
     * Testing the different branches of {@link BuildHistory#getRunWithResult(hudson.model.Run,
     * io.jenkins.plugins.analysis.core.history.ResultSelector, boolean, boolean)} in the first iteration of the loop.
     *
     * @param name
     *         Name of the Test
     * @param overallResult
     *         Mocked value for {@link AnalysisResult#getStatus()}
     * @param runResult
     *         Mocked value for {@link Run#getResult()} of the baseline run
     * @param previousRunResult
     *         Mocked value for {@link Run#getResult()} of the previous run that is processed
     * @param overallResultMustBeSuccess
     *         construtor param of {@link PreviousRunReference#PreviousRunReference(hudson.model.Run,
     *         io.jenkins.plugins.analysis.core.history.ResultSelector, boolean)}
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource({"testDataOptionalIsEmpty", "testDataOptionalIsPresent"})
    void shouldTestFirstIterationOfLoop(String name,
            Status overallResult,
            Result runResult,
            Result previousRunResult,
            boolean overallResultMustBeSuccess,
            boolean enableAssertThatResultIsPresent,
            boolean enableAssertThatResultIsEmpty) {
        AnalysisResult analysisResult = createAnalysisResultStub(overallResult);
        Optional<ResultAction> resultActionOptional = createResultActionOptional(analysisResult);
        ResultSelector selector = createResultSelectorStub(resultActionOptional);

        Run previousBuild = createRunStub(null, previousRunResult);
        Run baseline = createRunStub(previousBuild, runResult);
        PreviousRunReference cut = new PreviousRunReference(baseline, selector, overallResultMustBeSuccess);

        if (enableAssertThatResultIsPresent) {
            assertThat(cut.getReferenceAction()).isPresent();
        }

        if (enableAssertThatResultIsEmpty) {
            assertThat(cut.getReferenceAction()).isEmpty();
        }
    }

    /**
     * Testing the different branches of {@link BuildHistory#getRunWithResult(hudson.model.Run,
     * io.jenkins.plugins.analysis.core.history.ResultSelector, boolean, boolean)} in the second iteration of the loop.
     *
     * @param name
     *         Name of the Test
     * @param overallResult
     *         Mocked value for {@link AnalysisResult#getStatus()}
     * @param runResult
     *         Mocked value for {@link Run#getResult()} of the baseline run
     * @param previousRunResult
     *         Mocked value for {@link Run#getResult()} of the previous run that is processed
     * @param overallResultMustBeSuccess
     *         construtor param of {@link PreviousRunReference#PreviousRunReference(hudson.model.Run,
     *         io.jenkins.plugins.analysis.core.history.ResultSelector, boolean)}
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource({"testDataOptionalIsEmpty", "testDataOptionalIsPresent"})
    void shouldTestSecondIterationOfLoop(String name,
            Status overallResult,
            Result runResult,
            Result previousRunResult,
            boolean overallResultMustBeSuccess,
            boolean enableAssertThatResultIsPresent,
            boolean enableAssertThatResultIsEmpty) {

        AnalysisResult analysisResult = createAnalysisResultStub(overallResult);
        Optional<ResultAction> resultActionOptional = createResultActionOptional(analysisResult);

        ResultSelector selector = mock(ResultSelector.class);
        when(selector.get(any())).thenReturn(Optional.empty()).thenReturn(resultActionOptional);

        Run previousPreviousBuild = createRunStub(null, previousRunResult);
        Run previousBuild = createRunStub(previousPreviousBuild, previousRunResult);
        Run baseline = createRunStub(previousBuild, runResult);
        PreviousRunReference cut = new PreviousRunReference(baseline, selector, overallResultMustBeSuccess);

        if (enableAssertThatResultIsPresent) {
            assertThat(cut.getReferenceAction()).isPresent();
        }

        if (enableAssertThatResultIsEmpty) {
            assertThat(cut.getReferenceAction()).isEmpty();
        }
    }

    /**
     * Testing the first branch in {@link BuildHistory#getPreviousAction(boolean, boolean)} if no previous runs are
     * available.
     */
    @Test
    void shouldBeEmptyIfNoPreviousRunsAreAvailable() {

        Run baseline = createRunStub(null, Result.SUCCESS);
        ResultSelector selector = createResultSelectorStub(Optional.empty());

        PreviousRunReference cut = new PreviousRunReference(baseline, selector, true);

        assertThat(cut.getReferenceAction()).isEmpty();
    }

    /**
     * Creating a stub of {@link Optional<ResultAction>} containing the nested stub of {@link AnalysisResult}.
     *
     * @param analysisResult
     *         already mocked instance of {@link AnalysisResult}
     *
     * @return Mocked optional of {@link ResultAction}
     */
    private Optional<ResultAction> createResultActionOptional(AnalysisResult analysisResult) {
        ResultAction resultAction = mock(ResultAction.class);
        when(resultAction.getResult()).thenReturn(analysisResult);

        // can be always true because the second part of the OR logic is also set to true
        when(resultAction.isSuccessful()).thenReturn(true);
        return Optional.of(resultAction);
    }

    /**
     * Creating a stub of {@link Run} containing the nested stub of another run.
     */
    private Run createRunStub(Run previousBuild, Result runResult) {
        Run stub = mock(Run.class);
        when(stub.getPreviousBuild()).thenReturn(previousBuild);
        when(stub.getResult()).thenReturn(runResult);
        return stub;
    }

    /**
     * creating complete stub of {@link ResultSelector} for the test.
     *
     * @return Created stub of {@link ResultSelector}
     */
    private ResultSelector createResultSelectorStub(Optional<ResultAction> optionalResultAction) {
        ResultSelector selector = mock(ResultSelector.class);
        when(selector.get(any())).thenReturn(optionalResultAction);

        return selector;
    }

    private AnalysisResult createAnalysisResultStub(final Status status) {
        AnalysisResult analysisResult = mock(AnalysisResult.class);

        when(analysisResult.getStatus()).thenReturn(status);
        return analysisResult;
    }

    /**
     * Builds arg for the parameterized test.
     */
    private static class TestArgumentsBuilder {

        private String testName;
        private Status status;
        private Result runResult;
        private Result previousRunResult;
        private boolean overallMustBeSuccess;
        private boolean enableAssertThatResultIsPresent;
        private boolean enableAssertThatResultIsEmpty;

        TestArgumentsBuilder setTestName(String name) {
            this.testName = name;
            return this;
        }

        TestArgumentsBuilder setStatus(Status status) {
            this.status = status;
            return this;
        }

        TestArgumentsBuilder setRunResult(Result runResult) {
            this.runResult = runResult;
            return this;
        }

        TestArgumentsBuilder setPreviousRunResult(Result previousRunResult) {
            this.previousRunResult = previousRunResult;
            return this;
        }

        TestArgumentsBuilder setOverallMustBeSuccess(boolean overallMustBeSuccess) {
            this.overallMustBeSuccess = overallMustBeSuccess;
            return this;
        }

        TestArgumentsBuilder setEnableAssertThatResultIsPresent(boolean enableAssertThatResultIsPresent) {
            this.enableAssertThatResultIsPresent = enableAssertThatResultIsPresent;
            return this;
        }

        TestArgumentsBuilder setEnableAssertThatResultIsEmpty(boolean enableAssertThatResultIsEmpty) {
            this.enableAssertThatResultIsEmpty = enableAssertThatResultIsEmpty;
            return this;
        }

        /**
         * Build the tests argument.
         *
         * @return test arg
         */
        public Object build() {
            return Arguments.of(
                    testName,
                    status,
                    runResult,
                    previousRunResult,
                    overallMustBeSuccess,
                    enableAssertThatResultIsPresent,
                    enableAssertThatResultIsEmpty
            );
        }
    }

}












