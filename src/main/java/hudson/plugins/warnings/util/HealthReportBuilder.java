package hudson.plugins.warnings.util;

import hudson.model.HealthReport;
import hudson.plugins.warnings.util.model.AnnotationProvider;
import hudson.plugins.warnings.util.model.Priority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * Creates a health report for integer values based on healthy and unhealthy
 * thresholds.
 *
 * @see HealthReport
 * @author Ulli Hafner
 */
public class HealthReportBuilder implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5191317904662711835L;
    /** Health descriptor. */
    private final AbstractHealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of {@link HealthReportBuilder}.
     *
     * @param healthDescriptor
     *            health descriptor
     */
    public HealthReportBuilder(final AbstractHealthDescriptor healthDescriptor) {
        this.healthDescriptor = healthDescriptor;
    }

    /**
     * Computes the healthiness of a build based on the specified results.
     * Reports a health of 100% when the specified counter is less than
     * {@link #healthy}. Reports a health of 0% when the specified counter is
     * greater than {@link #unHealthy}. The computation takes only annotations
     * of the specified severity into account.
     *
     * @param result
     *            annotations of the current build
     * @return the healthiness of a build
     */
    public HealthReport computeHealth(final AnnotationProvider result) {
        int numberOfAnnotations = 0;
        for (Priority priority : Priority.collectPrioritiesFrom(healthDescriptor.getMinimumPriority())) {
            numberOfAnnotations += result.getNumberOfAnnotations(priority);
        }

        return computeHealth(numberOfAnnotations, result);
    }

    /**
     * Computes the healthiness of a build based on the specified counter.
     * Reports a health of 100% when the specified counter is less than
     * {@link #healthy}. Reports a health of 0% when the specified counter is
     * greater than {@link #unHealthy}.
     *
     * @param counter
     *            the number of items in a build that should be considered for
     *            health computation
     * @param result
     *            annotations of the current build
     * @return the healthiness of a build
     */
    protected HealthReport computeHealth(final int counter, final AnnotationProvider result) {
        if (healthDescriptor.isHealthyReportEnabled()) {
            int percentage;
            if (counter < healthDescriptor.getHealthyAnnotations()) {
                percentage = 100;
            }
            else if (counter > healthDescriptor.getUnHealthyAnnotations()) {
                percentage = 0;
            }
            else {
                percentage = 100 - ((counter - healthDescriptor.getHealthyAnnotations()) * 100
                        / (healthDescriptor.getUnHealthyAnnotations() - healthDescriptor.getHealthyAnnotations()));
            }
            return new HealthReport(percentage, healthDescriptor.createDescription(result));
        }
        return null;
    }

    /**
     * Returns whether this health report build is enabled, i.e. at least one of
     * the health or failed thresholds are provided.
     *
     * @return <code>true</code> if health or failed thresholds are provided
     */
    public boolean isEnabled() {
        return healthDescriptor.isHealthyReportEnabled() || healthDescriptor.isThresholdEnabled();
    }

    /**
     * Creates a list of integer values used to create a three color graph
     * showing the items per build.
     *
     * @param totalCount
     *            total number of items
     * @return the list of values
     */
    public List<Integer> createSeries(final int totalCount) {
        List<Integer> series = new ArrayList<Integer>(3);
        int remainder = totalCount;

        if (healthDescriptor.isHealthyReportEnabled()) {
            series.add(Math.min(remainder, healthDescriptor.getHealthyAnnotations()));

            int range = healthDescriptor.getUnHealthyAnnotations() - healthDescriptor.getHealthyAnnotations();
            remainder -= healthDescriptor.getHealthyAnnotations();
            if (remainder > 0) {
                series.add(Math.min(remainder, range));
            }
            else {
                series.add(0);
            }

            remainder -= range;
            if (remainder > 0) {
                series.add(remainder);
            }
            else {
                series.add(0);
            }
        }
        else if (healthDescriptor.isThresholdEnabled()) {
            series.add(Math.min(remainder, healthDescriptor.getMinimumAnnotations()));

            remainder -= healthDescriptor.getMinimumAnnotations();
            if (remainder > 0) {
                series.add(remainder);
            }
            else {
                series.add(0);
            }
        }

        return series;
    }

    /**
     * Creates a trend graph for the corresponding action using the thresholds
     * of this health builder.
     *
     * @param useHealthBuilder
     *            if the health thresholds should be used at all
     * @param url
     *            the URL shown in the tool tips
     * @param dataset
     *            the data set of the values to render
     * @param toolTipProvider
     *            tooltip provider for the clickable map
     * @return the created graph
     */
    public JFreeChart createGraph(final boolean useHealthBuilder, final String url, final CategoryDataset dataset,
            final ToolTipProvider toolTipProvider) {
        StackedAreaRenderer renderer;
        if (useHealthBuilder && isEnabled()) {
            renderer = new ResultAreaRenderer(url, toolTipProvider);
        }
        else {
            renderer = new PrioritiesAreaRenderer(url, toolTipProvider);
        }

        return ChartBuilder.createChart(dataset, renderer, useThreeColors(useHealthBuilder));
    }


    /**
     * Returns whether to use three or two colors for the graph.
     *
     * @param useHealthBuilder
     *            determines whether to use the health builder
     * @return <code>true</code> if the graph should use three colors,
     *         <code>false</code> if the graph should use two colors.
     */
    private boolean useThreeColors(final boolean useHealthBuilder) {
        return healthDescriptor.isHealthyReportEnabled()
                || !healthDescriptor.isThresholdEnabled()
                || !useHealthBuilder;
    }

    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int healthy;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int unHealthy;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean isHealthEnabled;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean isThresholdEnabled;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int threshold;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient String reportName;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient String itemName;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient String reportSingleCount;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient String reportMultipleCount;
}

