package hudson.plugins.analysis.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;

import org.junit.Test;

import hudson.model.Result;
import hudson.model.AbstractBuild;

import hudson.plugins.analysis.util.model.AnnotationContainer;

/**
 * Tests the class {@link BuildHistory}.
 *
 * @author Ulli Hafner
 */
public class BuildHistoryTest {
    /**
     * Verifies that we have no results for the first build.
     *
     * @throws Exception the exception
     */
    @Test(expected = NoSuchElementException.class)
    public void testNoPreviousResult() throws Exception {
        BuildHistory history = createHistory(mockBuild());

        assertFalse("Build has a previous result", history.hasPreviousResult());
        assertEquals("Build has wrong reference annotations", 0,
                history.getReferenceAnnotations().getNumberOfAnnotations());

        history.getPreviousResult();
    }

    /**
     * Verifies that we find the correct results for the following constellation.
     * <ol>
     * <li>Build with result</li>
     * <li>Build with no result</li>
     * <li>Baseline</li>
     * </ol>
     * @throws Exception the exception
     */
    @Test
    @SuppressWarnings("rawtypes")
    public void testHasPreviousResult() throws Exception {
        AbstractBuild withResult = mockBuild();
        AbstractBuild noResult = mockBuild();
        AbstractBuild baseline = mockBuild();

        when(baseline.getPreviousBuild()).thenReturn(noResult);
        when(noResult.getPreviousBuild()).thenReturn(withResult);

        TestResultAction action = mockAction(withResult);
        when(withResult.getAction(TestResultAction.class)).thenReturn(action);
        BuildResult result = mock(BuildResult.class);
        when(action.getResult()).thenReturn(result);
        AnnotationContainer container = mock(AnnotationContainer.class);
        when(result.getContainer()).thenReturn(container);
        BuildHistory history = createHistory(baseline);

        assertTrue("Build has no previous result", history.hasPreviousResult());
        assertSame("Build has wrong previous result", result, history.getPreviousResult());
        assertSame("Build has wrong reference result", container, history.getReferenceAnnotations());
    }

    /**
     * Verifies that we find the correct results for the following constellation.
     * <ol>
     * <li>Build with result and build result ABORTED</li>
     * <li>Build with no result</li>
     * <li>Baseline</li>
     * </ol>
     * @throws Exception the exception
     */
    @Test
    @SuppressWarnings("rawtypes")
    public void testHasNoPreviousResultDueToFailure() throws Exception {
        AbstractBuild withResult = mockBuild(Result.ABORTED);
        AbstractBuild noResult = mockBuild();
        AbstractBuild baseline = mockBuild();

        when(baseline.getPreviousBuild()).thenReturn(noResult);
        when(noResult.getPreviousBuild()).thenReturn(withResult);

        TestResultAction action = mockAction(withResult);
        when(withResult.getAction(TestResultAction.class)).thenReturn(action);
        BuildResult result = mock(BuildResult.class);
        when(action.getResult()).thenReturn(result);
        AnnotationContainer container = mock(AnnotationContainer.class);
        when(result.getContainer()).thenReturn(container);
        BuildHistory history = createHistory(baseline);

        assertFalse("Build has previous result", history.hasPreviousResult());
    }

    @SuppressWarnings("rawtypes")
    private AbstractBuild mockBuild() {
        return mockBuild(Result.SUCCESS);
    }

    @SuppressWarnings("rawtypes")
    private AbstractBuild mockBuild(final Result result) {
        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getResult()).thenReturn(result);
        return build;
    }

    /**
     * Verifies that we find the correct results for the following constellation.
     * <ol>
     * <li>Build with result, build result = SUCCESS</li>
     * <li>Build with no result</li>
     * <li>Build with result, build result = FAILURE</li>
     * <li>Build with no result</li>
     * <li>Baseline</li>
     * </ol>
     * @throws Exception the exception
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testHasReferenceResult() throws Exception {
        AbstractBuild withSuccessResult = mockBuild();
        AbstractBuild noResult2 = mockBuild();
        AbstractBuild withFailureResult = mockBuild();
        AbstractBuild noResult1 = mockBuild();
        AbstractBuild baseline = mockBuild();

        when(baseline.getPreviousBuild()).thenReturn(noResult1);
        when(noResult1.getPreviousBuild()).thenReturn(withFailureResult);
        when(withFailureResult.getPreviousBuild()).thenReturn(noResult2);
        when(noResult2.getPreviousBuild()).thenReturn(withSuccessResult);

        BuildResult failureResult = createFailureResult(withFailureResult);

        AnnotationContainer container = createSuccessfulResult(withSuccessResult);

        BuildHistory history = createHistory(baseline);

        assertTrue("Build has no previous result", history.hasPreviousResult());
        assertSame("Build has wrong previous result", failureResult, history.getPreviousResult());
        assertTrue("Build has no reference build", history.hasReferenceBuild());
        assertSame("Build has wrong reference result", withSuccessResult, history.getReferenceBuild());
        assertSame("Build has wrong reference result", container, history.getReferenceAnnotations());
    }

    /**
     * Verifies that we find the correct results for the following constellation.
     * <ol>
     * <li>Build with result, build result = SUCCESS and build result ABORTED</li>
     * <li>Build with no result</li>
     * <li>Build with result, build result = FAILURE</li>
     * <li>Build with no result</li>
     * <li>Baseline</li>
     * </ol>
     * @throws Exception the exception
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testHasNoReferenceResult() throws Exception {
        AbstractBuild withSuccessResultAndSuccessfulBuild = mockBuild();
        AbstractBuild withSuccessResult = mockBuild(Result.ABORTED);
        AbstractBuild noResult2 = mockBuild();
        AbstractBuild withFailureResult = mockBuild();
        AbstractBuild noResult1 = mockBuild();
        AbstractBuild baseline = mockBuild();

        when(baseline.getPreviousBuild()).thenReturn(noResult1);
        when(noResult1.getPreviousBuild()).thenReturn(withFailureResult);
        when(withFailureResult.getPreviousBuild()).thenReturn(noResult2);
        when(noResult2.getPreviousBuild()).thenReturn(withSuccessResult);
        when(withSuccessResult.getPreviousBuild()).thenReturn(withSuccessResultAndSuccessfulBuild);

        BuildResult failureResult = createFailureResult(withFailureResult);

        createSuccessfulResult(withSuccessResult);
        AnnotationContainer used = createSuccessfulResult(withSuccessResultAndSuccessfulBuild);

        BuildHistory history = createHistory(baseline);

        assertTrue("Build has no previous result", history.hasPreviousResult());
        assertSame("Build has wrong previous result", failureResult, history.getPreviousResult());
        assertTrue("Build has no reference build", history.hasReferenceBuild());
        assertSame("Build has wrong reference result", withSuccessResultAndSuccessfulBuild, history.getReferenceBuild());
        assertSame("Build has wrong reference result", used, history.getReferenceAnnotations());
    }

    /**
     * Verifies that the previous build is used as reference build when it's unstable
     * and history is NOT configured to use only stable builds as reference builds.
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testUsesUnstableBuildAsReferenceBuildWhenConfigured() {
        AbstractBuild unstableBuild = mockBuild(Result.UNSTABLE);
        AbstractBuild stableBuild = mockBuild(Result.SUCCESS);
        AbstractBuild baseline = mockBuild();

        when(baseline.getPreviousBuild()).thenReturn(unstableBuild);
        when(unstableBuild.getPreviousBuild()).thenReturn(stableBuild);

        createSuccessfulResult(unstableBuild);
        createSuccessfulResult(stableBuild);

        BuildHistory history = createHistory(baseline);

        assertSame("Unstable build is not reference build", unstableBuild, history.getReferenceBuild());
    }

    /**
     * Verifies that the most recent STABLE build is used as reference build when the previous
     * build is unstable and history IS configured to use only stable builds as reference builds.
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testUsesStableBuildAsReferenceBuildWhenConfigured() {
        AbstractBuild unstableBuild = mockBuild(Result.UNSTABLE);
        AbstractBuild stableBuild = mockBuild(Result.SUCCESS);
        AbstractBuild baseline = mockBuild();

        when(baseline.getPreviousBuild()).thenReturn(unstableBuild);
        when(unstableBuild.getPreviousBuild()).thenReturn(stableBuild);

        createSuccessfulResult(unstableBuild);
        createSuccessfulResult(stableBuild);

        BuildHistory history = createStableBuildReferenceHistory(baseline);

        assertSame("Stable build is not reference build", stableBuild, history.getReferenceBuild());
    }

    @SuppressWarnings("rawtypes")
    private BuildResult createFailureResult(final AbstractBuild withFailureResult) {
        TestResultAction failureAction = mockAction(withFailureResult);
        when(withFailureResult.getAction(TestResultAction.class)).thenReturn(failureAction);
        when(failureAction.isSuccessful()).thenReturn(false);
        BuildResult failureResult = mock(BuildResult.class);
        when(failureAction.getResult()).thenReturn(failureResult);
        return failureResult;
    }

    @SuppressWarnings("rawtypes")
    private AnnotationContainer createSuccessfulResult(final AbstractBuild withSuccessResult) {
        TestResultAction successAction = mockAction(withSuccessResult);
        when(withSuccessResult.getAction(TestResultAction.class)).thenReturn(successAction);
        when(successAction.isSuccessful()).thenReturn(true);
        BuildResult successResult = mock(BuildResult.class);
        AnnotationContainer container = mock(AnnotationContainer.class);
        when(successResult.getContainer()).thenReturn(container);
        when(successAction.getResult()).thenReturn(successResult);
        return container;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private TestResultAction mockAction(final AbstractBuild build) {
        TestResultAction action = mock(TestResultAction.class);
        when(action.getBuild()).thenReturn(build);
        return action;
    }

    /**
     * Factory method to create a build history under test.
     *
     * @param baseline
     *            the build to start with
     * @return the build history under test
     */
    private BuildHistory createHistory(final AbstractBuild<?, ?> baseline) {
        return new BuildHistory(baseline, TestResultAction.class, false);
    }

    private BuildHistory createStableBuildReferenceHistory(final AbstractBuild<?, ?> baseline) {
        return new BuildHistory(baseline, TestResultAction.class, true);
    }

    /**
     * Action used in tests.
     */
    abstract static class TestResultAction implements ResultAction<BuildResult> {
        // empty
    }
}

