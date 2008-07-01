package hudson.plugins.warnings.util.model;

import hudson.plugins.warnings.util.model.AnnotationContainer.Hierarchy;

import java.util.Collection;
import java.util.Set;

/**
 * A simple annotation container that stores a set of annotations. The caching
 * hierarchy is {@link Hierarchy#PROJECT}.
 *
 * @author Ulli Hafner
 */
public class DefaultAnnotationContainer extends AnnotationContainer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -7969178785228510814L;

    /**
     * Creates a new instance of {@link DefaultAnnotationContainer}.
     */
    public DefaultAnnotationContainer() {
        super(Hierarchy.PROJECT);
    }

    /**
     * Creates a new instance of {@link DefaultAnnotationContainer}.
     *
     * @param annotations
     *      the annotations to be stored
     */
    public DefaultAnnotationContainer(final Set<FileAnnotation> annotations) {
        super(Hierarchy.PROJECT);

        addAnnotations(annotations);
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<? extends AnnotationContainer> getChildren() {
        return getModules();
    }
}

