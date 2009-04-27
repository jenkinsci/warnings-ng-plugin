package hudson.plugins.warnings.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.model.HealthReport;
import hudson.plugins.warnings.util.model.AnnotationProvider;

import org.junit.Test;

/**
 * Tests the class {@link HealthReportBuilder}.
 */
public class HealthReportBuilderTest extends AbstractEnglishLocaleTest {
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong healthiness calculation.";

    /**
     * Tests whether we evaluate correctly to a 50% health.
     */
    @Test
    public void testMiddle() {
        HealthReport health = createHealthReport(true, 50, 150, 100);
        assertEquals(ERROR_MESSAGE, 50, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 100% health.
     */
    @Test
    public void testHigh() {
        HealthReport health = createHealthReport(true, 50, 150, 20);
        assertEquals(ERROR_MESSAGE, 100, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 100% health if lower than minimum.
     */
    @Test
    public void testHighBoundary() {
        HealthReport health = createHealthReport(true, 50, 150, 50);
        assertEquals(ERROR_MESSAGE, 100, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 0% health.
     */
    @Test
    public void testLow() {
        HealthReport health = createHealthReport(true, 50, 150, 200);
        assertEquals(ERROR_MESSAGE, 0, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 0% health if larger than maximum.
     */
    @Test
    public void testLowBoundary() {
        HealthReport health = createHealthReport(true, 50, 150, 150);
        assertEquals(ERROR_MESSAGE, 0, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 25% health.
     */
    @Test
    public void test25Percent() {
        HealthReport health = createHealthReport(true, 0, 100, 75);
        assertEquals(ERROR_MESSAGE, 25, health.getScore());
    }

    /**
     * Tests whether we don't get a healthy report if the reporting is disabled.
     */
    @Test
    public void testNoHealthyReport() {
        HealthReport health = createHealthReport(false, 0, 100, 75);
        assertNull(ERROR_MESSAGE, health);
    }

    /**
     * Creates a health report using a {@link HealthReportBuilder} with the specified parameters.
     *
     * @param isEnabled
     *            defines whether health reporting is enabled
     * @param min
     *            minimum number of bugs
     * @param max
     *            maximum number of bugs
     * @param actual
     *            actual number of bugs
     * @return the actual healthiness
     */
    private HealthReport createHealthReport(final boolean isEnabled, final int min, final int max, final int actual) {
        HealthReportBuilder builder = createHealthBuilder(false, 0, isEnabled, min, max);
        AnnotationProvider result = mock(AnnotationProvider.class);
        when(result.getNumberOfAnnotations()).thenReturn(actual);
        return builder.computeHealth(actual, result);
    }

    /**
     * Creates the {@link HealthReportBuilder} under test with the specified
     * parameters.
     *
     * @param isHealthEnabled
     *            determines whether to use the provided unstable threshold
     * @param threshold
     *            bug threshold to be reached if a build should be considered as
     *            unstable.
     * @param isThresholdEnabled
     *            determines whether to use the provided healthy thresholds.
     * @param healthy
     *            report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            report health as 0% when the number of warnings is greater
     *            than this value
     * @return the {@link HealthReportBuilder} under test
     */
    private HealthReportBuilder createHealthBuilder(final boolean isThresholdEnabled, final int threshold,
            final boolean isHealthEnabled, final int healthy, final int unHealthy) {
        AbstractHealthDescriptor healthDescriptor = mock(AbstractHealthDescriptor.class);
        when(healthDescriptor.isThresholdEnabled()).thenReturn(isThresholdEnabled);
        when(healthDescriptor.getMinimumAnnotations()).thenReturn(threshold);
        when(healthDescriptor.isHealthyReportEnabled()).thenReturn(isHealthEnabled);
        when(healthDescriptor.getHealthyAnnotations()).thenReturn(healthy);
        when(healthDescriptor.getUnHealthyAnnotations()).thenReturn(unHealthy);

        return new HealthReportBuilder(healthDescriptor);
    }
}

