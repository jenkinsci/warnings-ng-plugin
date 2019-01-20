package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Report;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * A tool that can produce a {@link Report report of issues} in some way. If your tool produces issues by scanning a
 * compiler log or static analysis report file, consider deriving from {@link ReportScanningTool}.
 *
 * @author Ullrich Hafner
 * @see ReportScanningTool
 */
public class ToolSelection extends AbstractDescribableImpl<ToolSelection> {
    private String id = StringUtils.EMPTY;

    /** Creates a new instance of {@link ToolSelection}. */
    @DataBoundConstructor
    public ToolSelection() {
        super();
        // empty constructor required for stapler
    }

    /**
     * Selects the ID of the static analysis results.
     *
     * @param id
     *         the ID of the static analysis results
     */
    @DataBoundSetter
    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public ToolSelectionDescriptor getDescriptor() {
        return (ToolSelectionDescriptor) super.getDescriptor();
    }

    /** Descriptor for {@link ToolSelection}. **/
    @Extension
    public static class ToolSelectionDescriptor extends Descriptor<ToolSelection> {
        // empty constructor required for stapler
    }
}
