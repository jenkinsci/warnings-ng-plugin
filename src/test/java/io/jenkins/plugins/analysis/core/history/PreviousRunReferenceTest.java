package io.jenkins.plugins.analysis.core.history;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PreviousRunReferenceTest {

	private static Iterable<Object> testData() {
		return asList(
				new TestArgumentsBuilder()
						.setTestName("isPresent when analysisResult#overallResult is SUCCESS and result of previous run is SUCCESS")
						.setOverallResult(Result.SUCCESS)
						.setRunResult(Result.SUCCESS)
						.setPreviousRunResult(Result.SUCCESS)
						.setOverallMustBeSuccess(true)
						.setOptionalPresent(true)
						.build(),
				new TestArgumentsBuilder()
						.setTestName("isPresent when analysisResult#overallResult is SUCCESS and result of previous run is SUCCESS")
						.setOverallResult(Result.SUCCESS)
						.setRunResult(Result.SUCCESS)
						.setPreviousRunResult(Result.SUCCESS)
						.setOverallMustBeSuccess(false)
						.setOptionalPresent(true)
						.build(),
				new TestArgumentsBuilder()
						.setTestName("isPresent when analysisResult#overallResult is NOT_BUILT and result of previous run is SUCCESS")
						.setOverallResult(Result.FAILURE)
						.setRunResult(Result.SUCCESS)
						.setPreviousRunResult(Result.SUCCESS)
						.setOverallMustBeSuccess(false)
						.setOptionalPresent(true)
						.build(),
				new TestArgumentsBuilder()
						.setTestName("isPresent when analysisResult#overallResult is FAILURE and result of previous run is FAILURE")
						.setOverallResult(Result.FAILURE)
						.setRunResult(Result.SUCCESS)
						.setPreviousRunResult(Result.FAILURE)
						.setOverallMustBeSuccess(false)
						.setOptionalPresent(true)
						.build(),
				new TestArgumentsBuilder()
						.setTestName("isPresent when analysisResult#overallResult is NOT_BUILT and result of previous run is NOT_BUILT")
						.setOverallResult(Result.NOT_BUILT)
						.setRunResult(Result.SUCCESS)
						.setPreviousRunResult(Result.NOT_BUILT)
						.setOverallMustBeSuccess(false)
						.setOptionalPresent(true)
						.build(),
				new TestArgumentsBuilder()
						.setTestName("isEmpty when result of previous run is null")
						.setOverallResult(Result.SUCCESS)
						.setRunResult(Result.SUCCESS)
						.setPreviousRunResult(null)
						.setOverallMustBeSuccess(true)
						.setOptionalPresent(false)
						.build(),
				new TestArgumentsBuilder()
						.setTestName("isEmpty when result of previous run is FAILURE")
						.setOverallResult(Result.SUCCESS)
						.setRunResult(Result.SUCCESS)
						.setPreviousRunResult(Result.FAILURE)
						.setOverallMustBeSuccess(true)
						.setOptionalPresent(false)
						.build(),
				new TestArgumentsBuilder()
						.setTestName("isEmpty when analysisResult#overallResult is SUCCESS and result of previous run is NOT_BUILT")
						.setOverallResult(Result.SUCCESS)
						.setRunResult(Result.SUCCESS)
						.setPreviousRunResult(Result.NOT_BUILT)
						.setOverallMustBeSuccess(false)
						.setOptionalPresent(false)
						.build()
		);
	}

	/**
	 * Testing the different branches of {@link BuildHistory#getRunWithResult(hudson.model.Run, io.jenkins.plugins.analysis.core.history.ResultSelector, boolean, boolean)}
	 * in the first iteration of the loop
	 * @param name Name of the Test
	 * @param overallResult Mocked value for {@link AnalysisResult#getOverallResult()}
	 * @param runResult Mocked value for {@link Run#getResult()} of the baseline run
	 * @param previousRunResult Mocked value for {@link Run#getResult()} of the previous run that is processed
	 * @param overallResultMustBeSuccess construtor param of {@link PreviousRunReference#PreviousRunReference(hudson.model.Run, io.jenkins.plugins.analysis.core.history.ResultSelector, boolean)}
	 * @param isOptionalPresent indicates if the returned optional should be empty or present
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("testData")
	void shouldTestFirstIterationOfLoop(String name,
										Result overallResult,
										Result runResult,
										Result previousRunResult,
										boolean overallResultMustBeSuccess,
										boolean isOptionalPresent) {
		AnalysisResult analysisResult = createAnalysisResultStub(overallResult);
		Optional<ResultAction> resultActionOptional = createResultActionOptional(analysisResult);
		ResultSelector selector = createResultSelectorStub(resultActionOptional);

		Run previousBuild = createRunStub(null, previousRunResult);
		Run baseline = createRunStub(previousBuild, runResult);
		PreviousRunReference cut = new PreviousRunReference(baseline, selector, overallResultMustBeSuccess);
		if (isOptionalPresent) {
			assertThat(cut.getReferenceAction()).isPresent();
		} else {
			assertThat(cut.getReferenceAction()).isEmpty();
		}
	}

	/**
	 * Testing the different branches of {@link BuildHistory#getRunWithResult(hudson.model.Run, io.jenkins.plugins.analysis.core.history.ResultSelector, boolean, boolean)}
	 * in the second iteration of the loop
	 * @param name Name of the Test
	 * @param overallResult Mocked value for {@link AnalysisResult#getOverallResult()}
	 * @param runResult Mocked value for {@link Run#getResult()} of the baseline run
	 * @param previousRunResult Mocked value for {@link Run#getResult()} of the previous run that is processed
	 * @param overallResultMustBeSuccess construtor param of {@link PreviousRunReference#PreviousRunReference(hudson.model.Run, io.jenkins.plugins.analysis.core.history.ResultSelector, boolean)}
	 * @param isOptionalPresent indicates if the returned optional should be empty or present
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("testData")
	void shouldTestSecondIterationOfLoop(String name,
										Result overallResult,
										Result runResult,
										Result previousRunResult,
										boolean overallResultMustBeSuccess,
										boolean isOptionalPresent) {

		AnalysisResult analysisResult = createAnalysisResultStub(overallResult);
		Optional<ResultAction> resultActionOptional = createResultActionOptional(analysisResult);

		ResultSelector selector = mock(ResultSelector.class);
		when(selector.get(any())).thenReturn(Optional.empty()).thenReturn(resultActionOptional);

		Run previousPreviousBuild = createRunStub(null, previousRunResult);
		Run previousBuild = createRunStub(previousPreviousBuild, previousRunResult);
		Run baseline = createRunStub(previousBuild, runResult);
		PreviousRunReference cut = new PreviousRunReference(baseline, selector, overallResultMustBeSuccess);
		if (isOptionalPresent) {
			assertThat(cut.getReferenceAction()).isPresent();
		} else {
			assertThat(cut.getReferenceAction()).isEmpty();
		}
	}

	/**
	 * Testing the first branch in {@link BuildHistory#getPreviousAction(boolean, boolean)}
	 * if no previous runs are available
	 */
	@Test
	void shouldBeEmptyIfNoPreviousRunsAreAvailable() {

		Run baseline = createRunStub(null, Result.SUCCESS);
		ResultSelector selector = createResultSelectorStub(Optional.empty());

		PreviousRunReference cut = new PreviousRunReference(baseline, selector, true);

		assertThat(cut.getReferenceAction()).isEmpty();
	}


	/**
	 * Creating a stub of {@link Optional<ResultAction>} containing the nested stub of {@link AnalysisResult}
	 *
	 * @param analysisResult already mocked instance of {@link AnalysisResult}
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
	 * Creating a stub of {@link Run} containing the nested stub of another run
	 */
	private Run createRunStub(Run previousBuild, Result runResult) {
		Run stub = mock(Run.class);
		when(stub.getPreviousBuild()).thenReturn(previousBuild);
		when(stub.getResult()).thenReturn(runResult);
		return stub;
	}

	/**
	 * creating complete stub of {@link ResultSelector} for the test
	 *
	 * @return Created stub of {@link ResultSelector}
	 */
	private ResultSelector createResultSelectorStub(Optional<ResultAction> optionalResultAction) {
		ResultSelector selector = mock(ResultSelector.class);
		when(selector.get(any())).thenReturn(optionalResultAction);

		return selector;
	}

	private AnalysisResult createAnalysisResultStub(Result overallResult) {
		AnalysisResult analysisResult = mock(AnalysisResult.class);

		when(analysisResult.getOverallResult()).thenReturn(overallResult);
		return analysisResult;
	}

	/**
	 * Builds arg for the parameterized test.
	 */
	private static class TestArgumentsBuilder {

		private String testName;
		private Result overallResult;
		private Result runResult;
		private Result previousRunResult;
		private boolean overallMustBeSuccess;
		private boolean isOptionalPresent;

		TestArgumentsBuilder setTestName(String name) {
			this.testName = name;
			return this;
		}

		TestArgumentsBuilder setOverallResult(Result overallResult) {
			this.overallResult = overallResult;
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

		TestArgumentsBuilder setOptionalPresent(boolean optionalPresent) {
			this.isOptionalPresent = optionalPresent;
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
					overallResult,
					runResult,
					previousRunResult,
					overallMustBeSuccess,
					isOptionalPresent
			);
		}
	}

}












