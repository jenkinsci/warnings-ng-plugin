package io.jenkins.plugins.analysis.core.history;

import io.jenkins.plugins.analysis.core.steps.PipelineResultAction;

import hudson.model.Run;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public class PreviousBuildReference extends ReferenceFinder {
    private final boolean useStableBuildAsReference;

    /**
     * Creates a new instance of {@link PreviousBuildReference}.
     * @param baseline the build to start the history from
     * @param selector     type of the action that contains the build results
     * @param useStableBuildAsReference
     */
    public PreviousBuildReference(final Run<?, ?> baseline, final ResultSelector selector, final boolean useStableBuildAsReference) {
        super(baseline, selector);
        this.useStableBuildAsReference = useStableBuildAsReference;
    }

    @Override
    protected PipelineResultAction getReferenceAction() {
        return getPreviousAction(useStableBuildAsReference);
    }
}
