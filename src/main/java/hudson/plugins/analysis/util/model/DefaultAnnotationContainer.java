package hudson.plugins.analysis.util.model;

import java.util.Collection;

/**
 * A simple annotation container that stores a set of annotations.
 *
 * @author Ulli Hafner
 */
public class DefaultAnnotationContainer extends AnnotationContainer {
    /** Dummy name for temporary containers. */
    private static final String TEMPORARY = "temporary";
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -7969178785228510814L;

    /**
     * Creates a new instance of {@link DefaultAnnotationContainer}.
     */
    public DefaultAnnotationContainer() {
        super(TEMPORARY, Hierarchy.PROJECT);
    }

    /**
     * Creates a new instance of {@link DefaultAnnotationContainer}.
     *
     * @param name
     *            the name of this container
     */
    public DefaultAnnotationContainer(final String name) {
        super(name, Hierarchy.PROJECT);
    }

    /**
     * Creates a new instance of {@link DefaultAnnotationContainer}.
     *
     * @param name
     *            the name of this container
     * @param annotations
     *            the annotations to be stored
     */
    public DefaultAnnotationContainer(final String name, final Collection<FileAnnotation> annotations) {
        super(name, Hierarchy.PROJECT);

        addAnnotations(annotations);
    }

    /**
     * Creates a new instance of {@link DefaultAnnotationContainer}.
     *
     * @param annotations
     *            the annotations to be stored
     */
    public DefaultAnnotationContainer(final Collection<FileAnnotation> annotations) {
        super(TEMPORARY, Hierarchy.PROJECT);

        addAnnotations(annotations);
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        setHierarchy(Hierarchy.PROJECT);
        rebuildMappings();
        return this;
    }
}

