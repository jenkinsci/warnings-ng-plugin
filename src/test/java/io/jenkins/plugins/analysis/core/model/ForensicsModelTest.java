package io.jenkins.plugins.analysis.core.model;

import java.util.List;
import java.util.Locale;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        Locale.setDefault(Locale.ENGLISH);

        Report report = new Report();
        report.add(createIssue(1));
        report.add(createIssue(2));
        RepositoryStatistics statistics = mock(RepositoryStatistics.class);

        ForensicsModel model = createModel(statistics);

        assertThat(model.getHeaders(report)).hasSize(EXPECTED_COLUMNS_SIZE);
        assertThat(model.getWidths(report)).hasSize(EXPECTED_COLUMNS_SIZE);
        assertThat(model.getContent(report)).hasSize(2);
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
        when(fileStatistics.getLastModifiedInDays()).thenReturn(25L);
        when(fileStatistics.getAgeInDays()).thenReturn(30L);

        when(statistics.get(FILE_NAME)).thenReturn(fileStatistics);
        when(statistics.contains(FILE_NAME)).thenReturn(true);

        // FIXME: use int

        ForensicsModel model = createModel(statistics);

        List<List<String>> rows = model.getContent(report);
        assertThat(rows).hasSize(1);

        List<String> columns = rows.get(0);
        assertThat(columns).hasSize(EXPECTED_COLUMNS_SIZE);

        assertThat(columns.get(0)).contains(StringEscapeUtils.escapeHtml4(DESCRIPTION));
        assertThat(columns.get(0)).contains(StringEscapeUtils.escapeHtml4(MESSAGE));
        assertThat(columns.get(1)).contains("file-1:15");
        assertThat(columns.get(2)).contains("1");
        assertThat(columns.get(3)).contains("15");
        assertThat(columns.get(4)).contains("20");
        assertThat(columns.get(5)).contains("25");
        assertThat(columns.get(6)).contains("30");
    }

    @Test
    void shouldShowIssueWithoutForensics() {
        Locale.setDefault(Locale.ENGLISH);

        Report report = new Report();
        Issue issue = createIssue(1);
        report.add(issue);

        RepositoryStatistics blames = mock(RepositoryStatistics.class);

        ForensicsModel model = createModel(blames);

        List<List<String>> rows = model.getContent(report);
        assertThat(rows).hasSize(1);

        List<String> columns = rows.get(0);
        assertThat(columns).hasSize(EXPECTED_COLUMNS_SIZE);

        assertThat(columns.get(0)).contains(StringEscapeUtils.escapeHtml4(DESCRIPTION));
        assertThat(columns.get(0)).contains(StringEscapeUtils.escapeHtml4(MESSAGE));
        assertThat(columns.get(1)).contains("file-1:15");
        assertThat(columns.get(2)).contains("1");
        assertThat(columns.get(3)).contains(BlamesModel.UNDEFINED);
        assertThat(columns.get(4)).contains(BlamesModel.UNDEFINED);
        assertThat(columns.get(5)).contains(BlamesModel.UNDEFINED);
        assertThat(columns.get(6)).contains(BlamesModel.UNDEFINED);
    }

    private ForensicsModel createModel(final RepositoryStatistics statistics) {
        DescriptionProvider descriptionProvider = mock(DescriptionProvider.class);
        when(descriptionProvider.getDescription(any())).thenReturn(DESCRIPTION);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
        FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);
        DefaultAgeBuilder ageBuilder = new DefaultAgeBuilder(1, "url");

        return new ForensicsModel(ageBuilder, fileNameRenderer, issue -> DESCRIPTION, statistics);
    }
}
