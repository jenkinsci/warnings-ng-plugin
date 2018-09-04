package io.jenkins.plugins.analysis.core.quality;

import java.util.Map;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;
import org.jvnet.localizer.Localizable;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.HealthReport;

/**
 * Test {@link HealthReportBuilder}.
 *
 * @author Alexandra Wenzel
 */
@SuppressWarnings("ALL")
class HealthReportBuilderTest {
    /** Description message. */
    private static final String DESCRIPTION_MESSAGE = "Static Analysis: ";
    private static final String DESCRIPTION_MESSAGE_ENDING_PLURAL = " warnings found.";
    private static final String DESCRIPTION_MESSAGE_ENDING_SINGULAR = " warning found.";
    private static final String HEALTH_REPORT_MESSAGE = "Healthy Message";

    /**
     * Tests whether we evaluate correctly to a 50% health.
     */
    @Test
    void shouldTest50Health() {
        HealthReport reportHighPriority = createTestHealthReport(4, 16, Severity.WARNING_HIGH, 10, 20, 30, 0);
        HealthReport reportNormalPriority = createTestHealthReport(15, 45, Severity.WARNING_NORMAL, 10, 20, 30, 0);
        HealthReport reportLowPriority = createTestHealthReport(15, 105, Severity.WARNING_LOW, 10, 20, 30, 0);

        assertThat(reportHighPriority.getScore()).isEqualTo(50);
        assertThat(reportNormalPriority.getScore()).isEqualTo(50);
        assertThat(reportLowPriority.getScore()).isEqualTo(50);
    }

    /**
     * Tests whether we evaluate correctly to 100% health.
     */
    @Test
    void shouldTest100Health() {
        HealthReport reportHighPriority = createTestHealthReport(20, 22, Severity.WARNING_HIGH, 10, 20, 30, 0);
        HealthReport reportNormalPriority = createTestHealthReport(40, 45, Severity.WARNING_NORMAL, 10, 20, 30, 0);
        HealthReport reportLowPriority = createTestHealthReport(65, 105, Severity.WARNING_LOW, 10, 20, 30, 0);

        assertThat(reportHighPriority.getScore()).isEqualTo(100);
        assertThat(reportNormalPriority.getScore()).isEqualTo(100);
        assertThat(reportLowPriority.getScore()).isEqualTo(100);
    }

    /**
     * Test whether we evaluate correctly to 0% health.
     */
    @Test
    void shouldTest0Health() {
        HealthReport reportHighPriority = createTestHealthReport(4, 6, Severity.WARNING_HIGH, 10, 20, 30, 0);
        HealthReport reportNormalPriority = createTestHealthReport(15, 25, Severity.WARNING_NORMAL, 10, 20, 30, 0);
        HealthReport reportLowPriority = createTestHealthReport(15, 45, Severity.WARNING_LOW, 10, 20, 30, 0);

        assertThat(reportHighPriority.getScore()).isEqualTo(0);
        assertThat(reportNormalPriority.getScore()).isEqualTo(0);
        assertThat(reportLowPriority.getScore()).isEqualTo(0);
    }

    /**
     * Test whether the health descriptor is disabled.
     */
    @Test
    void shouldBeNullForDisabledHealthDescriptor() {
        HealthReport report = createTestHealthReport(0, 0, Severity.WARNING_NORMAL, 10, 20, 30, 0);

        assertThat(report).isNull();
    }

    /**
     * Test whether the health descriptor is disabled.
     */
    @Test
    void shouldBeNullForInvalidHealthDescriptor() {
        HealthReport sameBoundaries = createTestHealthReport(15, 15, Severity.WARNING_NORMAL, 10, 20, 30, 0);
        assertThat(sameBoundaries).isNull();

        HealthReport wrongBoundaryOrder = createTestHealthReport(15, 15, Severity.WARNING_NORMAL, 10, 20, 30, 0);
        assertThat(wrongBoundaryOrder).isNull();
    }

    /**
     * Tests whether we evaluate correctly to a 100% health if healthy threshold fits the priority issue number.
     */
    @Test
    void shouldTestHealthBoundary() {
        HealthReport reportHighPriority = createTestHealthReport(10, 15, Severity.WARNING_HIGH, 10, 20, 30, 0);
        HealthReport reportNormalPriority = createTestHealthReport(30, 35, Severity.WARNING_NORMAL, 10, 20, 30, 0);
        HealthReport reportLowPriority = createTestHealthReport(60, 65, Severity.WARNING_LOW, 10, 20, 30, 0);

        assertThat(reportHighPriority.getScore()).isEqualTo(100);
        assertThat(reportNormalPriority.getScore()).isEqualTo(100);
        assertThat(reportLowPriority.getScore()).isEqualTo(100);
    }

    /**
     * Tests whether we evaluate correctly to a 0% health if unhealthy threshold fits the priority issue number.
     */
    @Test
    void shouldTestUnHealthBoundary() {
        HealthReport reportHighPriority = createTestHealthReport(4, 10, Severity.WARNING_HIGH, 10, 20, 30, 0);
        HealthReport reportNormalPriority = createTestHealthReport(15, 30, Severity.WARNING_NORMAL, 10, 20, 30, 0);
        HealthReport reportLowPriority = createTestHealthReport(15, 60, Severity.WARNING_LOW, 10, 20, 30, 0);

        assertThat(reportHighPriority.getScore()).isEqualTo(0);
        assertThat(reportNormalPriority.getScore()).isEqualTo(0);
        assertThat(reportLowPriority.getScore()).isEqualTo(0);
    }

    /**
     * Tests the correct description for no items.
     */
    @Test
    void shouldReturnDescriptionForNoItem() {
        HealthReport report = createTestHealthReport(4, 10, Severity.WARNING_HIGH, 
                0, 0, 0, 0);
        assertThat(report.getDescription()).isEqualTo(HEALTH_REPORT_MESSAGE);
    }

    /**
     * Tests the correct description for a single item.
     */
    @Test
    void shouldReturnDescriptionForSingleItem() {
        HealthReport report = createTestHealthReport(4, 10, Severity.WARNING_HIGH, 
                1, 0, 0, 1);
        assertThat(report.getDescription()).isEqualTo(HEALTH_REPORT_MESSAGE);
    }

    /**
     * Tests the correct description for multiple items.
     */
    @Test
    void shouldReturnDescriptionForMultipleItem() {
        HealthReport report = createTestHealthReport(4, 10, Severity.WARNING_HIGH, 
                10, 30, 60, 10);
        assertThat(report.getDescription()).isEqualTo(HEALTH_REPORT_MESSAGE);
    }

    /**
     * Creates the {@link HealthReport} under test with specified parameters.
     *
     * @param healthyThreshold
     *         the healthy threshold, i.e. when health is reported as 100%.
     * @param unhealthyThreshold
     *         the unhealthy threshold, i.e. when health is reported as 0%.
     * @param priority
     *         the minimum priority that should be considered when computing build health
     * @param highSize
     *         the threshold for priority high
     * @param normalSize
     *         the threshold for priority normal
     * @param lowSize
     *         the threshold for priority low
     * @param expectedRelevantIssuesCount
     *         expected number of relevant issues
     *
     * @return the {@link HealthReport} under test
     */
    private HealthReport createTestHealthReport(final int healthyThreshold, final int unhealthyThreshold,
            final Severity priority, final int highSize, final int normalSize, final int lowSize,
            final int expectedRelevantIssuesCount) {
        HealthDescriptor healthDescriptor = new HealthDescriptor(healthyThreshold, unhealthyThreshold, priority);
        HealthReportBuilder builder = new HealthReportBuilder();

        Map<Severity, Integer> sizesPerSeverity = Maps.mutable.of(
                Severity.WARNING_HIGH, highSize, Severity.WARNING_NORMAL, normalSize, Severity.WARNING_LOW, lowSize);
        sizesPerSeverity.put(Severity.ERROR, 0);
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        Localizable message = mock(Localizable.class);
        when(message.toString()).thenReturn(HEALTH_REPORT_MESSAGE);
        when(labelProvider.getToolTipLocalizable(expectedRelevantIssuesCount)).thenReturn(message);
        HealthReport report = builder.computeHealth(healthDescriptor, labelProvider, sizesPerSeverity);
        assertThat(report);
        return report;
    }
}