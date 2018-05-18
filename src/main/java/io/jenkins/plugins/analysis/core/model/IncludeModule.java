package io.jenkins.plugins.analysis.core.model;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;

import hudson.Extension;

/**
 * Defines a filter criteria for {@link Report}.
 *
 * @author Ulli Hafner
 */
public class IncludeModule extends IssuesFilter {
    private static final long serialVersionUID = -7987177949039781041L;

    /**
     * Creates a new instance of {@link IncludeModule}.
     */
    @DataBoundConstructor
    public IncludeModule() {
        // Required for Stapler
    }

    @Override
    public void apply(final IssueFilterBuilder builder, final String pattern) {
        builder.setIncludeModuleNameFilter(pattern);
    }

    /**
     * Dummy descriptor for {@link IncludeModule}.
     *
     * @author Ulli Hafner
     */
    @Extension
    public static class DescriptorImpl extends IncludeFilterDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Include_Module();
        }
    }
}