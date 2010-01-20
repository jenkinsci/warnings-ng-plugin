package hudson.plugins.analysis.graph;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractProject;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Configures the default values for the trend graph of this plug-in.
 */
public class DefaultGraphConfigurationView extends GraphConfigurationView {
    /**
     * Creates a new instance of {@link DefaultGraphConfigurationView}.
     *
     * @param configuration
     *            the graph configuration
     * @param project
     *            the owning project to configure the graphs for
     * @param pluginName
     *            The name of the plug-in.
     */
    public DefaultGraphConfigurationView(final GraphConfiguration configuration, final AbstractProject<?, ?> project,
            final String pluginName) {
        super(configuration, project, pluginName);

        initialize(configuration, project, pluginName);
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
     * @param lastAction
     *            the last valid action for this project
     */
    public DefaultGraphConfigurationView(final GraphConfiguration configuration, final AbstractProject<?, ?> project,
            final String pluginName, final ResultAction<?> lastAction) {
        super(configuration, project, pluginName, lastAction);

        initialize(configuration, project, pluginName);
    }

    /**
     * Initializes the configuration values from the default value file.
     *
     * @param configuration
     *            the configuration
     * @param project
     *            the owning project to configure the graphs for
     * @param pluginName
     *            The name of the plug-in. Also used as the suffix of the cookie
     *            name that is used to persist the configuration per user.
     */
    private void initialize(final GraphConfiguration configuration,
            final AbstractProject<?, ?> project, final String pluginName) {
        configuration.initializeFromFile(createDefaultsFile(project, pluginName));
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.DefaultGraphConfiguration_Name();
    }

    /** {@inheritDoc} */
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
        return getRootUrl() + "/configureDefaults";
    }

    /** {@inheritDoc} */
    @Override
    protected void persistValue(final String value, final StaplerRequest request, final StaplerResponse response) throws FileNotFoundException, IOException {
        FileOutputStream output = new FileOutputStream(createDefaultsFile(getOwner(), getPluginName()));
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

