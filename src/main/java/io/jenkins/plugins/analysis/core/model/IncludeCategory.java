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
public class IncludeCategory extends IssuesFilter {
    private static final long serialVersionUID = -3109697929021646731L;

    /**
     * Creates a new instance of {@link IncludeCategory}.
     */
    @DataBoundConstructor
    public IncludeCategory() {
        super();
        // Required for Stapler
    }

    @Override
    public void apply(final IssueFilterBuilder builder, final String pattern) {
        builder.setIncludeCategoryFilter(pattern);
    }

    /**
     * Dummy descriptor for {@link IncludeCategory}.
     *
     * @author Ulli Hafner
     */
    @Extension
    public static class DescriptorImpl extends IncludeFilterDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Include_Category();
        }
    }
}