package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

import static io.jenkins.plugins.forensics.blame.FileLocationsAssert.*;

/**
 * Tests the class {@link ReportLocations}.
 *
 * @author Ullrich Hafner
 */
class ReportLocationsTest {
    @Test
    void shouldConvertEmptyReport() {
        Report report = new Report();

        ReportLocations reportLocations = new ReportLocations(report);

        assertThat(reportLocations.toFileLocations()).isEmpty();
    }
}
