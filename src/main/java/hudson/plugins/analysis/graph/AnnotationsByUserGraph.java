package hudson.plugins.analysis.graph;


import javax.annotation.CheckForNull;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
public class AnnotationsByUserGraph extends BuildResultGraph {
    @Override
    public JFreeChart create(final GraphConfiguration configuration,
            final ResultAction<? extends BuildResult> resultAction, @CheckForNull final String pluginName) {
        JFreeChart chart = createChart(createDataSetPerAuthorName(configuration, resultAction));

        attachRenderers(configuration, pluginName, chart, resultAction.getToolTipProvider());

        return chart;
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
        CategoryItemRenderer renderer = createRenderer(configuration, pluginName, toolTipProvider);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRenderer(renderer);
        setColors(chart, getColors());
    }

    @Override
    public JFreeChart createAggregation(final GraphConfiguration configuration, final Collection<ResultAction<? extends BuildResult>> resultActions, final String pluginName) {
        ResultAction<? extends BuildResult> resultAction = resultActions.iterator().next();
        return create(configuration, resultAction, pluginName);
    }

    private CategoryDataset createDataSetPerAuthorName(final GraphConfiguration configuration, final ResultAction<? extends BuildResult> action) {
        DataSetBuilder<String, String> builder = new DataSetBuilder<String, String>();
        BuildResult current = action.getResult();
        Map<String, Integer[]> annotationCountByUser = new HashMap<String, Integer[]>();

        Collection<FileAnnotation> annotations = current.getAnnotations();
        for (FileAnnotation annotation : annotations) {
            String author = annotation.getAuthor();
            if (annotationCountByUser.get(author) == null) {
                annotationCountByUser.put(author, new Integer[] {0, 0, 0});
            }
            Integer[] priorities = annotationCountByUser.get(author);
            int index = annotation.getPriority().ordinal();
            priorities[index]++;
        }

        for (Entry<String, Integer[]> entry : annotationCountByUser.entrySet()) {
            String userName= entry.getKey();
            Integer[] countsPerPriority = entry.getValue();
            for (int i = 0; i < countsPerPriority.length; i++) {
                builder.add(countsPerPriority[i], Integer.toString(i), userName);
            }
        }

        return builder.build();
    }

    @Override
    public String getId() {
        return "USERS";
    }

    @Override
    public String getLabel() {
        return Messages.Trend_type_authors();
    }

    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return createBlockChart(dataSet);
    }

    protected Color[] getColors() {
        return new Color[]{ColorPalette.BLUE, ColorPalette.YELLOW, ColorPalette.RED};
    }

    protected CategoryItemRenderer createRenderer(final GraphConfiguration configuration,
            final String pluginName, final ToolTipProvider toolTipProvider) {
        return new StackedBarRenderer();
    }
}
