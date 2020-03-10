package io.jenkins.plugins.analysis.core.filter;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Defines a filter criteria for a {@link Report}.
 *
 * @author Ullrich Hafner
 */
public class ExcludePackage extends RegexpFilter {
    private static final long serialVersionUID = 6618489619156239466L;

    /**
     * Creates a new instance of {@link ExcludePackage}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public ExcludePackage(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setExcludePackageNameFilter(getPattern());
    }

    /**
     * Descriptor for {@link ExcludePackage}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    @Symbol({"excludePackage", "excludeNamespace"})
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_Package();
        }
    }
}