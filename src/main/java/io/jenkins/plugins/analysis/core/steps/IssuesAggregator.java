package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;

import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;

/**
 * Aggregates the {@link AnalysisResult}s of all {@link ResultAction}s of several {@link MatrixRun}s into
 * {@link MatrixBuild}.
 *
 * @author Ullrich Hafner
 */
public class IssuesAggregator extends MatrixAggregator {
    private final IssuesRecorder recorder;
    private final MutableMap<String, List<Report>> results = Maps.mutable.empty();
    private final List<String> names = Lists.mutable.empty();

    private ReentrantLock aggregationTableLock = new ReentrantLock();

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
            results.put(action.getId(), Lists.mutable.of(action.getResult().getIssues()));
        }
    }

    private void updateMap(final List<ResultAction> actions) {
        for (ResultAction action : actions) {
            List<Report> runs = results.get(action.getId());
            runs.add(action.getResult().getIssues());
        }
    }

    @Override
    public boolean endBuild() throws IOException, InterruptedException {
        for (Entry<String, List<Report>> reportsPerId : results.entrySet()) {
            Report report = new Report(reportsPerId.getValue());
            recorder.publishResult(build, launcher, listener, report, StringUtils.EMPTY);
        }
        return true;
    }
}
