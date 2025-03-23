package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;

/**
 * Registers this class as a Jenkins extension that provides fallback for analysis builds.
 * This helps display warnings in the job view even when no analysis is present in the latest build.
 * The actions are rendered by finding the most recent build with valid {@link ResultAction} instances,
 * and then attaching the corresponding {@link JobAction} and {@link TransientProjectResultAction} to the job.
 */
@Extension
public final class MissingResultFallbackHandler extends TransientActionFactory<Job<?, ?>> {
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

        for (Run<?, ?> previousBuild = currentBuild.getPreviousBuild(); previousBuild != null; previousBuild = previousBuild.getPreviousBuild()) {
            List<ResultAction> resultActions = previousBuild.getActions(ResultAction.class);

            List<Action> actions = new ArrayList<>();
            for (ResultAction action : resultActions) {
                actions.addAll(action.getProjectActions());
                actions.add(new TransientProjectResultAction(action));
            }

            if (!resultActions.isEmpty()) {
                return actions;
            }
        }

        return Collections.emptyList();
    }

    /**
     * A wrapper record for {@link ResultAction} that provides an absolute URL for the link on the side panel.
     *
     * @param resultAction
     *          Valid Result Action
     */
    private record TransientProjectResultAction(ResultAction resultAction) implements Action {
        @Override
        public String getIconFileName() {
            return resultAction.getIconFileName();
        }

        @Override
        public String getDisplayName() {
            return resultAction.getDisplayName();
        }

        @Override
        public String getUrlName() {
            return resultAction.getAbsoluteUrl();
        }
    }
}