package hudson.plugins.analysis.graph;


import javax.annotation.CheckForNull;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.ToolTipProvider;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.util.DataSetBuilder;

/**
 * A build result graph that can create a graph based on authors of annotations.
 *
 * @author Lukas Krose
 */
// TODO: Refactor so that duplicate methods with CategoryBuildResultGraphs are part of BuildResultGraph
// TODO: For each graph, different properties could be set, e.g. show "-" author
public class AnnotationsByUserGraph extends BuildResultGraph {
    @Override
    public JFreeChart create(final GraphConfiguration configuration,
            final ResultAction<? extends BuildResult> resultAction, @CheckForNull final String pluginName) {
        Map<String, Integer[]> annotationCountByUser = new HashMap<>();

        mergeResults(resultAction.getResult(), annotationCountByUser);

        return createGraphFromUserMapping(configuration, pluginName, annotationCountByUser, resultAction.getToolTipProvider());
    }

    private JFreeChart createGraphFromUserMapping(final GraphConfiguration configuration, final @CheckForNull String pluginName, final Map<String, Integer[]> annotationCountByUser, final ToolTipProvider toolTipProvider) {
        JFreeChart chart = createBlockChart(buildDataSet(annotationCountByUser));

        attachRenderers(configuration, pluginName, chart, toolTipProvider);

        return chart;
    }

    @Override
    public JFreeChart createAggregation(final GraphConfiguration configuration, final Collection<ResultAction<? extends BuildResult>> resultActions, final String pluginName) {
        Map<String, Integer[]> annotationCountByUser = new HashMap<>();

        for (ResultAction<? extends BuildResult> resultAction : resultActions) {
            mergeResults(resultAction.getResult(), annotationCountByUser);
        }

        return createGraphFromUserMapping(configuration, pluginName, annotationCountByUser,
                resultActions.iterator().next().getToolTipProvider());
    }

    /**
     * Attach the renderers to the created graph.
     *
     * @param configuration
     *            the configuration parameters
     * @param pluginName
     *            the name of the plug-in
     * @param chart
     *            the graph to attach the renderer to
     * @param toolTipProvider the tooltip provider for the graph
     */
    private void attachRenderers(final GraphConfiguration configuration, final String pluginName, final JFreeChart chart,
            final ToolTipProvider toolTipProvider) {
        CategoryItemRenderer renderer = new StackedBarRenderer();
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRenderer(renderer);
        setColors(chart, new Color[]{ColorPalette.BLUE, ColorPalette.YELLOW, ColorPalette.RED});
    }

    private CategoryDataset buildDataSet(final Map<String, Integer[]> annotationCountByUser) {
        DataSetBuilder<String, String> builder = new DataSetBuilder<>();
        for (Entry<String, Integer[]> entry : annotationCountByUser.entrySet()) {
            String userName= entry.getKey();
            Integer[] countsPerPriority = entry.getValue();
            for (int i = 0; i < countsPerPriority.length; i++) {
                builder.add(countsPerPriority[i], Integer.toString(i), userName);
            }
        }

        return builder.build();
    }

    private void mergeResults(final BuildResult current, final Map<String, Integer[]> annotationCountByUser) {
        Collection<FileAnnotation> annotations = current.getAnnotations();
        for (FileAnnotation annotation : annotations) {
            String author = annotation.getAuthor();
            if (StringUtils.isNotBlank(author) && !"-".equals(author)) {
                annotationCountByUser.computeIfAbsent(author, k -> new Integer[]{0, 0, 0});
                Integer[] priorities = annotationCountByUser.get(author);
                int index = annotation.getPriority().ordinal();
                priorities[index]++;
            }
        }
    }

    @Override
    public String getId() {
        return "USERS";
    }

    @Override
    public String getLabel() {
        return Messages.Trend_type_authors();
    }

}
