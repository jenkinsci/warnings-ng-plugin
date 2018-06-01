package io.jenkins.plugins.analysis.core.graphs;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import io.jenkins.plugins.analysis.core.history.AnalysisHistory;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;

import hudson.model.Job;
import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * Configures the default values for the trend graph of this plug-in.
 */
public class DefaultGraphConfigurationView extends GraphConfigurationView {
    private final String url;

    /**
     * Creates a new instance of {@link DefaultGraphConfigurationView}.
     *
     * @param configuration
     *         the graph configuration
     * @param job
     *         the owning job to configure the graphs for
     * @param pluginName
     *         The name of the plug-in.
     * @param buildHistory
     *         the build history for this job
     * @param toolTipProvider
     *         the tool tip provider to use
     * @param healthDescriptor
     *         the health descriptor to use
     */
    public DefaultGraphConfigurationView(final GraphConfiguration configuration, final Job<?, ?> job,
            final String pluginName, final AnalysisHistory buildHistory,
            final ToolTipProvider toolTipProvider, final HealthDescriptor healthDescriptor) {
        super(configuration, job, pluginName, buildHistory, healthDescriptor);

        url = job.getAbsoluteUrl() + pluginName + "/configureDefaults";

        configuration.initializeFromFile(createDefaultsFile(job, pluginName));
        configuration.setToolTipProvider(toolTipProvider);
    }

    @Override
    public String getDisplayName() {
        return Messages.DefaultGraphConfiguration_Name();
    }

    @Override
    public String getDescription() {
        return Messages.DefaultGraphConfiguration_Description();
    }

    /**
     * Returns the URL of this object.
     *
     * @return the URL of this object
     */
    public String getUrl() {
        return url;
    }

    @Override
    protected void persistValue(final String value, final String pluginName, final StaplerRequest request,
            final StaplerResponse response) throws IOException {
        try (FileOutputStream output = new FileOutputStream(createDefaultsFile(getOwner(), pluginName))) {
            IOUtils.write(value, output);
        }
    }
}

