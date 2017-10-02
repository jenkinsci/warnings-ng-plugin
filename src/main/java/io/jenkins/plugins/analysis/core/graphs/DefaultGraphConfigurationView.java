package io.jenkins.plugins.analysis.core.graphs;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import io.jenkins.plugins.analysis.core.HistoryProvider;

import hudson.model.Job;
import hudson.plugins.analysis.Messages;

/**
 * Configures the default values for the trend graph of this plug-in.
 */
public class DefaultGraphConfigurationView extends GraphConfigurationView {
    private final String url;

    /**
     * Creates a new instance of {@link hudson.plugins.analysis.graph.DefaultGraphConfigurationView}.
     *
     * @param configuration
     *            the graph configuration
     * @param job
     *            the owning job to configure the graphs for
     * @param pluginName
     *            The URL of the job action (there might be a one to many mapping to this defaults view)
     * @param buildHistory
     *            the build history for this job
     * @param url
     *            The URL of this view
     */
    public DefaultGraphConfigurationView(final GraphConfiguration configuration, final Job<?, ?> job,
                                         final String pluginName, final HistoryProvider buildHistory, final String url) {
        super(configuration, job, pluginName, buildHistory);
        this.url = url;

        configuration.initializeFromFile(createDefaultsFile(job, pluginName));
    }

    /**
     * Creates a new instance of {@link hudson.plugins.analysis.graph.DefaultGraphConfigurationView}.
     *
     * @param configuration
     *            the graph configuration
     * @param job
     *            the owning job to configure the graphs for
     * @param pluginName
     *            The name of the plug-in.
     * @param buildHistory
     *            the build history for this job
     */
    public DefaultGraphConfigurationView(final GraphConfiguration configuration, final Job<?, ?> job,
                                         final String pluginName, final HistoryProvider buildHistory) {
        this(configuration, job, pluginName, buildHistory,
                job.getAbsoluteUrl() + pluginName + "/configureDefaults");
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
    protected void persistValue(final String value, final String pluginName, final StaplerRequest request, final StaplerResponse response) throws FileNotFoundException, IOException {
        FileOutputStream output = new FileOutputStream(createDefaultsFile(getOwner(), pluginName));
        try {
            IOUtils.write(value, output);
        }
        finally {
            output.close();
        }
    }
}

