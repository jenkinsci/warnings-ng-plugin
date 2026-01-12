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
     * Cache to ensure idempotency - same actions returned for same build state.
     * Key: Job, Value: CacheEntry with build number and actions.
     * Static to share across all instances of this extension.
     */
    private static final Map<Job<?, ?>, CacheEntry> CACHE = new WeakHashMap<>();

    private static final class CacheEntry {
        final int buildNumber;
        final Collection<? extends Action> actions;
        int callCount; 

        CacheEntry(int buildNumber, Collection<? extends Action> actions) {
            this.buildNumber = buildNumber;
            this.actions = actions;
            this.callCount = 1; 
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Job<?, ?>> type() {
        return (Class) Job.class;
    }

    @NonNull
    @Override
    public Collection<? extends Action> createFor(@NonNull final Job<?, ?> target) {
        synchronized (CACHE) {
            Run<?, ?> currentBuild = target.getLastBuild();
            if (currentBuild == null || currentBuild.isBuilding()) {
                CACHE.remove(target);
                return Collections.emptyList();
            }

            List<ResultAction> currentResultActions = currentBuild.getActions(ResultAction.class);
            if (!currentResultActions.isEmpty()) {
                CACHE.remove(target);
                return Collections.emptyList();
            }

            CacheEntry cached = CACHE.get(target);
            if (cached != null && cached.buildNumber == currentBuild.getNumber()) {
                int currentCallCount = ++cached.callCount;
                if (currentCallCount == 1) {
                    return cached.actions;
                } else {
                    return Collections.emptyList();
                }
            }

            int count = 0;
            for (Run<?, ?> previousBuild = currentBuild.getPreviousBuild();
                    previousBuild != null && count < MAX_BUILDS_TO_CONSIDER;
                    previousBuild = previousBuild.getPreviousBuild(), count++) {
                List<ResultAction> resultActions = previousBuild.getActions(ResultAction.class);

                if (!resultActions.isEmpty()) {
                    Map<String, JobAction> uniqueActions = new LinkedHashMap<>();
                    for (ResultAction resultAction : resultActions) {
                        Collection<? extends Action> projectActions = resultAction.getProjectActions();
                        for (Action action : projectActions) {
                            if (action instanceof JobAction jobAction) {
                                uniqueActions.putIfAbsent(jobAction.getUrlName(), jobAction);
                            }
                        }
                    }
                    Collection<? extends Action> actions = new ArrayList<>(uniqueActions.values());
                    CACHE.put(target, new CacheEntry(currentBuild.getNumber(), actions));
                    return actions;
                }
            }

            Collection<? extends Action> emptyList = Collections.emptyList();
            CACHE.put(target, new CacheEntry(currentBuild.getNumber(), emptyList));
            return emptyList;
        }
    }
}
