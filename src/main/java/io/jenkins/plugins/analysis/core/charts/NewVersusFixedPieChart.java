package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Report;

/**
 * Builds the model for a pie chart showing the number of new, fixed, and outstanding issues.
 *
 * @author Ullrich Hafner
 */
public class NewVersusFixedPieChart {
    /**
     * Creates the chart for the specified result.
     *
     * @param newIssues
     *         all new issues
     * @param outstandingIssues
     *         all outstanding issues
     * @param fixedIssues
     *         all fixed issues
     *
     * @return the chart model
     */
    public PieChartModel create(final Report newIssues, final Report outstandingIssues, final Report fixedIssues) {
        PieChartModel model = new PieChartModel(Messages.NewVersusFixed_Name());

        model.add(new PieData(Messages.New_Warnings_Short(), newIssues.size()), Palette.RED);
        model.add(new PieData(Messages.Outstanding_Warnings_Short(), outstandingIssues.size()), Palette.YELLOW);
        model.add(new PieData(Messages.Fixed_Warnings_Short(), fixedIssues.size()), Palette.GREEN);

        return model;
    }
}
