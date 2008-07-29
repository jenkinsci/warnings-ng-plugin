package hudson.plugins.warnings.util;

import hudson.maven.AbstractMavenProject;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Base class for a Hudson plug/in descriptor.
 *
 * @author Ulli Hafner
 */
public abstract class PluginDescriptor extends BuildStepDescriptor<Publisher> {
    /**
     * Creates a new instance of <code>PluginDescriptor</code>.
     *
     * @param clazz
     *            the type of the publisher
     */
    public PluginDescriptor(final Class<? extends Publisher> clazz) {
        super(clazz);
    }

    /** {@inheritDoc} */
    @Override
    public final String getHelpFile() {
        return getPluginRoot() + "help.html";
    }

    /**
     * Returns the root folder of this plug-in.
     *
     * @return the name of the root folder of this plug-in
     */
    public String getPluginRoot() {
        return "/plugin/" + getPluginName() + "/";
    }

    /**
     * Returns the name of the plug-in.
     *
     * @return the name of the plug-in
     */
    public final String getPluginResultUrlName() {
        return getPluginName() + "Result";
    }

    /**
     * Returns the name of the plug-in.
     *
     * @return the name of the plug-in
     */
    public abstract String getPluginName();

    /**
     * Returns the URL of the plug-in icon (24x24 image).
     *
     * @return the URL of the plug-in icon
     */
    public abstract String getIconUrl();

    /**
     * Performs on-the-fly validation on the file mask.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public final void doCheckPattern(final StaplerRequest request, final StaplerResponse response) throws IOException, ServletException {
        new FormFieldValidator.WorkspaceFileMask(request, response).process();
    }

    /**
     * Performs on-the-fly validation on the annotations threshold.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public final void doCheckThreshold(final StaplerRequest request, final StaplerResponse response) throws IOException, ServletException {
        new ThresholdValidator(request, response).process();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return !AbstractMavenProject.class.isAssignableFrom(jobType);
    }
}
