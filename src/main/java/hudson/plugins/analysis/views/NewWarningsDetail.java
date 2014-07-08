package hudson.plugins.analysis.views;

import java.util.Collection;

import hudson.model.AbstractBuild;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Result object to visualize the new warnings in a build.
 *
 * @author Ulli Hafner
 */
public class NewWarningsDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5093487322493056475L;

    /**
     * Creates a new instance of <code>NewWarningsDetail</code>.
     *
     * @param owner
     *            the current build as owner of this action
     * @param detailFactory
     *            factory to create detail objects with
     * @param newWarnings
     *            all new warnings in this build
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param header
     *            header to be shown on detail page
     */
    public NewWarningsDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> newWarnings, final String defaultEncoding, final String header) {
        super(owner, detailFactory, newWarnings, defaultEncoding, header, Hierarchy.PROJECT);
    }

    @Override
    public String getDisplayName() {
        return Messages.NewWarningsDetail_Name();
    }
}

