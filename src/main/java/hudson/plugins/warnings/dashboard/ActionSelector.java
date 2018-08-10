package hudson.plugins.warnings.dashboard;

import java.util.List;

import javax.annotation.CheckForNull;

import hudson.model.Job;

import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.warnings.WarningsProjectAction;

/**
 * Selects a specific action from the available set of actions.
 *
 * @author Ullrich Hafner
 */
public class ActionSelector {
    private final String parserName;

    /**
     * Creates a new instance of {@link ActionSelector}.
     *
     * @param parserName
     *            the name of the parser
     */
    public ActionSelector(@CheckForNull final String parserName) {
        this.parserName = parserName;
    }

    /**
     * Creates a new instance of {@link ActionSelector}.
     */
    ActionSelector() {
        this(null);
    }

    /**
     * Returns an {@link AbstractProjectAction} that matches the parser of this
     * selector.
     *
     * @param job
     *            the job to get the action from
     * @return the action that uses the parser of this selector, or
     *         <code>null</code> if no such action is found
     */
    public AbstractProjectAction<?> select(final Job<?, ?> job) {
        List<WarningsProjectAction> actions = job.getActions(WarningsProjectAction.class);
        for (WarningsProjectAction action : actions) {
            if (action.isInGroup(parserName)) {
                return action;
            }
        }
        if (actions.size() == 1) { // Fallback for 3.x release
            return actions.get(0);
        }
        return null;
    }

    /**
     * Returns the parser name.
     *
     * @return the parser name
     */
    public String getParserName() {
        return parserName;
    }
}

