package hudson.plugins.analysis.core;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.FilePath;

import hudson.model.AbstractProject;
import hudson.model.Hudson;

import hudson.plugins.analysis.graph.GraphConfiguration;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.plugins.analysis.util.ThresholdValidator;

import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

/**
 * Base class for a Jenkins plug-in descriptor.
 *
 * @author Ulli Hafner
 */
public abstract class PluginDescriptor extends BuildStepDescriptor<Publisher> {
    /** Suffix of the URL of the plug-in result. */
    protected static final String RESULT_URL_SUFFIX = "Result";
    private static final String NEW_SECTION_KEY = "canComputeNew";

    /**
     * Returns the result URL for the specified plug-in.
     *
     * @param pluginName
     *            the name of the plug-in
     * @return the result URL of the plug-in
     */
    public static String createResultUrlName(final String pluginName) {
        return pluginName + RESULT_URL_SUFFIX;
    }

    /**
     * Returns whether the specified plug-in is installed.
     *
     * @param shortName
     *            the plug-in to check
     * @return <code>true</code> if the specified plug-in is installed,
     *         <code>false</code> if not.
     */
    public static boolean isPluginInstalled(final String shortName) {
        Hudson instance = Hudson.getInstance();
        if (instance != null) {
            return instance.getPlugin(shortName) != null;
        }
        return true;
    }

    /**
     * Converts the hierarchical JSON object that contains a sub-section for
     * {@value #NEW_SECTION_KEY} to a corresponding flat JSON object.
     *
     * @param hierarchical
     *            the JSON object containing a sub-section
     * @return the flat structure
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("WMI")
    protected static JSONObject convertHierarchicalFormData(final JSONObject hierarchical) {
        if (hierarchical.containsKey(NEW_SECTION_KEY)) {
            JSONObject newSection = hierarchical.getJSONObject(NEW_SECTION_KEY);

            JSONObject output = JSONObject.fromObject(hierarchical);
            output.remove(NEW_SECTION_KEY);
            for (Object key : newSection.keySet()) {
                output.element((String)key, newSection.get(key));
            }
            output.element(NEW_SECTION_KEY, true);

            return output;
        }
        else {
            return hierarchical;
        }
    }

    /**
     * Creates a new instance of <code>PluginDescriptor</code>.
     *
     * @param clazz
     *            the type of the publisher
     */
    public PluginDescriptor(final Class<? extends Publisher> clazz) {
        super(clazz);
    }

    @Override
    public Publisher newInstance(final StaplerRequest req, final JSONObject formData)
            throws hudson.model.Descriptor.FormException {
        return super.newInstance(req, convertHierarchicalFormData(formData));
    }

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
        return createResultUrlName(getPluginName());
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
     * Returns the URL of the build summary icon (48x48 image).
     *
     * @return the URL of the plug-in icon
     * @since 1.41
     */
    public String getSummaryIconUrl() {
        return StringUtils.EMPTY;
    }

    /**
     * Performs on-the-fly validation on the character encoding.
     *
     * @param defaultEncoding
     *            the character encoding
     * @return the validation result
     */
    public FormValidation doCheckDefaultEncoding(@QueryParameter final String defaultEncoding) {
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
     * @throws IOException
     *             if the encoding is not valid
     */
    public FormValidation doCheckPattern(@AncestorInPath final AbstractProject<?, ?> project,
            @QueryParameter final String pattern) throws IOException {
        return FilePath.validateFileMask(project.getSomeWorkspace(), pattern);
    }

    /**
     * Performs on-the-fly validation on the annotations threshold.
     *
     * @param threshold
     *            the character encoding
     * @return the validation result
     */
    public FormValidation doCheckThreshold(@QueryParameter final String threshold) {
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
        return GraphConfiguration.checkHeight(height);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return !(isPluginInstalled("maven-plugin") && MavenProjectChecker.isMavenProject(jobType));
    }
}
