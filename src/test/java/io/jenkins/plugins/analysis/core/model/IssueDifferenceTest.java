package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.LineRange;
import edu.hm.hafner.analysis.LineRangeList;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import static java.util.Collections.*;

/**
 * Unit Tests of the class {@link IssueDifference}.
 *
 * @author Artem Polovyi
 */
class IssueDifferenceTest {
    private static final String REFERENCE_BUILD = "100";
    private static final String CURRENT_BUILD = "2";

    /**
     * Verifies that issue difference report is created correctly.
     */
    @Test
    void shouldCreateIssueDifference() {
        Report referenceIssues = new Report().addAll(
                createIssue("OUTSTANDING 1", "OUT 1"),
                createIssue("OUTSTANDING 2", "OUT 2"),
                createIssue("OUTSTANDING 3", "OUT 3"),
                createIssue("TO FIX 1", "FIX 1"),
                createIssue("TO FIX 2", "FIX 2"));

        Report currentIssues = new Report().addAll(
                createIssue("UPD OUTSTANDING 1", "OUT 1"),
                createIssue("OUTSTANDING 2", "UPD OUT 2"),
                createIssue("OUTSTANDING 3", "OUT 3"),
                createIssue("NEW 1", "NEW 1"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 2, referenceIssues);

        Report outstanding = issueDifference.getOutstandingIssues();
        assertThat(outstanding).hasSize(3);
        assertThat(outstanding.get(0)).hasMessage("OUTSTANDING 2").hasReference(REFERENCE_BUILD);
        assertThat(outstanding.get(1)).hasMessage("OUTSTANDING 3").hasReference(REFERENCE_BUILD);
        assertThat(outstanding.get(2)).hasMessage("UPD OUTSTANDING 1").hasReference(REFERENCE_BUILD);

        Report fixed = issueDifference.getFixedIssues();
        assertThat(fixed).hasSize(2);
        assertThat(fixed.get(0)).hasMessage("TO FIX 1").hasReference(REFERENCE_BUILD);
        assertThat(fixed.get(1)).hasMessage("TO FIX 2").hasReference(REFERENCE_BUILD);

        assertThat(issueDifference.getNewIssues()).hasSize(1);
        assertThat(issueDifference.getNewIssues().get(0)).hasMessage("NEW 1").hasReference("2");
    }

    /**
     * Verifies that issue difference report has only outstanding issues when current report and reference report have
     * same issues.
     */
    @Test
    void shouldCreateOutstandingIssueDifference() {
        shouldFindOutstandingFromEqualsOrFingerprint("NEW", "OLD");
        shouldFindOutstandingFromEqualsOrFingerprint("OLD", "OLD");
        shouldFindOutstandingFromEqualsOrFingerprint("OLD", "NEW");
    }

    private void shouldFindOutstandingFromEqualsOrFingerprint(
            final String currentMessage, final String currentFingerprint) {
        Report referenceIssues = new Report().add(createIssue("OLD", "OLD"));
        Report currentIssues = new Report().add(createIssue(currentMessage, currentFingerprint));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 2, referenceIssues);

        assertThat(issueDifference.getFixedIssues()).isEmpty();
        assertThat(issueDifference.getNewIssues()).isEmpty();
        Report outstanding = issueDifference.getOutstandingIssues();

        assertThat(outstanding).hasSize(1);
        assertThat(outstanding.get(0))
                .hasMessage(currentMessage)
                .hasFingerprint(currentFingerprint)
                .hasReference(REFERENCE_BUILD);
    }

    /**
     * Verifies that issue difference report has only fixed issues when current report is empty.
     */
    @Test
    void shouldCreateIssueDifferenceWithEmptyCurrent() {
        Report referenceIssues = new Report().addAll(createIssue("OLD 1", "FA"),
                createIssue("OLD 2", "FB"));
        Report currentIssues = new Report();

        IssueDifference issueDifference = new IssueDifference(currentIssues, 2, referenceIssues);

        assertThat(issueDifference.getNewIssues()).isEmpty();
        assertThat(issueDifference.getOutstandingIssues()).isEmpty();

        Report fixed = issueDifference.getFixedIssues();

        assertThat(fixed).hasSize(2);
        assertThat(fixed.get(0)).hasMessage("OLD 1").hasReference(REFERENCE_BUILD);
        assertThat(fixed.get(1)).hasMessage("OLD 2").hasReference(REFERENCE_BUILD);
    }

    /**
     * Verifies that issue difference report has only new issues when reference report is empty.
     */
    @Test
    void shouldCreateIssueDifferenceWithEmptyReference() {
        Report referenceIssues = new Report();
        Report currentIssues = new Report().addAll(createIssue("NEW 1", "FA"),
                createIssue("NEW 2", "FB"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 2, referenceIssues);

        assertThat(issueDifference.getFixedIssues()).isEmpty();
        assertThat(issueDifference.getOutstandingIssues()).isEmpty();

        Report newIssues = issueDifference.getNewIssues();
        assertThat(newIssues).hasSize(2);
        assertThat(newIssues.get(0)).hasMessage("NEW 1").hasReference(CURRENT_BUILD);
        assertThat(newIssues.get(1)).hasMessage("NEW 2").hasReference(CURRENT_BUILD);
    }

    /**
     * Verifies that if two issues have the same fingerprint then equals is used to select the matching issue in the
     * reference build.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-56324">Issue 56324</a>
     */
    @Test
    void shouldAlsoUseFingerprintIfIssuesAreEqual() {
        Report referenceIssues = new Report().addAll(
                createIssue("OLD 1", "FP"));
        Report currentIssues = new Report().addAll(
                createIssue("NEW 1", "FP"),
                createIssue("OLD 1", "FP"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 2, referenceIssues);

        assertThat(issueDifference.getFixedIssues()).isEmpty();

        Report outstandingIssues = issueDifference.getOutstandingIssues();
        assertThat(outstandingIssues).hasSize(1);
        assertThat(outstandingIssues.get(0)).hasMessage("OLD 1").hasReference(REFERENCE_BUILD);

        Report newIssues = issueDifference.getNewIssues();
        assertThat(newIssues).hasSize(1);
        assertThat(newIssues.get(0)).hasMessage("NEW 1").hasReference(CURRENT_BUILD);
    }

    private Issue createIssue(final String message, final String fingerprint) {
        IssueBuilder builder = new IssueBuilder();
        builder.setFileName("file-name")
                .setLineStart(1)
                .setLineEnd(2)
                .setColumnStart(3)
                .setColumnEnd(4)
                .setCategory("category")
                .setType("type")
                .setPackageName("package-name")
                .setModuleName("module-name")
                .setSeverity(Severity.WARNING_HIGH)
                .setMessage(message)
                .setDescription("description")
                .setOrigin("origin")
                .setLineRanges(new LineRangeList(singletonList(new LineRange(5, 6))))
                .setFingerprint(fingerprint)
                .setReference(REFERENCE_BUILD);
        return builder.build();
    }
}
