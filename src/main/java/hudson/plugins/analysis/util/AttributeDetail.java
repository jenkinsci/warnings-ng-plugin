package hudson.plugins.analysis.util;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.util.model.FileAnnotation;

import java.util.Collection;

/**
 * Result object to visualize the statistics of a given attribute.
 *
 * @author Ulli Hafner
 */
public class AttributeDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -1854984151887397361L;
    /** Name of the attribute. */
    private final String attributeName;

    /**
     * Creates a new instance of {@link AttributeDetail}.
     *
     * @param owner
     *            current build as owner of this action.
     * @param annotations
     *            the module to show the details for
     * @param header
     *            header to be shown on detail page
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param name
     *            name of the attribute shown in the bread crumb
     */
    public AttributeDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations, final String defaultEncoding, final String header, final String name) {
        super(owner, annotations, defaultEncoding, header, Hierarchy.PROJECT);
        attributeName = name;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return attributeName;
    }
}

