package hudson.plugins.analysis.core;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;

import org.junit.Test;

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
        BuildHistory history = createHistory(mock(AbstractBuild.class));

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
        AbstractBuild withResult = mock(AbstractBuild.class);
        AbstractBuild noResult = mock(AbstractBuild.class);
        AbstractBuild baseline = mock(AbstractBuild.class);
        when(baseline.getPreviousBuild()).thenReturn(noResult);
        when(noResult.getPreviousBuild()).thenReturn(withResult);

        TestResultAction action = mock(TestResultAction.class);
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
        AbstractBuild withSuccessResult = mock(AbstractBuild.class);
        AbstractBuild noResult2 = mock(AbstractBuild.class);
        AbstractBuild withFailureResult = mock(AbstractBuild.class);
        AbstractBuild noResult1 = mock(AbstractBuild.class);
        AbstractBuild baseline = mock(AbstractBuild.class);
        when(baseline.getPreviousBuild()).thenReturn(noResult1);
        when(noResult1.getPreviousBuild()).thenReturn(withFailureResult);
        when(withFailureResult.getPreviousBuild()).thenReturn(noResult2);
        when(noResult2.getPreviousBuild()).thenReturn(withSuccessResult);

        TestResultAction failureAction = mock(TestResultAction.class);
        when(withFailureResult.getAction(TestResultAction.class)).thenReturn(failureAction);
        when(failureAction.isSuccessful()).thenReturn(false);
        BuildResult failureResult = mock(BuildResult.class);
        when(failureAction.getResult()).thenReturn(failureResult);

        TestResultAction successAction = mock(TestResultAction.class);
        when(withSuccessResult.getAction(TestResultAction.class)).thenReturn(successAction);
        when(successAction.isSuccessful()).thenReturn(true);
        BuildResult successResult = mock(BuildResult.class);
        AnnotationContainer container = mock(AnnotationContainer.class);
        when(successResult.getContainer()).thenReturn(container);
        when(successAction.getResult()).thenReturn(successResult);

        BuildHistory history = createHistory(baseline);

        assertTrue("Build has no previous result", history.hasPreviousResult());
        assertSame("Build has wrong previous result", failureResult, history.getPreviousResult());
        assertSame("Build has wrong reference result", container, history.getReferenceAnnotations());
    }

    /**
     * Factory method to create a build history under test.
     *
     * @param baseline
     *            the build to start with
     * @return the build history under test
     */
    private BuildHistory createHistory(final AbstractBuild<?, ?> baseline) {
        return new BuildHistory(baseline, TestResultAction.class);
    }

    /**
     * Action used in tests.
     */
    abstract static class TestResultAction implements ResultAction<BuildResult> {
        // empty
    }
}

