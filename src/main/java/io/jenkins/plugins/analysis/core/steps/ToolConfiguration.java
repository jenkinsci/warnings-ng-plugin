package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.Nonnull;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

/**
 * Defines the configuration to parse a set of files using a predefined parser.
 *
 * @author Ullrich Hafner
 */
public class ToolConfiguration extends AbstractDescribableImpl<ToolConfiguration> {
    private final StaticAnalysisTool tool;
    private final String pattern;
    private String id = StringUtils.EMPTY;
    private String name = StringUtils.EMPTY;

    /**
     * Creates a new instance of {@link ToolConfiguration}.
     *
     * @param tool
     *         the ID of the tool to use
     */
    public ToolConfiguration(final StaticAnalysisTool tool) {
        this(tool, StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of {@link ToolConfiguration}.
     *
     * @param tool
     *         the ID of the tool to use
     * @param pattern
     *         the pattern of files to parse
     */
    @DataBoundConstructor
    public ToolConfiguration(final StaticAnalysisTool tool, final String pattern) {
        super();

        this.pattern = pattern;
        this.tool = tool;
    }

    /**
     * Returns the static analysis tool that will scan files and create issues.
     *
     * @return the tool
     */
    public StaticAnalysisTool getTool() {
        return tool;
    }

    /**
     * Returns the Ant file-set pattern of files to work with. If the pattern is undefined then the console log is
     * scanned.
     *
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Defines the ID of the results. The ID is used as URL of the results and as name in UI elements. If no ID is
     * given, then the ID of the associated {@link StaticAnalysisTool} is used.
     *
     * @param id
     *         the ID of the results
     * @see #getTool() 
     */
    @DataBoundSetter
    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns whether a user defined ID has been set. This ID will then override the default ID.
     * 
     * @return returns {@code true} if a user defined ID is present, {@code false} otherwise
     */
    public boolean hasId() {
        return StringUtils.isNotBlank(id);
    }

    /**
     * Defines the name of the results. The name is used for all labels in the UI. If no name is given, then the name of
     * the associated {@link StaticAnalysisLabelProvider} of the {@link StaticAnalysisTool} is used.
     *
     * @param name
     *         the name of the results
     * @see #getTool() 
     */
    @DataBoundSetter
    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the actual name of the tool. If no user defined name is given, then the default name is returned.
     * 
     * @return the name
     */
    public String getActualName() {
        return StringUtils.defaultIfBlank(name, getTool().getName());
    }

    /**
     * Returns whether a user defined name has been set. This name will then override the default name.
     *
     * @return returns {@code true} if a user defined name is present, {@code false} otherwise
     */
    public boolean hasName() {
        return StringUtils.isNotBlank(name);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", tool.getName(), pattern);
    }

    /**
     * Dummy descriptor for {@link ToolConfiguration}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ToolConfiguration> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return StringUtils.EMPTY;
        }

        /**
         * Performs on-the-fly validation on the ant pattern for input files.
         *
         * @param project
         *         the project
         * @param pattern
         *         the file pattern
         *
         * @return the validation result
         */
        public FormValidation doCheckPattern(@AncestorInPath final AbstractProject<?, ?> project,
                @QueryParameter final String pattern) {
            if (project != null) { // there is no workspace in pipelines
                try {
                    FilePath workspace = project.getSomeWorkspace();
                    if (workspace != null && workspace.exists()) {
                        return validatePatternInWorkspace(pattern, workspace);
                    }
                }
                catch (InterruptedException | IOException ignore) {
                    // ignore and return ok
                }
            }

            return FormValidation.ok();
        }

        private FormValidation validatePatternInWorkspace(final @QueryParameter String pattern,
                final FilePath workspace) throws IOException, InterruptedException {
            String result = workspace.validateAntFileMask(pattern, FilePath.VALIDATE_ANT_FILE_MASK_BOUND);
            if (result != null) {
                return FormValidation.error(result);
            }
            return FormValidation.ok();
        }
    }
}

