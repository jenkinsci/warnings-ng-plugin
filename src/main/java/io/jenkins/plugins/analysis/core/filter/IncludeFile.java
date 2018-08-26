package io.jenkins.plugins.analysis.core.filter;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import io.jenkins.plugins.analysis.core.model.Messages;

import hudson.Extension;
import hudson.model.Descriptor;

/**
 * Defines a filter criteria for a {@link Report}.
 *
 * @author Ullrich Hafner
 */
public class IncludeFile extends RegexpFilter {
    private static final long serialVersionUID = 6549206934593163281L;

    /**
     * Creates a new instance of {@link IncludeFile}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public IncludeFile(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setIncludeFilenameFilter(getPattern());
    }

    /**
     * Descriptor for {@link IncludeFile}.
     *
     * @author Ullrich Hafner
     */
    @Extension @Symbol("includeFile")
    public static class DescriptorImpl extends Descriptor<RegexpFilter> {
        @Nonnull
        @Override
        public String getDisplayName () {
            return Messages.Filter_Include_File();
        }
    }
}