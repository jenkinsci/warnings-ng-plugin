package io.jenkins.plugins.analysis.core.filter;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import io.jenkins.plugins.analysis.core.model.Messages;

import hudson.Extension;

/**
 * Defines a filter criteria for {@link Report}.
 *
 * @author Ullrich Hafner
 */
public class IncludePackage extends RegexpFilter {
    private static final long serialVersionUID = -168542391859856306L;

    /**
     * Creates a new instance of {@link IncludePackage}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public IncludePackage(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setIncludePackageNameFilter(getPattern());
    }

    /**
     * Descriptor for {@link IncludePackage}.
     *
     * @author Ullrich Hafner
     */
    @Extension @Symbol({"includePackage", "includeNamespace"})
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Include_Package();
        }
    }
}