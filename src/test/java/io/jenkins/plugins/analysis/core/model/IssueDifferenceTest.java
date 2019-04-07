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

class IssueDifferenceTest {

    @Test
    void shouldCreateIssueDifference() {

        Report referenceIssues = new Report().addAll(createFilledIssueWithFingerprint("fixed 1", "fixed fingerprint 1"),
                createFilledIssueWithFingerprint("fixed 2", "fixed fingerprint 2"),
                createFilledIssueWithFingerprint("fixed 3", "fixed fingerprint 3"),
                createFilledIssueWithFingerprint("to fix 1", "to fix fingerprint 1"),
                createFilledIssueWithFingerprint("to fix 2", "to fix fingerprint 2"));

        Report currentIssues = new Report().addAll(createFilledIssueWithFingerprint("fixed 1", "fixed fingerprint 1"),
                createFilledIssueWithFingerprint("updated fixed 2", "fixed fingerprint 2"),
                createFilledIssueWithFingerprint("fixed 3", "fixed fingerprint 3"),
                createFilledIssueWithFingerprint("new 1", "new fingerprint 1"));

        IssueDifference issueDifference = new IssueDifference(currentIssues, 0, referenceIssues);

        assertThat(issueDifference.getFixedIssues().getSize()).isEqualTo(2);
        assertThat(issueDifference.getNewIssues().getSize()).isEqualTo(1);
        assertThat(issueDifference.getOutstandingIssues().getSize()).isEqualTo(3);
    }

    private Issue createFilledIssueWithFingerprint(final String message, final String fingerprint) {
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
                .setReference("reference");
        return builder.build();
    }
}
