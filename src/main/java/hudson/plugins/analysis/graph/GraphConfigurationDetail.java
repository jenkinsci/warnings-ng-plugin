package hudson.plugins.analysis.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import hudson.model.AbstractProject;
import hudson.model.ModelObject;

import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.NullHealthDescriptor;
import hudson.plugins.analysis.core.ResultAction;

import hudson.util.ChartUtil;
import hudson.util.FormValidation;
import hudson.util.Graph;

/**
 * Configuration properties of a trend graph.
 */
public abstract class GraphConfigurationDetail implements ModelObject {
    /** The default counter. */
    private static final int DEFAULT_COUNT = 0;
    /** The default width. */
    protected static final int DEFAULT_WIDTH = 500;
    /** The default height. */
    protected static final int DEFAULT_HEIGHT = 200;

    /** Separator of cookie values. */
    private static final String SEPARATOR = "!";

    /** The height of the trend graph. */
    private int height;
    /** The width of the trend graph. */
    private int width;
    /** The type of the graph. */
    private BuildResultGraph graphType;
    /** The number of builds to consider. */
    private int buildCount;
    /** The number of days to consider. */
    private int dayCount;

    /** The default graph. */
    private final BuildResultGraph defaultGraph = new PriorityGraph(this);

    /** Maps graph ID's to graphs. */
    private final Map<String, BuildResultGraph> graphId2Graph = Maps.newHashMap();

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
        initializeGraphs();

        if (!initializeFrom(value)) {
            File defaultsFile = createDefaultsFile(project, pluginName);
            if (defaultsFile.exists()) {
                String defaultValue = readFromDefaultsFile(defaultsFile);
                if (!initializeFrom(defaultValue)) {
                    reset();
                }
            }
            else {
                reset();
            }
        }
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

        initializeGraphs();
    }


    /**
     * Initialize the graphs with the predefined graphs.
     */
    private void initializeGraphs() {
        addGraph(new EmptyGraph(this));
        addGraph(new NewVersusFixedGraph(this));
        addGraph(new PriorityGraph(this));
        addGraph(new HealthGraph(this));
        addGraph(new DifferenceGraph(this));
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
     * Returns the list of available graphs.
     *
     * @return the list of available graphs
     */
    public List<? extends BuildResultGraph> getAvailableGraphs() {
        return ImmutableList.copyOf(graphId2Graph.values());
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
    // CHECKSTYLE:OFF
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
            BuildResultGraph graphType = graphId2Graph.get(formData.getString("graphType"));

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
    // CHECKSTYLE:ON

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
     * Returns the last {@link ResultAction}. If there is no such action, then
     * <code>null</code> is returned.
     *
     * @return the last result action
     */
    public ResultAction<?> getLastAction() {
        return lastAction;
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
     * This method will be called by Stapler if an example image for the
     * specified graph should be rendered.
     *
     * @param graphId
     *            the ID of the graph to render
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return <code>null</code>
     */
    public Object getDynamic(final String graphId, final StaplerRequest request,
            final StaplerResponse response) {
        try {
            BuildResultGraph graph = graphId2Graph.get(graphId);
            if (graph != null) {
                graph.setRootUrl(ROOT_URL);
                if (hasMeaningfulGraph() && graph.isVisible()) {
                    ChartUtil.generateGraph(request, response, graph.create(this, lastAction,
                            pluginName), width, height);
                }
                else {
                    // FIXME: image...
                    response.sendRedirect2(request.getContextPath() + "/images/headless.png");
                }
            }
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Can't create graph: " + request, exception);
        }

        return null;
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
     * Reads the default values from file.
     *
     * @param defaultsFile
     *            the file with the default values
     * @return the default values from file.
     */
    private String readFromDefaultsFile(final File defaultsFile) {
        String defaultValue = StringUtils.EMPTY;
        FileInputStream input = null;
        try {
            input = new FileInputStream(defaultsFile);
            defaultValue = IOUtils.toString(input);
        }
        catch (IOException exception) {
            // ignore
        }
        finally {
            IOUtils.closeQuietly(input);
        }
        return defaultValue;
    }

    /**
     * Resets the graph configuration to the default values.
     */
    private void reset() {
        height = DEFAULT_HEIGHT;
        width  = DEFAULT_WIDTH;
        buildCount = DEFAULT_COUNT;
        dayCount = DEFAULT_COUNT;
        graphType = defaultGraph;
    }

    /**
     * Parses the provided string and initializes the members. If the string is
     * not in the expected format, then <code>false</code> is returned.
     *
     * @param value
     *            the initialization value stored in the format
     *            <code>width:height:isVisible</code>
     * @return true is the initialization was successful, <code>false</code>
     *         otherwise
     * @see #serializeToString(int, int, int, int, BuildResultGraph)
     */
    private boolean initializeFrom(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        String[] values = StringUtils.split(value, SEPARATOR);
        if (values.length != 5) {
            return false;
        }

        try {
            width = Integer.parseInt(values[0]);
            height = Integer.parseInt(values[1]);
            buildCount = Integer.parseInt(values[2]);
            dayCount = Integer.parseInt(values[3]);
            graphType = graphId2Graph.get(values[4]);
        }
        catch (NumberFormatException exception) {
            return false;
        }
        catch (IllegalArgumentException exception) {
            return false;
        }

        return isValid(width, height, buildCount, dayCount, graphType);
    }

    /**
     * Serializes the values of this configuration.
     *
     * @param width
     *            width of graph
     * @param height
     *            height of graph
     * @param buildCount
     *            the build count
     * @param dayCount
     *            the day count
     * @param graphType
     *            type of graph
     * @return serialized configuration
     * @see #initializeFrom(String)
     */
    // CHECKSTYLE:OFF
    protected String serializeToString(final int width, final int height,
            final int buildCount, final int dayCount, final BuildResultGraph graphType) {
        return width + SEPARATOR
                + height + SEPARATOR
                + buildCount + SEPARATOR
                + dayCount + SEPARATOR
                + graphType.getId();
    }
    // CHECKSTYLE:ON


    /**
     * Returns whether the configuration parameters are valid.
     *
     * @param newWidth
     *            the new width
     * @param newHeight
     *            the new height
     * @param newBuildCount
     *            the new build count
     * @param newDayCount
     *            the new day count
     * @param newGraphType
     *            the new graph type
     * @return <code>true</code> if the configuration parameters are valid,
     *         <code>false</code> otherwise.
     */
    //CHECKSTYLE:OFF
    protected static boolean isValid(final int newWidth, final int newHeight,
            final int newBuildCount, final int newDayCount, final BuildResultGraph newGraphType) {
        return isValidWidth(newWidth)
                && isValidHeight(newHeight)
                && newGraphType != null
                && newDayCount >= 0
                && isValidBuildCount(newBuildCount);
    }
    //CHECKSTYLE:ON

    /**
     * Returns if the build count is valid.
     *
     * @param newBuildCount
     *            the new build count
     * @return <code>true</code> if the build count is valid.
     */
    protected static boolean isValidBuildCount(final int newBuildCount) {
        return newBuildCount == 0 || newBuildCount > 1;
    }

    /**
     * Returns whether the width is valid.
     *
     * @param newWidth
     *            the new width
     * @return <code>true</code> if the width is valid, <code>false</code>
     *         otherwise
     */
    protected static boolean isValidWidth(final int newWidth) {
        return newWidth > 25 && newWidth < 2000;
    }

    /**
     * Returns whether the width is valid.
     *
     * @param newHeight
     *            the new height
     * @return <code>true</code> if the width is valid, <code>false</code>
     *         otherwise
     */
    protected static boolean isValidHeight(final int newHeight) {
        return newHeight > 25 && newHeight  < 2000;
    }

    /**
     * Returns the height.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the width.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the time stamp of the associated build.
     *
     * @return the time stamp of the associated build.
     */
    public long getTimestamp() {
        if (lastAction != null) {
            return lastAction.getBuild().getTimestamp().getTimeInMillis();
        }
        else {
            return -1;
        }
    }

    /**
     * Returns the number of builds to consider.
     *
     * @return the number of builds to consider
     */
    public int getBuildCount() {
        return buildCount;
    }

    /**
     * Returns whether a valid build count is defined.
     *
     * @return <code>true</code> if there is a valid build count is defined,
     *         <code>false</code> otherwise
     */
    public boolean isBuildCountDefined() {
        return buildCount > 1;
    }

    /**
     * Returns the number of days to consider.
     *
     * @return the number of days to consider
     */
    public int getDayCount() {
        return dayCount;
    }

    /**
     * Returns whether a valid day count is defined.
     *
     * @return <code>true</code> if there is a valid day count is defined,
     *         <code>false</code> otherwise
     */
    public boolean isDayCountDefined() {
        return dayCount > 0;
    }

    /**
     * Returns the type of the graph.
     *
     * @return the type
     */
    public BuildResultGraph getGraphType() {
        return graphType;
    }

    /**
     * Creates the graph.
     *
     * @param healthDescriptor
     *            the health descriptor
     * @param resultAction
     *            the action to start the graph with
     * @param pluginName
     *            base URL of the graph links
     * @return the graph
     */
    public JFreeChart createGraph(final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String pluginName) {
        return getGraphType().create(this, resultAction, pluginName);
    }

    /**
     * Returns whether this instance is initialized with its default values.
     *
     * @return <code>true</code> if this instance is initialized with its default values.
     */
    // CHECKSTYLE:OFF
    public boolean isDefault() {
        return width == DEFAULT_WIDTH
                && height == DEFAULT_HEIGHT
                && graphType == defaultGraph
                && buildCount == DEFAULT_COUNT
                && dayCount == DEFAULT_COUNT;
    }
    // CHECKSTYLE:ON

    /**
     * Returns whether the trend graph is visible or not.
     *
     * @return <code>true</code>, if the trend graph is visible, <code>false</code> otherwise
     */
    public boolean isVisible() {
        return graphType.isVisible();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "type: " + graphType + ", size: " + width + "x" + height
                + ", # builds " + buildCount + ", # days " + dayCount;
    }

    /**
     * Adds the specified graph to the list of available graphs.
     *
     * @param graph
     *            the graph to add
     */
    public void addGraph(final BuildResultGraph graph) {
        graphId2Graph.put(graph.getId(), graph);
    }

    /**
     * Returns the current trend graph.
     *
     * @return the trend graph
     */
    public Graph getGraph() {
        return getGraphType();
    }

    /**
     * Returns the current health descriptor.
     *
     * @return the health descriptor
     */
    public AbstractHealthDescriptor getHealthDescriptor() {
        return healthDescriptor;
    }

    /**
     * FIXME: Remove this method.
     */
    public void clearGraphs() {
        graphId2Graph.clear();
    }
}

