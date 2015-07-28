package hudson.plugins.analysis.views;

import java.util.Collection;

import hudson.model.AbstractBuild;
import hudson.model.Run;

import hudson.plugins.analysis.util.model.FileAnnotation;

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
     * Creates a new instance of {@link TabDetail}.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            factory to create detail objects with
     * @param annotations
     *            the module to show the details for
     * @param url
     *            URL to render the content of this tab
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public TabDetail(final Run<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> annotations, final String url, final String defaultEncoding) {
        super(owner, detailFactory, annotations, defaultEncoding, "No Header", Hierarchy.PROJECT);
        this.url = url;
    }

    @Override
    public String getDisplayName() {
        return "NOT USED";
    }

    /**
     * Returns the URL that renders the content of this tab.
     *
     * @return the URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the jelly script the will render the details.
     *
     * @return the name of the jelly script
     */
    public String getDetails() {
        return "details.jelly";
    }

    /**
     * Returns the jelly script the will render the warnings table.
     *
     * @return the name of the jelly script
     */
    public String getWarnings() {
        return "warnings.jelly";
    }

    /**
     * Returns the jelly script the will render the fixed warnings table.
     *
     * @return the name of the jelly script
     */
    public String getFixed() {
        return "fixed.jelly";
    }

    /**
     * Creates a new instance of {@link TabDetail}.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            factory to create detail objects with
     * @param annotations
     *            the module to show the details for
     * @param url
     *            URL to render the content of this tab
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @deprecated use {@link #TabDetail(Run, DetailFactory, Collection, String, String)} instead
     */
    @Deprecated
    public TabDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> annotations, final String url, final String defaultEncoding) {
        super(owner, detailFactory, annotations, defaultEncoding, "No Header", Hierarchy.PROJECT);
        this.url = url;
    }
}

