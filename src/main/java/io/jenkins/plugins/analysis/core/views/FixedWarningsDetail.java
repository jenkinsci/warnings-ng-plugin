package io.jenkins.plugins.analysis.core.views;

import edu.hm.hafner.analysis.Issues;

import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.Messages;

/**
 * Result object to visualize the fixed issues in a run.
 *
 * @author Ulli Hafner
 */
public class FixedWarningsDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -8601095040123486522L;

    /**
     * Creates a new instance of {@code FixedWarningsDetail}.
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
    public FixedWarningsDetail(final Run<?, ?> owner, final Issues fixedWarnings, final String defaultEncoding, final ModelObject parent) {
        super(owner, fixedWarnings, defaultEncoding, parent, Messages.FixedWarningsDetail_Name());
    }
}

