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
public class ExcludePackage extends IssuesFilter {
    private static final long serialVersionUID = 6618489619156239466L;

    /**
     * Creates a new instance of {@link ExcludePackage}.
     */
    @DataBoundConstructor
    public ExcludePackage() {
        super();
        // Required for Stapler
    }

    @Override
    public void apply(final IssueFilterBuilder builder, final String pattern) {
        builder.setExcludePackageNameFilter(pattern);
    }

    /**
     * Dummy descriptor for {@link ExcludePackage}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    public static class DescriptorImpl extends IncludeFilterDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_Package();
        }
    }
}