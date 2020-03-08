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
public class ExcludeMessage extends RegexpFilter {
    private static final long serialVersionUID = 6248933081535800869L;

    /**
     * Creates a new instance of {@link ExcludeMessage}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public ExcludeMessage(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setExcludeMessageFilter(getPattern());
    }

    /**
     * Descriptor for {@link ExcludeMessage}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    @Symbol("excludeMessage")
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_Message();
        }
    }
}