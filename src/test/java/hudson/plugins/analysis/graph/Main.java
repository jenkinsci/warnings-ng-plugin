package hudson.plugins.analysis.graph;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Calendar;

import javax.annotation.CheckForNull;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import hudson.model.AbstractBuild;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

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

        DifferenceGraph graph = new DifferenceGraph();

        GraphConfiguration configuration = new GraphConfiguration(new ArrayList<BuildResultGraph>());
        ResultAction resultAction1 = createAction(1, null, 5, 10);
        ResultAction resultAction2 = createAction(2, resultAction1, 15, 10);
        ResultAction resultAction3 = createAction(3, resultAction2, 25, 20);
        ResultAction resultAction4 = createAction(4, resultAction3, 5, 15);

        JFreeChart chart = graph.create(configuration, resultAction4, "frame");

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }

    /**
     * Creates a mock {@link ResultAction}.
     *
     * @param buildNumber
     *            build number
     * @param previous
     *            previous build (might be null)
     * @param fixedWarnings
     *            number of fixed warnings
     * @param newWarnings
     *            number of new warnings
     * @return a {@link ResultAction} mock.
     */
    @SuppressWarnings("rawtypes")
    private ResultAction createAction(final int buildNumber, @CheckForNull final ResultAction previous, final int fixedWarnings, final int newWarnings) {
        ResultAction action = mock(ResultAction.class);
        AbstractBuild build = mock(AbstractBuild.class);
        BuildResult result = mock(BuildResult.class);

        when(build.getTimestamp()).thenReturn(Calendar.getInstance());
        when(build.getNumber()).thenReturn(buildNumber);

        when(result.getNumberOfFixedWarnings()).thenReturn(fixedWarnings);
        when(result.getNumberOfNewWarnings()).thenReturn(newWarnings);

        when(action.getBuild()).thenReturn(build);
        when(action.getPreviousResultAction()).thenReturn(previous);
        when(action.hasPreviousResultAction()).thenReturn(previous != null);
        when(action.getResult()).thenReturn(result);

        return action;
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

