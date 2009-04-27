package hudson.plugins.warnings.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;

/**
 * Tests the class {@link HealthReportBuilder}.
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings("SIC")
public class HealthGraphTest {
    /** Number of elements in a series with failure threshold. */
    private static final int THRESHOLD_SERIES_SIZE = 2;
    /** Number of elements in a series with healthy threshold. */
    private static final int HEALTHY_SERIES_SIZE = 3;
    /** Error message. */
    private static final String WRONG_SERIES_VALUE = "Wrong series value.";
    /** Error message. */
    private static final String WRONG_NUMBER = "Number of created points is wrong.";

    /**
     * Tests whether we correctly compute the series if health reporting is enabled.
     */
    @Test
    public void testHealthySeriesCalculator() {
        AbstractHealthDescriptor healthDescriptor = createHealthBuilder(true, 0, true, 10, 30);
        HealthGraph builder = new HealthGraph(healthDescriptor);
        BuildResult result = mock(BuildResult.class);

        List<Integer> series;
        when(result.getNumberOfAnnotations()).thenReturn(5);
        series = builder.computeSeries(result);
        assertEquals(WRONG_NUMBER, HEALTHY_SERIES_SIZE, series.size());
        assertEquals(WRONG_SERIES_VALUE, 5, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(1));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(2));

        when(result.getNumberOfAnnotations()).thenReturn(10);
        series = builder.computeSeries(result);
        assertEquals(WRONG_NUMBER, HEALTHY_SERIES_SIZE, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(1));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(2));

        when(result.getNumberOfAnnotations()).thenReturn(11);
        series = builder.computeSeries(result);
        assertEquals(WRONG_NUMBER, HEALTHY_SERIES_SIZE, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 1, (int)series.get(1));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(2));

        when(result.getNumberOfAnnotations()).thenReturn(30);
        series = builder.computeSeries(result);
        assertEquals(WRONG_NUMBER, HEALTHY_SERIES_SIZE, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 20, (int)series.get(1));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(2));

        when(result.getNumberOfAnnotations()).thenReturn(31);
        series = builder.computeSeries(result);
        assertEquals(WRONG_NUMBER, HEALTHY_SERIES_SIZE, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 20, (int)series.get(1));
        assertEquals(WRONG_SERIES_VALUE, 1, (int)series.get(2));
    }

    /**
     * Tests whether we don't get a healthy report if the reporting is disabled.
     */
    @Test
    public void testThresholdSeriesCalculator() {
        AbstractHealthDescriptor healthDescriptor = createHealthBuilder(true, 10, false, 20, 50);
        HealthGraph builder = new HealthGraph(healthDescriptor);
        BuildResult result = mock(BuildResult.class);

        List<Integer> series;
        when(result.getNumberOfAnnotations()).thenReturn(5);
        series = builder.computeSeries(result);
        assertEquals(WRONG_NUMBER, THRESHOLD_SERIES_SIZE, series.size());
        assertEquals(WRONG_SERIES_VALUE, 5, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(1));

        when(result.getNumberOfAnnotations()).thenReturn(10);
        series = builder.computeSeries(result);
        assertEquals(WRONG_NUMBER, THRESHOLD_SERIES_SIZE, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(1));

        when(result.getNumberOfAnnotations()).thenReturn(11);
        series = builder.computeSeries(result);
        assertEquals(WRONG_NUMBER, THRESHOLD_SERIES_SIZE, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 1, (int)series.get(1));
    }

    /**
     * Tests Issue 796.
     */
    @Test
    public void testIssue796() {
        AbstractHealthDescriptor healthDescriptor = createHealthBuilder(false, 0, true, 1, 10);
        HealthGraph builder = new HealthGraph(healthDescriptor);
        BuildResult result = mock(BuildResult.class);

        List<Integer> series;
        when(result.getNumberOfAnnotations()).thenReturn(1);
        series = builder.computeSeries(result);

        assertEquals(WRONG_NUMBER, HEALTHY_SERIES_SIZE, series.size());
        assertEquals(WRONG_SERIES_VALUE, 1, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(1));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(2));

        when(result.getNumberOfAnnotations()).thenReturn(7);
        series = builder.computeSeries(result);
        assertEquals(WRONG_NUMBER, HEALTHY_SERIES_SIZE, series.size());
        assertEquals(WRONG_SERIES_VALUE, 1, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 6, (int)series.get(1));
        assertEquals(WRONG_SERIES_VALUE, 0, (int)series.get(2));
    }


    /**
     * Creates an {@link AbstractHealthDescriptor} mock with the specified
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
    private AbstractHealthDescriptor createHealthBuilder(final boolean isThresholdEnabled, final int threshold,
            final boolean isHealthEnabled, final int healthy, final int unHealthy) {
        AbstractHealthDescriptor healthDescriptor = mock(AbstractHealthDescriptor.class);
        when(healthDescriptor.isThresholdEnabled()).thenReturn(isThresholdEnabled);
        when(healthDescriptor.getMinimumAnnotations()).thenReturn(threshold);
        when(healthDescriptor.isHealthyReportEnabled()).thenReturn(isHealthEnabled);
        when(healthDescriptor.getHealthyAnnotations()).thenReturn(healthy);
        when(healthDescriptor.getUnHealthyAnnotations()).thenReturn(unHealthy);

        return healthDescriptor;
    }
}

