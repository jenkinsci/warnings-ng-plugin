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
public final class AnalysisBuildsFallback extends TransientActionFactory<Job<?, ?>> {
    @Override
    @SuppressWarnings("unchecked")
    public Class<Job<?, ?>> type() {
        return (Class) Job.class;
    }

    @NonNull
    @Override
    public Collection<? extends Action> createFor(@NonNull final Job<?, ?> target) {
        //Check if the current build has valid action(s) and returns an empty list.
        Run<?, ?> currentBuild = target.getLastBuild();
        if (currentBuild == null) {
            return Collections.emptyList();
        }

        List<ResultAction> currentResultActions = currentBuild.getActions(ResultAction.class);

        if (!currentResultActions.isEmpty() || currentBuild.isBuilding()) {
            return Collections.emptyList();
        }

        Run<?, ?> previousBuild = currentBuild.getPreviousBuild();
        while (previousBuild != null) {
            List<ResultAction> resultActions = previousBuild.getActions(ResultAction.class);

            List<Action> actions = new ArrayList<>();
            for (ResultAction action : resultActions) {
                actions.addAll(action.getProjectActions());
                actions.add(new TransientProjectResultAction(action));
            }

            if (!resultActions.isEmpty()) {
                return actions;
            }

            previousBuild = previousBuild.getPreviousBuild();
        }

        return Collections.emptyList();
    }

    /**
     * A wrapper class for {@link ResultAction} that provides an absolute URL for the link on the side panel.
     */
    static class TransientProjectResultAction implements Action {
        private final ResultAction resultAction;

        TransientProjectResultAction(final ResultAction resultActions) {
            this.resultAction = resultActions;
        }

        @Override
        public String getIconFileName() {
            return resultAction.getIconFileName();
        }

        @Override
        public String getDisplayName() {
            return resultAction.getDisplayName() + " (Previous)";
        }

        @Override
        public String getUrlName() {
            return resultAction.getAbsoluteUrl();
        }
    }
}