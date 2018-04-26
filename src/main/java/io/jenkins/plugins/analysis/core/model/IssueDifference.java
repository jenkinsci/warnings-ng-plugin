package io.jenkins.plugins.analysis.core.model;

import java.util.Optional;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;

/**
 * Computes old, new, and fixed issues based on the reports of two consecutive static analysis runs for the same
 * software artifact.
 *
 * @author Ullrich Hafner
 */
public class IssueDifference {
    private final Issues newIssues;
    private final Issues fixedIssues;
    private final Issues outstandingIssues;

    /**
     * Creates a new instance of {@link IssueDifference}.
     *
     * @param currentIssues
     *         the issues of the current report
     * @param currentBuildNumber
     *         number of the current build
     * @param referenceIssues
     *         the issues of a previous report (reference)
     */
    public IssueDifference(final Issues currentIssues, final int currentBuildNumber,
            final Issues referenceIssues) {
        newIssues = currentIssues.copy();
        fixedIssues = referenceIssues.copy();
        outstandingIssues = new Issues();

        for (Issue current : currentIssues) {
            Optional<Issue> referenceToRemove = findReferenceByEquals(current);

            if (!referenceToRemove.isPresent()) {
                referenceToRemove = findReferenceByFingerprint(current);
            }

            if (referenceToRemove.isPresent()) {
                Issue oldIssue = referenceToRemove.get();
                Issue issueWithLatestProperties = newIssues.remove(current.getId());
                issueWithLatestProperties.setReference(oldIssue.getReference());
                outstandingIssues.add(issueWithLatestProperties);
                fixedIssues.remove(oldIssue.getId());
            }
        }
        newIssues.forEach(issue -> issue.setReference(String.valueOf(currentBuildNumber)));
    }

    private Optional<Issue> findReferenceByFingerprint(final Issue current) {
        for (Issue reference : fixedIssues) {
            if (current.getFingerprint().equals(reference.getFingerprint())) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }

    private Optional<Issue> findReferenceByEquals(final Issue current) {
        for (Issue reference : fixedIssues) {
            if (current.equals(reference)) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the outstanding issues. I.e. all issues, that are part of the previous report and that are still part of
     * the current report.
     *
     * @return the outstanding issues
     */
    public Issues getOutstandingIssues() {
        return outstandingIssues;
    }

    /**
     * Returns the new issues. I.e. all issues, that are part of the current report but that have not been shown up in
     * the previous report.
     *
     * @return the new issues
     */
    public Issues getNewIssues() {
        return newIssues;
    }

    /**
     * Returns the fixed issues. I.e. all issues, that are part of the previous report but that are not present in the
     * current report anymore.
     *
     * @return the fixed issues
     */
    public Issues getFixedIssues() {
        return fixedIssues;
    }
}

