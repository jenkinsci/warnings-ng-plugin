package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;

/**
 * Registers this class as a Jenkins extension that provides fallback for analysis builds.
 * This helps display warnings in the job view even when no analysis is present in the latest build.
 * The actions are rendered by attaching the most recent valid {@link ResultAction} to the build.
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
        List<ResultAction> currentResultActions = currentBuild.getActions().stream()
                .filter(ResultAction.class::isInstance)
                .map(ResultAction.class::cast)
                .collect(Collectors.toList());

        if (!currentResultActions.isEmpty()) {
            return Collections.emptyList();
        }

        //Iterate through previous builds to find most recent valid ResultAction instance and return it.
        Run<?, ?> previousBuild = currentBuild.getPreviousBuild();
        while (previousBuild != null) {
            List<ResultAction> resultActions = previousBuild.getActions().stream()
                    .filter(ResultAction.class::isInstance)
                    .map(ResultAction.class::cast)
                    .collect(Collectors.toList());

            List<Action> actionList = new ArrayList<>();
            for (ResultAction action : resultActions) {
                actionList.addAll(action.getProjectActions());
                actionList.add(new TransientProjectResultAction(action));
            }

            if (!resultActions.isEmpty()) {
                return actionList;
            }

            previousBuild = previousBuild.getPreviousBuild();
        }

        return Collections.emptyList();
    }
}

//Wrapper class for ResultActions to get the absolute URL for the link on the side panel.
class TransientProjectResultAction implements Action {
    private final ResultAction resultActions;

    TransientProjectResultAction(final ResultAction resultActions) {
        this.resultActions = resultActions;
    }

    @Override
    public String getIconFileName() {
        return resultActions.getIconFileName();
    }

    @Override
    public String getDisplayName() {
        return resultActions.getDisplayName() + " (Previous)";
    }

    @Override
    public String getUrlName() {
        return resultActions.getAbsoluteUrl();
    }
}