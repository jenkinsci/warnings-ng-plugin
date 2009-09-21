package hudson.plugins.analysis.util;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.util.ChartUtil;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * A project action displays a link on the side panel of a project.
 *
 * @param <T>
 *            result action type
 * @author Ulli Hafner
 */
public abstract class AbstractProjectAction<T extends ResultAction<?>> implements Action  {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -8775531952208541253L;
    /** Project that owns this action. */
    @SuppressWarnings("Se")
    private final AbstractProject<?, ?> project;
    /** The type of the result action.  */
    private final Class<T> resultActionType;
    /** The icon URL of this action: it will be shown as soon as a result is available. */
    private final String iconUrl;
    /** Plug-in URL. */
    private final String url;
    /** Plug-in results URL. */
    private final String resultUrl;

    /**
     * Creates a new instance of <code>AbstractProjectAction</code>.
     *
     * @param project
     *            the project that owns this action
     * @param resultActionType
     *            the type of the result action
     * @param plugin
     *            the plug-in that owns this action
     */
    public AbstractProjectAction(final AbstractProject<?, ?> project, final Class<T> resultActionType, final PluginDescriptor plugin) {
        this.project = project;
        this.resultActionType = resultActionType;
        iconUrl = plugin.getIconUrl();
        url = plugin.getPluginName();
        resultUrl = plugin.getPluginResultUrlName();
    }

    /**
     * Returns the title of the trend graph.
     *
     * @return the title of the trend graph.
     */
    public abstract String getTrendName();

    /**
     * Returns the project.
     *
     * @return the project
     */
    public final AbstractProject<?, ?> getProject() {
        return project;
    }

    /**
     * Returns the graph configuration for this project.
     *
     * @param link
     *            not used
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the analysis (detail page).
     */
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
     * Creates a view to configure the trend graph for the current user.
     *
     * @param request
     *            Stapler request
     * @return a view to configure the trend graph for the current user
     */
    private Object createUserConfiguration(final StaplerRequest request) {
        if (hasValidResults()) {
            return new UserGraphConfigurationDetail(getProject(), getUrlName(), request, getLastAction());
        }
        else {
            return new UserGraphConfigurationDetail(getProject(), getUrlName(), request);
        }
    }


    /**
     * Creates a view to configure the trend graph defaults.
     *
     * @return a view to configure the trend graph defaults
     */
    private Object createDefaultConfiguration() {
        if (hasValidResults()) {
            return new DefaultGraphConfigurationDetail(getProject(), getUrlName(), getLastAction());
        }
        else {
            return new DefaultGraphConfigurationDetail(getProject(), getUrlName());
        }
    }

    /**
     * Returns whether we have enough valid results in order to draw a
     * meaningful graph.
     *
     * @return <code>true</code> if the results are valid in order to draw a
     *         graph
     */
    public final boolean hasValidResults() {
        AbstractBuild<?, ?> build = getLastFinishedBuild();
        if (build != null) {
            ResultAction<?> resultAction = build.getAction(resultActionType);
            if (resultAction != null) {
                return resultAction.hasPreviousResultAction();
            }
        }
        return false;
    }

    /**
     * Returns the icon URL for the side-panel in the project screen. If there
     * is yet no valid result, then <code>null</code> is returned.
     *
     * @return the icon URL for the side-panel in the project screen
     */
    public String getIconFileName() {
        if (getLastAction() != null) {
            return iconUrl;
        }
        return null;
    }

    /** {@inheritDoc} */
    public final String getUrlName() {
        return url;
    }

    /**
     * Returns the last valid result action.
     *
     * @return the last valid result action, or <code>null</code> if no such action is found
     */
    public ResultAction<?> getLastAction() {
        AbstractBuild<?, ?> lastBuild = getLastFinishedBuild();
        if (lastBuild != null) {
            return lastBuild.getAction(resultActionType);
        }
        return null;
    }

    /**
     * Returns the last finished build.
     *
     * @return the last finished build or <code>null</code> if there is no
     *         such build
     */
    public AbstractBuild<?, ?> getLastFinishedBuild() {
        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        while (lastBuild != null && (lastBuild.isBuilding() || lastBuild.getAction(resultActionType) == null)) {
            lastBuild = lastBuild.getPreviousBuild();
        }
        return lastBuild;
    }

    /**
     * Display the trend graph.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {
        ResultAction<?> action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            doGraph(action, request, response);
        }
    }

    /**
     * Display the trend map. Delegates to the the associated
     * {@link ResultAction}.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doTrendMap(final StaplerRequest request, final StaplerResponse response) throws IOException {
        ResultAction<?> action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            doGraphMap(action, request, response);
        }
    }

    /**
     * Returns whether the trend graph is visible.
     *
     * @param request
     *            the request to get the cookie from
     * @return the graph configuration
     */
    public boolean isTrendVisible(final StaplerRequest request) {
        return hasValidResults() && new UserGraphConfigurationDetail(project, url, request).isVisible();
    }

    /**
     * Generates a PNG image for the trend graph.
     *
     * @param action
     *            the last result action
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    private void doGraph(final ResultAction<?> action, final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
        }
        else {
            GraphConfiguration configuration = createGraphConfiguration(request, action);
            if (configuration.isVisible()) {
                JFreeChart graph = configuration.createGraph(action.getHealthDescriptor(), action, url);
                ChartUtil.generateGraph(request, response, graph, configuration.getWidth(), configuration.getHeight());
            }
        }
    }

    /**
     * Generates the clickable map for the trend graph.
     *
     * @param action
     *            the last result action
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    private void doGraphMap(final ResultAction<?> action, final StaplerRequest request, final StaplerResponse response) throws IOException {
        GraphConfiguration configuration = createGraphConfiguration(request, action);
        if (configuration.isVisible()) {
            JFreeChart graph = configuration.createGraph(action.getHealthDescriptor(), action, url);
            ChartUtil.generateClickableMap(request, response, graph, configuration.getWidth(), configuration.getHeight());
        }
    }

    /**
     * Creates the graph configuration from the cookie.
     *
     * @param request
     *            the request to get the cookie from
     * @param action
     *            the last result action
     * @return the graph configuration
     */
    public GraphConfiguration createGraphConfiguration(final StaplerRequest request, final ResultAction<?> action) {
        return new UserGraphConfigurationDetail(project, url, request, action);
    }

    /**
     *
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
        AbstractBuild<?, ?> build = getLastFinishedBuild();
        if (build != null) {
            response.sendRedirect2(String.format("../%d/%s", build.getNumber(), resultUrl));
        }
    }
}
