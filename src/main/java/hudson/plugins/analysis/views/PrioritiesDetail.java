package hudson.plugins.analysis.views;

import java.util.Collection;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

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
     * Creates a new instance of <code>PrioritiesDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            factory to create detail objects with
     * @param annotations
     *            the package to show the details for
     * @param priority
     *            the priority of all annotations
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param header
     *            header to be shown on detail page
     */
    public PrioritiesDetail(final Run<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> annotations, final Priority priority, final String defaultEncoding, final String header) {
        super(owner, detailFactory, annotations, defaultEncoding, header, Hierarchy.PROJECT);
        this.priority = priority;
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    @Override
    public String getHeader() {
        return getName() + " - " + priority.getLongLocalizedString();
    }

    @Override
    public String getDisplayName() {
        return priority.getLongLocalizedString();
    }

    @Deprecated
    public PrioritiesDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> annotations, final Priority priority, final String defaultEncoding, final String header) {
        this((Run<?, ?>) owner, detailFactory, annotations, priority, defaultEncoding, header);
    }
}

