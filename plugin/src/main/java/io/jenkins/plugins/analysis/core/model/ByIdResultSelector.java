package io.jenkins.plugins.analysis.core.model;

import java.util.List;
import java.util.Optional;

import hudson.model.Run;

/**
 * Selects actions using the specific ID of an action.
 *
 * @author Ullrich Hafner
 */
public class ByIdResultSelector implements ResultSelector {
    private final String id;

    /**
     * Creates a new instance of {@link ByIdResultSelector}.
     *
     * @param id
     *         the ID of the result
     */
    public ByIdResultSelector(final String id) {
        this.id = id;
    }

    @Override
    public Optional<ResultAction> get(final Run<?, ?> build) {
        List<ResultAction> actions = build.getActions(ResultAction.class);
        for (ResultAction action : actions) {
            if (id.equals(action.getId())) {
                return Optional.of(action);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return String.format("%s with ID %s", ResultAction.class.getName(), id);
    }
}

