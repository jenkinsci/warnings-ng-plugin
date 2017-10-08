package io.jenkins.plugins.analysis.core.graphs;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.google.common.collect.Lists;

import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.history.RunResultHistory;
import io.jenkins.plugins.analysis.core.steps.AnalysisResult;
import net.sf.json.JSONObject;

import hudson.model.Job;
import hudson.model.ModelObject;
import hudson.util.Graph;

/**
 * Configuration properties of a trend graph.
 */
public abstract class GraphConfigurationView implements ModelObject {
    private static final Logger LOGGER = Logger.getLogger(hudson.plugins.analysis.graph.GraphConfigurationView.class.getName());

    /** The owning job to configure the graphs for. */
    private final Job<?, ?> owner;

    private final String key;
    private final RunResultHistory buildHistory;
    private final HealthDescriptor healthDescriptor; // NOPMD
    private final GraphConfiguration configuration;
    private String urlPrefix;

    /**
     * Creates a new instance of {@link hudson.plugins.analysis.graph.GraphConfigurationView}.
     *
     * @param configuration
     *            the graph configuration
     * @param job
     *            the owning job to configure the graphs for
     * @param key
     *            unique key of this graph
     * @param buildHistory
     *            the build history for this job
     */
    public GraphConfigurationView(final GraphConfiguration configuration, final Job<?, ?> job, final String key, final RunResultHistory buildHistory) {
        this.configuration = configuration;
        this.owner = job;
        this.key = key;
        this.buildHistory = buildHistory;
        healthDescriptor = new HealthDescriptor();
    }

    /**
     * Creates a file with for the default values.
     *
     * @param job
     *            the job used as directory for the file
     * @param pluginName
     *            the name of the plug-in
     * @return the created file
     */
    protected static File createDefaultsFile(final Job<?, ?> job, final String pluginName) {
        return new File(job.getRootDir(), pluginName + ".txt");
    }

    /**
     * Returns the list of available graphs.
     *
     * @return the list of available graphs
     */
    public List<? extends BuildResultGraph> getAvailableGraphs() {
        ArrayList<BuildResultGraph> selectable = Lists.newArrayList();
        for (BuildResultGraph graph : configuration.getRegisteredGraphs()) {
            if (graph.isSelectable()) {
                selectable.add(graph);
            }
        }
        return selectable;
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public Job<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the key of this graph.
     *
     * @return the key
     */
    public String getKey() {
        return key;
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

            if (configuration.initializeFrom(formData)) {
                persistValue(configuration.serializeToString(), key, request, response);
            }
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Can't save the form data: " + request, exception);
        }
        catch (ServletException exception) {
            LOGGER.log(Level.SEVERE, "Can't process the form data: " + request, exception);
        }
        finally {
            try {
                response.sendRedirect(owner.getAbsoluteUrl());
            }
            catch (IOException exception) {
                LOGGER.log(Level.SEVERE, "Can't redirect", exception);
            }
        }
    }

    /**
     * Checks whether a meaningful graph is available.
     *
     * @return <code>true</code>, if there is such a graph
     */
    public boolean hasMeaningfulGraph() {
        Optional<AnalysisResult> previousResult = buildHistory.getPreviousResult();
        if (previousResult.isPresent()) {
            return !SeriesBuilder.areResultsTooOld(configuration, previousResult.get());
        }
        return false;
    }

    /**
     * Persists the configured values.
     *
     * @param value
     *            the values configured by the user.
     * @param pluginName
     *            the name of the plug-in
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             if the values could not be persisted
     */
    protected abstract void persistValue(String value, String pluginName,
            StaplerRequest request, StaplerResponse response) throws IOException;

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
    public Object getDynamic(final String graphId, final StaplerRequest request, final StaplerResponse response) {
        try {
            BuildResultGraph graph = configuration.getGraph(graphId);
            if (hasMeaningfulGraph() && graph.isVisible()) {
                return graph.getGraph(-1, configuration, null, buildHistory);
            }
            response.sendRedirect2(request.getContextPath() + graph.getExampleImage());
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Can't create graph: " + request, exception);
        }

        return null;
    }

    /**
     * Returns the graph renderer of the specified graph.
     *
     * @param graph
     *            the graph
     * @param url
     *            the URL of links in the trend graph
     * @return the graph renderer of the specified graph
     */
    public Graph getGraphRenderer(final BuildResultGraph graph, final String url) {
        return graph.getGraph(getTimestamp(), configuration, url, buildHistory);
    }

    /**
     * Returns the graph renderer of the current graph.
     *
     * @return the graph renderer of the current graph
     */
    public Graph getGraphRenderer() {
        return getGraphRenderer(getGraphType(), null);
    }

    /**
     * Returns the graph renderer of the current graph.
     *
     * @param url
     *            the URL of links in the trend graph
     * @return the graph renderer of the current graph
     */
    public Graph getGraphRenderer(final String url) {
        return getGraphRenderer(getGraphType(), url);
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
     * Returns the height.
     *
     * @return the height
     */
    public int getHeight() {
        return configuration.getHeight();
    }

    /**
     * Returns the width.
     *
     * @return the width
     */
    public int getWidth() {
        return configuration.getWidth();
    }

    /**
     * Returns whether the build date or the build number should be used as domain.
     *
     * @return the build date or the build number should be used as domain
     */
    public boolean getUseBuildDateAsDomain() {
        return configuration.useBuildDateAsDomain();
    }

    /**
     * Returns the time stamp of the associated build.
     *
     * @return the time stamp of the associated build.
     */
    public long getTimestamp() {
        return 0; // FIXME: train wreck
    }

    /**
     * Returns the number of builds to consider.
     *
     * @return the number of builds to consider
     */
    public int getBuildCount() {
        return configuration.getBuildCount();
    }

    /**
     * Returns the number of builds to consider.
     *
     * @return the number of builds to consider
     */
    public String getBuildCountString() {
        return getStringValue(getBuildCount());
    }

    /**
     * Returns the value as integer. If the value is 0, then an empty string is
     * returned.
     *
     * @param value
     *            the value to convert
     * @return string representation of <code>value</code>
     */
    private String getStringValue(final int value) {
        return value == 0 ? StringUtils.EMPTY : String.valueOf(value);
    }

    /**
     * Returns the number of days to consider.
     *
     * @return the number of days to consider
     */
    public int getDayCount() {
        return configuration.getDayCount();
    }

    /**
     * Returns the number of days to consider.
     *
     * @return the number of days to consider
     */
    public String getDayCountString() {
        return getStringValue(getDayCount());
    }

    /**
     * Returns the parameter name to consider.
     *
     * @return the parameter name to consider.
     */
    public String getParameterName() {
        return configuration.getParameterName();
    }

    /**
     * Returns the parameter value to consider.
     *
     * @return the parameter value to consider.
     */
    public String getParameterValue() {
        return configuration.getParameterValue();
    }

    /**
     * Returns the type of the graph.
     *
     * @return the type
     */
    public BuildResultGraph getGraphType() {
        BuildResultGraph graphType = configuration.getGraphType();
        graphType.setRootUrl(StringUtils.defaultString(urlPrefix));
        return graphType;
    }

    /**
     * Returns whether the trend graph is visible or not.
     *
     * @return <code>true</code>, if the trend graph is visible, <code>false</code> otherwise
     */
    public boolean isVisible() {
        return getGraphType().isVisible();
    }

    /**
     * Returns whether the trend graph completely is deactivated.
     *
     * @return <code>true</code>, if the trend graph is deactivated, <code>false</code> otherwise
     */
    public boolean isDeactivated() {
        return getGraphType().isDeactivated();
    }

    @Override
    public String toString() {
        return configuration.toString();
    }

    /**
     * Returns the current health descriptor.
     *
     * @return the health descriptor
     */
    public HealthDescriptor getHealthDescriptor() {
        return healthDescriptor;
    }

    /**
     * Sets the prefix of the URLs in the trend graph. Depending on the sub page this trend is shown a different
     * prefix can be set for the relative URL.
     *
     * @param urlPrefix prefix, might be empty
     * @since 1.73
     */
    public void setUrlPrefix(final String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }
}
