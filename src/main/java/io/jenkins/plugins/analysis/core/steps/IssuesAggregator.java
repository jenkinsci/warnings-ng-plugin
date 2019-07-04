package io.jenkins.plugins.analysis.core.steps;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.util.RunResultHandler;

/**
 * Aggregates the {@link AnalysisResult}s of all {@link ResultAction}s of several {@link MatrixRun}s into {@link
 * MatrixBuild}.
 *
 * @author Ullrich Hafner
 */
public class IssuesAggregator extends MatrixAggregator {
    private final IssuesRecorder recorder;
    private final MutableMap<String, List<AnnotatedReport>> results = Maps.mutable.empty();
    private final List<String> names = Lists.mutable.empty();

    private final ReentrantLock aggregationTableLock = new ReentrantLock();

    /**
     * Creates a new instance of {@link IssuesAggregator}.
     *
     * @param build
     *         the associated matrix build
     * @param launcher
     *         the launcher to communicate with the build agent
     * @param listener
     *         the listener to log messages to
     * @param recorder
     *         the recorder that actually scans for issues and records the found issues
     */
    public IssuesAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener,
            final IssuesRecorder recorder) {
        super(build, launcher, listener);

        this.recorder = recorder;
    }

    @Override
    public boolean endRun(final MatrixRun run) {
        aggregationTableLock.lock();
        try {
            names.add(run.getParent().getName());
            List<ResultAction> actions = run.getActions(ResultAction.class);
            if (results.isEmpty()) {
                initializeMap(actions);
            }
            else {
                updateMap(actions);
            }
            run.getWorkspace();
        }
        finally {
            aggregationTableLock.unlock();
        }
        return true;
    }

    private void initializeMap(final List<ResultAction> actions) {
        for (ResultAction action : actions) {
            results.put(action.getId(), Lists.mutable.of(createReport(action.getId(), action.getResult())));
        }
    }

    private AnnotatedReport createReport(final String id, final AnalysisResult result) {
        return new AnnotatedReport(id, result.getIssues(), result.getBlames());
    }

    private void updateMap(final List<ResultAction> actions) {
        for (ResultAction action : actions) {
            List<AnnotatedReport> runs = results.get(action.getId());
            runs.add(createReport(action.getId(), action.getResult()));
        }
    }

    @Override
    public boolean endBuild() {
        for (Entry<String, List<AnnotatedReport>> reportsPerId : results.entrySet()) {
            AnnotatedReport aggregatedReport = new AnnotatedReport(reportsPerId.getKey(), reportsPerId.getValue());
            recorder.publishResult(build, listener, Messages.Tool_Default_Name(), aggregatedReport, StringUtils.EMPTY,
                    new RunResultHandler(build));
        }
        return true;
    }
}
