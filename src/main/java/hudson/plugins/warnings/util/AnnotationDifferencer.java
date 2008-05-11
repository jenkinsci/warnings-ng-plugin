package hudson.plugins.warnings.util;

import hudson.plugins.warnings.util.model.FileAnnotation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides several utility methods based on sets of annotations.
 *
 * @author Ulli Hafner
 */
public final class AnnotationDifferencer {
    /**
     * Returns the new annotations, i.e., the annotations that are in the actual build
     * but not in the previous.
     *
     * @param actual
     *            annotations in actual build
     * @param previous
     *            annotations in previous build
     * @return the new annotations
     */
    public static Set<FileAnnotation> getNewWarnings(final Collection<FileAnnotation> actual, final Collection<FileAnnotation> previous) {
        Set<FileAnnotation> warnings = new HashSet<FileAnnotation>(actual);
        warnings.removeAll(previous);
        return warnings;
    }

    /**
     * Returns the fixed annotations, i.e., the annotations that are in the previous build
     * but not in the actual.
     *
     * @param actual
     *            annotations in actual build
     * @param previous
     *            annotations in previous build
     * @return the fixed annotations
     */
    public static Set<FileAnnotation> getFixedWarnings(final Collection<FileAnnotation> actual, final Collection<FileAnnotation> previous) {
        Set<FileAnnotation> warnings = new HashSet<FileAnnotation>(previous);
        warnings.removeAll(actual);
        return warnings;
    }

    /**
     * Creates a new instance of <code>AnnotationDifferencer</code>.
     */
    private AnnotationDifferencer() {
        // prevents instantiation
    }
}

