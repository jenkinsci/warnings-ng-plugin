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
    @Extension
    @Symbol({"includePackage", "includeNamespace"})
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Include_Package();
        }
    }
}