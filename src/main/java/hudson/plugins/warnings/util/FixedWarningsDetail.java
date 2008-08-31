package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.util.Collection;

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
     * @param fixedWarnings
     *            all fixed warnings in this build
     * @param header
     *            header to be shown on detail page
     */
    public FixedWarningsDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> fixedWarnings, final String header) {
        super(owner, fixedWarnings, header, Hierarchy.PROJECT);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.FixedWarningsDetail_Name();
    }
}

