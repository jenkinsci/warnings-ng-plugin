package io.jenkins.plugins.analysis.core.model;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * ThreadLocal to prevent infinite recursion when checking for existing actions.
     * Stores the set of Job instances currently being processed in this thread.
     */
    private static final ThreadLocal<Set<Job<?, ?>>> PROCESSING_JOBS = ThreadLocal.withInitial(HashSet::new);

    @Override
    @SuppressWarnings("unchecked")
    public Class<Job<?, ?>> type() {
        return (Class) Job.class;
    }

    @NonNull
    @Override
    public Collection<? extends Action> createFor(@NonNull final Job<?, ?> target) {
        Set<Job<?, ?>> processingJobs = PROCESSING_JOBS.get();
        if (!processingJobs.add(target)) {
            return Collections.emptyList();
        }

        try {
            return createActionsForJob(target);
        }
        finally {
            processingJobs.remove(target);
            if (processingJobs.isEmpty()) {
                PROCESSING_JOBS.remove();
            }
        }
    }

    private Collection<? extends Action> createActionsForJob(final Job<?, ?> target) {
        Run<?, ?> currentBuild = target.getLastBuild();
        if (currentBuild == null || currentBuild.isBuilding()) {
            return Collections.emptyList();
        }

        List<ResultAction> currentResultActions = currentBuild.getActions(ResultAction.class);
        if (!currentResultActions.isEmpty()) {
            return Collections.emptyList();
        }

        List<JobAction> existingJobActions = target.getActions(JobAction.class);
        Set<String> existingIds = new HashSet<>();
        for (JobAction existing : existingJobActions) {
            existingIds.add(existing.getId());
        }

        int count = 0;
        for (Run<?, ?> previousBuild = currentBuild.getPreviousBuild();
                previousBuild != null && count < MAX_BUILDS_TO_CONSIDER;
                previousBuild = previousBuild.getPreviousBuild(), count++) {
            List<ResultAction> resultActions = previousBuild.getActions(ResultAction.class);

            if (!resultActions.isEmpty()) {
                List<Action> actions = new ArrayList<>();
                for (ResultAction action : resultActions) {
                    Collection<? extends Action> projectActions = action.getProjectActions();
                    for (Action projectAction : projectActions) {
                        if (projectAction instanceof JobAction jobAction) {
                            if (!existingIds.contains(jobAction.getId())) {
                                actions.add(jobAction);
                            }
                        }
                        else {
                            actions.add(projectAction);
                        }
                    }
                }
                return actions;
            }
        }

        return Collections.emptyList();
    }
}