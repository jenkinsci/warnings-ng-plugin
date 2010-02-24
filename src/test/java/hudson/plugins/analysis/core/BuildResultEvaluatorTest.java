package hudson.plugins.analysis.core;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import hudson.model.AbstractBuild;
import hudson.model.Result;

import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link BuildResultEvaluator}.
 *
 * @author Ulli Hafner
 */
public class BuildResultEvaluatorTest {
    /** Error message. */
    private static final String WRONG_BUILD_FAILURE_STATE = "Wrong build failure state.";

    /**
     * Checks whether the new threshold is correctly evaluated with regard to the
     * warnings of the last successful build. Verifies the results for
     * {@link Result#SUCCESS successful} builds that switch to
     * {@link Result#UNSTABLE unstable} and for {@link Result#UNSTABLE unstable
     * builds} that switch to {@link Result#FAILURE failure}
     */
    @Test
    public void checkNewThreshold() {
        verifyResult(Result.UNSTABLE, Result.SUCCESS, "0", "");
        verifyResult(Result.FAILURE, Result.SUCCESS, "", "0");
        // FIXME: decide when to use other warnings. verifyResult(Result.FAILURE, Result.UNSTABLE, "", "0");
    }

    /**
     * Checks whether the new threshold is correctly evaluated with regard to
     * the warnings of the last successful build.
     *
     * @param expectedResult
     *            expected build result
     * @param previousResult
     *            previous build result
     * @param newUnstableThreshold
     *            threshold for unstable
     * @param newFailureThreshold
     *            threshold for failure
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void verifyResult(final Result expectedResult, final Result previousResult,
            final String newUnstableThreshold, final String newFailureThreshold) {
        FileAnnotation warning = mock(FileAnnotation.class);
        when(warning.getPriority()).thenReturn(Priority.HIGH);

        AbstractBuild previousBuild = mock(AbstractBuild.class);
        when(previousBuild.getResult()).thenReturn(previousResult);

        AbstractBuild currentBuild = mock(AbstractBuild.class);
        when(currentBuild.getPreviousBuild()).thenReturn(previousBuild);

        BuildResult buildResult = mock(BuildResult.class);
        when(buildResult.getOwner()).thenReturn(currentBuild);

        ArrayList<FileAnnotation> oneWarning = Lists.newArrayList(warning);
        when(buildResult.getAnnotations()).thenReturn(oneWarning);
        when(buildResult.getNewWarnings()).thenReturn(oneWarning);

        PluginLogger logger = mock(PluginLogger.class);
        HealthDescriptor descriptor = newDescriptor(Priority.NORMAL, "", "", newUnstableThreshold, newFailureThreshold);

        BuildResultEvaluator parser = new BuildResultEvaluator();
        Result result = parser.evaluateBuildResult(logger, descriptor, buildResult);
        assertEquals(expectedResult, result);

        when(previousBuild.getResult()).thenReturn(expectedResult);
        when(buildResult.getNewWarnings()).thenReturn(new ArrayList<FileAnnotation>());
        when(buildResult.getNewWarnings(previousBuild)).thenReturn(oneWarning);

        result = parser.evaluateBuildResult(logger, descriptor, buildResult);
        assertEquals(expectedResult, result);
    }


    /**
     * Checks whether valid thresholds are correctly converted.
     */
    @Test
    public void checkThresholds() {
        BuildResultEvaluator parser = new BuildResultEvaluator();

        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, ""));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, "0"));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, "1"));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, "-1"));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, "A"));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, null));

        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, ""));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, "1"));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, "2"));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, "-1"));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, null));
        assertTrue(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, "0"));

        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, ""));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, "2"));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, "3"));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, "-1"));
        assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, null));
        assertTrue(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, "0"));
        assertTrue(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, "1"));
    }

    /**
     * Checks whether valid thresholds are correctly converted.
     */
    @Test
    public void checkResultComputation() {
        BuildResultEvaluator parser = new BuildResultEvaluator();
        List<FileAnnotation> allAnnotations = new ArrayList<FileAnnotation>();
        List<FileAnnotation> newAnnotations = new ArrayList<FileAnnotation>();

        PluginLogger logger = mock(PluginLogger.class);
        assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "", ""), allAnnotations, newAnnotations));
        assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "0", "", ""), allAnnotations, newAnnotations));
        assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "0", "0"), allAnnotations, newAnnotations));
        assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "0", "0", "0"), allAnnotations, newAnnotations));
        allAnnotations.add(createAnnotation());
        assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "", ""), allAnnotations, newAnnotations));
        assertEquals(Result.UNSTABLE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "", "", ""), allAnnotations, newAnnotations));
        assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "0", "", ""), allAnnotations, newAnnotations));
        assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "0", "", ""), allAnnotations, newAnnotations));
        newAnnotations.add(createAnnotation());
        assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "", ""), allAnnotations, newAnnotations));
        assertEquals(Result.UNSTABLE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "0", ""), allAnnotations, newAnnotations));
        assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "", "0"), allAnnotations, newAnnotations));
        assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "0", "0"), allAnnotations, newAnnotations));

        assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "", ""), allAnnotations, newAnnotations));
        assertEquals(Result.UNSTABLE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "", "0", ""), allAnnotations, newAnnotations));
        assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "", "", "0"), allAnnotations, newAnnotations));
        assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "0", "", "0"), allAnnotations, newAnnotations));
        assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "0", "0", ""), allAnnotations, newAnnotations));
    }

    /**
     * Creates a mock health descriptor.
     *
     * @param unstableThreshold
     *            Annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param newUnstableThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param failureThreshold
     *            Annotation threshold to be reached if a build should be
     *            considered as failure.
     * @param newFailureThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as failure.
     * @param minimumPriority
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @return the health descriptor
     */
    private HealthDescriptor newDescriptor(final Priority minimumPriority,
            final String unstableThreshold, final String failureThreshold,
            final String newUnstableThreshold, final String newFailureThreshold) {
        HealthDescriptor descriptor = mock(HealthDescriptor.class);
        when(descriptor.getMinimumPriority()).thenReturn(minimumPriority);
        when(descriptor.getThreshold()).thenReturn(unstableThreshold);
        when(descriptor.getFailureThreshold()).thenReturn(failureThreshold);
        when(descriptor.getNewThreshold()).thenReturn(newUnstableThreshold);
        when(descriptor.getNewFailureThreshold()).thenReturn(newFailureThreshold);

        return descriptor;
    }

    /**
     * Returns an annotation with {@link Priority#HIGH}.
     *
     * @return an annotation with {@link Priority#HIGH}
     */
    private FileAnnotation createAnnotation() {
        FileAnnotation annotation = mock(FileAnnotation.class);
        when(annotation.getPriority()).thenReturn(Priority.HIGH);

        return annotation;
    }
}

