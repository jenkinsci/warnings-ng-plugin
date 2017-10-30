package io.jenkins.plugins.analysis.core.views;

import edu.hm.hafner.analysis.Issues;

import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.Messages;

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
    public NewWarningsDetail(final Run<?, ?> owner, final Issues newWarnings, final String defaultEncoding, final ModelObject parent) {
        super(owner, newWarnings, defaultEncoding, parent, Messages.NewWarningsDetail_Name())       ;
    }
}

