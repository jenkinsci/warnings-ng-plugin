package hudson.plugins.analysis.util;

import hudson.model.AbstractProject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Configures the default values for the trend graph of this plug-in.
 */
public class DefaultGraphConfigurationDetail extends GraphConfigurationDetail {
    /**
     * Creates a new instance of {@link DefaultGraphConfigurationDetail}.
     *
     * @param project
     *            the owning project to configure the graphs for
     * @param fileName
     *            the file name that is used to persist the configuration
     */
    public DefaultGraphConfigurationDetail(final AbstractProject<?, ?> project, final String fileName) {
        super(project, fileName, StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of {@link DefaultGraphConfigurationDetail}.
     *
     * @param project
     *            the owning project to configure the graphs for
     * @param fileName
     *            the file name that is used to persist the configuration
     * @param lastAction
     *            the last valid action for this project
     */
    public DefaultGraphConfigurationDetail(final AbstractProject<?, ?> project, final String fileName, final ResultAction<?> lastAction) {
        super(project, fileName, StringUtils.EMPTY, lastAction);
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

