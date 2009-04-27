package hudson.plugins.warnings.util;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;

/**
 * Configuration properties of a trend graph.
 */
public class GraphConfiguration {
    /** The default graph. */
    private static final GraphType DEFAULT_GRAPH = GraphType.PRIORITY;
    /** The default width. */
    private static final int DEFAULT_WIDTH = 500;
    /** The default height. */
    private static final int DEFAULT_HEIGHT = 200;

    /** Separator of cookie values. */
    private static final String SEPARATOR = ":";

    /** The height of the trend graph. */
    private int height;
    /** The width of the trend graph. */
    private int width;
    /** The type of the graph. */
    private GraphType graphType;

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
     * Resets the graph configuration to the default values.
     */
    private void reset() {
        height = DEFAULT_HEIGHT;
        width  = DEFAULT_WIDTH;
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
     * @see #serializeToString(int, int, GraphType)
     */
    private boolean initializeFrom(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        String[] values = StringUtils.split(value, SEPARATOR);
        if (values.length != 3) {
            return false;
        }

        try {
            width = Integer.parseInt(values[0]);
            height = Integer.parseInt(values[1]);
            graphType = GraphType.valueOf(values[2]);
        }
        catch (NumberFormatException exception) {
            return false;
        }
        catch (IllegalArgumentException exception) {
            return false;
        }

        return isValid(width, height, graphType);
    }

    /**
     * Serializes the values of this configuration.
     *
     * @param width
     *            width of graph
     * @param height
     *            height of graph
     * @param graphType
     *            type of graph
     * @return serialized configuration
     * @see #initializeFrom(String)
     */
    // CHECKSTYLE:OFF
    protected String serializeToString(final int width, final int height, final GraphType graphType) {
        return width + SEPARATOR + height + SEPARATOR + graphType;
    }
    // CHECKSTYLE:ON

    /**
     * Returns whether the configuration parameters are valid.
     * @param newWidth
     *            the new width
     * @param newHeight
     *            the new height
     * @param newGraphType
     *            the new graph type
     *
     * @return <code>true</code> if the configuration parameters are valid,
     *         <code>false</code> otherwise.
     */
    protected boolean isValid(final int newWidth, final int newHeight, final GraphType newGraphType) {
        return isValidWidth() && isValidHeight() && graphType != null;
    }

    /**
     * Returns whether the width is valid.
     *
     * @return <code>true</code> if the width is valid, <code>false</code> otherwise
     */
    private boolean isValidWidth() {
        return width > 25 && width  < 2000;
    }

    /**
     * Returns whether the width is valid.
     *
     * @return <code>true</code> if the width is valid, <code>false</code> otherwise
     */
    private boolean isValidHeight() {
        return height > 25 && height  < 2000;
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
     * @return the graph
     */
    public JFreeChart createGraph(final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction) {
        return getGraphType().createGraph(healthDescriptor, resultAction);
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
        return getGraphType().createGraph(healthDescriptor, resultAction, url);
    }

    /**
     * Returns whether this instance is initialized with its default values.
     *
     * @return <code>true</code> if this instance is initialized with its default values.
     */
    public boolean isDefault() {
        return width == DEFAULT_WIDTH && height == DEFAULT_HEIGHT && graphType == DEFAULT_GRAPH;
    }

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
        return "type: " + graphType + ", size: " + width + "x" + height;
    }
}

