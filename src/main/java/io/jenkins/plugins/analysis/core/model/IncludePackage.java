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
public class IncludePackage extends IssuesFilter {
    private static final long serialVersionUID = -168542391859856306L;

    /**
     * Creates a new instance of {@link IncludePackage}.
     */
    @DataBoundConstructor
    public IncludePackage() {
        super();
        // Required for Stapler
    }

    @Override
    public void apply(final IssueFilterBuilder builder, final String pattern) {
        builder.setIncludePackageNameFilter(pattern);
    }

    /**
     * Dummy descriptor for {@link IncludePackage}.
     *
     * @author Ulli Hafner
     */
    @Extension
    public static class DescriptorImpl extends IncludeFilterDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Include_Package();
        }
    }
}