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
        builder.setExcludeFilenameFilter(getPattern());
    }

    /**
     * Descriptor for {@link ExcludeFile}.
     *
     * @author Ullrich Hafner
     */
    @Extension @Symbol("excludeFile")
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_File();
        }
    }
}