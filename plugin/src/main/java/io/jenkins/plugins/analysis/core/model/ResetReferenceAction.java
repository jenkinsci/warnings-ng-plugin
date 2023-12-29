package io.jenkins.plugins.analysis.core.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import hudson.model.Action;

/**
 * Marker for a build to indicate that this build should serve as a new reference build for the quality gate evaluation
 * of the next build. This marker helps to reset the reference build computation to restart the new issue computation.
 *
 * @author Ullrich Hafner
 */
public class ResetReferenceAction implements Action {
    private final String id;

    /**
     * Creates a new action for the specified tool ID.
     *
     * @param id
     *         the ID of the tool to reset the reference build
     */
    ResetReferenceAction(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
    }
}
