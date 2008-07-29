package hudson.plugins.warnings.util;

import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores the collection of parsed annotations and associated error messages.
 *
 * @author Ulli Hafner
 */
public class ParserResult implements Serializable {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -8414545334379193330L;
    /** The parsed annotations. */
    private final List<FileAnnotation> annotations = new ArrayList<FileAnnotation>();
    /** The collection of error messages. */
    private final List<String> errorMessages = new ArrayList<String>();
    /** Number of annotations by priority. */
    private final Map<Priority, Integer> annotationCountByPriority = new HashMap<Priority, Integer>();
    /** The set of modules. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private final Set<String> modules = new HashSet<String>();

    /**
     * Creates a new instance of {@link ParserResult}.
     */
    public ParserResult() {
        Priority[] priorities = Priority.values();

        for (int priority = 0; priority < priorities.length; priority++) {
            annotationCountByPriority.put(priorities[priority], 0);
        }
    }

    /**
     * Adds the specified annotation to this container.
     *
     * @param annotation the annotation to add
     */
    public void addAnnotation(final FileAnnotation annotation) {
        annotations.add(annotation);
        Integer count = annotationCountByPriority.get(annotation.getPriority());
        annotationCountByPriority.put(annotation.getPriority(), count + 1);
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations the annotations to add
     */
    public final void addAnnotations(final Collection<? extends FileAnnotation> newAnnotations) {
        for (FileAnnotation annotation : newAnnotations) {
            addAnnotation(annotation);
        }
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations the annotations to add
     */
    public final void addAnnotations(final FileAnnotation[] newAnnotations) {
        addAnnotations(Arrays.asList(newAnnotations));
    }

    /**
     * Sets an error message for the specified module name.
     *
     * @param message
     *            the error message
     */
    public void addErrorMessage(final String message) {
        errorMessages.add(message);
    }

    /**
     * Returns the errorMessages.
     *
     * @return the errorMessages
     */
    public Collection<String> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }

    /**
     * Returns the annotations of this result.
     *
     * @return the annotations of this result
     */
    public Collection<FileAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    /**
     * Returns the total number of annotations for this object.
     *
     * @return total number of annotations for this object
     */
    public int getNumberOfAnnotations() {
        return annotations.size();
    }

    /**
     * Returns the total number of annotations of the specified priority for
     * this object.
     *
     * @param priority
     *            the priority
     * @return total number of annotations of the specified priority for this
     *         object
     */
    public int getNumberOfAnnotations(final Priority priority) {
        return annotationCountByPriority.get(priority);
    }

    /**
     * Returns whether this objects has annotations.
     *
     * @return <code>true</code> if this objects has annotations.
     */
    public boolean hasAnnotations() {
        return !annotations.isEmpty();
    }

    /**
     * Returns whether this objects has annotations with the specified priority.
     *
     * @param priority
     *            the priority
     * @return <code>true</code> if this objects has annotations.
     */
    public boolean hasAnnotations(final Priority priority) {
        return annotationCountByPriority.get(priority) > 0;
    }

    /**
     * Returns whether this objects has no annotations.
     *
     * @return <code>true</code> if this objects has no annotations.
     */
    public boolean hasNoAnnotations() {
        return !hasAnnotations();
    }

    /**
     * Returns whether this objects has no annotations with the specified priority.
     *
     * @param priority
     *            the priority
     * @return <code>true</code> if this objects has no annotations.
     */
    public boolean hasNoAnnotations(final Priority priority) {
        return !hasAnnotations(priority);
    }

    /**
     * Returns the number of modules.
     *
     * @return the number of modules
     */
    public int getNumberOfModules() {
        return modules.size();
    }

    /**
     * Returns the parsed modules.
     *
     * @return the parsed modules
     */
    public Set<String> getModules() {
        return Collections.unmodifiableSet(modules);
    }

    /**
     * Adds a new parsed module.
     *
     * @param moduleName
     *            the name of the parsed module
     */
    public void addModule(final String moduleName) {
        modules.add(moduleName);
    }

    /**
     * Adds the specified parsed modules.
     *
     * @param additionalModules
     *            the name of the parsed modules
     */
    public void addModules(final Collection<String> additionalModules) {
        additionalModules.addAll(additionalModules);
    }
}

