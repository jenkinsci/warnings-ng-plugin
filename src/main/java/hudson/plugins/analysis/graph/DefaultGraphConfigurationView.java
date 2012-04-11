package hudson.plugins.analysis.graph;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractProject;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildHistory;

/**
 * Configures the default values for the trend graph of this plug-in.
 */
public class DefaultGraphConfigurationView extends GraphConfigurationView {
    private final String url;

    /**
     * Creates a new instance of {@link DefaultGraphConfigurationView}.
     *
     * @param configuration
     *            the graph configuration
     * @param project
     *            the owning project to configure the graphs for
     * @param pluginName
     *            The URL of the project action (there might be a one to many mapping to this defaults view)
     * @param buildHistory
     *            the build history for this project
     * @param url
     *            The URL of this view
     */
    public DefaultGraphConfigurationView(final GraphConfiguration configuration, final AbstractProject<?, ?> project,
            final String pluginName, final BuildHistory buildHistory, final String url) {
        super(configuration, project, pluginName, buildHistory);
        this.url = url;

        configuration.initializeFromFile(createDefaultsFile(project, pluginName));
    }

    /**
     * Creates a new instance of {@link DefaultGraphConfigurationView}.
     *
     * @param configuration
     *            the graph configuration
     * @param project
     *            the owning project to configure the graphs for
     * @param pluginName
     *            The name of the plug-in.
     * @param buildHistory
     *            the build history for this project
     */
    public DefaultGraphConfigurationView(final GraphConfiguration configuration, final AbstractProject<?, ?> project,
            final String pluginName, final BuildHistory buildHistory) {
        this(configuration, project, pluginName, buildHistory,
                project.getAbsoluteUrl() + pluginName + "/configureDefaults");
    }

    /** {@inheritDoc} */
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
        FileOutputStream output = new FileOutputStream(createDefaultsFile(getOwner(), getKey()));
        try {
            IOUtils.write(value, output);
        }
        finally {
            if (output != null) {
                output.close();
            }
        }
    }
}

