package io.jenkins.plugins.analysis.core.steps;

import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * FIXME: write comment.
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

    @Override
    String getTooltip(int numberOfItems);

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
}
