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
public class ExcludeType extends IssuesFilter {
    private static final long serialVersionUID = -9215604002784734848L;

    /**
     * Creates a new instance of {@link ExcludeType}.
     */
    @DataBoundConstructor
    public ExcludeType() {
        super();
        // Required for Stapler
    }

    @Override
    public void apply(final IssueFilterBuilder builder, final String pattern) {
        builder.setExcludeTypeFilter(pattern);
    }

    /**
     * Dummy descriptor for {@link ExcludeType}.
     *
     * @author Ulli Hafner
     */
    @Extension
    public static class DescriptorImpl extends IncludeFilterDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_Type();
        }
    }
}