package hudson.plugins.analysis.graph;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Calendar;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import hudson.model.AbstractBuild;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Plots a graph.
 *
 * @author Ulli Hafner
 */
public class Main extends ApplicationFrame {
    /** Unique ID. */
    private static final long serialVersionUID = 1640077724803031029L;

    /**
     * Creates a new instance of {@link Main}.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Main() {
        super("Hello Graph");

        BuildResultGraph graph = new PriorityGraph();

        GraphConfiguration configuration = new GraphConfiguration(new ArrayList<BuildResultGraph>());
        BuildResult result1 = createResult(1, 5, 10);
        when(result1.hasPreviousResult()).thenReturn(false);
        BuildResult result2 = createResult(2, 15, 10);
        when(result2.hasPreviousResult()).thenReturn(true);
        when(result2.getPreviousResult()).thenReturn(result1);
        BuildResult result3 = createResult(3, 25, 20);
        when(result3.hasPreviousResult()).thenReturn(true);
        when(result3.getPreviousResult()).thenReturn(result2);
        BuildResult result4 = createResult(4, 5, 15);
        when(result4.hasPreviousResult()).thenReturn(true);
        when(result4.getPreviousResult()).thenReturn(result3);

        ResultAction action = mock(ResultAction.class);
        when(action.getResult()).thenReturn(result4);

        JFreeChart chart = graph.create(configuration, action, "frame");

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }

    /**
     * Creates a mock {@link BuildResult}.
     *
     * @param buildNumber
     *            current build number
     * @param fixedWarnings
     *            number of fixed warnings
     * @param newWarnings
     *            number of new warnings
     * @return a {@link ResultAction} mock.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private BuildResult createResult(final int buildNumber, final int fixedWarnings, final int newWarnings) {
        BuildResult result = mock(BuildResult.class);

        when(result.getNumberOfWarnings()).thenReturn(newWarnings);
        when(result.getNumberOfAnnotations(Priority.HIGH)).thenReturn(newWarnings);
        when(result.getNumberOfAnnotations(Priority.HIGH)).thenReturn(fixedWarnings);
        when(result.getNumberOfAnnotations(Priority.HIGH)).thenReturn(Math.abs(newWarnings - fixedWarnings));
        when(result.getNumberOfFixedWarnings()).thenReturn(fixedWarnings);
        when(result.getNumberOfNewWarnings()).thenReturn(newWarnings);

        AbstractBuild build = mock(AbstractBuild.class);

        when(build.getTimestamp()).thenReturn(Calendar.getInstance());
        when(build.getNumber()).thenReturn(buildNumber);
        when(build.getDisplayName()).thenReturn("#" + buildNumber);

        when(result.getOwner()).thenReturn(build);

        return result;
    }

    /**
     * Shows the graph.
     *
     * @param args not used
     */
    public static void main(final String[] args) {
        Main chart = new Main();
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }
}

