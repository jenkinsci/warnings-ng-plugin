package hudson.plugins.warnings.util;

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
}

