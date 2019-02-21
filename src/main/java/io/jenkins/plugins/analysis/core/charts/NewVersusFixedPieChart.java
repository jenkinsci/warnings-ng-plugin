package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

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
    public List<PieModel> create(final Report newIssues, final Report outstandingIssues, final Report fixedIssues) {
        List<PieModel> model = new ArrayList<>();
        model.add(new PieModel(Messages.New_Warnings_Short(), newIssues.size()));
        model.add(new PieModel(Messages.Outstanding_Warnings_Short(), outstandingIssues.size()));
        model.add(new PieModel(Messages.Fixed_Warnings_Short(), fixedIssues.size()));

        return model;
    }
}
