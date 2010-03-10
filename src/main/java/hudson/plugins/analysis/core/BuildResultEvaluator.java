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
     * @param annotationDelta
     *            the annotation difference between this build and the reference
     *            build (i.e., the difference #numbersOfAnnotations(build) - #numbersOfAnnotations(referenceBuild))
     * @return the build result
     */
    public Result evaluateBuildResult(final PluginLogger logger, final HealthDescriptor descriptor,
            final Collection<FileAnnotation> allAnnotations, final int annotationDelta) {

        int annotationCount = extractNumberOfRelevantAnnotations(logger, descriptor, "", allAnnotations);
        if (descriptor.getMinimumPriority() != Priority.LOW) {
            logger.log(String.format("Considering %d annotations for build status evaluation", annotationCount));
            logger.log(String.format("Considering %d new annotations for build status evaluation", annotationDelta));
        }
        if (isAnnotationCountExceeded(annotationCount, descriptor.getFailureThreshold())) {
            logger.log("Setting build status to FAILURE since total number of annotations exceeds the threshold " + descriptor.getFailureThreshold());
            return Result.FAILURE;
        }
        if (isAnnotationCountExceeded(annotationDelta, descriptor.getNewFailureThreshold())) {
            logger.log("Setting build status to FAILURE since total number of new annotations exceeds the threshold " + descriptor.getNewFailureThreshold());
            return Result.FAILURE;
        }
        if (isAnnotationCountExceeded(annotationCount, descriptor.getThreshold())) {
            logger.log("Setting build status to UNSTABLE since total number of annotations exceeds the threshold " + descriptor.getThreshold());
            return Result.UNSTABLE;
        }
        if (isAnnotationCountExceeded(annotationDelta, descriptor.getNewThreshold())) {
            logger.log("Setting build status to UNSTABLE since total number of new annotations exceeds the threshold " + descriptor.getNewThreshold());
            return Result.UNSTABLE;
        }
        logger.log("Not changing build status, since no threshold has been exceeded");
        return Result.SUCCESS;

    }

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
     *            the asymmetric set intersection of the annotations of this build and the annotations of the reference
     *            build (i.e., the operation annotations(referenceBuild).removeAll(numbersOfAnnotations(build))
     * @return the build result
     */
    public Result evaluateBuildResult(final PluginLogger logger, final HealthDescriptor descriptor,
            final Collection<FileAnnotation> allAnnotations, final Collection<FileAnnotation> newAnnotations) {
        int newAnnotationCount = extractNumberOfRelevantAnnotations(logger, descriptor, "new", newAnnotations);

        return evaluateBuildResult(logger, descriptor, allAnnotations, newAnnotationCount);
    }

    /**
     * Extracts the relevant annotations from the specified collection of
     * annotations. A annotation is relevant, if its priority is greater or
     * equal than the minimum priority of the health descriptor.
     *
     * @param logger
     *            logs the results
     * @param descriptor
     *            health descriptor
     * @param description
     *            the description of the analyzed annotations
     * @param annotations
     *            the annotations to consider
     * @return the number of relevant annotations
     */
    private int extractNumberOfRelevantAnnotations(final PluginLogger logger,
            final HealthDescriptor descriptor, final String description, final Collection<FileAnnotation> annotations) {
        ParserResult result = new ParserResult(annotations);

        int annotationCount = 0;
        for (Priority priority : Priority.collectPrioritiesFrom(descriptor.getMinimumPriority())) {
            annotationCount += result.getNumberOfAnnotations(priority);
        }
        logger.log(String.format("Found %d %s annotations (%d high, %d normal, %d low)",
                result.getNumberOfAnnotations(),
                description,
                result.getNumberOfAnnotations(Priority.HIGH),
                result.getNumberOfAnnotations(Priority.NORMAL),
                result.getNumberOfAnnotations(Priority.LOW)));
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

