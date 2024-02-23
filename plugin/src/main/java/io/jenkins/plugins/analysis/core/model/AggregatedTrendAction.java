package io.jenkins.plugins.analysis.core.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.charts.CompositeBuildResult;
import io.jenkins.plugins.analysis.core.charts.JenkinsBuild;
import io.jenkins.plugins.analysis.core.charts.ToolsTrendChart;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.echarts.AsyncConfigurableTrendChart;

/**
 * Project action that renders a combined trend chart of all tools in the job.
 *
 * @author Ullrich Hafner
 */
public class AggregatedTrendAction implements Action, AsyncConfigurableTrendChart {
    private static final int MIN_TOOLS = 2;

    private final Job<?, ?> owner;

    /**
     * Creates a new action.
     *
     * @param owner
     *         job of this action
     */
    AggregatedTrendAction(final Job<?, ?> owner) {
        super();

        this.owner = owner;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "warnings-aggregation";
    }

    private Set<AnalysisHistory> createBuildHistory() {
        Run<?, ?> lastFinishedRun = owner.getLastCompletedBuild();
        if (lastFinishedRun == null) {
            return new HashSet<>();
        }
        else {
            return owner.getActions(JobAction.class)
                    .stream()
                    .map(JobAction::getId)
                    .map(id -> new AnalysisHistory(lastFinishedRun, new ByIdResultSelector(id)))
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Returns the trend chart model that renders the aggregated build results.
     *
     * @param configuration
     *         JSON configuration of the chart (number of builds, etc.)
     *
     * @return the trend chart
     */
    @Override
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getConfigurableBuildTrendModel(final String configuration) {
        return new JacksonFacade().toJson(createChartModel(ChartModelConfiguration.fromJson(configuration)));
    }

    private LinesChartModel createChartModel(final ChartModelConfiguration configuration) {
        Run<?, ?> lastBuild = owner.getLastBuild();
        if (lastBuild == null) {
            return new LinesChartModel();
        }
        return new ToolsTrendChart().create(new CompositeBuildResultsIterable(lastBuild), configuration);
    }

    @Override
    public boolean isTrendVisible() {
        Set<AnalysisHistory> history = createBuildHistory();

        if (history.size() < MIN_TOOLS) {
            return false;
        }

        AnalysisHistory singleResult = history.iterator().next();
        return singleResult.hasMultipleResults();
    }

    /**
     * Combines the history results of several {@link AnalysisBuildResult static analysis results} into a single result
     * history.
     *
     * @author Ullrich Hafner
     */
    private static class CompositeBuildResultsIterable implements Iterable<BuildResult<AnalysisBuildResult>> {
        private final Run<?, ?> lastBuild;

        CompositeBuildResultsIterable(final Run<?, ?> lastBuild) {
            this.lastBuild = lastBuild;
        }

        @Override
        @NonNull
        public Iterator<BuildResult<AnalysisBuildResult>> iterator() {
            return new CompositeIterator(lastBuild);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class CompositeIterator implements Iterator<BuildResult<AnalysisBuildResult>> {
        private Optional<Run<?, ?>> latestAction;

        CompositeIterator(final Run<?, ?> current) {
            latestAction = Optional.of(current);
        }

        @Override
        public boolean hasNext() {
            return latestAction.isPresent();
        }

        @Override
        public BuildResult<AnalysisBuildResult> next() {
            if (latestAction.isEmpty()) {
                throw new NoSuchElementException("No more build results available");
            }
            Run<?, ?> run = latestAction.get();
            latestAction = Optional.ofNullable(run.getPreviousBuild());

            Set<AnalysisResult> results = run.getActions(ResultAction.class)
                    .stream()
                    .map(ResultAction::getResult)
                    .collect(Collectors.toSet());
            return new BuildResult<>(new JenkinsBuild(run), new CompositeBuildResult(results));
        }
    }
}
