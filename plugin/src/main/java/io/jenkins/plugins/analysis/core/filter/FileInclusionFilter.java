package io.jenkins.plugins.analysis.core.filter;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.jenkinsci.Symbol;

import java.io.Serial;
import java.io.Serializable;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * A filter that restricts static analysis issues to only those contained within a specific list of files.
 *
 * <p>
 * This is particularly useful for CI pipelines where you want to report issues only for files
 * changed in a specific Git patch, avoiding the character limits associated with long
 * regular expression strings.
 * </p>
 *
 * @author Your Name
 */
public class FileInclusionFilter extends AbstractDescribableImpl<FileInclusionFilter> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1643462711241633469L;

    /**
     * The path to the text file containing the list of files to be included.
     * Each line in this file should represent a relative or absolute path
     * to a file that is allowed to have reported issues.
     */
    private final String fileName;

    @Override
    public Descriptor<FileInclusionFilter> getDescriptor() {
        return new DescriptorImpl();
    }

    /**
     * Creates a new instance of {@link FileInclusionFilter}.
     *
     * @param fileName
     * the path to the file containing the list of allowed file names (one per line).
     * Note: If running on a distributed Jenkins setup, this path must be accessible
     * on the controller or handled via FilePath.
     */
    public FileInclusionFilter(final String fileName) {
        super();
        this.fileName = fileName;
    }

    /**
     * Returns the path to the file containing the inclusion list.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Descriptor for {@link FileInclusionFilter}.
     */
    @Extension
    @Symbol("fileInclusionFilter")
    public static class DescriptorImpl extends Descriptor<FileInclusionFilter> {
        @Override
        @NonNull
        public String getDisplayName() {
            return "Include only files listed in file";
        }
    }
}
