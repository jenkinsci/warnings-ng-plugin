package hudson.plugins.warnings.util;

import hudson.model.Result;
import hudson.plugins.warnings.util.model.Priority;

import org.apache.commons.lang.StringUtils;

/**
 * Evaluates if the number of annotations exceeds a given threshold value.
 *
 * @author Ulli Hafner
 */
public class BuildResultEvaluator {
    /**
     * Evaluates the build result. The build is marked as unstable if one of the
     * thresholds has been exceeded.
     * @param minimumPriority
     *            determines which warning priorities should be considered
     * @param result
     *            the result collecting all annotations
     * @param threshold
     *            annotation threshold to be reached if a build should be considered as unstable
     * @param newResult
     *            the result collecting the new annotations
     * @param newThreshold
     *            threshold for new annotations to be reached if a build should be considered as unstable
     *
     * @return the build result
     */
    public Result evaluateBuildResult(final Priority minimumPriority, final ParserResult result,
            final String threshold, final ParserResult newResult, final String newThreshold) {
        int annotationCount = 0;
        int newAnnotationCount = 0;
        for (Priority priority : Priority.collectPrioritiesFrom(minimumPriority)) {
            int numberOfAnnotations = result.getNumberOfAnnotations(priority);
            annotationCount += numberOfAnnotations;

            int numberOfNewAnnotations = newResult.getNumberOfAnnotations(priority);
            newAnnotationCount += numberOfNewAnnotations;
        }

        BuildResultEvaluator thresholdParser = new BuildResultEvaluator();
        if (thresholdParser.isAnnotationCountExceeded(annotationCount, threshold)) {
            return Result.UNSTABLE;
        }
        if (thresholdParser.isAnnotationCountExceeded(newAnnotationCount, newThreshold)) {
            return Result.UNSTABLE;
        }
        return Result.SUCCESS;
    }

    /**
     * Returns whether the new annotation count exceeds the user defined threshold
     * and the build should be set to unstable.
     *
     * @param annotationCount
     *            the number of new annotations
     * @param annotationThreshold
     *            string representation of the threshold value
     * @return <code>true</code> if the build should be set to unstable
     */
    public boolean isAnnotationCountExceeded(final int annotationCount, final String annotationThreshold) {
        if (annotationCount > 0 && isValidThreshold(annotationThreshold)) {
            return annotationCount > convertThreshold(annotationThreshold);
        }
        return false;
    }

    /**
     * Returns whether the provided threshold string parameter is a valid
     * threshold number, i.e. an integer value greater or equal zero.
     *
     * @param annotationThreshold
     *            string representation of the threshold value
     * @return <code>true</code> if the provided threshold string parameter is a
     *         valid number >= 0
     */
    private boolean isValidThreshold(final String annotationThreshold) {
        if (StringUtils.isNotBlank(annotationThreshold)) {
            try {
                return Integer.valueOf(annotationThreshold) >= 0;
            }
            catch (NumberFormatException exception) {
                // not valid
            }
        }
        return false;
    }

    /**
     * Converts the provided string threshold into an integer value.
     *
     * @param annotationThreshold
     *            string representation of the threshold value
     * @return integer threshold
     */
    private int convertThreshold(final String annotationThreshold) {
        if (StringUtils.isNotBlank(annotationThreshold)) {
            try {
                return Integer.valueOf(annotationThreshold);
            }
            catch (NumberFormatException exception) {
                // not valid
            }
        }
        throw new IllegalArgumentException("Not a parsable integer value: " + annotationThreshold);
    }
}

