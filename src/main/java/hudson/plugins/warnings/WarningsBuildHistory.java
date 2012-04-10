package hudson.plugins.warnings;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildHistory;

import java.util.List;

import javax.annotation.CheckForNull;

/**
 * A build history for warnings results. Picks the right action using the parser group.
 *
 * @author Ulli Hafner
 */
public class WarningsBuildHistory extends BuildHistory {
    private final String group;

    /**
     * Creates a new instance of {@link WarningsBuildHistory}.
     *
     * @param lastFinishedBuild
     *            the last finished build
     * @param group
     *            the parser group
     */
    public WarningsBuildHistory(final AbstractBuild<?, ?> lastFinishedBuild, @CheckForNull final String group) {
        super(lastFinishedBuild, WarningsResultAction.class);

        this.group = group;
    }

    @Override
    public WarningsResultAction getResultAction(final AbstractBuild<?, ?> build) {
        List<WarningsResultAction> actions = build.getActions(WarningsResultAction.class);
        if (group != null) {
            for (WarningsResultAction action : actions) {
                if (group.equals(action.getParser())) {
                    return action;
                }
            }
        }
        if (!actions.isEmpty() && actions.get(0).getParser() == null) { // fallback 3.x
            return actions.get(0);
        }
        return null;
    }
}

