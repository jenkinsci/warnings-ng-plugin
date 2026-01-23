package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.forensics.miner.CommitDiffItem;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link AnnotatedReport}.
 *
 * @author Ullrich Hafner, Michael Schmid
 */
class AnnotatedReportTest {
    private static final String ID = "id";

    private static final Issue ISSUE1 = new IssueBuilder().setMessage("issue-1").build();
    private static final Issue ISSUE2 = new IssueBuilder().setMessage("issue-2").build();
    private static final Issue ISSUE3 = new IssueBuilder().setMessage("issue-3").build();

    private static final Report REPORT1 = new Report().add(ISSUE1);
    private static final Report REPORT2 = new Report().add(ISSUE2).add(ISSUE3);

    @Test
    void shouldCreateEmptyReport() {
        var report = new AnnotatedReport(ID);

        assertThat(report.getId()).isEqualTo(ID);
        assertThat(report.size()).isZero();
    }

    @Test
    void constructAnnotatedReportWithOneReport() {
        var sut = new AnnotatedReport(ID, REPORT1);

        assertThat(sut.getId()).isEqualTo(ID);
        assertThat(sut.size()).isEqualTo(1);
        assertThat(sut.getReport()).contains(ISSUE1);
        assertThat(sut.getSizeOfOrigin()).containsExactly(entry(ID, 1));
    }

    @Test
    void constructAnnotatedReportFromEmptyListOfAnnotatedReports() {
        List<AnnotatedReport> reports = new ArrayList<>();
        var sut = new AnnotatedReport(ID, reports);

        assertThat(sut.size()).isZero();
        assertThat(sut.getSizeOfOrigin()).isEmpty();
    }

    @Test
    void addTwoReportsWithoutActualIdToAnEmptyAnnotatedReport() {
        var sut = new AnnotatedReport("sut");
        sut.add(new AnnotatedReport("report-1", REPORT1));
        sut.add(new AnnotatedReport("report-2", REPORT2));

        assertThat(sut.getSizeOfOrigin()).containsExactly(entry("sut", 3));
        assertThreeIssuesOfReport(sut);
    }

    @Test
    void addTwoReportsWithActualIdToAnEmptyAnnotatedReport() {
        var sut = new AnnotatedReport("sut");
        sut.add(new AnnotatedReport("r1", REPORT1), "report-1");
        sut.add(new AnnotatedReport("r1", REPORT2), "report-2");

        assertThat(sut.getSizeOfOrigin()).containsOnly(entry("report-1", 1), entry("report-2", 2));
        assertThreeIssuesOfReport(sut);
    }

    @Test
    void constructAnnotatedReportWithListOfAnnotatedReports() {
        List<AnnotatedReport> annotatedReports = new ArrayList<>();
        annotatedReports.add(new AnnotatedReport("report-1", REPORT1));
        annotatedReports.add(new AnnotatedReport("report-2", REPORT2));

        var sut = new AnnotatedReport("sut", annotatedReports);

        assertThat(sut.getSizeOfOrigin()).containsOnly(entry("report-1", 1), entry("report-2", 2));
        assertThreeIssuesOfReport(sut);
    }

    @Test
    void addAllReportsToAnEmptyAnnotatedReport() {
        List<AnnotatedReport> annotatedReports = new ArrayList<>();
        annotatedReports.add(new AnnotatedReport("report-1", REPORT1));
        annotatedReports.add(new AnnotatedReport("report-2", REPORT2));

        var sut = new AnnotatedReport("sut");
        sut.addAll(annotatedReports);

        assertThat(sut.getSizeOfOrigin()).containsOnly(entry("report-1", 1), entry("report-2", 2));
        assertThreeIssuesOfReport(sut);
    }

    @Test
    void logInfoAtAnnotatedReport() {
        var sut = new AnnotatedReport("sut");

        sut.logInfo("info");

        assertThat(sut.getReport().getInfoMessages()).containsExactly("info");
    }

    @Test
    void shouldCreateDefensiveCopiesOfRepositoryStatisticsToAvoidConcurrentModification() {
        var repositoryStatistics = createRepositoryStatisticsWithFileStatistics();
        var originalReport = new AnnotatedReport("original", new Report(), new io.jenkins.plugins.forensics.blame.Blames(), repositoryStatistics);

        var aggregatedReport = new AnnotatedReport("aggregated");
        aggregatedReport.add(originalReport);

        var aggregatedStats = aggregatedReport.getStatistics();
        var originalStats = originalReport.getStatistics();

        assertThat(aggregatedStats.size()).isEqualTo(originalStats.size());
        assertThat(aggregatedStats.getFiles()).isEqualTo(originalStats.getFiles());

        for (String fileName : originalStats.getFiles()) {
            var originalFileStats = originalStats.get(fileName);
            var aggregatedFileStats = aggregatedStats.get(fileName);
            
            assertThat(aggregatedFileStats.getCommits()).isEqualTo(originalFileStats.getCommits());
            
            assertThat(aggregatedFileStats.getCommits()).isNotSameAs(originalFileStats.getCommits());
        }
    }

    private RepositoryStatistics createRepositoryStatisticsWithFileStatistics() {
        var statistics = new RepositoryStatistics();
        var treeStringBuilder = new TreeStringBuilder();
        TreeString fileName = treeStringBuilder.intern("TestFile.java");
        
        var commit1 = new CommitDiffItem("commit1", "author1", 1_234_567_890)
                .addLines(10)
                .deleteLines(5)
                .setNewPath(fileName);
        var commit2 = new CommitDiffItem("commit2", "author2", 1_234_567_900)
                .addLines(20)
                .deleteLines(3)
                .setNewPath(fileName);
        
        List<CommitDiffItem> commits = List.of(commit1, commit2);
        statistics.addAll(commits);
        
        return statistics;
    }

    private void assertThreeIssuesOfReport(final AnnotatedReport sut) {
        assertThat(sut.size()).isEqualTo(3);
        assertThat(sut.getReport().get(0)).isEqualTo(ISSUE1);
        assertThat(sut.getReport().get(1)).isEqualTo(ISSUE2);
        assertThat(sut.getReport().get(2)).isEqualTo(ISSUE3);
    }
}
