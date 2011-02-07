package hudson.plugins.analysis.views;

import java.util.Collection;

import hudson.model.AbstractBuild;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Result object to visualize the fixed warnings in a build.
 *
 * @author Ulli Hafner
 */
public class FixedWarningsDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -8601095040123486522L;

    /**
     * Creates a new instance of <code>FixedWarningsDetail</code>.
     *
     * @param owner
     *            the current results object as owner of this action
     * @param detailFactory
     *            factory to create detail objects with
     * @param fixedWarnings
     *            all fixed warnings in this build
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param header
     *            header to be shown on detail page
     */
    public FixedWarningsDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> fixedWarnings, final String defaultEncoding, final String header) {
        super(owner, detailFactory, fixedWarnings, defaultEncoding, header, Hierarchy.PROJECT);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.FixedWarningsDetail_Name();
    }
}

