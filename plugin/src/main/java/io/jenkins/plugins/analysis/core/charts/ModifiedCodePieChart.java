package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;

import io.jenkins.plugins.echarts.JenkinsPalette;

/**
 * Builds the model for a pie chart showing the number of issues in modified code.
 *
 * @author Ullrich Hafner
 */
public class ModifiedCodePieChart {
    /**
     * Creates the chart for the specified result.
     *
     * @param issues
     *         all issues
     *
     * @return the chart model
     */
    public PieChartModel create(final Report issues) {
        PieChartModel model = new PieChartModel(Messages.NewVersusFixed_Name());

        var totals = issues.size();
        var modified = issues.getInModifiedCode().size();
        model.add(new PieData(Messages.Modified_Code_Warnings_Short(), modified), JenkinsPalette.RED.normal());
        model.add(new PieData(Messages.Unchanged_Code_Warnings_Short(), totals - modified), JenkinsPalette.YELLOW.normal());

        return model;
    }
}
