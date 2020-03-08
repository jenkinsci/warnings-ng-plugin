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
public class IncludeMessage extends RegexpFilter {
    private static final long serialVersionUID = 6374887110498122543L;

    /**
     * Creates a new instance of {@link IncludeMessage}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public IncludeMessage(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setIncludeMessageFilter(getPattern());
    }

    /**
     * Descriptor for {@link IncludeMessage}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    @Symbol("includeMessage")
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Include_Message();
        }
    }
}