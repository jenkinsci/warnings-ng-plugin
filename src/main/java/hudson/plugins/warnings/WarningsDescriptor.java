package hudson.plugins.warnings;

import hudson.model.AbstractProject;
import hudson.plugins.warnings.util.PluginDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for the class {@link WarningsPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
public final class WarningsDescriptor extends PluginDescriptor {
    /** Plug-in name. */
    private static final String PLUGIN_NAME = "warnings";
    /** Icon to use for the result and project action. */
    private static final String ACTION_ICON = "/plugin/warnings/icons/warnings-24x24.png";

    /**
     * Instantiates a new find bugs descriptor.
     */
    WarningsDescriptor() {
        super(WarningsPublisher.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return Messages.Warnings_Publisher_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public String getIconUrl() {
        return ACTION_ICON;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public WarningsPublisher newInstance(final StaplerRequest request, final JSONObject formData) throws FormException {
        return request.bindParameters(WarningsPublisher.class, PLUGIN_NAME + ".");
    }
}