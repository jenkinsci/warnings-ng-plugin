package io.jenkins.plugins.analysis.core.steps;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Multimaps;

import edu.hm.hafner.util.VisibleForTesting;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import hudson.AbortException;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.util.RunResultHandler;

/**
 * Aggregates the {@link AnalysisResult}s of all {@link ResultAction}s of several {@link MatrixRun}s into {@link
 * MatrixBuild}.
 *
 * @author Ullrich Hafner
 */
public class IssuesAggregator extends MatrixAggregator {
    private final IssuesRecorder recorder;
    private final MutableMultimap<String, AnnotatedReport> resultsPerTool = Multimaps.mutable.list.empty();
    private final Map<AnnotatedReport, String> reportToAxisName = new HashMap<>();
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

    @VisibleForTesting
    List<String> getNames() {
        return names;
    }

    @VisibleForTesting
    Map<String, RichIterable<AnnotatedReport>> getResultsPerTool() {
        return resultsPerTool.toMap();
    }

    @Override
    public boolean endRun(final MatrixRun run) {
        aggregationTableLock.lock();
        try {
            var axisName = run.getParent().getName();
            names.add(axisName);
            List<ResultAction> actions = run.getActions(ResultAction.class);
            for (ResultAction action : actions) {
                var report = createReport(action.getId(), action.getResult());
                resultsPerTool.put(action.getId(), report);
                reportToAxisName.put(report, axisName);
            }
        }
        finally {
            aggregationTableLock.unlock();
        }
        return true;
    }

    private AnnotatedReport createReport(final String id, final AnalysisResult result) {
        return new AnnotatedReport(id, result.getIssues(), result.getBlames(), result.getForensics());
    }

    @Override
    public boolean endBuild() {
        try {
            resultsPerTool.forEachKeyMultiValues((tool, reports) -> {
                var reportsList = Lists.mutable.withAll(reports);
                reportsList.sortThis(Comparator.comparing(reportToAxisName::get));
                var aggregatedReport = new AnnotatedReport(tool, reportsList);
                try {
                    recorder.publishResult(build, build.getWorkspace(), listener, Messages.Tool_Default_Name(),
                            aggregatedReport, StringUtils.EMPTY, recorder.getIcon(), new RunResultHandler(build));
                }
                catch (AbortException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        catch (RuntimeException e) {
            if (e.getCause() instanceof AbortException) {
                listener.getLogger().println(e.getCause().getMessage());
                build.setResult(hudson.model.Result.FAILURE);
                return false;
            }
            throw e;
        }
        return true;
    }
}
