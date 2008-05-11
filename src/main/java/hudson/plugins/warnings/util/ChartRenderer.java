package hudson.plugins.warnings.util;

import hudson.plugins.warnings.util.model.AnnotationProvider;
import hudson.plugins.warnings.util.model.Priority;
import hudson.util.ChartUtil;

import java.io.IOException;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Renders various charts and attaches the graph to the stapler response.
 *
 * @author Ulli Hafner
 */
public final class ChartRenderer {
    /**
     * Creates a priority distribution graph for the specified annotation
     * provider. The graph displays the distribution of warnings by priority as
     * a horizontal bar (in different colors).
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @param detailObject
     *            the detail object to compute the graph for
     * @param upperBound
     *            the upper bound of all tasks
     * @throws IOException
     *             in case of an error
     */
    public static void renderPriorititesChart(final StaplerRequest request, final StaplerResponse response,
            final AnnotationProvider detailObject, final int upperBound) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        JFreeChart chart = ChartBuilder.createHighNormalLowChart(
                detailObject.getNumberOfAnnotations(Priority.HIGH),
                detailObject.getNumberOfAnnotations(Priority.NORMAL),
                detailObject.getNumberOfAnnotations(Priority.LOW), upperBound);

        ChartUtil.generateGraph(request, response, chart, 400, 20);
    }

    /**
     * Creates a new instance of <code>ChartBuilder</code>.
     */
    private ChartRenderer() {
        // prevents instantiation
    }
}

