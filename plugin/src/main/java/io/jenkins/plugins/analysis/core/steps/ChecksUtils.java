package io.jenkins.plugins.analysis.core.steps;

import edu.hm.hafner.analysis.Severity;
import hudson.model.TaskListener;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;

/**
 * Helpers class containing utility methods for Check publishing.
 */
public class ChecksUtils {
    private ChecksUtils() {
        // prevent instantiation of this utility class
    }

    /**
     * Convert a Severity to an appropriate ChecksAnnotationLevel.
     * @param severity the severity to map.
     * @param listener taskListener where any issues can be reported.
     * @return ChecksAnnotationLevel a level corresponsing to the given severity.
     * */
    public static ChecksAnnotationLevel toChecksAnnotationLevel(final Severity severity, final TaskListener listener) {
        // normalize the severity then map the issue severity to the check level.
        final Severity sev = Severity.guessFromString(severity.getName());
    
        // do not use .equals here - guessFromString returns static instances and we do not want to fall back
        // to checking strings in the case of a non match
        if (sev == Severity.ERROR) {
            return ChecksAnnotationLevel.FAILURE;
        }
        if (sev == Severity.WARNING_HIGH) {
            return ChecksAnnotationLevel.WARNING;
        }
        if (sev == Severity.WARNING_NORMAL) {
            return ChecksAnnotationLevel.NOTICE;
        }
        if (sev == Severity.WARNING_LOW) {
            // this could be because the state was mapped to this, or that it is the default
            // but no API is exposed for this case
            return ChecksAnnotationLevel.NOTICE;
        }
        listener.error("Checks publisher received an unexpected severity (%s)", sev.getName());
        return ChecksAnnotationLevel.NOTICE;
    }
}
