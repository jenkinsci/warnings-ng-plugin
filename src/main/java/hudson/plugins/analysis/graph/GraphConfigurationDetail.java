package hudson.plugins.analysis.graph;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractProject;
import hudson.model.ModelObject;

import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.NullHealthDescriptor;
import hudson.plugins.analysis.core.ResultAction;

import hudson.util.ChartUtil;
import hudson.util.FormValidation;

/**
 * Configures the trend graph of this plug-in.
 */
// CHECKSTYLE:COUPLING-OFF
public abstract class GraphConfigurationDetail extends GraphConfiguration implements ModelObject {
    /** The root URL to return back when leaving this page. */
    private static final String ROOT_URL = "../../";
    /** The owning project to configure the graphs for. */
    private final AbstractProject<?, ?> project;
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(GraphConfigurationDetail.class.getName());
    /** The plug-in name. */
    private final String pluginName;
    /** The last result action to start the trend report computation from. */
    private ResultAction<?> lastAction;
    /** The health descriptor. */
    private AbstractHealthDescriptor healthDescriptor;

    /** Drawing Mode. */
    enum Mode {
        /** PNG image. */
        PNG,
        /** Clickable map for the PNG image. */
        MAP}

    /**
     * Creates a new instance of {@link GraphConfigurationDetail}.
     *
     * @param project
     *            the owning project to configure the graphs for
     * @param pluginName
     *            the name of the plug-in
     * @param value
     *            the initial value of this configuration
     */
    public GraphConfigurationDetail(final AbstractProject<?, ?> project, final String pluginName, final String value) {
        super(value, createDefaultsFile(project, pluginName));

        this.project = project;
        this.pluginName = pluginName;
        healthDescriptor = new NullHealthDescriptor();
    }

    /**
     * Creates a new instance of {@link GraphConfigurationDetail}.
     *
     * @param project
     *            the owning project to configure the graphs for
     * @param pluginName
     *            the name of the plug-in
     * @param value
     *            the initial value of this configuration
     * @param lastAction
     *            the last valid action for this project
     */
    public GraphConfigurationDetail(final AbstractProject<?, ?> project, final String pluginName, final String value, final ResultAction<?> lastAction) {
        this(project, pluginName, value);

        this.lastAction = lastAction;
        healthDescriptor = lastAction.getHealthDescriptor();
    }

    /**
     * Creates a file with for the default values.
     *
     * @param project
     *            the project used as directory for the file
     * @param pluginName
     *            the name of the plug-in
     * @return the created file
     */
    protected static File createDefaultsFile(final AbstractProject<?, ?> project, final String pluginName) {
        return new File(project.getRootDir(), pluginName + ".txt");
    }

    /**
     * Returns the project.
     *
     * @return the project
     */
    public AbstractProject<?, ?> getOwner() {
        return project;
    }

    /**
     * Returns the root URL of this object.
     *
     * @return the root URL of this object
     */
    public String getRootUrl() {
        return project.getAbsoluteUrl() + pluginName;
    }

    /**
     * Returns the plug-in name.
     *
     * @return the plug-in name
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Returns the description for this view.
     *
     * @return the description for this view
     */
    public abstract String getDescription();

    /**
     * Saves the configured values. Subclasses need to implement the actual persistence.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public void doSave(final StaplerRequest request, final StaplerResponse response) {
        try {
            JSONObject formData = request.getSubmittedForm();
            int width = formData.getInt("width");
            int height = formData.getInt("height");
            String buildCountString = formData.getString("buildCountString");
            int buildCount = 0;
            if (StringUtils.isNotBlank(buildCountString)) {
                buildCount = formData.getInt("buildCountString");
            }
            String dayCountString = formData.getString("dayCountString");
            int dayCount = 0;
            if (StringUtils.isNotBlank(dayCountString)) {
                dayCount = formData.getInt("dayCountString");
            }
            GraphType graphType = GraphType.valueOf(StringUtils.upperCase(formData.getString("graphType")));

            if (isValid(width, height, buildCount, dayCount, graphType)) {
                String value = serializeToString(width, height, buildCount, dayCount, graphType);
                persistValue(value, request, response);
            }
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Can't save the form data: " + request, exception);
        }
        catch (JSONException exception) {
            LOGGER.log(Level.SEVERE, "Can't parse the JSON form data: " + request, exception);
        }
        catch (IllegalArgumentException exception) {
            LOGGER.log(Level.SEVERE, "Can't parse the form data: " + request, exception);
        }
        catch (ServletException exception) {
            LOGGER.log(Level.SEVERE, "Can't process the form data: " + request, exception);
        }
        finally {
            try {
                response.sendRedirect(ROOT_URL);
            }
            catch (IOException exception) {
                LOGGER.log(Level.SEVERE, "Can't redirect", exception);
            }
        }
    }

    /**
     * Performs on-the-fly validation on the trend graph height.
     *
     * @param height
     *            the height
     * @return the form validation
     */
    public static FormValidation checkHeight(final String height) {
        try {
            if (isValidHeight(Integer.valueOf(height))) {
                return FormValidation.ok();
            }
        }
        catch (NumberFormatException f) {
            // ignore
        }
        return FormValidation.error("Height hallo gaats no.");
    }

    /**
     * Checks whether a meaningful graph is available.
     *
     * @return <code>true</code>, if there is such a graph
     */
    public boolean hasMeaningfulGraph() {
        return lastAction != null;
    }

    /**
     * Persists the configured values.
     *
     * @param value
     *            the values configured by the user.
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException if the values could not be persisted
     */
    protected abstract void persistValue(String value, StaplerRequest request, StaplerResponse response) throws IOException;

    /**
     * Returns the build count as a string. If no build count is defined, then an
     * empty string is returned.
     *
     * @return the day count string
     */
    public String getBuildCountString() {
        if (isBuildCountDefined()) {
            return String.valueOf(getBuildCount());
        }
        else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Returns the day count as a string. If no day count is defined, then an
     * empty string is returned.
     *
     * @return the day count string
     */
    public String getDayCountString() {
        if (isDayCountDefined()) {
            return String.valueOf(getDayCount());
        }
        else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Draws a PNG image with the new versus fixed warnings graph.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doFixed(final StaplerRequest request, final StaplerResponse response) {
        drawFixed(request, response, Mode.PNG);
    }

    /**
     * Draws a MAP with the new versus fixed warnings graph.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doFixedMap(final StaplerRequest request, final StaplerResponse response) {
        drawFixed(request, response, Mode.MAP);
    }

    /**
     * Draws new versus fixed warnings graph.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param mode
     *            drawing mode
     */
    private void drawFixed(final StaplerRequest request, final StaplerResponse response, final Mode mode) {
        drawGraph(request, response, mode, new NewVersusFixedGraph());
    }

    /**
     * Draws a PNG image with a graph with warning differences.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doDifference(final StaplerRequest request, final StaplerResponse response) {
        drawDifference(request, response, Mode.PNG);
    }

    /**
     * Draws a MAP with the warnings difference graph.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doDifferenceMap(final StaplerRequest request, final StaplerResponse response) {
        drawDifference(request, response, Mode.MAP);
    }

    /**
     * Draws a warnings difference graph.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param mode
     *            drawing mode
     */
    private void drawDifference(final StaplerRequest request, final StaplerResponse response, final Mode mode) {
        drawGraph(request, response, mode, new DifferenceGraph());
    }

    /**
     * Draws a PNG image with a graph with warnings by priority.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doPriority(final StaplerRequest request, final StaplerResponse response) {
        drawPriority(request, response, Mode.PNG);
    }

    /**
     * Draws a MAP width a graph with warnings by priority.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doPriorityMap(final StaplerRequest request, final StaplerResponse response) {
        drawPriority(request, response, Mode.MAP);
    }

    /**
     * Draws a graph with warnings by priority.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param mode
     *            drawing mode
     */
    private void drawPriority(final StaplerRequest request, final StaplerResponse response, final Mode mode) {
        drawGraph(request, response, mode, new PriorityGraph());
    }

    /**
     * Checks if the health graph is available.
     *
     * @return <code>true</code>, if the health graph is available
     */
    public boolean isHealthGraphAvailable() {
        return healthDescriptor.isEnabled();
    }

    /**
     * Draws a PNG image with a graph with warnings by health thresholds.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doHealth(final StaplerRequest request, final StaplerResponse response) {
        drawHealth(request, response, Mode.PNG);
    }

    /**
     * Draws a MAP with a graph with warnings by health thresholds.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doHealthMap(final StaplerRequest request, final StaplerResponse response) {
        drawHealth(request, response, Mode.MAP);
    }

    /**
     * Draws a graph with warnings by health thresholds.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param mode
     *            drawing mode
     */
    private void drawHealth(final StaplerRequest request, final StaplerResponse response, final Mode mode) {
        drawGraph(request, response, mode, new HealthGraph(healthDescriptor));
    }

    /**
     * Draws the graph.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param mode
     *            drawing mode
     * @param buildResultGraph
     *            the graph that actually renders the results
     */
    private void drawGraph(final StaplerRequest request, final StaplerResponse response,
            final Mode mode, final BuildResultGraph buildResultGraph) {
        buildResultGraph.setRootUrl(ROOT_URL);
        if (hasMeaningfulGraph()) {
            JFreeChart graph = buildResultGraph.create(this, lastAction, pluginName);
            generateGraph(request, response, graph, mode);
        }
    }

    /**
     * Generates the graph in PNG format and sends that to the response.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param graph
     *            the graph
     * @param mode
     *            drawing mode
     */
    private void generateGraph(final StaplerRequest request, final StaplerResponse response, final JFreeChart graph, final Mode mode) {
        try {
            if (mode == Mode.PNG) {
                ChartUtil.generateGraph(request, response, graph, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            }
            else {
                ChartUtil.generateClickableMap(request, response, graph, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            }
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Can't create graph: " + request, exception);
        }
    }
}

