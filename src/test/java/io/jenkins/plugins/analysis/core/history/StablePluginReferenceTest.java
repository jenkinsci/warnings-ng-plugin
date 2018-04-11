package io.jenkins.plugins.analysis.core.history;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ByIdResultSelector;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Result;
import hudson.model.Run;

/**
 * Unit test for {@link StablePluginReference}.
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
class StablePluginReferenceTest {

    /**
     * Verifies that an optional empty is returned when the result of a baseline is null.
     */
    @Test
    void shouldReturnAnOptionalEmptyWhenTheResultOfABaselineIsNull() {
        Run baseline = mock(Run.class);
        Run baselineBefore = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(baselineBefore);

        ResultSelector selector = mock(ByIdResultSelector.class);
        ResultAction resultAction = mock(ResultAction.class);
        when(selector.get(baselineBefore)).thenReturn(Optional.of(resultAction));
        StablePluginReference stablePluginReference = new StablePluginReference(baseline, selector,
                true);

        assertThat(stablePluginReference.getReferenceAction()).isEmpty();
    }

    /**
     * Verifies that a NullPointerException is thrown when the selector parameter is null.
     */
    @Test
    void shouldThrowANullPointerExceptionWhenSelectorIsNull() {
        Run baseline = mock(Run.class);
        Run baselineBefore = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(baselineBefore);
        checkForThrownNullPointerException(baseline, null);
    }

    /**
     * Verifies that a NullPointerException is thrown when the baseline parameter is null.
     */
    @Test
    void shouldThrowANullPointerExceptionWhenBaselineIsNull() {
        ResultSelector selector = mock(ByIdResultSelector.class);
        checkForThrownNullPointerException(null, selector);
    }

    /**
     * Verifies that StablePluginReference runs with two baselines and returns the correct {@code getReferenceAction}.
     */
    @Test
    void shouldReturnCorrectReferenceActionFromStablePluginReferenceWithTwoBaselines() {
        runPermutations(false);
    }

    /**
     * Verifies that StablePluginReference runs with three baselines and returns the correct {@code getReferenceAction}.
     */
    @Test
    void shouldReturnCorrectReferenceActionFromStablePluginReferenceWithThreeBaselines() {
        runPermutations(true);
    }

    private void runPermutations(final boolean withThreeBaselines) {
        List<Result> results = Stream.of(Result.SUCCESS, Result.UNSTABLE, Result.FAILURE, Result.NOT_BUILT,
                Result.ABORTED).collect(Collectors.toList());
        List<Boolean> booleanValues = Stream.of(true, false).collect(Collectors.toList());

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            results.forEach(result -> results.forEach(
                    resultFromActionResult -> booleanValues.forEach(
                            isSuccessful -> booleanValues.forEach(overallResultMustBeSuccess -> {
                                if (withThreeBaselines) {
                                    buildStablePluginReferenceWithThreeBaselinesThatReturnsAnOptional(result,
                                            resultFromActionResult,
                                            isSuccessful,
                                            overallResultMustBeSuccess,
                                            softly);
                                }
                                else {
                                    buildStablePluginReferenceWithTwoBaselinesThatReturnsAnOptional(result,
                                            resultFromActionResult,
                                            isSuccessful,
                                            overallResultMustBeSuccess,
                                            softly);
                                }
                            }))));
        }
    }

    private void buildStablePluginReferenceWithTwoBaselinesThatReturnsAnOptional(final Result baselineResult,
            final Result resultActionResult, final boolean isSuccessful, final boolean overallResultMustBeSuccess,
            final AutoCloseableSoftAssertions softly) {
        Run baseline = mock(Run.class);
        Run baselineBefore = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(baselineBefore);
        ResultAction resultAction = mock(ResultAction.class);

        StablePluginReference stablePluginReference = createAndPrepareNecessaryObjects(baseline, baselineBefore,
                baselineResult, resultActionResult, resultAction, isSuccessful, overallResultMustBeSuccess);

        doAConclusiveAssertOnThePreparedStablePluginReference(baselineResult, overallResultMustBeSuccess, resultAction,
                stablePluginReference, softly);
    }

    private StablePluginReference createAndPrepareNecessaryObjects(final Run baseline, final Run baselineBefore,
            final Result baselineResult,
            final Result resultActionResult, final ResultAction resultAction, final boolean isSuccessful,
            final boolean overallResultMustBeSuccess) {
        ResultSelector selector = mock(ByIdResultSelector.class);
        when(selector.get(baselineBefore)).thenReturn(Optional.of(resultAction));
        when(baselineBefore.getResult()).thenReturn(baselineResult);
        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(resultAction.getResult()).thenReturn(analysisResult);
        when(resultAction.getResult().getOverallResult()).thenReturn(resultActionResult);
        when(resultAction.isSuccessful()).thenReturn(baselineResult == Result.SUCCESS && isSuccessful);
        return new StablePluginReference(baseline, selector,
                overallResultMustBeSuccess);
    }

    private void buildStablePluginReferenceWithThreeBaselinesThatReturnsAnOptional(
            final Result baselineBeforeBaselineBeforeResult, final Result resultActionResult,
            final boolean isSuccessful, final boolean overallResultMustBeSuccess,
            final AutoCloseableSoftAssertions softly) {
        Run baseline = mock(Run.class);
        Run baselineBefore = mock(Run.class);
        Run baselineBeforeBaselineBefore = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(baselineBefore);
        when(baselineBefore.getPreviousBuild()).thenReturn(baselineBeforeBaselineBefore);
        ResultAction resultAction = mock(ResultAction.class);

        StablePluginReference stablePluginReference = createAndPrepareNecessaryObjects(baseline,
                baselineBeforeBaselineBefore, baselineBeforeBaselineBeforeResult, resultActionResult, resultAction,
                isSuccessful, overallResultMustBeSuccess);

        doAConclusiveAssertOnThePreparedStablePluginReference(baselineBeforeBaselineBeforeResult,
                overallResultMustBeSuccess, resultAction,
                stablePluginReference, softly);
    }

    private void doAConclusiveAssertOnThePreparedStablePluginReference(final Result givenBaselineResult,
            final boolean overallResultMustBeSuccess, final ResultAction resultAction,
            final StablePluginReference stablePluginReference, final AutoCloseableSoftAssertions softly) {
        if (checkSimulatedConditions(givenBaselineResult, overallResultMustBeSuccess,
                resultAction)) {
            softly.assertThat(stablePluginReference.getReferenceAction()).isEqualTo(Optional.of(resultAction));
        }
        else {
            softly.assertThat(stablePluginReference.getReferenceAction()).isEmpty();
        }
    }

    private boolean checkSimulatedConditions(final Result givenBaselineResult,
            final boolean overallResultMustBeSuccess,
            final ResultAction resultAction) {
        boolean isOverallResultMustBeSuccessAndBaselineResultEqualsResultSuccess =
                overallResultMustBeSuccess && givenBaselineResult == Result.SUCCESS;
        boolean isResultBetterThanFailure = givenBaselineResult.isBetterThan(Result.FAILURE);
        boolean isPluginCauseForFailure = resultAction.getResult().getOverallResult().isWorseOrEqualTo(Result.FAILURE);

        return resultAction.isSuccessful() && (isOverallResultMustBeSuccessAndBaselineResultEqualsResultSuccess
                || isResultBetterThanFailure || isPluginCauseForFailure);
    }

    private void checkForThrownNullPointerException(final Run baseline, final ResultSelector selector) {
        StablePluginReference stablePluginReference = new StablePluginReference(baseline, selector,
                true);

        assertThatNullPointerException().isThrownBy(
                stablePluginReference::getReferenceAction)
                .withNoCause();
    }
}