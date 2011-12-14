package hudson.plugins.analysis.core;

import static hudson.plugins.analysis.util.ThresholdValidator.*;

import java.util.Arrays;
import java.util.Collection;

import hudson.model.Result;

import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Evaluates if the number of annotations exceeds a given threshold value.
 *
 * @author Ulli Hafner
 */
public class BuildResultEvaluator {
    /**
     * Evaluates the build result. The build is marked as unstable or failed if
     * one of the thresholds has been exceeded.
     *
     * @param logger
     *            logs the results
     * @param t
     *            the thresholds
     * @param allAnnotations
     *            all annotations
     * @return the build result
     */
    public Result evaluateBuildResult(final PluginLogger logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations) {
        if (checkAllWarningsForFailure(logger, t, allAnnotations)) {
            return Result.FAILURE;
        }
        if (checkAllWarningsForUnstable(logger, t, allAnnotations)) {
            return Result.UNSTABLE;
        }

        return logSuccess(logger);
    }

    /**
     * Evaluates the build result. The build is marked as unstable or failed if
     * one of the thresholds has been exceeded.
     *
     * @param logger
     *            logs the results
     * @param t
     *            the thresholds
     * @param delta
     *            delta between this build and reference build
     * @param highDelta
     *            delta between this build and reference build (priority high)
     * @param normalDelta
     *            delta between this build and reference build (priority normal)
     * @param lowDelta
     *            delta between this build and reference build (priority low)
     * @param allAnnotations
     *            all annotations
     * @return the build result
     */
    public Result evaluateBuildResult(final PluginLogger logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations,
            final int delta, final int highDelta, final int normalDelta, final int lowDelta) {
        if (checkAllWarningsForFailure(logger, t, allAnnotations)) {
            return Result.FAILURE;
        }
        if (checkFailedNew(logger, delta, highDelta, normalDelta, lowDelta, t)) {
            return Result.FAILURE;
        }
        if (checkAllWarningsForUnstable(logger, t, allAnnotations)) {
            return Result.UNSTABLE;
        }
        if (checkUnstableNew(logger, delta, highDelta, normalDelta, lowDelta, t)) {
            return Result.UNSTABLE;
        }

        return logSuccess(logger);
    }

    private boolean checkAllWarningsForUnstable(final PluginLogger logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations) {
        return check(logger, allAnnotations, Result.UNSTABLE,
                t.unstableTotalAll, t.unstableTotalHigh, t.unstableTotalNormal, t.unstableTotalLow);
    }

    private boolean checkAllWarningsForFailure(final PluginLogger logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations) {
        return check(logger, allAnnotations, Result.FAILURE,
                t.failedTotalAll, t.failedTotalHigh, t.failedTotalNormal, t.failedTotalLow);
    }

    /**
     * Evaluates the build result. The build is marked as unstable or failed if one of the
     * thresholds has been exceeded.
     *
     * @param logger
     *            logs the results
     * @param t
     *            the thresholds
     * @param allAnnotations
     *            all annotations
     * @param newAnnotations
     *            the new annotations
     * @return the build result
     */
    public Result evaluateBuildResult(final PluginLogger logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations,
            final Collection<FileAnnotation> newAnnotations) {
        if (checkAllWarningsForFailure(logger, t, allAnnotations)) {
            return Result.FAILURE;
        }
        if (check(logger, newAnnotations, Result.FAILURE,
                t.failedNewAll, t.failedNewHigh, t.failedNewNormal, t.failedNewLow)) {
            return Result.FAILURE;
        }
        if (checkAllWarningsForUnstable(logger, t, allAnnotations)) {
            return Result.UNSTABLE;
        }
        if (check(logger, newAnnotations, Result.UNSTABLE,
                t.unstableNewAll, t.unstableNewHigh, t.unstableNewNormal, t.unstableNewLow)) {
            return Result.UNSTABLE;
        }

        return logSuccess(logger);
    }

    private Result logSuccess(final PluginLogger logger) {
        logger.log("Not changing build status, since no threshold has been exceeded");

        return Result.SUCCESS;
    }

    private boolean check(final PluginLogger logger, final Collection<? extends FileAnnotation> annotations,
            final Result result, final String all, final String high, final String normal, final String low) {
        if (checkThresholds(logger, annotations, all, result, Priority.HIGH, Priority.NORMAL, Priority.LOW)) {
            return true;
        }
        if (checkThresholds(logger, annotations, high, result, Priority.HIGH)) {
            return true;
        }
        if (checkThresholds(logger, annotations, normal, result, Priority.NORMAL)) {
            return true;
        }
        if (checkThresholds(logger, annotations, low, result, Priority.LOW)) {
            return true;
        }
        return false;
    }

    private boolean checkFailedNew(final PluginLogger logger, final int delta, final int highDelta, final int normalDelta, final int lowDelta, final Thresholds t) {
        if (checkThresholds(logger, delta, t.failedNewAll, Result.FAILURE, Priority.HIGH, Priority.NORMAL, Priority.LOW)) {
            return true;
        }
        if (checkThresholds(logger, highDelta, t.failedNewHigh, Result.FAILURE, Priority.HIGH)) {
            return true;
        }
        if (checkThresholds(logger, normalDelta, t.failedNewNormal, Result.FAILURE, Priority.NORMAL)) {
            return true;
        }
        if (checkThresholds(logger, lowDelta, t.failedNewLow, Result.FAILURE, Priority.LOW)) {
            return true;
        }
        return false;
    }

    private boolean checkUnstableNew(final PluginLogger logger, final int delta, final int highDelta, final int normalDelta, final int lowDelta, final Thresholds t) {
        if (checkThresholds(logger, delta, t.unstableNewAll, Result.UNSTABLE, Priority.HIGH, Priority.NORMAL, Priority.LOW)) {
            return true;
        }
        if (checkThresholds(logger, highDelta, t.unstableNewHigh, Result.UNSTABLE, Priority.HIGH)) {
            return true;
        }
        if (checkThresholds(logger, normalDelta, t.unstableNewNormal, Result.UNSTABLE, Priority.NORMAL)) {
            return true;
        }
        if (checkThresholds(logger, lowDelta, t.unstableNewLow, Result.UNSTABLE, Priority.LOW)) {
            return true;
        }
        return false;
    }

    private boolean checkThresholds(final PluginLogger logger, final Collection<? extends FileAnnotation> allAnnotations,
            final String threshold, final Result result, final Priority... priorities) {
        return checkThresholds(logger, countAnnotations(allAnnotations, priorities), threshold, result, priorities);
    }

    private boolean checkThresholds(final PluginLogger logger, final int annotationCount,
            final String threshold, final Result result, final Priority... priorities) {
        if (isAnnotationCountExceeded(annotationCount, threshold)) {
            logger.log("Setting build status to " + result
                    + " since total number of annotations exceeds the threshold " + threshold + ": "
                    + Arrays.toString(priorities));
            return true;
        }
        return false;
    }

    /**
     * Extracts the relevant annotations from the specified collection of
     * annotations. A annotation is relevant, if its priority is greater or
     * equal than the minimum priority of the health descriptor.
     *
     * @param annotations
     *            the annotations to consider
     * @return the number of relevant annotations
     */
    private int countAnnotations(final Collection<? extends FileAnnotation> annotations, final Priority... priorities) {
        ParserResult result = new ParserResult(annotations);
        int annotationCount = 0;
        for (Priority priority : priorities) {
            annotationCount += result.getNumberOfAnnotations(priority);
        }
        return annotationCount;
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
        if (annotationCount > 0 && isValid(annotationThreshold)) {
            return annotationCount > convert(annotationThreshold);
        }
        return false;
    }
}

