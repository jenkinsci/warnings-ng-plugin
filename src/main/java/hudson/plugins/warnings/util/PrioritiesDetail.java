package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.util.Collection;

/**
 * Result object to visualize the priorities statistics of an annotation container.
 *
 * @author Ulli Hafner
 */
public class PrioritiesDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5315146140343619856L;
    /** Priority of the annotations. */
    private final Priority priority;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param annotations
     *            the package to show the details for
     * @param priority
     *            the priority of all annotations
     * @param header
     *            header to be shown on detail page
     */
    public PrioritiesDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations, final Priority priority, final String header) {
        super(owner, annotations, header, Hierarchy.PROJECT);
        this.priority = priority;
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    public String getHeader() {
        return getName() + " - " + priority.getLongLocalizedString();
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return priority.getLongLocalizedString();
    }

    /**
     * Returns the dynamic result of the FindBugs analysis (detail page for a package).
     *
     * @param link the package name to get the result for
     * @return the dynamic result of the FindBugs analysis (detail page for a package).
     */
    @Override
    public ModelObject getDynamic(final String link) {
        return new SourceDetail(getOwner(), getAnnotation(link));
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<? extends AnnotationContainer> getChildren() {
        return getModules();
    }
}

