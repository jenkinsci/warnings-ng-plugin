package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import com.google.common.collect.Lists;

import io.jenkins.plugins.analysis.core.graphs.AnnotationsByUserGraph;
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
import io.jenkins.plugins.analysis.core.history.BuildHistory;
import io.jenkins.plugins.analysis.core.history.NullBuildHistory;
import io.jenkins.plugins.analysis.core.history.ResultHistory;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import jenkins.model.Jenkins;

import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.analysis.core.GlobalSettings;
import hudson.util.Graph;

/**
 * A job action displays a link on the side panel of a job. This action
 * also is responsible to render the historical trend via its associated
 * 'floatingBox.jelly' view.
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
@ExportedBean
public class JobAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(JobAction.class.getName());

    private final Job<?, ?> job;
    private final String id;
    private final String name;

    /**
     * Creates a new instance of {@link JobAction}.
     *  @param job
     *            the job that owns this action
     * @param id
     * @param name
     */
    public JobAction(final Job<?, ?> job, final String id, final String name) {
        this.job = job;
        this.id = id;
        this.name = name;
    }

    /**
     * Gets the remote API for this action.
     *
     * @return the remote API
     */
    public Api getApi() {
        return new Api(this);
    }

    @Override @Exported
    public String getDisplayName() {
        return getTool().getLinkName();
    }

    private StaticAnalysisLabelProvider getTool() {
        return StaticAnalysisTool.find(id, name);
    }

    /**
     * Returns the title of the trend graph.
     *
     * @return the title of the trend graph.
     */
    public String getTrendName() {
        return getTool().getTrendName();
    }

    /**
     * Returns the job this action belongs to.
     *
     * @return the job
     */
    public final Job<?, ?> getJob() {
        return job;
    }

    /**
     * Returns the graph configuration view for the associated job. If the requested
     * link is neither the user graph configuration nor the default
     * configuration then <code>null</code> is returned.
     *
     * @param link
     *            the requested link
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the analysis (detail page).
     */
    @CheckForNull
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
    public Object getTrendDetails() {
        return getTrendDetails(Stapler.getCurrentRequest(), Stapler.getCurrentResponse());
    }

    /**
     * Returns the trend graph details.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the details
     */
    public Object getTrendDetails(final StaplerRequest request, final StaplerResponse response) {
        return new TrendDetails(getJob(), getTrendGraph(request, response, "../../"), getTrendGraphId());
    }

    /**
     * Returns the trend graph.
     *
     * @return the current trend graph
     */
    public Object getTrendGraph() {
        return getTrendGraph(Stapler.getCurrentRequest(), Stapler.getCurrentResponse());
    }

    /**
     * Returns the configured trend graph.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the trend graph
     */
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
                LOGGER.log(Level.SEVERE, "Can't create graph: " + graphType, exception);
            }

            return null;
        }
    }

    /**
     * Returns whether the trend graph is visible.
     *
     * @param request
     *            the request to get the cookie from
     * @return <code>true</code> if the trend is visible
     */
    public boolean isTrendVisible(final StaplerRequest request) {
        GraphConfigurationView configuration = createUserConfiguration(request);

        return configuration.isVisible() && configuration.hasMeaningfulGraph();
    }

    /**
     * Returns the ID of the selected trend graph.
     *
     * @return ID of the selected trend graph
     */
    public String getTrendGraphId() {
        GraphConfigurationView configuration = createUserConfiguration(Stapler.getCurrentRequest());

        return configuration.getGraphType().getId();
    }

    /**
     * Returns whether the trend graph is deactivated.
     *
     * @param request
     *            the request to get the cookie from
     * @return <code>true</code> if the trend is deactivated
     */
    public boolean isTrendDeactivated(final StaplerRequest request) {
        return createUserConfiguration(request).isDeactivated();
    }

    /**
     * Returns whether the enable trend graph link should be shown.
     *
     * @param request
     *            the request to get the cookie from
     * @return the graph configuration
     */
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
     *            Stapler request
     * @return a view to configure the trend graph for the current user
     */
    protected GraphConfigurationView createUserConfiguration(final StaplerRequest request) {
        return new UserGraphConfigurationView(createConfiguration(), getJob(),
                getUrlName(), request.getCookies(), createBuildHistory(), getTool());
    }

    /**
     * Creates a view to configure the trend graph defaults.
     *
     * @return a view to configure the trend graph defaults
     */
    protected GraphConfigurationView createDefaultConfiguration() {
        return new DefaultGraphConfigurationView(createConfiguration(), getJob(),
                getUrlName(), createBuildHistory(), getTool());
    }

    private ResultHistory createBuildHistory() {
        Run<?, ?> lastFinishedRun = getLastFinishedRun();
        if (lastFinishedRun == null) {
            return new NullBuildHistory();
        }
        else {
            return new BuildHistory(lastFinishedRun, new ByIdResultSelector(id));
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
        if (hasValidResults()) {
            availableGraphs.add(new HealthGraph(new HealthDescriptor())); // FIXME: get health descriptor
        }
        else {
            availableGraphs.add(new HealthGraph(new HealthDescriptor()));
        }
        availableGraphs.add(new DifferenceGraph());
        availableGraphs.add(new EmptyGraph());
        availableGraphs.add(new NullGraph());
        if (!GlobalSettings.instance().getNoAuthors()) {
            availableGraphs.add(new AnnotationsByUserGraph());
        }

        return availableGraphs;
    }

    /**
     * Creates the graph configuration.
     *
     * @param availableGraphs
     *            the available graphs
     * @return the graph configuration.
     */
    protected GraphConfiguration createConfiguration(final List<BuildResultGraph> availableGraphs) {
        return new GraphConfiguration(availableGraphs);
    }

    /**
     * Returns the icon URL for the side-panel in the job screen. If there
     * is no valid result yet, then <code>null</code> is returned.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
    public String getIconFileName() {
        ResultAction lastAction = getLastAction();
        if (lastAction != null && lastAction.getResult().getNumberOfWarnings() > 0) {
            return Jenkins.RESOURCE_PATH + getTool().getSmallIconUrl();
        }
        return null;
    }

    @Override
    public final String getUrlName() {
        return id;
    }

    /**
     * Returns whether this owner has a valid result action attached.
     *
     * @return <code>true</code> if the results are valid
     */
    public final boolean hasValidResults() {
        return getLastAction() != null;
    }

    /**
     * Returns the last valid result action.
     *
     * @return the last valid result action, or <code>null</code> if no such
     *         action is found
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
     *            the build to get the action for
     * @return the action or <code>null</code> if there is no such action
     */
    @CheckForNull
    protected ResultAction getResultAction(final Run<?, ?> lastRun) {
        return lastRun.getAction(ResultAction.class);
    }

    /**
     * Returns the last finished run.
     *
     * @return the last finished run or <code>null</code> if there is no
     *         such run
     */
    @CheckForNull @Exported
    public Run<?, ?> getLastFinishedRun() {
        if (job == null) { // FIXME: can't be null
            return null;
        }
        Run<?, ?> lastRun = job.getLastBuild();
        while (lastRun != null && (lastRun.isBuilding() || getResultAction(lastRun) == null)) {
            lastRun = lastRun.getPreviousBuild();
        }
        return lastRun;
    }

    /**
     * Redirects the index page to the last result.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        Run<?, ?> lastRun = getLastFinishedRun();
        if (lastRun != null) {
            response.sendRedirect2(String.format("../%d/%s", lastRun.getNumber(), getTool().getResultUrl()));
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getName(), id);
    }
}
