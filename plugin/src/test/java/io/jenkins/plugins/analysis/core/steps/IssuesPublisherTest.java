package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.FilteredLog;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.DeltaReport;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.forensics.delta.Change;
import io.jenkins.plugins.forensics.delta.ChangeEditType;
import io.jenkins.plugins.forensics.delta.Delta;
import io.jenkins.plugins.forensics.delta.DeltaCalculator;
import io.jenkins.plugins.forensics.delta.FileChanges;
import io.jenkins.plugins.forensics.delta.FileEditType;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.NullResultHandler;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link IssuesPublisher}.
 *
 * @author Akash Manna
 */
class IssuesPublisherTest {
    private static final String ID = "test-id";
    private static final String MODIFIED_FILE = "modified-file.java";
    private static final String DELTA_ERROR = "Calculating the Git code delta is not possible due to an unknown commit ID";

    @Test
    void shouldMarkIssuesInModifiedCodeIfDeltaIsAvailable() {
        var issues = createReportWithIssueInLine(11);
        var publisher = createPublisher(issues, createDeltaCalculator(Optional.of(createDelta()), false));

        assertThat(publisher.markIssuesInModifiedFiles(mock(Run.class), issues, new DeltaReport(issues, 1)))
                .isTrue();
        assertThat(issues.getInfoMessages())
                .contains("Detect all issues that are part of modified code",
                        "Issues in modified code: 1 (new: 0, outstanding: 1)");
        assertThat(issues.getErrorMessages()).isEmpty();
        assertThat(issues.get(0).isPartOfModifiedCode()).isTrue();
    }

    @Test
    void shouldReportNoModifiedCodeIfDeltaIsEmpty() {
        var issues = createReportWithIssueInLine(11);
        var publisher = createPublisher(issues, createDeltaCalculator(Optional.empty(), false));

        assertThat(publisher.markIssuesInModifiedFiles(mock(Run.class), issues, new DeltaReport(issues, 1)))
                .isTrue();
        assertThat(issues.getInfoMessages())
                .contains("Detect all issues that are part of modified code", "No relevant modified code found");
        assertThat(issues.getErrorMessages()).isEmpty();
        assertThat(issues.get(0).isPartOfModifiedCode()).isFalse();
    }

    /**
     * Verifies that a failing delta computation is not silently reported as 'no modified code'. Otherwise, all quality
     * gates that are based on the modified code would pass silently.
     *
     * @see <a href="https://github.com/jenkinsci/warnings-ng-plugin/issues/3380">Issue 3380</a>
     */
    @Test
    void shouldReportUnknownModifiedCodeIfDeltaComputationFails() {
        var issues = createReportWithIssueInLine(11);
        var publisher = createPublisher(issues, createDeltaCalculator(Optional.empty(), true));

        assertThat(publisher.markIssuesInModifiedFiles(mock(Run.class), issues, new DeltaReport(issues, 1)))
                .isFalse();
        assertThat(issues.getInfoMessages())
                .contains("Detect all issues that are part of modified code",
                        "Skipping detection of issues in modified code since the SCM delta is not available")
                .doesNotContain("No relevant modified code found");
        assertThat(issues.getErrorMessages()).contains(DELTA_ERROR);
        assertThat(issues.get(0).isPartOfModifiedCode()).isFalse();
    }

    @Test
    void shouldSkipDetectionOfModifiedCodeIfThereAreNoIssues() {
        var issues = new Report();
        var deltaCalculator = createDeltaCalculator(Optional.empty(), true);
        var publisher = createPublisher(issues, deltaCalculator);

        assertThat(publisher.markIssuesInModifiedFiles(mock(Run.class), issues, new DeltaReport(issues, 1)))
                .isTrue();
        assertThat(issues.getInfoMessages()).contains("Skip detection of issues in modified code");
        assertThat(issues.getErrorMessages()).isEmpty();

        verify(deltaCalculator, never()).calculateDelta(any(), any(), any(FilteredLog.class));
    }

    private Report createReportWithIssueInLine(final int lineStart) {
        var report = new Report();
        try (var builder = new IssueBuilder()) {
            report.add(builder.setFileName(MODIFIED_FILE).setLineStart(lineStart).setFingerprint(MODIFIED_FILE).build());
        }
        return report;
    }

    private Delta createDelta() {
        var change = new Change(ChangeEditType.INSERT, 10, 12, 10, 12);
        var fileChanges = new FileChanges(MODIFIED_FILE, MODIFIED_FILE, "content", FileEditType.MODIFY,
                Map.of(ChangeEditType.INSERT, Set.of(change)));

        return new Delta("current", "reference", Map.of(MODIFIED_FILE, fileChanges));
    }

    private DeltaCalculator createDeltaCalculator(final Optional<Delta> delta, final boolean withError) {
        DeltaCalculator deltaCalculator = mock(DeltaCalculator.class);
        when(deltaCalculator.calculateDelta(any(), any(), any(FilteredLog.class)))
                .thenAnswer(invocation -> {
                    if (withError) {
                        invocation.getArgument(2, FilteredLog.class).logError(DELTA_ERROR);
                    }
                    return delta;
                });
        return deltaCalculator;
    }

    private IssuesPublisher createPublisher(final Report issues, final DeltaCalculator deltaCalculator) {
        return new IssuesPublisher(mock(Run.class), new AnnotatedReport(ID, issues), deltaCalculator,
                new HealthDescriptor(0, 0, Severity.WARNING_LOW), List.of(), "name", "icon", false,
                StandardCharsets.UTF_8, mock(LogHandler.class), new NullResultHandler(), false);
    }
}
