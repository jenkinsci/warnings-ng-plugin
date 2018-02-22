package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider.AgeBuilder;
import net.sf.json.JSONObject;

import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * Provides UI labels and texts for a specific static analysis tool.
 *
 * @author Ullrich Hafner
 */
public interface StaticAnalysisLabelProvider extends ToolTipProvider {
    String getName();

    String getLinkName();

    String getTrendName();

    String getSmallIconUrl();

    String getLargeIconUrl();

    String getResultUrl();

    /**
     * Returns a detailed description of the specified issue.
     *
     * @param issue
     *         the issue to get the description for
     *
     * @return the description
     */
    String getDescription(Issue issue);

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @param numberOfIssues
     *         the number of issues in the report
     * @param numberOfModules
     *         the number of modules in the report
     *
     * @return the summary message
     */
    String getSummary(int numberOfIssues, int numberOfModules);

    /**
     * Creates a default delta message for the build result.
     *
     * @param newSize
     *         number of new issues
     * @param fixedSize
     *         number of fixed issues
     *
     * @return the summary message
     */
    String getDeltaMessage(int newSize, int fixedSize);

    String getId();

    /**
     * Returns the table headers of the issues table.
     *
     * @return the table headers
     */
    String[] getTableHeaders();

    /**
     * Returns the widths of the table headers of the issues table.
     *
     * @return the width of the table headers
     */
    int[] getTableWidths();

    /**
     * Converts the specified set of issues into a table.
     *
     * @param issues
     *         the issues to show in the table
     * @param ageBuilder
     *         produces the age of an issue based on the current build number
     *
     * @return the table as String
     */
    JSONObject toJsonArray(Issues<?> issues, AgeBuilder ageBuilder);
}
