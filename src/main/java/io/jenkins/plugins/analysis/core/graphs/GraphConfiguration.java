package io.jenkins.plugins.analysis.core.graphs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.json.JSONObject;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.util.ToolTipProvider;
import hudson.util.FormValidation;

/**
 * Configuration properties of a trend graph.
 */
public class GraphConfiguration  {
    private static final int MAXIMUM_SIZE = 2000;
    private static final int MINIMUM_SIZE = 25;

    private static final boolean DEFAULT_USE_BUILD_DATE = false;
    private static final int DEFAULT_BUILD_COUNT = 50;
    private static final int DEFAULT_DAY_COUNT = 30;
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 200;
    private static final String DEFAULT_NAME = StringUtils.EMPTY;
    private static final String DEFAULT_VALUE = StringUtils.EMPTY;
    private ToolTipProvider toolTipProvider;

    private static PriorityGraph createDefaultGraph() {
        return new PriorityGraph();
    }

    /** Separator of cookie values. */
    protected static final String SEPARATOR = "!";

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
    /** Determines if the build date or the build number should be used as domain. */
    private boolean useBuildDate;
    /** Used to filter builds. Build that have parameter set to value below will be included in graph parameter name. */
    private String parameterName;
    private String parameterValue;

    /** Maps graph ID's to graphs. */
    private final Map<String, BuildResultGraph> graphId2Graph = Maps.newHashMap();
    /** Available graphs in the order to display. */
    private final List<BuildResultGraph> availableGraphs;

    /**
     * Creates a configuration for a deactivated graph.
     *
     * @return a configuration for a deactivated graph
     */
    public static GraphConfiguration createDeactivated() {
        return new GraphConfiguration(new NullGraph());
    }

    /**
     * Creates a default configuration.
     *
     * @return a default configuration
     */
    public static GraphConfiguration createDefault() {
        return new GraphConfiguration(createDefaultGraph());
    }

    /**
     * Creates a new instance of {@link hudson.plugins.analysis.graph.GraphConfiguration}.
     *
     * @param availableGraphs
     *            the available build graphs
     */
    public GraphConfiguration(final Collection<? extends BuildResultGraph> availableGraphs) {
        this.availableGraphs = ImmutableList.copyOf(availableGraphs);
        for (BuildResultGraph graph : availableGraphs) {
            graphId2Graph.put(graph.getId(), graph);
        }
    }

    /**
     * Creates a new instance of {@link hudson.plugins.analysis.graph.GraphConfiguration}.
     *
     * @param availableGraphs
     *            the available build graphs
     */
    public GraphConfiguration(final BuildResultGraph... availableGraphs) {
        this(Arrays.asList(availableGraphs));
    }

    /**
     * Creates a new instance of {@link hudson.plugins.analysis.graph.GraphConfiguration}.
     *
     * @param graph
     *            the graph to use
     */
    public GraphConfiguration(final BuildResultGraph graph) {
        reset();

        availableGraphs = Lists.newArrayList();
        graphType = graph;
        availableGraphs.add(graphType);
    }

    /**
     * Parses the provided string and initializes the members. If the string is
     * not in the expected format, then <code>false</code> is returned and the
     * members are reset to their default values.
     *
     * @param value
     *            the initialization value stored in the format
     *            <code>width!height!buildCount!dayCount!graphType</code>
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     * @see #serializeToString()
     */
    public boolean initializeFrom(final String value) {
        return resetIfInvalid(intializeFromStringValue(value));
    }

    /**
     * Initializes this configuration with the specified values.
     *
     * @param width
     *            the width of the graph
     * @param height
     *            the height of the graph
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     */
    @SuppressWarnings("hiding")
    public boolean initializeFrom(final int width, final int height) { // NOCHECKSTYLE
        return initializeFrom(width, height, 0);
    }

    /**
     * Initializes this configuration with the specified values.
     *
     * @param width
     *            the width of the graph
     * @param height
     *            the height of the graph
     * @param dayCount
     *            the number of days to build the graph for
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     */
    @SuppressWarnings("hiding")
    public boolean initializeFrom(final int width, final int height, final int dayCount) { // NOCHECKSTYLE
        return initializeFrom(width, height, dayCount, DEFAULT_NAME, DEFAULT_VALUE);
    }

    /**
     * Initializes this configuration with the specified values.
     *
     * @param width
     *            the width of the graph
     * @param height
     *            the height of the graph
     * @param dayCount
     *            the number of days to build the graph for
     * @param parameterName
     *            the name of the parameter used to filter results for the graph
     * @param parameterValue
     *            the value of the parameter used to filter results for the graph
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     * @since 1.73
     */
    @SuppressWarnings("hiding")
    public boolean initializeFrom(final int width, final int height, final int dayCount, final String parameterName, final String parameterValue) { // NOCHECKSTYLE
        this.width = width;
        this.height = height;
        this.dayCount = dayCount;
        buildCount = 0;
        useBuildDate = true;
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;

        return resetIfInvalid(isValid(width, height, buildCount, dayCount, graphType, parameterName, parameterValue));
    }

    /**
     * Initializes this configuration with the specified values.
     *
     * @param width
     *            the width of the graph
     * @param height
     *            the height of the graph
     * @param dayCountString
     *            the number of days to build the graph for
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     */
    @SuppressWarnings("hiding")
    public boolean initializeFrom(final String width, final String height, final String dayCountString) { // NOCHECKSTYLE
        return initializeFrom(width, height, dayCountString, DEFAULT_NAME, DEFAULT_VALUE);
    }

    /**
     * Initializes this configuration with the specified values.
     *
     * @param width
     *            the width of the graph
     * @param height
     *            the height of the graph
     * @param dayCountString
     *            the number of days to build the graph for
     * @param parameterName
     *            name of parameter by which builds will be filtered for the graph
     * @param parameterValue
     *            value of parameter by which builds will be filtered for the graph
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     * @since 1.73
     */
    @SuppressWarnings("hiding")
    public boolean initializeFrom(final String width, final String height, final String dayCountString, final String parameterName, final String parameterValue) { // NOCHECKSTYLE
        try {
            if (StringUtils.isBlank(dayCountString)) {
                dayCount = 0;
            }
            else {
                dayCount = Integer.parseInt(dayCountString);
            }

            return initializeFrom(Integer.parseInt(width), Integer.parseInt(height), dayCount, parameterName, parameterValue);
        }
        catch (NumberFormatException exception) {
            reset();

            return false;
        }
    }

    /**
     * Resets this configuration if the result is false.
     *
     * @param isSuccessful
     *            the result of the conversion
     * @return the result
     */
    private boolean resetIfInvalid(final boolean isSuccessful) {
        if (!isSuccessful) {
            reset();
        }

        return isSuccessful;
    }

    /**
     * See {@link #initializeFrom(String)}.
     *
     * @param value the initialization value
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     */
    // CHECKSTYLE:CONSTANTS-OFF
    private boolean intializeFromStringValue(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        String[] values = StringUtils.splitByWholeSeparatorPreserveAllTokens(value, SEPARATOR);
        if (values.length < 6) {
            return false;
        }

        try {
            width = Integer.parseInt(values[0]);
            height = Integer.parseInt(values[1]);
            buildCount = Integer.parseInt(values[2]);
            dayCount = Integer.parseInt(values[3]);
            graphType = graphId2Graph.get(values[4]);
            if ("0".equals(values[5])) {
                useBuildDate = false;
            }
            else if ("1".equals(values[5])) {
                useBuildDate = true;
            }
            else {
                return false;
            }
            if (values.length > 7) {
                parameterName = values[6];
                parameterValue = values[7];
            }
        }
        catch (IllegalArgumentException exception) {
            return false;
        }

        String[] localConfiguration = new String[values.length - 6];
        System.arraycopy(values, 6, localConfiguration, 0, values.length - 6);
        boolean isLocalValid = initializeLocal(localConfiguration);

        return isLocalValid && isValid(width, height, buildCount, dayCount, graphType, parameterName, parameterValue);
    }
    // CHECKSTYLE:CONSTANTS-ON

    /**
     * Parses the provided array of string values and initializes the members of
     * the local configuration. If the values are not in the expected format,
     * then <code>false</code> is returned and the members are reset to their
     * default values.
     * <p>The provided default implementation simply returns <code>true</code>.
     * </p>
     *
     * @param localConfiguration
     *            the initialization values
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     * @see #serializeToString()
     */
    protected boolean initializeLocal(final String[] localConfiguration) {
        return true;
    }

    /**
     * Parses the provided JSON object and initializes the members. If the JSON
     * object is not in the expected format, then <code>false</code> is returned
     * and the members are reset to their default values.
     *
     * @param value
     *            the initialization value
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     * @see #serializeToString()
     */
    public boolean initializeFrom(final JSONObject value) {
        return resetIfInvalid(initializeFromJsonObject(value));
    }

    /**
     * See {@link #initializeFrom(JSONObject)}.
     *
     * @param value the initialization value
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     */
    private boolean initializeFromJsonObject(final JSONObject value) {
        width = value.getInt("width");
        height = value.getInt("height");
        String buildCountString = value.getString("buildCountString");
        buildCount = 0;
        if (StringUtils.isNotBlank(buildCountString)) {
            buildCount = value.getInt("buildCountString");
        }
        String dayCountString = value.getString("dayCountString");
        dayCount = 0;
        if (StringUtils.isNotBlank(dayCountString)) {
            dayCount = value.getInt("dayCountString");
        }
        String graphTypeString = value.getString("graphType");
        graphType = graphId2Graph.get(graphTypeString);

        useBuildDate = value.getBoolean("useBuildDateAsDomain");

        if (value.has("parameterName")) {
            parameterName = value.getString("parameterName");
        }
        else {
            parameterName = DEFAULT_NAME;
        }
        if (value.has("parameterValue")) {
            parameterValue = value.getString("parameterValue");
        }
        else {
            parameterValue = DEFAULT_VALUE;
        }

        boolean isLocalValid = initializeLocal(value);

        return isLocalValid && isValid(width, height, buildCount, dayCount, graphType, parameterName, parameterValue);
    }

    /**
     * Parses the provided JSON object and initializes the members of
     * the local configuration. If the values are not in the expected format,
     * then <code>false</code> is returned and the members are reset to their
     * default values.
     * <p>The provided default implementation simply returns <code>true</code>.
     * </p>
     *
     * @param localConfiguration
     *            the initialization values
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     * @see #serializeToString()
     */
    protected boolean initializeLocal(final JSONObject localConfiguration) {
        return true;
    }

    /**
     * Reads the specified file, parses the content and initializes the members.
     * If the string is not in the expected format of the file could not be
     * read, then <code>false</code> is returned and the members are reset to
     * their default values.
     *
     * @param file
     *            the file with the initialization values
     * @return <code>true</code> is the initialization was successful,
     *         <code>false</code> otherwise
     * @see #serializeToString()
     */
    public boolean initializeFromFile(final File file) {
        return initializeFrom(readFromDefaultsFile(file));
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
    protected File createDefaultsFile(final AbstractProject<?, ?> project, final String pluginName) {
        return new File(project.getRootDir(), pluginName + ".txt");
    }

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
        buildCount = DEFAULT_BUILD_COUNT;
        dayCount = DEFAULT_DAY_COUNT;
        graphType = createDefaultGraph();
        useBuildDate = DEFAULT_USE_BUILD_DATE;
        parameterName = DEFAULT_NAME;
        parameterValue = DEFAULT_VALUE;
    }

    /**
     * Serializes the values of this configuration.
     *
     * @return serialized configuration
     * @see #initializeFrom(String)
     */
    public String serializeToString() {
        return width + SEPARATOR
                + height + SEPARATOR
                + buildCount + SEPARATOR
                + dayCount + SEPARATOR
                + graphType.getId() + SEPARATOR
                + serializeBoolean(useBuildDate) + SEPARATOR
                + parameterName + SEPARATOR
                + parameterValue + SEPARATOR;
    }

    /**
     * Serializes a boolean.
     *
     * @param value the value
     * @return serialized value
     */
    protected String serializeBoolean(final boolean value) {
        if (value) {
            return "1";
        }
        else {
            return "0";
        }
    }

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
     * @param newParameterName
     *            the parameter name
     * @param newParameterValue
     *            the parameter value
     * @return <code>true</code> if the configuration parameters are valid,
     *         <code>false</code> otherwise.
     * @since 1.73
     */
    //CHECKSTYLE:OFF
    protected static boolean isValid(final int newWidth, final int newHeight,
            final int newBuildCount, final int newDayCount, final BuildResultGraph newGraphType,
            final String newParameterName, final String newParameterValue) {
        return isValidWidth(newWidth)
                && isValidHeight(newHeight)
                && newGraphType != null
                && newDayCount >= 0
                && isValidBuildCount(newBuildCount)
                && !(StringUtils.isNotBlank(newParameterName) ^ StringUtils.isNotBlank(newParameterValue));
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
        return newWidth > MINIMUM_SIZE && newWidth < MAXIMUM_SIZE;
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
        return newHeight > MINIMUM_SIZE && newHeight  < MAXIMUM_SIZE;
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
     * Returns whether the build date or the build number should be used as domain.
     *
     * @return the build date or the build number should be used as domain
     */
    public boolean useBuildDateAsDomain() {
        return useBuildDate;
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
     * Returns the parameter name used to filter results for graph.
     *
     * @return the parameter name
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Returns the parameter value used to filter results for graph.
     *
     * @return the parameter value
     */
    public String getParameterValue() {
        return parameterValue;
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
                && graphType.getId() == createDefaultGraph().getId() // NOPMD
                && buildCount == DEFAULT_BUILD_COUNT
                && dayCount == DEFAULT_DAY_COUNT
                && useBuildDate == DEFAULT_USE_BUILD_DATE
                && parameterName == DEFAULT_NAME
                && parameterValue == DEFAULT_VALUE;
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

    @Override
    public String toString() {
        return "type: " + graphType + ", size: " + width + "x" + height
                + ", # builds " + buildCount + ", # days " + dayCount + ", useBuildDate:" + useBuildDate
                + ", parameterName:" + parameterName + ", parameterValue:" + parameterValue;
    }

    /**
     * Returns the registered graphs.
     *
     * @return the registered graphs
     */
    public Collection<BuildResultGraph> getRegisteredGraphs() {
        return availableGraphs;
    }

    /**
     * Returns the graph with the specified ID.
     *
     * @param graphId
     *            the graph ID
     * @return the graph with the specified ID. If the graph is not found, then
     *         {@link #createDefaultGraph()} is returned.
     */
    public BuildResultGraph getGraph(final String graphId) {
        if (graphId2Graph.containsKey(graphId)) {
            return graphId2Graph.get(graphId);
        }
        else {
            return createDefaultGraph();
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
        return FormValidation.error("Invalid height value: " + height);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GraphConfiguration that = (GraphConfiguration) o;

        if (height != that.height) {
            return false;
        }
        if (width != that.width) {
            return false;
        }
        if (buildCount != that.buildCount) {
            return false;
        }
        if (dayCount != that.dayCount) {
            return false;
        }
        if (useBuildDate != that.useBuildDate) {
            return false;
        }
        if (graphType != null ? !graphType.equals(that.graphType) : that.graphType != null) {
            return false;
        }
        if (parameterName != null ? !parameterName.equals(that.parameterName) : that.parameterName != null) {
            return false;
        }
        return parameterValue != null ? parameterValue.equals(that.parameterValue) : that.parameterValue == null;
    }

    @Override
    public int hashCode() {
        int result = height;
        result = 31 * result + width;
        result = 31 * result + (graphType != null ? graphType.hashCode() : 0);
        result = 31 * result + buildCount;
        result = 31 * result + dayCount;
        result = 31 * result + (useBuildDate ? 1 : 0);
        result = 31 * result + (parameterName != null ? parameterName.hashCode() : 0);
        result = 31 * result + (parameterValue != null ? parameterValue.hashCode() : 0);
        return result;
    }

    public void setToolTipProvider(final ToolTipProvider toolTipProvider) {
        this.toolTipProvider = toolTipProvider;
    }

    public ToolTipProvider getToolTipProvider() {
        return toolTipProvider;
    }
}

