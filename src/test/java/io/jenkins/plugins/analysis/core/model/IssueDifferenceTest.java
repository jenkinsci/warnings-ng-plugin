package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.LineRange;
import edu.hm.hafner.analysis.LineRangeList;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests of the class {@link IssueDifference}.
 *
 * @author Artem Polovyi
 */
class IssueDifferenceTest {
    /**
     * Verifies that issue difference report is created correctly.
     */
    @Test
    public void shouldCreateIssueDifference() {
        Report referenceIssues = new Report().addAll(
                createFilledIssueForIssueDifference("FIXED 1", "FF 1", "100"),
                createFilledIssueForIssueDifference("FIXED 2", "FF 2", "100"),
                createFilledIssueForIssueDifference("FIXED 3", "FF 3", "100"),
                createFilledIssueForIssueDifference("TO FIX 1", "TF 1", "100"),
                createFilledIssueForIssueDifference("TO FIX 2", "TF 2", "100"));

        Report currentIssues = new Report().addAll(
                createFilledIssueForIssueDifference("UPD FIXED 1", "FF 1", "100"),
                createFilledIssueForIssueDifference("UPD FIXED 2", "FF 2", "100"),
                createFilledIssueForIssueDifference("FIXED 3", "FF 3", "100"),
                createFilledIssueForIssueDifference("NEW 1", "NF 1", "100"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 200, referenceIssues);

        assertThat(issueDifference.getOutstandingIssues()).hasSize(3);
        assertThat(issueDifference.getOutstandingIssues().get(0).getMessage()).isEqualTo("UPD FIXED 1");
        assertThat(issueDifference.getOutstandingIssues().get(1).getMessage()).isEqualTo("UPD FIXED 2");
        assertThat(issueDifference.getOutstandingIssues().get(2).getMessage()).isEqualTo("FIXED 3");
        assertThat(issueDifference.getOutstandingIssues().get(0).getReference()).isEqualTo("100");
        assertThat(issueDifference.getOutstandingIssues().get(1).getReference()).isEqualTo("100");
        assertThat(issueDifference.getOutstandingIssues().get(2).getReference()).isEqualTo("100");

        assertThat(issueDifference.getFixedIssues()).hasSize(2);
        assertThat(issueDifference.getFixedIssues().get(0).getMessage()).isEqualTo("TO FIX 1");
        assertThat(issueDifference.getFixedIssues().get(1).getMessage()).isEqualTo("TO FIX 2");
        assertThat(issueDifference.getFixedIssues().get(0).getReference()).isEqualTo("100");
        assertThat(issueDifference.getFixedIssues().get(1).getReference()).isEqualTo("100");

        assertThat(issueDifference.getNewIssues()).hasSize(1);
        assertThat(issueDifference.getNewIssues().get(0).getMessage()).isEqualTo("NEW 1");
        assertThat(issueDifference.getNewIssues().get(0).getReference()).isEqualTo("200");
    }

    /**
     * Verifies that issue difference report has only outstanding issues when current report and reference report have
     * same issues.
     */
    @Test
    public void shouldCreateOutstandingIssueDifference() {
        Report currentIssues = new Report().add(createFilledIssueForIssueDifference("A NEW", "FA", "100"));
        Report referenceIssues = new Report().add(createFilledIssueForIssueDifference("A OLD", "FA", "100"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 200, referenceIssues);

        assertThat(issueDifference.getFixedIssues()).isEmpty();
        assertThat(issueDifference.getNewIssues()).isEmpty();
        assertThat(issueDifference.getOutstandingIssues()).hasSize(1);
        assertThat(issueDifference.getOutstandingIssues().get(0).getMessage()).isEqualTo("A NEW");
        assertThat(issueDifference.getOutstandingIssues().get(0).getReference()).isEqualTo("100");
    }

    /**
     * Verifies that issue difference report has only fixed issues when current report is empty.
     */
    @Test
    public void shouldCreateIssueDifferenceWithEmptyCurrent() {
        Report currentIssues = new Report();
        Report referenceIssues = new Report().addAll(createFilledIssueForIssueDifference("OLD 1", "FA", "100"),
                createFilledIssueForIssueDifference("OLD 2", "FB", "100"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 200, referenceIssues);

        assertThat(issueDifference.getFixedIssues()).hasSize(2);
        assertThat(issueDifference.getNewIssues()).isEmpty();
        assertThat(issueDifference.getOutstandingIssues()).isEmpty();
        assertThat(issueDifference.getFixedIssues().get(0).getMessage()).isEqualTo("OLD 1");
        assertThat(issueDifference.getFixedIssues().get(1).getMessage()).isEqualTo("OLD 2");
        assertThat(issueDifference.getFixedIssues().get(0).getReference()).isEqualTo("100");
        assertThat(issueDifference.getFixedIssues().get(1).getReference()).isEqualTo("100");
    }

    /**
     * Verifies that issue difference report has only new issues when reference report is empty.
     */
    @Test
    public void shouldCreateIssueDifferenceWithEmptyReference() {
        Report referenceIssues = new Report();
        Report currentIssues = new Report().addAll(createFilledIssueForIssueDifference("NEW 1", "FA", "100"),
                createFilledIssueForIssueDifference("NEW 2", "FB", "100"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 200, referenceIssues);

        assertThat(issueDifference.getNewIssues()).hasSize(2);
        assertThat(issueDifference.getFixedIssues()).isEmpty();
        assertThat(issueDifference.getOutstandingIssues()).isEmpty();
        assertThat(issueDifference.getNewIssues().get(0).getMessage()).isEqualTo("NEW 1");
        assertThat(issueDifference.getNewIssues().get(1).getMessage()).isEqualTo("NEW 2");
        assertThat(issueDifference.getNewIssues().get(0).getReference()).isEqualTo("200");
        assertThat(issueDifference.getNewIssues().get(1).getReference()).isEqualTo("200");
    }

    /**
     * Verifies that issue difference report can distinguish issues based on fingerprint.
     */
    @Test
    public void shouldCreateIssueDifferenceWithNewAndOutstandingIssue() {
        Report referenceIssues = new Report().add(createFilledIssueForIssueDifference("B", "F", "100"));
        Report currentIssues = new Report().addAll(createFilledIssueForIssueDifference("A", "F1", "100"),
                createFilledIssueForIssueDifference("B", "F", "100"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 200, referenceIssues);

        assertThat(issueDifference.getFixedIssues()).isEmpty();
        assertThat(issueDifference.getNewIssues()).hasSize(1);
        assertThat(issueDifference.getOutstandingIssues()).hasSize(1);
        assertThat(issueDifference.getNewIssues().get(0).getMessage()).isEqualTo("A");
        assertThat(issueDifference.getNewIssues().get(0).getReference()).isEqualTo("200");
        assertThat(issueDifference.getOutstandingIssues().get(0).getMessage()).isEqualTo("B");
        assertThat(issueDifference.getOutstandingIssues().get(0).getReference()).isEqualTo("100");
    }

    private Issue createFilledIssueForIssueDifference(final String message, final String fingerprint,
            final String reference) {
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
                .setReference(reference);
        return builder.build();
    }
}
