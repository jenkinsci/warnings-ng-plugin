package hudson.plugins.warnings.util;

import static org.mockito.Mockito.*;
import hudson.model.Result;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;
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
        ParserResult allAnnotations = new ParserResult();
        ParserResult newAnnotations = new ParserResult();

        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "", newAnnotations, "", ""));
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "0", "0", newAnnotations, "", ""));
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "", newAnnotations, "0", "0"));
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "0", "0", newAnnotations, "0", "0"));
        allAnnotations.addAnnotation(createAnnotation());
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "", newAnnotations, "", ""));
        Assert.assertEquals(Result.UNSTABLE,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "0", "", newAnnotations, "", ""));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "0", newAnnotations, "", ""));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "0", "0", newAnnotations, "", ""));
        newAnnotations.addAnnotation(createAnnotation());
        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "", newAnnotations, "", ""));
        Assert.assertEquals(Result.UNSTABLE,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "", newAnnotations, "0", ""));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "", newAnnotations, "", "0"));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "", newAnnotations, "0", "0"));

        Assert.assertEquals(Result.SUCCESS,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "", newAnnotations, "", ""));
        Assert.assertEquals(Result.UNSTABLE,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "0", "", newAnnotations, "0", ""));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "0", "", newAnnotations, "", "0"));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "0", newAnnotations, "", "0"));
        Assert.assertEquals(Result.FAILURE,
                parser.evaluateBuildResult(Priority.NORMAL, allAnnotations, "", "0", newAnnotations, "0", ""));
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

