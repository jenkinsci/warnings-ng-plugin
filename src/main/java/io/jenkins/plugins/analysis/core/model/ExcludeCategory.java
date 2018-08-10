package io.jenkins.plugins.analysis.core.model;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;

import hudson.Extension;

/**
 * Defines a filter criteria for {@link Report}.
 *
 * @author Ullrich Hafner
 */
public class ExcludeCategory extends IssuesFilter {
    private static final long serialVersionUID = 8704648332922985878L;

    /**
     * Creates a new instance of {@link ExcludeCategory}.
     */
    @DataBoundConstructor
    public ExcludeCategory() {
        super();
        // Required for Stapler
    }

    @Override
    public void apply(final IssueFilterBuilder builder, final String pattern) {
        builder.setExcludeCategoryFilter(pattern);
    }

    /**
     * Dummy descriptor for {@link ExcludeCategory}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    public static class DescriptorImpl extends IncludeFilterDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_Category();
        }
    }
}