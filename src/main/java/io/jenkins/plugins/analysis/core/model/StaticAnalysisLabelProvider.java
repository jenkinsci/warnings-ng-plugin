package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider.AgeBuilder;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import net.sf.json.JSONObject;

import hudson.model.Result;
import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * Provides UI labels and texts for a specific static analysis tool.
 *
 * @author Ullrich Hafner
 */
// TODO: check if the interface is required or DefaultLabelrovider is the thing to use
public interface StaticAnalysisLabelProvider extends ToolTipProvider {
    String getName();

    String getLinkName();

    String getTrendName();

    String getSmallIconUrl();

    String getLargeIconUrl();

    String getResultUrl();

    ContainerTag getTitle(AnalysisResult analysisRun);

    /**
     * Returns a detailed description of the specified issue.
     *
     * @param issue
     *         the issue to get the description for
     *
     * @return the description
     */
    String getDescription(Issue issue);

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

    ContainerTag getNewIssuesLabel(int newSize);

    ContainerTag getFixedIssuesLabel(int fixedSize);

    DomContent getNoIssuesSinceLabel(int currentBuild, int noIssuesSinceBuild);

    DomContent getQualityGateResult(Result overallResult, int referenceBuild);
}
