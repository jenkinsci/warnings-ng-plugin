package hudson.plugins.warnings.util;

import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.AnnotationProvider;
import hudson.plugins.warnings.util.model.DefaultAnnotationContainer;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
     * Generates a PNG image for high/normal/low distribution of the specified object.
     * The type of the object is determined by the 'object' parameter of the {@link StaplerRequest}.
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doStatistics(final StaplerRequest request, final StaplerResponse response, final AnnotationContainer container) throws IOException {
        String parameter = request.getParameter("object");
        if (parameter.startsWith("category.")) {
            Set<FileAnnotation> annotations = container.getCategory(StringUtils.substringAfter(parameter, "category."));
            renderPriorititesChart(request, response, new DefaultAnnotationContainer(annotations), container.getAnnotationBound());
        }
        else if (parameter.startsWith("type.")) {
            Set<FileAnnotation> annotations = container.getType(StringUtils.substringAfter(parameter, "type."));
            renderPriorititesChart(request, response, new DefaultAnnotationContainer(annotations), container.getAnnotationBound());
        }
        else if (parameter.startsWith("file.")) {
            AnnotationContainer annotations = container.getFile(Integer.valueOf(StringUtils.substringAfter(parameter, "file.")));
            renderPriorititesChart(request, response, annotations, container.getAnnotationBound());
        }
        else if (parameter.startsWith("package.")) {
            AnnotationContainer annotations = container.getPackage(StringUtils.substringAfter(parameter, "package."));
            renderPriorititesChart(request, response, annotations, container.getAnnotationBound());
        }
        else if (parameter.startsWith("module.")) {
            String moduleName = StringUtils.substringAfter(parameter, "module.");
            AnnotationProvider annotations;
            if (container.containsModule(moduleName)) {
                annotations = container.getModule(moduleName);
            }
            else {
                annotations = new DefaultAnnotationContainer();
            }
            renderPriorititesChart(request, response, annotations, container.getAnnotationBound());
        }
        // TODO: we should parameterize the annotation bound (second parameter instead of getChild)
    }

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
    private void renderPriorititesChart(final StaplerRequest request, final StaplerResponse response,
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
}

