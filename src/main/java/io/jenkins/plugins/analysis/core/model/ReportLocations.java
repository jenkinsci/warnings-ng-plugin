package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.forensics.blame.FileLocations;

/**
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class ReportLocations {
    private final Report report;

    public ReportLocations(final Report report) {

        this.report = report;
    }

    public FileLocations toFileLocations() {
        return new FileLocations();
    }
}
