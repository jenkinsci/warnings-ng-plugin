package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.util.Collection;

/**
 * Result object representing a dynamic tab.
 *
 * @author Ulli Hafner
 */
public class TabDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -1854984151887397361L;
    /** URL of the content to load. */
    private final String url;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param annotations
     *            the module to show the details for
     * @param url
     *            URL to render the content of this tab
     */
    public TabDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations, final String url) {
        super(owner, annotations, "No Header", Hierarchy.PROJECT);
        this.url = url;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "NOT USED";
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<? extends AnnotationContainer> getChildren() {
        return getModules();
    }

    /**
     * Returns the url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }
}

