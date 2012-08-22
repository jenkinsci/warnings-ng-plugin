package hudson.plugins.analysis.core;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Provides several utility methods based on sets of annotations.
 *
 * @author Ulli Hafner
 */
public final class AnnotationDifferencer {
    /**
     * Returns the new annotations, i.e., the annotations that are in the current build
     * but not in the previous.
     *
     * @param current
     *            annotations in current build
     * @param previous
     *            annotations in previous build
     * @return the new annotations
     */
    public static Set<FileAnnotation> getNewAnnotations(final Set<FileAnnotation> current, final Set<FileAnnotation> previous) {
        return removeDuplicates(difference(current, previous), previous);
    }

    /**
     * Computes the elements of the first set without the elements in the second
     * set.
     *
     * @param target
     *            the first set
     * @param other
     *            the second set
     * @return the difference of the sets
     */
    private static Set<FileAnnotation> difference(
            final Set<FileAnnotation> target, final Set<FileAnnotation> other) {
        Set<FileAnnotation> difference = Sets.newHashSet(target);
        difference.removeAll(other);
        return difference;
    }

    /**
     * Removes the annotations from the {@code targetSet} that have the same
     * hash code as one annotation in the {@code otherSet}.
     *
     * @param targetSet
     *            the target set
     * @param previous
     *            the other set that is checked for duplicates
     * @return the unique annotations
     */
    private static Set<FileAnnotation> removeDuplicates(final Set<FileAnnotation> targetSet, final Set<FileAnnotation> previous) {
        Set<Long> otherHashCodes = extractHashCodes(previous);
        Set<FileAnnotation> duplicates = Sets.newHashSet();
        for (FileAnnotation annotation : targetSet) {
            if (otherHashCodes.contains(annotation.getContextHashCode())) {
                duplicates.add(annotation);
            }
        }

        targetSet.removeAll(duplicates);
        return targetSet;
    }

    /**
     * Extracts the hash codes of the specified collection of annotations.
     *
     * @param previous
     *            the annotations to get the hash codes from
     * @return the hash codes of the specified collection of annotations
     */
    private static Set<Long> extractHashCodes(final Set<FileAnnotation> previous) {
        HashSet<Long> hashCodes = new HashSet<Long>();
        for (FileAnnotation annotation : previous) {
            hashCodes.add(annotation.getContextHashCode());
        }
        return hashCodes;
    }

    /**
     * Returns the fixed annotations, i.e., the annotations that are in the previous build
     * but not in the current.
     *
     * @param current
     *            annotations in current build
     * @param previous
     *            annotations in previous build
     * @return the fixed annotations
     */
    public static Set<FileAnnotation> getFixedAnnotations(final Set<FileAnnotation> current, final Set<FileAnnotation> previous) {
        return removeDuplicates(difference(previous, current), current);
    }

    /**
     * Creates a new instance of <code>AnnotationDifferencer</code>.
     */
    private AnnotationDifferencer() {
        // prevents instantiation
    }
}

