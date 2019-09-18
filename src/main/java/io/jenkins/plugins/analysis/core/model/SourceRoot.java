package io.jenkins.plugins.analysis.core.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Defines the properties of a warnings parser that uses a Groovy script to parse the warnings log.
 *
 * @author Ullrich Hafner
 */
public class SourceRoot extends AbstractDescribableImpl<SourceRoot> implements Serializable {
    private static final long serialVersionUID = -3864564528382064924L;
    private final String folderName;

    /**
     * Creates a new instance of {@link SourceRoot}.
     *
     * @param folderName
     *         the name of the folder
     */
    @DataBoundConstructor
    public SourceRoot(final String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

    /**
     * Descriptor to validate {@link SourceRoot}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<SourceRoot> {
        @NonNull
        @Override
        public String getDisplayName() {
            return StringUtils.EMPTY;
        }
    }
}

