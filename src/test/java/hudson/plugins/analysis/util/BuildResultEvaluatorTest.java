package hudson.plugins.analysis.util;

import static org.mockito.Mockito.*;
import hudson.model.Result;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the class {@link BuildResultEvaluator}.
 *
 * @author Ulli Hafner
 */
public class BuildResultEvaluatorTest {
    /** Error message. */
    private static final String WRONG_BUILD_FAILURE_STATE = "Wrong build failure state.";

    /**
     * Checks whether valid thresholds are correctly converted.
     */
    @Test
    public void checkThresholds() {
        BuildResultEvaluator parser = new BuildResultEvaluator();

        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, ""));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, "0"));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, "1"));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, "-1"));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, "A"));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(0, null));

        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, ""));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, "1"));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, "2"));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, "-1"));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, null));
        Assert.assertTrue(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(1, "0"));

        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, ""));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, "2"));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, "3"));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, "-1"));
        Assert.assertFalse(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, null));
        Assert.assertTrue(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, "0"));
        Assert.assertTrue(WRONG_BUILD_FAILURE_STATE, parser.isAnnotationCountExceeded(2, "1"));
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
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "", ""), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "0", "", ""), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "0", "0"), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "0", "0", "0"), allAnnotations, newAnnotations));
        allAnnotations.add(createAnnotation());
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "", ""), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.UNSTABLE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "", "", ""), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "0", "", ""), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "0", "", ""), allAnnotations, newAnnotations));
        newAnnotations.add(createAnnotation());
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "", ""), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.UNSTABLE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "0", ""), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "", "0"), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "0", "0"), allAnnotations, newAnnotations));

        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "", "", ""), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.UNSTABLE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "", "0", ""), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "0", "", "", "0"), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "0", "", "0"), allAnnotations, newAnnotations));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(logger, newDescriptor(Priority.NORMAL, "", "0", "0", ""), allAnnotations, newAnnotations));
    }

    /**
     * Creates a mock health descriptor.
     *
     * @param threshold
     *            Annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param newThreshold
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
            final String threshold, final String failureThreshold,
            final String newThreshold, final String newFailureThreshold) {
        HealthDescriptor descriptor = mock(HealthDescriptor.class);
        when(descriptor.getMinimumPriority()).thenReturn(minimumPriority);
        when(descriptor.getThreshold()).thenReturn(threshold);
        when(descriptor.getFailureThreshold()).thenReturn(failureThreshold);
        when(descriptor.getNewThreshold()).thenReturn(newThreshold);
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

