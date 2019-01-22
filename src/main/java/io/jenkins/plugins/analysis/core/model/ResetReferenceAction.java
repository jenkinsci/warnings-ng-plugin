package io.jenkins.plugins.analysis.core.model;

import edu.umd.cs.findbugs.annotations.Nullable;

import hudson.model.Action;

import io.jenkins.plugins.analysis.core.util.QualityGate;

/**
 * Marker for a build to indicate that this build should serve as new reference build for the {@link QualityGate}
 * evaluation of the next build. This marker helps to reset the reference build computation in order to restart the new
 * issue computation.
 *
 * @author Ullrich Hafner
 */
public class ResetReferenceAction implements Action {
    // TODO: remember the ID?
    @Nullable
    @Override
    public String getIconFileName() {
        return null;
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public String getUrlName() {
        return null;
    }
}
