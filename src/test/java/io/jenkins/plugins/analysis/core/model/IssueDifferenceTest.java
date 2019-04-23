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
    void shouldCreateIssueDifference() {
        Report referenceIssues = new Report().addAll(
                createIssue("OUTSTANDING 1", "FF 1"),
                createIssue("OUTSTANDING 2", "FF 2"),
                createIssue("OUTSTANDING 3", "FF 3"),
                createIssue("TO FIX 1", "TF 1"),
                createIssue("TO FIX 2", "TF 2"));

        Report currentIssues = new Report().addAll(
                createIssue("UPD OUTSTANDING 1", "FF 1"),
                createIssue("UPD OUTSTANDING 2", "FF 2"),
                createIssue("OUTSTANDING 3", "FF 3"),
                createIssue("NEW 1", "NF 1"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 200, referenceIssues);

        assertThat(issueDifference.getOutstandingIssues()).hasSize(3);
        assertThat(issueDifference.getOutstandingIssues().get(0).getMessage()).isEqualTo("UPD OUTSTANDING 1");
        assertThat(issueDifference.getOutstandingIssues().get(1).getMessage()).isEqualTo("UPD OUTSTANDING 2");
        assertThat(issueDifference.getOutstandingIssues().get(2).getMessage()).isEqualTo("OUTSTANDING 3");
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
    void shouldCreateOutstandingIssueDifference() {
        Report currentIssues = new Report().add(createIssue("A NEW", "FA"));
        Report referenceIssues = new Report().add(createIssue("A OLD", "FA"));

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
    void shouldCreateIssueDifferenceWithEmptyCurrent() {
        Report currentIssues = new Report();
        Report referenceIssues = new Report().addAll(createIssue("OLD 1", "FA"),
                createIssue("OLD 2", "FB"));

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
    void shouldCreateIssueDifferenceWithEmptyReference() {
        Report referenceIssues = new Report();
        Report currentIssues = new Report().addAll(createIssue("NEW 1", "FA"),
                createIssue("NEW 2", "FB"));

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
    void shouldCreateIssueDifferenceWithNewAndOutstandingIssue() {
        Report referenceIssues = new Report().add(createIssue("B", "F"));
        Report currentIssues = new Report().addAll(createIssue("A", "F1"),
                createIssue("B", "F"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 200, referenceIssues);

        assertThat(issueDifference.getFixedIssues()).isEmpty();
        assertThat(issueDifference.getNewIssues()).hasSize(1);
        assertThat(issueDifference.getOutstandingIssues()).hasSize(1);
        assertThat(issueDifference.getNewIssues().get(0).getMessage()).isEqualTo("A");
        assertThat(issueDifference.getNewIssues().get(0).getReference()).isEqualTo("200");
        assertThat(issueDifference.getOutstandingIssues().get(0).getMessage()).isEqualTo("B");
        assertThat(issueDifference.getOutstandingIssues().get(0).getReference()).isEqualTo("100");
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
                .setReference("100");
        return builder.build();
    }
}
