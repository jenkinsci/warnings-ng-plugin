package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ForensicsModel}.
 *
 * @author Ullrich Hafner
 */
class ForensicsModelTest extends AbstractDetailsModelTest {
    private static final String FILE_NAME = "/path/to/file-1";

    @Test
    void shouldConvertIssueToArrayWithAllColumnsAndRows() {
        var report = new Report();
        report.add(createIssue(1));
        report.add(createIssue(2));
        RepositoryStatistics statistics = mock(RepositoryStatistics.class);

        var model = createModel(report, statistics);

        var columnDefinitions = model.getColumnsDefinition();
        assertThatJson(columnDefinitions).isArray().hasSize(10);

        String[] columns = {"description", "fileName", "age", "authorsSize",
                "commitsSize", "modifiedAt", "addedAt", "linesOfCode", "churn", "message"};
        for (int column = 0; column < columns.length; column++) {
            verifyColumnProperty(model, column, columns[column]);
        }
        verifyFileNameColumn(columnDefinitions);

        assertThat(getLabels(model))
                .containsExactly("Details", "File", "Age", "#Authors",
                        "#Commits", "Last Commit", "Added", "#LoC", "Code Churn", "Hiddendetails");

        assertThat(model.getRows()).hasSize(2);
    }

    @Test
    void shouldShowIssueWithForensics() {
        var report = new Report();
        var issue = createIssue(1);
        report.add(issue);

        RepositoryStatistics statistics = mock(RepositoryStatistics.class);

        FileStatistics fileStatistics = mock(FileStatistics.class);
        when(fileStatistics.getNumberOfAuthors()).thenReturn(15);
        when(fileStatistics.getNumberOfCommits()).thenReturn(20);
        when(fileStatistics.getLastModificationTime()).thenReturn(25);
        when(fileStatistics.getCreationTime()).thenReturn(30);

        when(statistics.get(FILE_NAME)).thenReturn(fileStatistics);
        when(statistics.contains(FILE_NAME)).thenReturn(true);

        var model = createModel(report, statistics);

        var actualRow = model.getRow(issue);
        assertThat(actualRow).hasDescription(EXPECTED_DESCRIPTION)
                .hasAge("1")
                .hasAuthorsSize("15")
                .hasCommitsSize("20")
                .hasModifiedAt(25)
                .hasAddedAt(30);

        assertThatDetailedColumnContains(actualRow.getFileName(),
                createExpectedFileName(issue), "/path/to/file-1:0000015");
    }

    @Test
    void shouldShowIssueWithoutForensics() {
        var report = new Report();
        var issue = createIssue(1);
        report.add(issue);

        RepositoryStatistics blames = mock(RepositoryStatistics.class);

        var model = createModel(report, blames);

        var actualRow = model.getRow(issue);
        assertThat(actualRow).hasDescription(EXPECTED_DESCRIPTION)
                .hasAge("1")
                .hasAuthorsSize(ForensicsModel.UNDEFINED)
                .hasCommitsSize(ForensicsModel.UNDEFINED);

        assertThatDetailedColumnContains(actualRow.getFileName(),
                createExpectedFileName(issue), "/path/to/file-1:0000015");
    }

    private ForensicsModel createModel(final Report report, final RepositoryStatistics statistics) {
        return new ForensicsModel(report, statistics, createFileNameRenderer(), createAgeBuilder(),
                issue -> DESCRIPTION, createJenkinsFacade());
    }
}
