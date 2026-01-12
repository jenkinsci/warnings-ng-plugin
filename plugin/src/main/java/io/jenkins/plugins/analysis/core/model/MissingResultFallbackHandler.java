package io.jenkins.plugins.analysis.core.model;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;

/**
 * Registers this class as a Jenkins extension that provides fallback for analysis builds.
 * This helps display warnings in the job view even when no analysis is present in one of
 * the latest builds.
 *
 * <p>
 * The actions are rendered by finding the most recent build with valid {@link ResultAction}
 * instances, and then attaching the corresponding {@link JobAction} to the job.
 * </p>
 *
 * <p>
 * IMPORTANT: This factory must be idempotent. Jenkins may call {@link #createFor(Job)}
 * multiple times per request. Therefore, job actions must never be attached more than once.
 * </p>
 */
@Extension
public final class MissingResultFallbackHandler extends TransientActionFactory<Job<?, ?>> {

    /**
     * The maximum number of builds to consider when looking for a valid {@link ResultAction}.
     * Limited to avoid performance issues on large job histories.
     */
    public static final int MAX_BUILDS_TO_CONSIDER = 5;

    /**
     * Cache of JobActions per job to ensure idempotency across multiple createFor() calls.
     * Uses WeakHashMap to allow garbage collection of jobs.
     * Key: Job instance, Value: Map of urlName to JobAction.
     */
    private final Map<Job<?, ?>, Map<String, JobAction>> cache = new WeakHashMap<>();

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
            cache.remove(target);
            return Collections.emptyList();
        }

        if (!currentBuild.getActions(ResultAction.class).isEmpty()) {
            cache.remove(target);
            return Collections.emptyList();
        }

        Map<String, JobAction> cachedActions = cache.get(target);
        if (cachedActions != null) {
            return new ArrayList<>(cachedActions.values());
        }

        Map<String, JobAction> uniqueActionsMap = new LinkedHashMap<>();
        int count = 0;
        
        for (Run<?, ?> previousBuild = currentBuild.getPreviousBuild();
                previousBuild != null && count < MAX_BUILDS_TO_CONSIDER;
                previousBuild = previousBuild.getPreviousBuild(), count++) {

            List<ResultAction> resultActions = previousBuild.getActions(ResultAction.class);
            if (resultActions.isEmpty()) {
                continue;
            }

            for (ResultAction resultAction : resultActions) {
                Collection<? extends Action> projectActions = resultAction.getProjectActions();
                for (Action action : projectActions) {
                    if (action instanceof JobAction jobAction) {
                        uniqueActionsMap.putIfAbsent(jobAction.getUrlName(), jobAction);
                    }
                }
            }
            
            break;
        }

        cache.put(target, uniqueActionsMap);
        
        return new ArrayList<>(uniqueActionsMap.values());
    }
}