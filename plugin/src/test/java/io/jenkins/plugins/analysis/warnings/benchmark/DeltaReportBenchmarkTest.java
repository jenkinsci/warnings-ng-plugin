package io.jenkins.plugins.analysis.warnings.benchmark;

import java.util.Optional;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import hudson.model.Run;
import jenkins.benchmark.jmh.JmhBenchmark;

import io.jenkins.plugins.analysis.core.model.DeltaReport;
import io.jenkins.plugins.analysis.core.model.History;
import io.jenkins.plugins.analysis.core.util.IssuesStatistics;

import static org.mockito.Mockito.*;

/**
 * Performance benchmarks for {@link DeltaReport}.
 *
 * @author Kevin Richter
 * @author Simon Schönwiese
 */
@JmhBenchmark
public class DeltaReportBenchmarkTest {
    /**
     * Benchmarking for the creation of {@link IssuesStatistics} based on a {@link DeltaReport}.
     * @param state a {@link BenchmarkState} object containing the predefined objects for the test
     * @param blackhole a {@link Blackhole} to avoid dead code elminination
     */
    @Benchmark
    public void benchmarkIssueStatisticsCreation(final BenchmarkState state, final Blackhole blackhole) {
        IssuesStatistics issuesStatistics = state.getReportForIssueStatistics().getStatistics();
        blackhole.consume(issuesStatistics);
    }

    /**
     * Benchmarking for the creation of a {@link DeltaReport}.
     * @param state a {@link BenchmarkState} object containing the predefined objects for the test
     * @param blackhole a {@link Blackhole} to avoid dead code elminination
     */
    @Benchmark
    public void benchmarkDeltaReportCreation(final BenchmarkState state, final Blackhole blackhole) {
        DeltaReport deltaReport = new DeltaReport(state.getNewIssuesReport(), state.getHistory(), 0);
        blackhole.consume(deltaReport);
    }

    /**
     * State for the benchmark containing all preconfigured and necessary objects.
     */
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private Report newIssuesReport;
        private History history;
        private DeltaReport reportForIssueStatistics;

        public Report getNewIssuesReport() {
            return newIssuesReport;
        }

        public History getHistory() {
            return history;
        }

        public DeltaReport getReportForIssueStatistics() {
            return reportForIssueStatistics;
        }

        /**
         * Initializes reports and history for the benchmarks.
         */
        @Setup(Level.Iteration)
        public void doSetup() {
            Report referenceIssuesReport;
            Run<?, ?> run = mock(Run.class);
            when(run.getExternalizableId()).thenReturn("refBuildId");

            history = mock(History.class);
            when(history.getBuild()).thenReturn(Optional.of(run));

            Issue issue = getIssue("issue");
            Issue fixedIssue = getIssue("fixedIssue");
            Issue newIssue = getIssue("newIssue");
            Issue warningLow = getIssueWithSeverity("warning1", Severity.WARNING_LOW);
            Issue warningHigh = getIssueWithSeverity("warning2", Severity.WARNING_HIGH);
            Issue error = getIssueWithSeverity("error", Severity.ERROR);

            referenceIssuesReport = new Report();
            referenceIssuesReport.add(issue);
            referenceIssuesReport.add(fixedIssue);
            referenceIssuesReport.add(warningLow);
            when(history.getIssues()).thenReturn(referenceIssuesReport);

            newIssuesReport = new Report();
            newIssuesReport.add(issue);
            newIssuesReport.add(newIssue);
            newIssuesReport.add(error);
            newIssuesReport.add(warningLow);
            newIssuesReport.add(warningHigh);

            reportForIssueStatistics = new DeltaReport(newIssuesReport, history, 0);
        }

        private Issue getIssue(final String name) {
            return new IssueBuilder().setFileName(name).setFingerprint(name).build();
        }

        private Issue getIssueWithSeverity(final String name, final Severity severity) {
            return new IssueBuilder().setFileName(name).setFingerprint(name).setSeverity(severity).build();
        }
    }
}
