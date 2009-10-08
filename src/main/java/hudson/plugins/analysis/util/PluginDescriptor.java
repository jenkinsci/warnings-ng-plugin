package hudson.plugins.analysis.util;

import hudson.FilePath;
import hudson.maven.AbstractMavenProject;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

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
     * Performs on-the-fly validation on the character encoding.
     *
     * @param defaultEncoding
     *            the character encoding
     * @return the validation result
     */
    public final FormValidation doCheckDefaultEncoding(@QueryParameter final String defaultEncoding) {
        try {
            return new EncodingValidator().check(defaultEncoding);
        }
        catch (FormValidation exception) {
            return exception;
        }
    }

    /**
     * Performs on-the-fly validation on the file name pattern.
     *
     * @param project
     *            the project
     * @param pattern
     *            the file pattern
     * @return the validation result
     */
    public final FormValidation doCheckPattern(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String pattern) throws IOException {
        return FilePath.validateFileMask(project.getSomeWorkspace(), pattern);
    }

    /**
     * Performs on-the-fly validation on the annotations threshold.
     *
     * @param threshold
     *            the character encoding
     * @return the validation result
     */
    public final FormValidation doCheckThreshold(@QueryParameter final String threshold) {
        try {
            return new ThresholdValidator().check(threshold);
        }
        catch (FormValidation exception) {
            return exception;
        }
    }

    /**
     * Performs on-the-fly validation on the trend graph height.
     *
     * @param height
     *            the height
     * @return the form validation
     */
    public FormValidation doCheckHeight(@QueryParameter final String height) {
        return GraphConfigurationDetail.checkHeight(height);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return !AbstractMavenProject.class.isAssignableFrom(jobType);
    }
}
