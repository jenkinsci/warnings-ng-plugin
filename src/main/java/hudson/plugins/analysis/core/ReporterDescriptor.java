package hudson.plugins.analysis.core;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.util.FormValidation;

/**
 * A maven reporter descriptor that uses a publisher descriptor as delegate to
 * obtain the relevant configuration data.
 *
 * @author Ulli Hafner
 */
public abstract class ReporterDescriptor extends MavenReporterDescriptor {
    /** Descriptor of the publisher. */
    private final PluginDescriptor publisherDescriptor;

    /**
     * Creates a new instance of <code>ReporterDescriptor</code>.
     *
     * @param clazz class of the reporter
     * @param pluginDescriptor the plug-in descriptor of the publisher
     */
    public ReporterDescriptor(final Class<? extends MavenReporter> clazz, final PluginDescriptor pluginDescriptor) {
        super(clazz);
        publisherDescriptor = pluginDescriptor;
    }

    /** {@inheritDoc} */
    @Override
    public final String getDisplayName() {
        return publisherDescriptor.getDisplayName();
    }

    /** {@inheritDoc} */
    @Override
    public final String getHelpFile() {
        return publisherDescriptor.getPluginRoot() + "help-m2.html";
    }

    /**
     * Gets the publisher descriptor.
     *
     * @return the publisher descriptor
     */
    public final PluginDescriptor getPublisherDescriptor() {
        return publisherDescriptor;
    }

    /**
     * Performs on-the-fly validation on the annotations threshold.
     *
     * @param threshold
     *            the threshold
     * @return the form validation
     */
    public final FormValidation doCheckThreshold(@QueryParameter final String threshold) {
        return publisherDescriptor.doCheckThreshold(threshold);
    }

    /**
     * Performs on-the-fly validation on the trend graph height.
     *
     * @param height
     *            the height
     * @return the form validation
     */
    public final FormValidation doCheckHeight(@QueryParameter final String height) {
        return publisherDescriptor.doCheckHeight(height);
    }

    /** {@inheritDoc} */
    @Override
    public MavenReporter newInstance(final StaplerRequest req, final JSONObject formData)
            throws hudson.model.Descriptor.FormException {
        return super.newInstance(req, PluginDescriptor.convertHierarchicalFormData(formData));
    }
}
