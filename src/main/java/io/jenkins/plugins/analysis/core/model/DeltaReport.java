package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory;

import hudson.model.Run;

/**
 * Provides the delta between the issues reports of two different builds.
 *
 * @author Ullrich Hafner
 */
public class DeltaReport {
    private static final Report EMPTY_REPORT = new Report();

    private final Report allIssues;
    private final Report outstandingIssues;
    private final Report newIssues;
    private final Report fixedIssues;
    private final String referenceJobName;
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
    public DeltaReport(final Report report, final AnalysisHistory history, final int currentBuildNumber) {
        allIssues = report;
        allIssues.setReference(String.valueOf(currentBuildNumber));
        if (history.getPreviousBuild().isPresent()) {
            Run<?, ?> build = history.getPreviousBuild().get();
            report.logInfo("Using reference build '%s' to compute new, fixed, and outstanding issues",
                    build);
            IssueDifference difference = new IssueDifference(report, currentBuildNumber, history.getPreviousIssues());

            outstandingIssues = difference.getOutstandingIssues();
            newIssues = difference.getNewIssues();
            fixedIssues = difference.getFixedIssues();
            report.logInfo("Issues delta (vs. reference build): outstanding: %d, new: %d, fixed: %d",
                    outstandingIssues.size(), newIssues.size(), fixedIssues.size());
            referenceJobName = build.getParent().getFullName();
            referenceBuildId = build.getId();
        }
        else {
            report.logInfo("No valid reference build found. All reported issues will be considered outstanding");
            report.forEach(issue -> issue.setReference(String.valueOf(currentBuildNumber)));
            outstandingIssues = report;
            newIssues = EMPTY_REPORT;
            fixedIssues = EMPTY_REPORT;
            referenceJobName = StringUtils.EMPTY;
            referenceBuildId = StringUtils.EMPTY;
        }
    }

    public boolean  isEmpty() {
        return allIssues.isEmpty();
    }

    public String getReferenceBuildId() {
        return referenceBuildId;
    }

    public String getReferenceJobName() {
        return referenceJobName;
    }

    public Report getAllIssues() {
        return allIssues;
    }

    public Report getOutstandingIssues() {
        return outstandingIssues;
    }

    public Report getNewIssues() {
        return newIssues;
    }

    public Report getFixedIssues() {
        return fixedIssues;
    }

    /**
     * Returns the total number of issues in this analysis run that have the specified {@link Severity}.
     *
     * @param severity
     *         the severity of the issues to match
     *
     * @return total number of issues
     */
    public int getTotalSizeOf(final Severity severity) {
        return allIssues.getSizeOf(severity);
    }

    /**
     * Returns the new number of issues in this analysis run that have the specified {@link Severity}.
     *
     * @param severity
     *         the severity of the issues to match
     *
     * @return total number of issues
     */
    public int getNewSizeOf(final Severity severity) {
        return newIssues.getSizeOf(severity);
    }

    public int getTotalSize() {
        return allIssues.size();
    }

    public int getNewSize() {
        return newIssues.size();
    }
}
