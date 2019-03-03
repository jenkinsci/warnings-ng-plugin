package io.jenkins.plugins.analysis.core.charts;

import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.util.VisibleForTesting;

import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Builds the series for a graph showing all warnings by health descriptor.
 *
 * @author Ullrich Hafner
 */
public class HealthSeriesBuilder extends SeriesBuilder {
    static final String HEALTHY = "healthy";
    static final String BETWEEN = "between";
    static final String UNHEALTHY = "unhealthy";
    static final String TOTAL = "total";

    private final HealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of {@link HealthSeriesBuilder}.
     *
     * @param healthDescriptor the health descriptor to determine the colors of the graph
     */
    public HealthSeriesBuilder(final HealthDescriptor healthDescriptor) {
        super();

        this.healthDescriptor = healthDescriptor;
    }

    @VisibleForTesting
    HealthSeriesBuilder(final HealthDescriptor healthDescriptor, final ResultTime resultTime) {
        super(resultTime);

        this.healthDescriptor = healthDescriptor;
    }

    @Override
    protected Map<String, Integer> computeSeries(final StaticAnalysisRun current) {
        Map<String, Integer> series = new HashMap<>();
        int remainder = current.getTotalSize();

        if (healthDescriptor.isEnabled()) {
            series.put(HEALTHY, Math.min(remainder, healthDescriptor.getHealthy()));

            int range = healthDescriptor.getUnhealthy() - healthDescriptor.getHealthy();
            remainder -= healthDescriptor.getHealthy();
            if (remainder > 0) {
                series.put(BETWEEN, Math.min(remainder, range));
                remainder -= range;
                if (remainder > 0) {
                    series.put(UNHEALTHY, remainder);
                }
                else {
                    series.put(UNHEALTHY, 0);
                }
            }
            else {
                series.put(BETWEEN, 0);
                series.put(UNHEALTHY, 0);
            }
        }
        else { // at least a graph should be shown if the health reporting has been disabled in the meantime
            series.put(TOTAL, current.getTotalSize());
        }

        return series;
    }
}
