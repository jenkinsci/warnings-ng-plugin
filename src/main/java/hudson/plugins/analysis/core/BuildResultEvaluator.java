package hudson.plugins.analysis.core;

import static hudson.plugins.analysis.util.ThresholdValidator.*;

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
     * Evaluates the build result. The build is marked as unstable if one of the
     * thresholds has been exceeded.
     *
     * @param logger
     *            logs the results
     * @param descriptor
     *            health descriptor
     * @param allAnnotations
     *            all annotations
     * @param newAnnotations
     *            new annotations
     * @return the build result
     */
    public Result evaluateBuildResult(final PluginLogger logger, final HealthDescriptor descriptor,
            final Collection<FileAnnotation> allAnnotations, final Collection<FileAnnotation> newAnnotations) {
        ParserResult result = new ParserResult(allAnnotations);
        ParserResult newResult = new ParserResult(newAnnotations);

        int annotationCount = 0;
        int newAnnotationCount = 0;

        for (Priority priority : Priority.collectPrioritiesFrom(descriptor.getMinimumPriority())) {
            annotationCount += result.getNumberOfAnnotations(priority);
            newAnnotationCount += newResult.getNumberOfAnnotations(priority);
        }
        logger.log(String.format("Found %d annotations (%d new, %d high, %d normal, %d low)",
                result.getNumberOfAnnotations(), newAnnotationCount,
                result.getNumberOfAnnotations(Priority.HIGH),
                result.getNumberOfAnnotations(Priority.NORMAL),
                result.getNumberOfAnnotations(Priority.LOW)));
        if (descriptor.getMinimumPriority() != Priority.LOW) {
            logger.log(String.format("Considering %d annotations for build status evaluation", annotationCount));
            logger.log(String.format("Considering %d new annotations for build status evaluation", newAnnotationCount));
        }
        if (isAnnotationCountExceeded(annotationCount, descriptor.getFailureThreshold())) {
            logger.log("Setting build status to FAILURE since total number of annotations exceeds the threshold " + descriptor.getFailureThreshold());
            return Result.FAILURE;
        }
        if (isAnnotationCountExceeded(newAnnotationCount, descriptor.getNewFailureThreshold())) {
            logger.log("Setting build status to FAILURE since total number of new annotations exceeds the threshold " + descriptor.getNewFailureThreshold());
            return Result.FAILURE;
        }
        if (isAnnotationCountExceeded(annotationCount, descriptor.getThreshold())) {
            logger.log("Setting build status to UNSTABLE since total number of annotations exceeds the threshold " + descriptor.getThreshold());
            return Result.UNSTABLE;
        }
        if (isAnnotationCountExceeded(newAnnotationCount, descriptor.getNewThreshold())) {
            logger.log("Setting build status to UNSTABLE since total number of new annotations exceeds the threshold " + descriptor.getNewThreshold());
            return Result.UNSTABLE;
        }
        logger.log("Not changing build status, since no threshold has been exceeded");
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
        if (annotationCount > 0 && isValid(annotationThreshold)) {
            return annotationCount > convert(annotationThreshold);
        }
        return false;
    }
}

