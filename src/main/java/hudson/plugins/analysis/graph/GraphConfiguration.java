package hudson.plugins.analysis.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import hudson.model.AbstractProject;

import hudson.util.FormValidation;

/**
 * Configuration properties of a trend graph.
 */
public class GraphConfiguration  {
    /** The default value for use build date. */
    private static final boolean DEFAULT_USE_BUILD_DATE = false;
    /** The default counter. */
    private static final int DEFAULT_COUNT = 0;
    /** The default width. */
    private static final int DEFAULT_WIDTH = 500;
    /** The default height. */
    private static final int DEFAULT_HEIGHT = 200;
    /** The default graph. */
    private static final BuildResultGraph DEFAULT_GRAPH = new PriorityGraph();

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
        return new GraphConfiguration(DEFAULT_GRAPH);
    }

    /**
     * Creates a new instance of {@link GraphConfiguration}.
     *
     * @param availableGraphs
     *            the available build graphs
     */
    public GraphConfiguration(final Collection<BuildResultGraph> availableGraphs) {
        this.availableGraphs = ImmutableList.copyOf(availableGraphs);
        for (BuildResultGraph graph : availableGraphs) {
            graphId2Graph.put(graph.getId(), graph);
        }
    }

    /**
     * Creates a new instance of {@link GraphConfiguration}.
     *
     * @param graph
     *            the graph to use
     */
    private GraphConfiguration(final BuildResultGraph graph) {
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
     * Resets this configuration if the result is false.
     *
     * @param isSuccessful
     *            the result of the conversion
     * @return the resul
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
    private boolean intializeFromStringValue(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        String[] values = StringUtils.split(value, SEPARATOR);
        if (values.length < 5) {
            return false;
        }

        try {
            width = Integer.parseInt(values[0]);
            height = Integer.parseInt(values[1]);
            buildCount = Integer.parseInt(values[2]);
            dayCount = Integer.parseInt(values[3]);
            graphType = graphId2Graph.get(values[4]);
            if (values.length == 6) {
                useBuildDate = Boolean.parseBoolean(values[5]);
            }
        }
        catch (NumberFormatException exception) {
            return false;
        }
        catch (IllegalArgumentException exception) {
            return false;
        }

        String[] localConfiguration = new String[values.length - 5];
        System.arraycopy(values, 5, localConfiguration, 0, values.length - 5);
        boolean isLocalValid = initializeLocal(localConfiguration);

        return isLocalValid && isValid(width, height, buildCount, dayCount, graphType);
    }

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
        String grapyTypeString = value.getString("graphType");
        graphType = graphId2Graph.get(grapyTypeString);

        useBuildDate = value.getBoolean("useBuildDate");

        boolean isLocalValid = initializeLocal(value);

        return isLocalValid && isValid(width, height, buildCount, dayCount, graphType);
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
        buildCount = DEFAULT_COUNT;
        dayCount = DEFAULT_COUNT;
        graphType = DEFAULT_GRAPH;
        useBuildDate = DEFAULT_USE_BUILD_DATE;
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
                + useBuildDate;
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
     * Returns whether this instance is initialized with its default values.
     *
     * @return <code>true</code> if this instance is initialized with its default values.
     */
    // CHECKSTYLE:OFF
    public boolean isDefault() {
        return width == DEFAULT_WIDTH
                && height == DEFAULT_HEIGHT
                && graphType == DEFAULT_GRAPH // NOPMD
                && buildCount == DEFAULT_COUNT
                && dayCount == DEFAULT_COUNT
                && useBuildDate == DEFAULT_USE_BUILD_DATE;
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
                + ", # builds " + buildCount + ", # days " + dayCount + ", useBuildDate:" + useBuildDate;
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
     *         {@link #DEFAULT_GRAPH} is returned.
     */
    public BuildResultGraph getGraph(final String graphId) {
        if (graphId2Graph.containsKey(graphId)) {
            return graphId2Graph.get(graphId);
        }
        else {
            return DEFAULT_GRAPH;
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + buildCount;
        result = prime * result + dayCount;
        result = prime * result + ((graphType == null) ? 0 : graphType.getId().hashCode());
        result = prime * result + height;
        result = prime * result + (useBuildDate ? 1231 : 1237);
        result = prime * result + width;
        return result;
    }

    /** {@inheritDoc} */
    @Override // NOCHECKSTYLE
    @SuppressWarnings("PMD")
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GraphConfiguration other = (GraphConfiguration)obj;
        if (buildCount != other.buildCount) {
            return false;
        }
        if (dayCount != other.dayCount) {
            return false;
        }
        if (graphType == null) {
            if (other.graphType != null) {
                return false;
            }
        }
        else if (!graphType.getId().equals(other.graphType.getId())) {
            return false;
        }
        if (height != other.height) {
            return false;
        }
        if (useBuildDate != other.useBuildDate) {
            return false;
        }
        if (width != other.width) {
            return false;
        }
        return true;
    }
}

