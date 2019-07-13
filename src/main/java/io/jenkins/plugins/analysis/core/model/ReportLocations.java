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
     * @param report
     *         the report to get the affected files from
     * @param fileLocations
     *         the fileLocations that will be filled with the affected lines
     */
    public void toFileLocations(final Report report, final FileLocations fileLocations) {
        report.stream().forEach(i -> fileLocations.addLine(i.getFileName(), i.getLineStart()));
    }
}
