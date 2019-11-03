package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.ForensicsModel.ForensicsRow;
import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ForensicsModel}.
 *
 * @author Ullrich Hafner
 */
class ForensicsModelTest extends AbstractDetailsModelTest {
    private static final int EXPECTED_COLUMNS_SIZE = 7;
    private static final String FILE_NAME = "/path/to/file-1";

    @Test
    void shouldConvertIssueToArrayWithAllColumnsAndRows() {
        Report report = new Report();
        report.add(createIssue(1));
        report.add(createIssue(2));
        RepositoryStatistics statistics = mock(RepositoryStatistics.class);

        ForensicsModel model = createModel(report, statistics);

        assertThat(model.getHeaders()).hasSize(EXPECTED_COLUMNS_SIZE);
        assertThat(model.getWidths()).hasSize(EXPECTED_COLUMNS_SIZE);
        assertThat(model.getColumnsDefinition()).isEqualTo("["
                + "{\"data\": \"description\"},"
                + "{"
                + "  \"type\": \"string\","
                + "  \"data\": \"fileName\","
                + "  \"render\": {"
                + "     \"_\": \"display\","
                + "     \"sort\": \"sort\""
                + "  }"
                + "},"
                + "{\"data\": \"age\"},"
                + "{\"data\": \"authorsSize\"},"
                + "{\"data\": \"commitsSize\"},"
                + "{"
                + "  \"type\": \"num\","
                + "  \"data\": \"modifiedDays\","
                + "  \"render\": {"
                + "     \"_\": \"display\","
                + "     \"sort\": \"sort\""
                + "  }"
                + "},"
                + "{"
                + "  \"type\": \"num\","
                + "  \"data\": \"addedDays\","
                + "  \"render\": {"
                + "     \"_\": \"display\","
                + "     \"sort\": \"sort\""
                + "  }"
                + "}"
                + "]");
        assertThat(model.getRows()).hasSize(2);
    }

    @Test
    void shouldShowIssueWithForensics() {
        Report report = new Report();
        Issue issue = createIssue(1);
        report.add(issue);

        RepositoryStatistics statistics = mock(RepositoryStatistics.class);

        FileStatistics fileStatistics = mock(FileStatistics.class);
        when(fileStatistics.getNumberOfAuthors()).thenReturn(15);
        when(fileStatistics.getNumberOfCommits()).thenReturn(20);
        when(fileStatistics.getLastModifiedInDays()).thenReturn(25);
        when(fileStatistics.getAgeInDays()).thenReturn(30);

        when(statistics.get(FILE_NAME)).thenReturn(fileStatistics);
        when(statistics.contains(FILE_NAME)).thenReturn(true);

        ForensicsModel model = createModel(report, statistics);

        ForensicsRow actualRow = model.getRow(issue);
        assertThat(actualRow).hasDescription(EXPECTED_DESCRIPTION)
                .hasAge("1")
                .hasAuthorsSize("15")
                .hasCommitsSize("20");

        assertThatDetailedColumnContains(actualRow.getFileName(),
                createExpectedFileName(issue), "/path/to/file-1:0000015");

        assertThatDetailedColumnContains(actualRow.getModifiedDays(),
                "4 weeks ago", "25");
        assertThatDetailedColumnContains(actualRow.getAddedDays(),
                "1 month ago", "30");
    }

    @Test
    void shouldShowIssueWithoutForensics() {
        Report report = new Report();
        Issue issue = createIssue(1);
        report.add(issue);

        RepositoryStatistics blames = mock(RepositoryStatistics.class);

        ForensicsModel model = createModel(report, blames);

        ForensicsRow actualRow = model.getRow(issue);
        assertThat(actualRow).hasDescription(EXPECTED_DESCRIPTION)
                .hasAge("1")
                .hasAuthorsSize(ForensicsModel.UNDEFINED)
                .hasCommitsSize(ForensicsModel.UNDEFINED);

        assertThatDetailedColumnContains(actualRow.getFileName(),
                createExpectedFileName(issue), "/path/to/file-1:0000015");

        assertThat(actualRow.getModifiedDays().getSort()).isEqualTo("0");
        assertThat(actualRow.getAddedDays().getSort()).isEqualTo("0");
    }

    private ForensicsModel createModel(final Report report, final RepositoryStatistics statistics) {
        return new ForensicsModel(report, statistics, createFileNameRenderer(), createAgeBuilder(), issue -> DESCRIPTION);
    }
}
