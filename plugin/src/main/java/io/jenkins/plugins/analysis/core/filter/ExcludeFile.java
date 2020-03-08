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
public class ExcludeFile extends RegexpFilter {
    private static final long serialVersionUID = 7647146296108359942L;

    /**
     * Creates a new instance of {@link ExcludeFile}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public ExcludeFile(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setExcludeFileNameFilter(getPattern());
    }

    /**
     * Descriptor for {@link ExcludeFile}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    @Symbol("excludeFile")
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_File();
        }
    }
}