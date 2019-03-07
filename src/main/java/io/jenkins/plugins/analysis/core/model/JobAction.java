package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.Nullable;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.charts.ChartModelConfiguration;
import io.jenkins.plugins.analysis.core.charts.LinesChartModel;
import io.jenkins.plugins.analysis.core.charts.SeverityTrendChart;
import io.jenkins.plugins.analysis.core.charts.ToolsTrendChart;

/**
 * A job action displays a link on the side panel of a job. This action also is responsible to render the historical
 * trend via its associated 'floatingBox.jelly' view.
 *
 * @author Ullrich Hafner
 */
public class JobAction implements Action {
    private static final int MIN_BUILDS = 2;

    private final Job<?, ?> owner;
    private final StaticAnalysisLabelProvider labelProvider;
    private final int numberOfTools;

    /**
     * Creates a new instance of {@link JobAction}.
     *
     * @param owner
     *         the job that owns this action
     * @param labelProvider
     *         the label provider
     * @deprecated use {@link #JobAction(Job, StaticAnalysisLabelProvider, int)}
     */
    @Deprecated
    public JobAction(final Job<?, ?> owner, final StaticAnalysisLabelProvider labelProvider) {
        this(owner, labelProvider, 1);
    }

    /**
     * Creates a new instance of {@link JobAction}.
     *
     * @param owner
     *         the job that owns this action
     * @param labelProvider
     *         the label provider
     * @param numberOfTools
     *         the number of tools that have results to show
     */
    public JobAction(final Job<?, ?> owner, final StaticAnalysisLabelProvider labelProvider, final int numberOfTools) {
        this.owner = owner;
        this.labelProvider = labelProvider;
        this.numberOfTools = numberOfTools;
    }

    /**
     * Returns the ID of this action and the ID of the associated results.
     *
     * @return the ID
     */
    public String getId() {
        return labelProvider.getId();
    }

    @Override
    public String getDisplayName() {
        return labelProvider.getLinkName();
    }

    /**
     * Returns the title of the trend graph.
     *
     * @return the title of the trend graph.
     */
    public String getTrendName() {
        return labelProvider.getTrendName();
    }

    /**
     * Returns the job this action belongs to.
     *
     * @return the job
     */
    public Job<?, ?> getOwner() {
        return owner;
    }

    private History createBuildHistory() {
        Run<?, ?> lastFinishedRun = owner.getLastCompletedBuild();
        if (lastFinishedRun == null) {
            return new NullAnalysisHistory();
        }
        else {
            return new AnalysisHistory(lastFinishedRun, new ByIdResultSelector(labelProvider.getId()));
        }
    }

    /**
     * Returns the icon URL for the side-panel in the job screen. If there is no valid result yet, then {@code null} is
     * returned.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
    @Nullable
    public String getIconFileName() {
        return createBuildHistory().getBaselineResult()
                .map(result -> Jenkins.RESOURCE_PATH + labelProvider.getSmallIconUrl())
                .orElse(null);
    }

    @Override
    public String getUrlName() {
        return labelProvider.getId();
    }

    /**
     * Redirects the index page to the last result.
     *
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @throws IOException
     *         in case of an error
     */
    @SuppressWarnings("unused") // Called by jelly view
    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        Optional<ResultAction> action = getLatestAction();
        if (action.isPresent()) {
            response.sendRedirect2(String.format("../%d/%s", action.get().getOwner().getNumber(),
                    labelProvider.getId()));
        }
    }

    /**
     * Returns the latest static analysis results for this job.
     *
     * @return the latest results (if available)
     */
    public Optional<ResultAction> getLatestAction() {
        return createBuildHistory().getBaselineAction();
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the issues stacked by severity.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public JSONObject getBuildTrend() {
        return JSONObject.fromObject(createChartModel());
    }

    private LinesChartModel createChartModel() {
        if (numberOfTools > 1) {
            return new ToolsTrendChart().create(createBuildHistory(), new ChartModelConfiguration());
        }
        else {
            return new SeverityTrendChart().create(createBuildHistory(), new ChartModelConfiguration());
        }
    }

    /**
     * Returns whether the trend chart is visible or not.
     *
     * @return {@code true} if the trend is visible, false otherwise
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean isTrendVisible() {
        History history = createBuildHistory();

        Iterator<AnalysisResult> iterator = history.iterator();
        for (int count = 1; iterator.hasNext(); count++) {
            if (count >= MIN_BUILDS) {
                return true;
            }
            iterator.next();
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getClass().getName(), labelProvider.getName());
    }
}
