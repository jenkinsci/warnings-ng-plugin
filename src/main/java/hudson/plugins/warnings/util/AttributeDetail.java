package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.util.Collection;

/**
 * Result object to visualize the statistics of a category.
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
     * @param name
     *            name of the attribute shown in the bread crumb
     */
    public AttributeDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations, final String header, final String name) {
        super(owner, annotations, header, Hierarchy.PROJECT);
        attributeName = name;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return attributeName;
    }

    /**
     * Returns a tooltip showing the distribution of priorities for the selected
     * category.
     *
     * @param category
     *            the category to show the distribution for
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip(final String category) {
        return "TODO";
    }
}

