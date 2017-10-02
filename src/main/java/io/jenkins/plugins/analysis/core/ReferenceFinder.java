package io.jenkins.plugins.analysis.core;

import javax.annotation.CheckForNull;

import io.jenkins.plugins.analysis.core.steps.PipelineResultAction;

import hudson.model.Run;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;

/**
 * FIXME: write comment.
 *
 * @since 2.0
 * @author Ullrich Hafner
 */
public abstract class ReferenceFinder extends BuildHistory implements ReferenceProvider {
    public static ReferenceProvider create(final Run<?, ?> run, final ResultSelector selector,
            final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference) {
        if (usePreviousBuildAsReference) {
            return new PreviousBuildReference(run, selector, useStableBuildAsReference);
        }
        else {
            return new StablePluginReference(run, selector, useStableBuildAsReference);
        }
    }

    /**
     * Creates a new instance of {@link BuildHistory}.
     *
     * @param baseline
     *            the build to start the history from
     * @param selector
     *            selects the associated action from a build
     */
    public ReferenceFinder(final Run<?, ?> baseline, final ResultSelector selector) {
        super(baseline, selector);
    }

    /**
     * Returns the action of the reference build.
     *
     * @return the action of the reference build, or {@code null} if no
     *         such build exists
     */
    protected abstract PipelineResultAction getReferenceAction();

    /**
     * Returns whether a reference build result exists.
     *
     * @return <code>true</code> if a reference build result exists.
     */
    private boolean hasReferenceAction() {
        return getReferenceAction() != null;
    }

    @Override @CheckForNull
    public Run<?, ?> getReference() {
        PipelineResultAction action = getReferenceAction();
        if (action != null) {
            Run<?, ?> build = action.getRun();
            if (hasValidResult(build)) {
                return build;
            }
        }
        return null;
    }

    @Override
    public boolean hasReference() {
        return getReference() != null;
    }

    /**
     * Returns the annotations of the reference build.
     *
     * @return the annotations of the reference build
     */
    @Override
    public AnnotationContainer getIssues() {
        PipelineResultAction action = getReferenceAction();
        if (action != null) {
            return action.getResult().getContainer();
        }
        return new DefaultAnnotationContainer();
    }
}
