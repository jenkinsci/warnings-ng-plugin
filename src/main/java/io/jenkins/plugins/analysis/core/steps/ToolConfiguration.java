package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.Tool;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Defines the configuration to parse a set of files using a predefined parser.
 *
 * @author Ullrich Hafner
 * @deprecated used to deserialize pre beta-6 configurations
 */
@Deprecated
public class ToolConfiguration extends AbstractDescribableImpl<ToolConfiguration> {
    private final ReportScanningTool tool;
    private final String pattern;
    private String id = StringUtils.EMPTY;
    private String name = StringUtils.EMPTY;

    /**
     * Creates a new instance of {@link ToolConfiguration}.
     *
     * @param tool
     *         the ID of the tool to use
     */
    public ToolConfiguration(final ReportScanningTool tool) {
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
    public ToolConfiguration(final ReportScanningTool tool, final String pattern) {
        super();

        this.pattern = pattern;
        this.tool = tool;
    }

    /**
     * Returns the static analysis tool that will scan files and create issues.
     *
     * @return the tool
     */
    public ReportScanningTool getTool() {
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
     * given, then the ID of the associated {@link Tool} is used.
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
     * Defines the name of the results. The name is used for all labels in the UI. If no name is given, then the name of
     * the associated {@link StaticAnalysisLabelProvider} of the {@link Tool} is used.
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
    }
}

