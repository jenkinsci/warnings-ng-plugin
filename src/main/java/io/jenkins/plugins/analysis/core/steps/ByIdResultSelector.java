package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import java.util.List;

import io.jenkins.plugins.analysis.core.ResultSelector;

import hudson.model.Run;

/**
 * Selects actions using the specific ID of an action.
 *
 * @author Ulli Hafner
 */
public class ByIdResultSelector implements ResultSelector {
    private final String id;

    /**
     * Creates a new instance of {@link ByIdResultSelector}.
     *
     * @param id the ID of the result
     */
    public ByIdResultSelector(final String id) {
        this.id = id;
    }

    @Override @CheckForNull
    public PipelineResultAction get(final Run<?, ?> build) {
        List<PipelineResultAction> actions = build.getActions(PipelineResultAction.class);
        for (PipelineResultAction action : actions) {
            if (id.equals(action.getId())) {
                return action;
            }
        }
        return null;
    }
}

