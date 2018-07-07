package io.jenkins.plugins.analysis.core.views;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.google.common.collect.Lists;

import io.jenkins.plugins.analysis.core.graphs.BuildResultGraph;
import io.jenkins.plugins.analysis.core.graphs.DefaultGraphConfigurationView;
import io.jenkins.plugins.analysis.core.graphs.DifferenceGraph;
import io.jenkins.plugins.analysis.core.graphs.EmptyGraph;
import io.jenkins.plugins.analysis.core.graphs.GraphConfiguration;
import io.jenkins.plugins.analysis.core.graphs.GraphConfigurationView;
import io.jenkins.plugins.analysis.core.graphs.HealthGraph;
import io.jenkins.plugins.analysis.core.graphs.NewVersusFixedGraph;
import io.jenkins.plugins.analysis.core.graphs.NullGraph;
import io.jenkins.plugins.analysis.core.graphs.PriorityGraph;
import io.jenkins.plugins.analysis.core.graphs.TotalsGraph;
import io.jenkins.plugins.analysis.core.graphs.TrendDetails;
import io.jenkins.plugins.analysis.core.graphs.UserGraphConfigurationView;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory;
import io.jenkins.plugins.analysis.core.history.NullAnalysisHistory;
import io.jenkins.plugins.analysis.core.model.ByIdResultSelector;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import jenkins.model.Jenkins;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.Graph;

/**
 * A job action displays a link on the side panel of a job. This action also is responsible to render the historical
 * trend via its associated 'floatingBox.jelly' view.
 *
 * @author Ulli Hafner
 */
public class JobAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(JobAction.class.getName());

    private final Job<?, ?> owner;
    private final StaticAnalysisLabelProvider labelProvider;
    private final HealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of {@link JobAction}.
     *
     * @param owner
     *         the job that owns this action
     * @param labelProvider
     *         the label provider
     * @param healthDescriptor
     *         the health descriptor
     */
    public JobAction(final Job<?, ?> owner, final StaticAnalysisLabelProvider labelProvider,
            final HealthDescriptor healthDescriptor) {
        this.owner = owner;
        this.labelProvider = labelProvider;
        this.healthDescriptor = healthDescriptor;
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
    public final Job<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the graph configuration view for the associated job. If the requested link is neither the user graph
     * configuration nor the default configuration then {@code null} is returned.
     *
     * @param link
     *         the requested link
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the dynamic result of the analysis (detail page).
     */
    @CheckForNull
    @SuppressWarnings("unused") // Called by jelly view
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        if ("configureDefaults".equals(link)) {
            return createDefaultConfiguration();
        }
        else if ("configure".equals(link)) {
            return createUserConfiguration(request);
        }
        else {
            return null;
        }
    }

    /**
     * Returns the trend graph details.
     *
     * @return the details
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Object getTrendDetails() {
        return getTrendDetails(Stapler.getCurrentRequest(), Stapler.getCurrentResponse());
    }

    /**
     * Returns the trend graph details.
     *
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the details
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Object getTrendDetails(final StaplerRequest request, final StaplerResponse response) {
        return new TrendDetails(getOwner(), getTrendGraph(request, response, "../../"), getTrendGraphId());
    }

    /**
     * Returns the trend graph.
     *
     * @return the current trend graph
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Object getTrendGraph() {
        return getTrendGraph(Stapler.getCurrentRequest(), Stapler.getCurrentResponse());
    }

    /**
     * Returns the configured trend graph.
     *
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the trend graph
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Graph getTrendGraph(final StaplerRequest request, final StaplerResponse response) {
        return getTrendGraph(request, response, "");
    }

    private Graph getTrendGraph(final StaplerRequest request, final StaplerResponse response, final String urlPrefix) {
        GraphConfigurationView configuration = createUserConfiguration(request);
        if (configuration.hasMeaningfulGraph()) {
            configuration.setUrlPrefix(urlPrefix);
            return configuration.getGraphRenderer(getUrlName());
        }
        else {
            BuildResultGraph graphType = configuration.getGraphType();
            try {
                response.sendRedirect2(request.getContextPath() + graphType.getExampleImage());
            }
            catch (IOException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "Can't create graph: " + graphType, exception);
                }
            }

            return null;
        }
    }

    /**
     * Returns whether the trend graph is visible.
     *
     * @param request
     *         the request to get the cookie from
     *
     * @return {@code true} if the trend is visible
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean isTrendVisible(final StaplerRequest request) {
        GraphConfigurationView configuration = createUserConfiguration(request);

        return configuration.isVisible() && configuration.hasMeaningfulGraph();
    }

    /**
     * Returns the ID of the selected trend graph.
     *
     * @return ID of the selected trend graph
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getTrendGraphId() {
        GraphConfigurationView configuration = createUserConfiguration(Stapler.getCurrentRequest());

        return configuration.getGraphType().getId();
    }

    /**
     * Returns whether the trend graph is deactivated.
     *
     * @param request
     *         the request to get the cookie from
     *
     * @return {@code true} if the trend is deactivated
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean isTrendDeactivated(final StaplerRequest request) {
        return createUserConfiguration(request).isDeactivated();
    }

    /**
     * Returns whether the enable trend graph link should be shown.
     *
     * @param request
     *         the request to get the cookie from
     *
     * @return the graph configuration
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean canShowEnableTrendLink(final StaplerRequest request) {
        GraphConfigurationView configuration = createUserConfiguration(request);
        if (configuration.hasMeaningfulGraph()) {
            return !configuration.isDeactivated() && !configuration.isVisible();
        }
        return false;
    }

    /**
     * Creates a view to configure the trend graph for the current user.
     *
     * @param request
     *         Stapler request
     *
     * @return a view to configure the trend graph for the current user
     */
    protected GraphConfigurationView createUserConfiguration(final StaplerRequest request) {
        return new UserGraphConfigurationView(createConfiguration(), getOwner(),
                getUrlName(), request.getCookies(), createBuildHistory(), labelProvider.getToolTipProvider(),
                healthDescriptor);
    }

    /**
     * Creates a view to configure the trend graph defaults.
     *
     * @return a view to configure the trend graph defaults
     */
    protected GraphConfigurationView createDefaultConfiguration() {
        return new DefaultGraphConfigurationView(createConfiguration(), getOwner(),
                getUrlName(), createBuildHistory(), labelProvider.getToolTipProvider(), healthDescriptor);
    }

    private AnalysisHistory createBuildHistory() {
        Run<?, ?> lastFinishedRun = getLastFinishedRun();
        if (lastFinishedRun == null) {
            return new NullAnalysisHistory();
        }
        else {
            return new AnalysisHistory(lastFinishedRun, new ByIdResultSelector(labelProvider.getId()));
        }
    }

    /**
     * Creates the graph configuration.
     *
     * @return the graph configuration
     */
    private GraphConfiguration createConfiguration() {
        return createConfiguration(getAvailableGraphs());
    }

    /**
     * Returns the sorted list of available graphs.
     *
     * @return the available graphs
     */
    @SuppressWarnings("NP")
    protected List<BuildResultGraph> getAvailableGraphs() {
        List<BuildResultGraph> availableGraphs = Lists.newArrayList();

        availableGraphs.add(new NewVersusFixedGraph());
        availableGraphs.add(new PriorityGraph());
        availableGraphs.add(new TotalsGraph());
        availableGraphs.add(new HealthGraph(healthDescriptor));
        availableGraphs.add(new DifferenceGraph());
        availableGraphs.add(new EmptyGraph());
        availableGraphs.add(new NullGraph());

        return availableGraphs;
    }

    /**
     * Creates the graph configuration.
     *
     * @param availableGraphs
     *         the available graphs
     *
     * @return the graph configuration.
     */
    protected GraphConfiguration createConfiguration(final List<BuildResultGraph> availableGraphs) {
        return new GraphConfiguration(availableGraphs);
    }

    /**
     * Returns the icon URL for the side-panel in the job screen. If there is no valid result yet, then {@code null} is
     * returned.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
    public String getIconFileName() {
        ResultAction lastAction = getLastAction();
        if (lastAction != null && lastAction.getResult().getTotalSize() > 0) {
            return Jenkins.RESOURCE_PATH + labelProvider.getSmallIconUrl();
        }
        return null;
    }

    @Override
    public final String getUrlName() {
        return labelProvider.getId();
    }

    /**
     * Returns whether this owner has a valid result action attached.
     *
     * @return {@code true} if the results are valid
     */
    public final boolean hasValidResults() {
        return getLastAction() != null;
    }

    /**
     * Returns the last valid result action.
     *
     * @return the last valid result action, or {@code null} if no such action is found
     */
    @CheckForNull
    public ResultAction getLastAction() {
        Run<?, ?> lastRun = getLastFinishedRun();
        if (lastRun == null) {
            return null;
        }
        else {
            return getResultAction(lastRun);
        }
    }

    /**
     * Returns the result action for the specified build.
     *
     * @param lastRun
     *         the build to get the action for
     *
     * @return the action or {@code null} if there is no such action
     */
    @CheckForNull
    protected ResultAction getResultAction(final Run<?, ?> lastRun) {
        return lastRun.getAction(ResultAction.class);
    }

    /**
     * Returns the last finished run.
     *
     * @return the last finished run or {@code null} if there is no such run
     */
    @CheckForNull
    public Run<?, ?> getLastFinishedRun() {
        Run<?, ?> lastRun = owner.getLastBuild();
        while (lastRun != null && (lastRun.isBuilding() || getResultAction(lastRun) == null)) {
            lastRun = lastRun.getPreviousBuild();
        }
        return lastRun;
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
    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        Run<?, ?> lastRun = getLastFinishedRun();
        if (lastRun != null) {
            response.sendRedirect2(String.format("../%d/%s", lastRun.getNumber(), labelProvider.getResultUrl()));
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getClass().getName(), labelProvider.getName());
    }
}
