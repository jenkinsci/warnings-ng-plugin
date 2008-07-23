package hudson.plugins.warnings.util.model;

import java.util.Collection;

/**
 * Provides an annotation counter for a model object.
 *
 * @author Ulli Hafner
 */
public interface AnnotationProvider {
    /**
     * Returns the total number of annotations for this object.
     *
     * @return total number of annotations for this object
     */
    int getNumberOfAnnotations();

    /**
     * Returns the total number of annotations of the specified priority for
     * this object.
     *
     * @param priority
     *            the priority
     * @return total number of annotations of the specified priority for this
     *         object
     */
    int getNumberOfAnnotations(final Priority priority);

    /**
     * Returns the annotations of the specified priority for this object.
     *
     * @param priority
     *            the priority as a string object
     * @return annotations of the specified priority for this object
     */
    int getNumberOfAnnotations(final String priority);

    /**
     * Returns whether this objects has annotations.
     *
     * @return <code>true</code> if this objects has annotations.
     */
    boolean hasAnnotations();

    /**
     * Returns whether this objects has annotations with the specified priority.
     *
     * @param priority
     *            the priority
     * @return <code>true</code> if this objects has annotations.
     */
    boolean hasAnnotations(final Priority priority);

    /**
     * Returns whether this objects has annotations with the specified priority.
     *
     * @param priority
     *            the priority as a string object
     * @return <code>true</code> if this objects has annotations.
     */
    boolean hasAnnotations(final String priority);

    /**
     * Returns whether this objects has no annotations.
     *
     * @return <code>true</code> if this objects has no annotations.
     */
    boolean hasNoAnnotations();

    /**
     * Returns whether this objects has no annotations with the specified priority.
     *
     * @param priority
     *            the priority
     * @return <code>true</code> if this objects has no annotations.
     */
    boolean hasNoAnnotations(final Priority priority);

    /**
     * Returns whether this objects has no annotations with the specified priority.
     *
     * @param priority
     *            the priority as a string object
     * @return <code>true</code> if this objects has no annotations.
     */
    boolean hasNoAnnotations(final String priority);

    /**
     * Returns the annotations for this object.
     *
     * @return annotations for this object
     */
    Collection<FileAnnotation> getAnnotations();

    /**
     * Returns the annotations of the specified priority for this object.
     *
     * @param priority
     *            the priority
     * @return annotations of the specified priority for this object
     */
    Collection<FileAnnotation> getAnnotations(final Priority priority);

    /**
     * Returns the annotations of the specified priority for this object.
     *
     * @param priority
     *            the priority as a string object
     * @return annotations of the specified priority for this object
     */
    Collection<FileAnnotation> getAnnotations(final String priority);

    /**
     * Returns the annotation with the specified key.
     *
     * @param key
     *            the key of the annotation
     * @return the annotation with the specified key
     */
    FileAnnotation getAnnotation(final long key);

    /**
     * Returns the annotation with the specified key.
     *
     * @param key
     *            the key of the annotation as a long value in string representation
     * @return the annotation with the specified key
     */
    FileAnnotation getAnnotation(final String key);
}
