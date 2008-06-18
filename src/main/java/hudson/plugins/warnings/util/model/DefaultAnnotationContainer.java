package hudson.plugins.warnings.util.model;


import java.util.Collection;
import java.util.Set;

/**
 * FIXME: Document type DefaultAnnotatoionContainer.
 *
 * @author Ulli Hafner
 */
public class DefaultAnnotationContainer extends AnnotationContainer {
    /**
     * Creates a new instance of {@link DefaultAnnotationContainer}.
     * @param annotations
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

