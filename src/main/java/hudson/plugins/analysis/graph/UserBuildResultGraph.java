package hudson.plugins.analysis.graph;


import javax.annotation.CheckForNull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.ToolTipProvider;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.util.DataSetBuilder;

public abstract class UserBuildResultGraph extends BuildResultGraph {
    @Override
    public JFreeChart create(final GraphConfiguration configuration, final ResultAction<? extends BuildResult> resultAction, @CheckForNull final String pluginName) {
        CategoryDataset dataSet;
        dataSet = createDataSetPerAuthorName(configuration, resultAction);
        return createChart(dataSet);
    }

    @Override
    public JFreeChart createAggregation(final GraphConfiguration configuration, final Collection<ResultAction<? extends BuildResult>> resultActions, final String pluginName) {
        ResultAction<? extends BuildResult> resultAction = resultActions.iterator().next();
        return create(configuration, resultAction, pluginName);
    }

    private CategoryDataset createDataSetPerAuthorName(final GraphConfiguration configuration, final ResultAction<? extends BuildResult> action) {
        DataSetBuilder<String, String> builder = new DataSetBuilder<String, String>();
        BuildResult current = action.getResult();
        String parameterName = configuration.getParameterName();
        String parameterValue = configuration.getParameterValue();
        int buildCount = 0;
        Map<String, List<Integer>> annotationCountByUser = new HashMap<String, List<Integer>>();

        while (true) {
            if (isBuildTooOld(configuration, current)) {
                break;
            }
            if (passesFilteringByParameter(current.getOwner(), parameterName, parameterValue)) {
                Collection<FileAnnotation> annotations = current.getAnnotations();
                //Collection<FileAnnotation> annotations = current.getNewWarnings();
                Map<String, List<FileAnnotation>>annotationsByUser = new HashMap<String, List<FileAnnotation>>();
                for (FileAnnotation annotation : annotations)
                {
                    String name = annotation.getAuthorName();
                    if(name == null) continue;
                    if (annotationsByUser.get(name) == null)annotationsByUser.put(name, new ArrayList<FileAnnotation>());
                    annotationsByUser.get(name).add(annotation);
                }

                for (Entry<String, List<FileAnnotation>> entry : annotationsByUser.entrySet()) {
                    int numAnnot = entry.getValue().size();
                    String name = entry.getKey();
                    if(annotationCountByUser.get(name) == null)annotationCountByUser.put(name, new ArrayList<Integer>());
                    annotationCountByUser.get(name).add(numAnnot);
                }
            }

            if (current.hasPreviousResult()) {
                current = current.getPreviousResult();
                if (current == null) {
                    break; // see: JENKINS-6613
                }
            }
            else {
                break;
            }

            if (configuration.isBuildCountDefined()) {
                buildCount++;
                if (buildCount >= configuration.getBuildCount()) {
                    break;
                }
            }
        }


        for (Entry<String, List<Integer>> entry : annotationCountByUser.entrySet()) {
            String userName= entry.getKey();
            List<Integer> annotations = entry.getValue();
            int level = 0;
            for(int numAnnotPerBuild : annotations)
            {
                builder.add(numAnnotPerBuild,Integer.toString(level),userName);
            }
            level++;
        }



        return builder.build();
    }

    /**
     * Returns the series to plot for the specified build result.
     *
     * @param current the current build result
     * @return the series to plot
     */
    protected abstract List<Integer> computeSeries(BuildResult current);

    /**
     * Creates the chart for the specified data set.
     *
     * @param dataSet the data set to show in the graph
     * @return the created graph
     */
    protected abstract JFreeChart createChart(CategoryDataset dataSet);

    /**
     * Creates the renderer for this graph.
     *
     * @param configuration
     *            the graph configuration
     * @param pluginName
     *            the name of the plug-in
     * @param toolTipProvider
     *            the tooltip provider
     * @return the renderer
     */
    protected abstract CategoryItemRenderer createRenderer(GraphConfiguration configuration, final String pluginName, final ToolTipProvider toolTipProvider);

    /**
     * Returns the colors for this graph. The first color is used for the first
     * series value, etc.
     *
     * @return the colors
     */
    protected abstract Color[] getColors();
}
