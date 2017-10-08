package hudson.plugins.analysis.core;

import java.util.Collection;

import static hudson.plugins.analysis.util.ThresholdValidator.*;

import hudson.model.Result;
import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Checks if the number of annotations exceeds a given threshold value.
 *
 * @author Ulli Hafner
 */
public class BuildResultEvaluator {
    private final String url;

    /**
     * Creates a new instance of {@link BuildResultEvaluator}.
     *
     * @param url
     *            the url of the build results
     */
    public BuildResultEvaluator(final String url) {
        this.url = url;
    }

    protected String getUrl() {
        return url;
    }

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
    public Result evaluateBuildResult(final StringBuilder logger, final Thresholds t,
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
    public Result evaluateBuildResult(final StringBuilder logger, final Thresholds t,
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
    public Result evaluateBuildResult(final StringBuilder logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations,
            final Collection<FileAnnotation> newAnnotations) {
        if (checkAllWarningsForFailure(logger, t, allAnnotations)) {
            return Result.FAILURE;
        }
        if (check(logger, newAnnotations, t.failedNewAll,
                t.failedNewHigh, t.failedNewNormal, t.failedNewLow, false)) {
            return Result.FAILURE;
        }
        if (checkAllWarningsForUnstable(logger, t, allAnnotations)) {
            return Result.UNSTABLE;
        }
        if (check(logger, newAnnotations, t.unstableNewAll,
                t.unstableNewHigh, t.unstableNewNormal, t.unstableNewLow, false)) {
            return Result.UNSTABLE;
        }

        return logSuccess(logger);
    }

    private boolean checkAllWarningsForUnstable(final StringBuilder logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations) {
        return check(logger, allAnnotations, t.unstableTotalAll,
                t.unstableTotalHigh, t.unstableTotalNormal, t.unstableTotalLow, true);
    }

    private boolean checkAllWarningsForFailure(final StringBuilder logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations) {
        return check(logger, allAnnotations, t.failedTotalAll,
                t.failedTotalHigh, t.failedTotalNormal, t.failedTotalLow, true);
    }


    private Result logSuccess(final StringBuilder logger) {
        logger.append(Messages.BuildResultEvaluator_success());

        return Result.SUCCESS;
    }

    private boolean check(final StringBuilder logger, final Collection<? extends FileAnnotation> annotations,
            final String all, final String high, final String normal, final String low, final boolean isTotals) {
        if (checkThresholds(logger, annotations, all, isTotals, Priority.HIGH, Priority.NORMAL, Priority.LOW)) {
            return true;
        }
        if (checkThresholds(logger, annotations, high, isTotals, Priority.HIGH)) {
            return true;
        }
        if (checkThresholds(logger, annotations, normal, isTotals, Priority.NORMAL)) {
            return true;
        }
        if (checkThresholds(logger, annotations, low, isTotals, Priority.LOW)) {
            return true;
        }
        return false;
    }

    private boolean checkFailedNew(final StringBuilder logger, final int delta, final int highDelta, final int normalDelta, final int lowDelta, final Thresholds t) {
        if (checkThresholds(logger, delta, t.failedNewAll, false, Priority.HIGH, Priority.NORMAL, Priority.LOW)) {
            return true;
        }
        if (checkThresholds(logger, highDelta, t.failedNewHigh, false, Priority.HIGH)) {
            return true;
        }
        if (checkThresholds(logger, normalDelta, t.failedNewNormal, false, Priority.NORMAL)) {
            return true;
        }
        if (checkThresholds(logger, lowDelta, t.failedNewLow, false, Priority.LOW)) {
            return true;
        }
        return false;
    }

    private boolean checkUnstableNew(final StringBuilder logger, final int delta, final int highDelta, final int normalDelta, final int lowDelta, final Thresholds t) {
        if (checkThresholds(logger, delta, t.unstableNewAll, false, Priority.HIGH, Priority.NORMAL, Priority.LOW)) {
            return true;
        }
        if (checkThresholds(logger, highDelta, t.unstableNewHigh, false, Priority.HIGH)) {
            return true;
        }
        if (checkThresholds(logger, normalDelta, t.unstableNewNormal, false, Priority.NORMAL)) {
            return true;
        }
        if (checkThresholds(logger, lowDelta, t.unstableNewLow, false, Priority.LOW)) {
            return true;
        }
        return false;
    }

    private boolean checkThresholds(final StringBuilder logger, final Collection<? extends FileAnnotation> allAnnotations,
            final String threshold, final boolean isTotals, final Priority... priorities) {
        return checkThresholds(logger, countAnnotations(allAnnotations, priorities), threshold, isTotals, priorities);
    }

    private boolean checkThresholds(final StringBuilder logger, final int annotationCount,
            final String threshold, final boolean isTotals, final Priority... priorities) {
        if (isAnnotationCountExceeded(annotationCount, threshold)) {
            int delta = annotationCount - convert(threshold);
            if (isTotals) {
                createAllMessage(logger, annotationCount, threshold, delta, priorities);
            }
            else {
                createNewMessage(logger, annotationCount, threshold, delta, priorities);
            }
            return true;
        }
        return false;
    }

    private void createNewMessage(final StringBuilder logger, final int annotationCount,
            final String threshold, final int delta, final Priority... priorities) {
        String newUrl = url + "/new";
        if (priorities.length == 1) {
            Priority priority = priorities[0];
            if (annotationCount == 1) {
                logger.append(Messages.BuildResultEvaluator_unstable_one_new_priority(threshold, delta,
                        priorities[0].getLocalizedString(),
                        newUrl, getPriorityUrl(priority)));
            }
            else {
                logger.append(Messages.BuildResultEvaluator_unstable_new_priority(
                        annotationCount, threshold, delta,
                        priorities[0].getLocalizedString(),
                        newUrl, getPriorityUrl(priority)));
            }
        }
        else {
            if (annotationCount == 1) {
                logger.append(Messages.BuildResultEvaluator_unstable_one_new(threshold, delta, newUrl));
            }
            else {
                logger.append(Messages.BuildResultEvaluator_unstable_new(annotationCount,
                        threshold, delta, newUrl));
            }
        }
    }

    private void createAllMessage(final StringBuilder logger, final int annotationCount,
            final String threshold, final int delta, final Priority... priorities) {
        if (priorities.length == 1) {
            Priority priority = priorities[0];
            if (annotationCount == 1) {
                logger.append(Messages.BuildResultEvaluator_unstable_one_all_priority(
                        threshold, delta, priorities[0].getLocalizedString(), url,
                        getPriorityUrl(priority)));
            }
            else {
                logger.append(Messages.BuildResultEvaluator_unstable_all_priority(annotationCount,
                        threshold, delta, priorities[0].getLocalizedString(), url,
                        getPriorityUrl(priority)));
            }
        }
        else {
            if (annotationCount == 1) {
                logger.append(Messages.BuildResultEvaluator_unstable_one_all(
                        threshold, delta, url));
            }
            else {
                logger.append(Messages.BuildResultEvaluator_unstable_all(annotationCount,
                        threshold, delta, url));
            }
        }
    }

    private String getPriorityUrl(final Priority priority) {
        return url + "/" + priority.name();
    }

    /**
     * Extracts the relevant annotations from the specified collection of
     * annotations. A annotation is relevant, if its priority is greater or
     * equal than the minimum priority of the health descriptor.
     *
     * @param annotations
     *            the annotations to consider
     * @param priorities
     *            the priorities to count
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
     * @deprecated use {@link #evaluateBuildResult(StringBuilder, Thresholds, Collection)}
     */
    @Deprecated
    public Result evaluateBuildResult(final PluginLogger logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations) {
        StringBuilder log = new StringBuilder();
        Result result = evaluateBuildResult(log, t, allAnnotations);
        logger.log(log.toString());
        return result;
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
     * @deprecated use {@link #evaluateBuildResult(StringBuilder, Thresholds, Collection, int, int, int, int)}
     */
    @Deprecated
    public Result evaluateBuildResult(final PluginLogger logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations,
            final int delta, final int highDelta, final int normalDelta, final int lowDelta) {
        StringBuilder log = new StringBuilder();
        Result result = evaluateBuildResult(log, t, allAnnotations, delta, highDelta, normalDelta, lowDelta);
        logger.log(log.toString());
        return result;
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
     * @deprecated use {@link #evaluateBuildResult(StringBuilder, Thresholds, Collection, Collection)}
     */
    @Deprecated
    public Result evaluateBuildResult(final PluginLogger logger, final Thresholds t,
            final Collection<? extends FileAnnotation> allAnnotations,
            final Collection<FileAnnotation> newAnnotations) {
        StringBuilder log = new StringBuilder();
        Result result = evaluateBuildResult(log, t, allAnnotations, newAnnotations);
        logger.log(log.toString());
        return result;
    }
}