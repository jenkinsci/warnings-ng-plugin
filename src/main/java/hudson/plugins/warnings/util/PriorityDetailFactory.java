package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.Priority;

/**
 * Creates priority detail objects.
 *
 * @author Ulli Hafner
 */
public class PriorityDetailFactory {
    /**
     * Returns whether the provided value is a valid priority.
     *
     * @param value the value to check
     * @return <code>true</code> if the provided value is a valid priority, <code>false</code> otherwise
     */
    public boolean isPriority(final String value) {
        for (Priority priority : Priority.values()) {
            if (priority.toString().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new priorities detail object.
     *
     * @param priority
     *            the priority to show
     * @param owner
     *            owner of the build
     * @param container
     *            annotation container
     * @param header
     *            header to show
     * @return the priority detail
     */
    public PrioritiesDetail create(final String priority, final AbstractBuild<?, ?> owner, final AnnotationContainer container, final String header) {
        if (Priority.HIGH.toString().equalsIgnoreCase(priority)) {
            return createPrioritiesDetail(Priority.HIGH, owner, container, header);
        }
        else if (Priority.NORMAL.toString().equalsIgnoreCase(priority)) {
            return createPrioritiesDetail(Priority.NORMAL, owner, container, header);
        }
        else if (Priority.LOW.toString().equalsIgnoreCase(priority)) {
            return createPrioritiesDetail(Priority.LOW, owner, container, header);
        }
        throw new IllegalArgumentException("Wrong priority provided: " + priority);
    }

    /**
     * Creates a new priorities detail.
     *
     * @param priority
     *            the priority to show
     * @param owner
     *            owner of the build
     * @param container
     *            annotation container
     * @param header
     *            header to show
     * @return the priority detail
     */
    protected PrioritiesDetail createPrioritiesDetail(final Priority priority, final AbstractBuild<?, ?> owner, final AnnotationContainer container, final String header) {
        return new PrioritiesDetail(owner, container.getAnnotations(priority), priority, header);
    }
}

