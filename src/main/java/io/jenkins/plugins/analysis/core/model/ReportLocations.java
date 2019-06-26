package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.forensics.blame.FileLocations;

/**
 * Extracts all affected files of a {@link Report} and adds them to a new {@link FileLocations} instance.
 *
 * @author Ullrich Hafner
 */
public class ReportLocations {
    /**
     * Returns the affected file locations in the report.
     *
     * @param workspace
     *         the workspace that contains the affected files
     * @param report
     *         the report to get the affected files from
     *
     * @param fileLocations
     * @return the file locations
     */
    public FileLocations toFileLocations(final String workspace, final Report report,
            final FileLocations fileLocations) {
        fileLocations.setWorkspace(workspace);
        report.stream().forEach(i -> fileLocations.addLine(i.getFileName(), i.getLineStart()));
        return fileLocations;
    }
}
