package io.jenkins.plugins.analysis.core.steps;

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
    private final Issues<BuildIssue> newIssues;
    private final Issues<BuildIssue> fixedIssues;
    private final Issues<BuildIssue> oldIssues;

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
    public IssueDifference(final Issues<Issue> currentIssues, final int currentBuildNumber,
            final Issues<BuildIssue> referenceIssues) {
        newIssues = new Issues<>(currentIssues.stream().map(issue -> new BuildIssue(issue, currentBuildNumber)));
        fixedIssues = referenceIssues.copy();
        oldIssues = new Issues<>();

        for (Issue current : currentIssues) {
            Optional<BuildIssue> referenceToRemove = findReferenceByEquals(current);

            if (!referenceToRemove.isPresent()) {
                referenceToRemove = findReferenceByFingerprint(current);
            }

            if (referenceToRemove.isPresent()) {
                BuildIssue issueWithLatestProperties = newIssues.remove(current.getId());
                BuildIssue oldIssue = referenceToRemove.get();
                oldIssues.add(new BuildIssue(issueWithLatestProperties, oldIssue.getBuild()));
                fixedIssues.remove(oldIssue.getId());
            }
        }
    }

    private Optional<BuildIssue> findReferenceByFingerprint(final Issue current) {
        for (BuildIssue reference : fixedIssues) {
            if (current.getFingerprint().equals(reference.getFingerprint())) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }

    private Optional<BuildIssue> findReferenceByEquals(final Issue current) {
        for (BuildIssue reference : fixedIssues) {
            if (current.equals(reference)) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the old issues. I.e. all issues, that are part of the current and previous report.
     *
     * @return the old issues
     */
    public Issues<BuildIssue> getOldIssues() {
        return oldIssues;
    }

    /**
     * Returns the new issues. I.e. all issues, that are part of the current report but have not been shown up in the
     * previous report.
     *
     * @return the new issues
     */
    public Issues<BuildIssue> getNewIssues() {
        return newIssues;
    }

    /**
     * Returns the fixed issues. I.e. all issues, that are part of the previous report but are not present in the
     * current report anymore.
     *
     * @return the fixed issues
     */
    public Issues<BuildIssue> getFixedIssues() {
        return fixedIssues;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

