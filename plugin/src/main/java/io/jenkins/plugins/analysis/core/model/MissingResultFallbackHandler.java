package io.jenkins.plugins.analysis.core.model;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;

/**
 * Registers this class as a Jenkins extension that provides fallback for analysis builds. This helps display warnings
 * in the job view even when no analysis is present in one of the latest builds. The actions are rendered by finding the
 * most recent build with valid {@link ResultAction} instances, and then attaching the corresponding {@link JobAction}
 * to the job.
 */
@Extension
public final class MissingResultFallbackHandler extends TransientActionFactory<Job<?, ?>> {
    /**
     * The maximum number of builds to consider when looking for a valid {@link ResultAction}. This is set to a maximum
     * of 5 to avoid performance issues with large job histories.
     */
    public static final int MAX_BUILDS_TO_CONSIDER = 5;

    @Override
    @SuppressWarnings("unchecked")
    public Class<Job<?, ?>> type() {
        return (Class) Job.class;
    }

    @NonNull
    @Override
    public Collection<? extends Action> createFor(@NonNull final Job<?, ?> target) {
        Run<?, ?> currentBuild = target.getLastBuild();
        if (currentBuild == null || currentBuild.isBuilding()) {
            return Collections.emptyList();
        }

        List<ResultAction> currentResultActions = currentBuild.getActions(ResultAction.class);
        if (!currentResultActions.isEmpty()) {
            return Collections.emptyList();
        }

        Run<?, ?> previousSuccessfulBuild = currentBuild.getPreviousSuccessfulBuild();
        if (previousSuccessfulBuild != null) {
            if (!previousSuccessfulBuild.getActions(ResultAction.class).isEmpty()) {
                return Collections.emptyList();
            }
        }

        int count = 0;
        for (Run<?, ?> previousBuild = currentBuild.getPreviousBuild();
                previousBuild != null && count < MAX_BUILDS_TO_CONSIDER;
                previousBuild = previousBuild.getPreviousBuild(), count++) {
            List<ResultAction> resultActions = previousBuild.getActions(ResultAction.class);

            List<Action> actions = new ArrayList<>();
            for (ResultAction action : resultActions) {
                actions.addAll(action.getProjectActions());
            }

            if (!resultActions.isEmpty()) {
                return actions;
            }
        }

        return Collections.emptyList();
    }
}
