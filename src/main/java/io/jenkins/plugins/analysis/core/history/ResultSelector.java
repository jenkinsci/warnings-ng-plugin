package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import io.jenkins.plugins.analysis.core.steps.ResultAction;

import hudson.model.Run;

/**
 * Selects a {@link ResultAction} from all registered actions in a given job.
 *
 * @author Ullrich Hafner
 */
public interface ResultSelector {
    /**
     * Tries to find a result action of the specified run that should be used to compute the history.
     *
     * @param run the run
     * @return the result action, if there is one attached to the job
     */
    Optional<ResultAction> get(Run<?, ?> run);
}
