package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Report;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.IssuesStatistics;
import io.jenkins.plugins.analysis.core.util.IssuesStatisticsBuilder;

import static edu.hm.hafner.analysis.Severity.*;

/**
 * Provides the delta between the reports of two different builds.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.DataClass")
public class DeltaReport {
    private static final Report EMPTY_REPORT = new Report();

    private final Report allIssues;
    private final Report outstandingIssues;
    private final Report newIssues;
    private final Report fixedIssues;
    private final Report referenceIssues;
    private final String referenceBuildId;

    /**
     * Creates a new instance of {@link DeltaReport}.
     *
     * @param report
     *         the current report
     * @param history
     *         the history that will provide the reference build (if there is any)
     * @param currentBuildNumber
     *         the number of the current build, the reference of all new warnings will be set to this number
     */
    public DeltaReport(final Report report, final History history, final int currentBuildNumber) {
        allIssues = report;
        if (history.getBuild().isPresent()) {
            Run<?, ?> build = history.getBuild().get();
            report.logInfo("Using reference build '%s' to compute new, fixed, and outstanding issues",
                    build);

            referenceIssues = history.getIssues();
            IssueDifference difference = new IssueDifference(report, currentBuildNumber, referenceIssues);
            outstandingIssues = difference.getOutstandingIssues();
            newIssues = difference.getNewIssues();
            fixedIssues = difference.getFixedIssues();
            report.logInfo("Issues delta (vs. reference build): outstanding: %d, new: %d, fixed: %d",
                    outstandingIssues.size(), newIssues.size(), fixedIssues.size());
            referenceBuildId = build.getExternalizableId();
        }
        else {
            report.logInfo("No valid reference build found that meets the criteria (%s)", history);
            report.logInfo("All reported issues will be considered outstanding");
            report.forEach(issue -> issue.setReference(String.valueOf(currentBuildNumber)));
            outstandingIssues = report;
            referenceIssues = EMPTY_REPORT;
            newIssues = EMPTY_REPORT;
            fixedIssues = EMPTY_REPORT;
            referenceBuildId = StringUtils.EMPTY;
        }
    }

    /**
     * Returns whether this report contains issues or not.
     *
     * @return {@code true} if the report is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return allIssues.isEmpty();
    }

    /**
     * Returns the ID of the reference build.
     *
     * @return the reference build ID
     */
    public String getReferenceBuildId() {
        return referenceBuildId;
    }

    /**
     * Returns all issues of the current build.
     *
     * @return the issues of the current build.
     */
    public Report getAllIssues() {
        return allIssues;
    }

    /**
     * Returns all outstanding issues: i.e. all issues, that are part of the current and reference report.
     *
     * @return the outstanding issues
     */
    public Report getOutstandingIssues() {
        return outstandingIssues;
    }

    /**
     * Returns all new issues: i.e. all issues, that are part of the current report but have not been shown up in the
     * reference report.
     *
     * @return the new issues
     */
    public Report getNewIssues() {
        return newIssues;
    }

    /**
     * Returns all fixed issues: i.e. all issues, that are part of the reference report but are not present in the
     * current report anymore.
     *
     * @return the fixed issues
     */
    public Report getFixedIssues() {
        return fixedIssues;
    }

    /**
     * Returns statistics about the number of issues (total, new, delta).
     * 
     * @return the issues statistics
     */
    public IssuesStatistics getStatistics() {
        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();
        builder.setTotalSize(allIssues.size())
                .setTotalErrorSize(allIssues.getSizeOf(ERROR))
                .setTotalHighSize(allIssues.getSizeOf(WARNING_HIGH))
                .setTotalNormalSize(allIssues.getSizeOf(WARNING_NORMAL))
                .setTotalLowSize(allIssues.getSizeOf(WARNING_LOW));
        builder.setNewSize(newIssues.size())
                .setNewErrorSize(newIssues.getSizeOf(ERROR))
                .setNewHighSize(newIssues.getSizeOf(WARNING_HIGH))
                .setNewNormalSize(newIssues.getSizeOf(WARNING_NORMAL))
                .setNewLowSize(newIssues.getSizeOf(WARNING_LOW));
        builder.setDeltaSize(allIssues.size() - referenceIssues.size())
                .setDeltaErrorSize(allIssues.getSizeOf(ERROR) - referenceIssues.getSizeOf(ERROR))
                .setDeltaHighSize(allIssues.getSizeOf(WARNING_HIGH) - referenceIssues.getSizeOf(WARNING_HIGH))
                .setDeltaNormalSize(allIssues.getSizeOf(WARNING_NORMAL) - referenceIssues.getSizeOf(WARNING_NORMAL))
                .setDeltaLowSize(allIssues.getSizeOf(WARNING_LOW) - referenceIssues.getSizeOf(WARNING_LOW));

        return builder.build();
    }
}
