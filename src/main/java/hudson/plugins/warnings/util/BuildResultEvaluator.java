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
     *
     * @param logger
     *            logs the results
     * @param minimumPriority
     *            determines which warning priorities should be considered
     * @param result
     *            the result collecting all annotations
     * @param threshold
     *            annotation threshold to be reached if a build should be
     *            considered as unstable
     * @param failureThreshold
     *            annotation threshold to be reached if a build should be
     *            considered as failure
     * @param newResult
     *            the result collecting the new annotations
     * @param newThreshold
     *            threshold for new annotations to be reached if a build should
     *            be considered as unstable
     * @param newFailureThreshold
     *            threshold for new annotations to be reached if a build should
     *            be considered as failure
     * @return the build result
     */
    // CHECKSTYLE:OFF
    public Result evaluateBuildResult(final PluginLogger logger, final Priority minimumPriority,
            final ParserResult result, final String threshold, final String failureThreshold,
            final ParserResult newResult, final String newThreshold, final String newFailureThreshold) {
    // CHECKSTYLE:ON

        int annotationCount = 0;
        int newAnnotationCount = 0;

        for (Priority priority : Priority.collectPrioritiesFrom(minimumPriority)) {
            annotationCount += result.getNumberOfAnnotations(priority);
            newAnnotationCount += newResult.getNumberOfAnnotations(priority);
        }
        logger.log(String.format("Found %d annotations (%d new, %d high, %d normal, %d low)",
                result.getNumberOfAnnotations(), newAnnotationCount,
                result.getNumberOfAnnotations(Priority.HIGH),
                result.getNumberOfAnnotations(Priority.NORMAL),
                result.getNumberOfAnnotations(Priority.LOW)));
        if (minimumPriority != Priority.LOW) {
            logger.log(String.format("Considering %d annotations for build status evaluation", annotationCount));
            logger.log(String.format("Considering %d new annotations for build status evaluation", newAnnotationCount));
        }
        if (isAnnotationCountExceeded(annotationCount, failureThreshold)) {
            logger.log("Setting build status to FAILURE since total number of annotations exceeds the threshold " + failureThreshold);
            return Result.FAILURE;
        }
        if (isAnnotationCountExceeded(newAnnotationCount, newFailureThreshold)) {
            logger.log("Setting build status to FAILURE since total number of new annotations exceeds the threshold " + newFailureThreshold);
            return Result.FAILURE;
        }
        if (isAnnotationCountExceeded(annotationCount, threshold)) {
            logger.log("Setting build status to UNSTABLE since total number of annotations exceeds the threshold " + threshold);
            return Result.UNSTABLE;
        }
        if (isAnnotationCountExceeded(newAnnotationCount, newThreshold)) {
            logger.log("Setting build status to UNSTABLE since total number of new annotations exceeds the threshold " + newThreshold);
            return Result.UNSTABLE;
        }
        logger.log("Don't changing build status, no threshold has been exceeded");
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

