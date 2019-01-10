package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link AnnotatedReport}.
 *
 * @author Ullrich Hafner
 */
class AnnotatedReportTest {
    private static final String ID = "id";

    @Test
    void shouldCreateEmptyReport() {
        AnnotatedReport report = new AnnotatedReport(ID);

        assertThat(report.getId()).isEqualTo(ID);
        assertThat(report.size()).isZero();
    }
}