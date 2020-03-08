package io.jenkins.plugins.analysis.core.steps;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import io.jenkins.plugins.analysis.core.model.Tool;

/**
 * Proxy to a static analysis tool.
 *
 * @author Ullrich Hafner
 */
public class ToolProxy extends AbstractDescribableImpl<ToolProxy> {
    private final Tool tool;

    /**
     * Creates a new instance of {@link ToolProxy}.
     *
     * @param tool
     *         the ID of the tool to use
     */
    @DataBoundConstructor
    public ToolProxy(final Tool tool) {
        super();

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
     * Dummy descriptor for {@link ToolProxy}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ToolProxy> {
        @NonNull
        @Override
        public String getDisplayName() {
            return StringUtils.EMPTY;
        }
    }
}

