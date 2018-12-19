package io.jenkins.plugins.analysis.core.model;

import java.util.Optional;

import hudson.model.Run;

/**
 * Selects a {@link ResultAction} from all registered actions in a given job.
 *
 * @author Ullrich Hafner
 */
public interface ResultSelector {
    /**
     * Tries to find a result action of the specified build that should be used to compute the history.
     *
     * @param build
     *         the build
     *
     * @return the result action, if there is one attached to the job
     */
    Optional<ResultAction> get(Run<?, ?> build);
}
