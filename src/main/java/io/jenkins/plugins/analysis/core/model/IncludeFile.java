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
public class IncludeFile extends IssuesFilter {
    /**
     * Creates a new instance of {@link IncludeFile}.
     */
    @DataBoundConstructor
    public IncludeFile() {
        // Required for Stapler
    }

    @Override
    public void apply(final IssueFilterBuilder builder, final String pattern) {
        builder.setIncludeFilenameFilter(pattern);
    }

    /**
     * Dummy descriptor for {@link IncludeFile}.
     *
     * @author Ulli Hafner
     */
    @Extension
    public static class DescriptorImpl extends IncludeFilterDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Include_File();
        }
    }
}