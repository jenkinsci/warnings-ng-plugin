package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.BlamesModel.BlamesRow;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileBlame;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link BlamesModel}.
 *
 * @author Colin Kaschel
 */
class BlamesModelTest extends AbstractDetailsModelTest {
    private static final String COMMIT = "commit";
    private static final String NAME = "name";
    private static final String EMAIL = "email";

    private static final int EXPECTED_COLUMNS_SIZE = 6;

    @Test
    void shouldConvertIssueToArrayWithAllColumnsAndRows() {
        Report report = new Report();
        report.add(createIssue(1));
        report.add(createIssue(2));
        Blames blames = mock(Blames.class);

        BlamesModel model = createModel(blames);

        assertThat(model.getHeaders(report)).hasSize(EXPECTED_COLUMNS_SIZE);
        assertThat(model.getWidths(report)).hasSize(EXPECTED_COLUMNS_SIZE);
        assertThat(model.getColumnsDefinition(report)).isEqualTo("["
                + "{\"data\": \"description\"},"
                + "{\"data\": \"fileName\"},"
                + "{\"data\": \"age\"},"
                + "{\"data\": \"author\"},"
                + "{\"data\": \"email\"},"
                + "{\"data\": \"commit\"}"
                + "]");
        assertThat(model.getContent(report)).hasSize(2);
    }

    @Test
    void shouldShowIssueWithBlames() {
        Report report = new Report();
        Issue issue = createIssue(1);
        report.add(issue);

        FileBlame blameRequest = mock(FileBlame.class);
        when(blameRequest.getCommit(issue.getLineStart())).thenReturn(COMMIT);
        when(blameRequest.getEmail(issue.getLineStart())).thenReturn(EMAIL);
        when(blameRequest.getName(issue.getLineStart())).thenReturn(NAME);

        Blames blames = mock(Blames.class);
        when(blames.contains(issue.getFileName())).thenReturn(true);
        when(blames.getBlame(issue.getFileName())).thenReturn(blameRequest);

        BlamesModel model = createModel(blames);

        BlamesRow expected = new BlamesRow();
        expected.setDescription(EXPECTED_DESCRIPTION);
        expected.setFileName(String.format("<a href=\"source.%s/#15\">file-1:15</a>",  issue.getId().toString()));
        expected.setAge("1");
        expected.setCommit(COMMIT);
        expected.setAuthor(NAME);
        expected.setEmail(EMAIL);

        assertThat(model.getRow(report, issue, "d")).isEqualToComparingFieldByField(expected);
    }

    @Test
    void shouldShowIssueWithoutBlames() {
        Report report = new Report();
        Issue issue = createIssue(1);
        report.add(issue);

        Blames blames = mock(Blames.class);

        BlamesModel model = createModel(blames);

        BlamesRow expected = new BlamesRow();
        expected.setDescription(EXPECTED_DESCRIPTION);
        expected.setFileName(String.format("<a href=\"source.%s/#15\">file-1:15</a>",  issue.getId().toString()));
        expected.setAge("1");
        expected.setCommit(BlamesModel.UNDEFINED);
        expected.setAuthor(BlamesModel.UNDEFINED);
        expected.setEmail(BlamesModel.UNDEFINED);

        assertThat(model.getRow(report, issue, "d")).isEqualToComparingFieldByField(expected);
    }

    private BlamesModel createModel(final Blames blames) {
        DescriptionProvider descriptionProvider = mock(DescriptionProvider.class);
        when(descriptionProvider.getDescription(any())).thenReturn(DESCRIPTION);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
        FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);
        DefaultAgeBuilder ageBuilder = new DefaultAgeBuilder(1, "url");

        return new BlamesModel(ageBuilder, fileNameRenderer, issue -> DESCRIPTION, blames);
    }
}
