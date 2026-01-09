package io.jenkins.plugins.analysis.core.model;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        if (shouldSkipFallback(currentBuild)) {
            return Collections.emptyList();
        }

        return findActionsInPreviousBuilds(currentBuild);
    }

    private boolean shouldSkipFallback(final Run<?, ?> currentBuild) {
        if (currentBuild == null || currentBuild.isBuilding()) {
            return true;
        }

        List<ResultAction> currentResultActions = currentBuild.getActions(ResultAction.class);
        return !currentResultActions.isEmpty();
    }

    private Collection<? extends Action> findActionsInPreviousBuilds(final Run<?, ?> currentBuild) {
        int count = 0;
        for (Run<?, ?> previousBuild = currentBuild.getPreviousBuild();
                previousBuild != null && count < MAX_BUILDS_TO_CONSIDER;
                previousBuild = previousBuild.getPreviousBuild(), count++) {
            List<ResultAction> resultActions = previousBuild.getActions(ResultAction.class);

            if (!resultActions.isEmpty()) {
                return deduplicateActions(resultActions);
            }
        }

        return Collections.emptyList();
    }

    private Collection<? extends Action> deduplicateActions(final List<ResultAction> resultActions) {
        Map<String, Action> uniqueActions = new LinkedHashMap<>();
        for (ResultAction action : resultActions) {
            addUniqueProjectActions(action, uniqueActions);
        }
        return new ArrayList<>(uniqueActions.values());
    }

    private void addUniqueProjectActions(final ResultAction action, final Map<String, Action> uniqueActions) {
        for (Action projectAction : action.getProjectActions()) {
            if (projectAction instanceof JobAction jobAction) {
                uniqueActions.putIfAbsent(jobAction.getUrlName(), projectAction);
            }
            else {
                String key = projectAction.getClass().getName() + System.identityHashCode(projectAction);
                uniqueActions.put(key, projectAction);
            }
        }
    }
}
