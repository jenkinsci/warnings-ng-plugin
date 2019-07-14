package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;

import io.jenkins.plugins.forensics.blame.FileLocations;
import io.jenkins.plugins.forensics.blame.FileLocations.FileSystem;

/**
 * Extracts all affected files of a {@link Report} and adds them to a new {@link FileLocations} instance.
 *
 * @author Ullrich Hafner
 */
public class ReportLocations {
    /**
     * Returns the affected file locations in the report.
     *
     * @param report
     *         the report to get the affected files from
     * @param workspace
     *         the workspace to get the SCM repository from
     *
     * @return the affected file locations
     */
    public FileLocations toFileLocations(final Report report, final String workspace) {
        return toFileLocations(report, workspace, new FileSystem());
    }

    @VisibleForTesting
    FileLocations toFileLocations(final Report report, final String workspace, final FileSystem fileSystem) {
        FileLocations fileLocations = new FileLocations(workspace, fileSystem);
        report.stream().forEach(i -> fileLocations.addLine(i.getFileName(), i.getLineStart()));
        return fileLocations;
    }
}
