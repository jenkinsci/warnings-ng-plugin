package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.Tool;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Defines the configuration to parse a set of files using a predefined parser.
 *
 * @author Ullrich Hafner
 */
public class ToolConfiguration extends AbstractDescribableImpl<ToolConfiguration> {
    private final Tool tool;

    /**
     * Creates a new instance of {@link ToolConfiguration}.
     *
     * @param tool
     *         the ID of the tool to use
     */
    @DataBoundConstructor
    public ToolConfiguration(final Tool tool) {
        this.tool = tool;
    }

    /**
     * Returns the static analysis tool that will scan files and create issues.
     *
     * @return the tool
     */
    public Tool getTool() {
        return tool;
    }

    @Override
    public String toString() {
        return tool.getActualName();
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

