package hudson.plugins.analysis.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;

import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Configuration properties of a trend graph.
 */
public class GraphConfiguration {
    /** The default counter. */
    private static final int DEFAULT_COUNT = 0;
    /** The default graph. */
    private static final GraphType DEFAULT_GRAPH = GraphType.PRIORITY;
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
    private GraphType graphType;
    /** The number of builds to consider. */
    private int buildCount;
    /** The number of days to consider. */
    private int dayCount;

    /**
     * Creates a new instance of {@link GraphConfiguration}.
     *
     * @param value
     *            the initial value of this configuration (the syntax of the
     *            string is defined in {@link #initializeFrom(String)})
     */
    public GraphConfiguration(final String value) {
        if (!initializeFrom(value)) {
            reset();
        }
    }

    /**
     * Creates a new instance of {@link GraphConfiguration}.
     *
     * @param value
     *            the initial value of this configuration (the syntax of the
     *            string is defined in {@link #initializeFrom(String)})
     * @param defaultsFile
     *            a file with default values in case the specified value is not
     *            valid
     */
    public GraphConfiguration(final String value, final File defaultsFile) {
        if (!initializeFrom(value)) {
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
     * @see #serializeToString(int, int, int, int, GraphType)
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
            graphType = GraphType.valueOf(values[4]);
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
    protected String serializeToString(final int width, final int height, final int buildCount, final int dayCount, final GraphType graphType) {
        return width + SEPARATOR
                + height + SEPARATOR
                + buildCount + SEPARATOR
                + dayCount + SEPARATOR
                + graphType;
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
    protected static boolean isValid(final int newWidth, final int newHeight, final int newBuildCount, final int newDayCount, final GraphType newGraphType) {
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
    public GraphType getGraphType() {
        return graphType;
    }

    /**
     * Creates the graph.
     *
     * @param healthDescriptor
     *            the health descriptor
     * @param resultAction
     *            the action to start the graph with
     * @param url
     *            base URL of the graph links
     * @return the graph
     */
    public JFreeChart createGraph(final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String url) {
        return getGraphType().createGraph(this, healthDescriptor, resultAction, url);
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
                && graphType == DEFAULT_GRAPH
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
        return graphType != GraphType.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "type: " + graphType + ", size: " + width + "x" + height
                + ", # builds " + buildCount + ", # days " + dayCount;
    }
}

