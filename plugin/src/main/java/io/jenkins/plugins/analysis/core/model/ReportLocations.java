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
     * @return the affected file locations
     */
    public FileLocations toFileLocations(final Report report) {
        FileLocations fileLocations = new FileLocations();
        report.stream().forEach(i -> fileLocations.addLine(i.getFileName(), i.getLineStart()));
        return fileLocations;
    }

    /**
     * Returns the affected file locations in the report.
     *
     * @param report
     *         the report to get the affected files from
     * @param workspace
     *         the workspace to get the SCM repository from
     *
     * @return the affected file locations
     * @deprecated use {@link #toFileLocations(Report)}
     */
    @Deprecated
    public FileLocations toFileLocations(final Report report, @SuppressWarnings("unused") final String workspace) {
        return toFileLocations(report);
    }
}
