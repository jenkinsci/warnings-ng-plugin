package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.BlamesModel.BlamesRow;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileBlame;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
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
    private static final int TIME = 12_345;

    private static final int EXPECTED_COLUMNS_SIZE = 7;

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
                + "{"
                + "  \"type\": \"string\","
                + "  \"data\": \"fileName\","
                + "  \"render\": {"
                + "     \"_\": \"display\","
                + "     \"sort\": \"sort\""
                + "  }"
                + "},"
                + "{\"data\": \"age\"},"
                + "{\"data\": \"author\"},"
                + "{\"data\": \"email\"},"
                + "{\"data\": \"commit\"},"
                + "{\"data\": \"addedAt\"}"
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
        when(blameRequest.getTime(issue.getLineStart())).thenReturn(TIME);

        Blames blames = mock(Blames.class);
        when(blames.contains(issue.getFileName())).thenReturn(true);
        when(blames.getBlame(issue.getFileName())).thenReturn(blameRequest);

        BlamesModel model = createModel(blames);

        BlamesRow actualRow = model.getRow(report, issue);
        assertThat(actualRow).hasDescription(EXPECTED_DESCRIPTION)
                .hasAge("1")
                .hasCommit(COMMIT)
                .hasAuthor(NAME)
                .hasEmail(EMAIL)
                .hasAddedAt(TIME);
        assertThat(actualRow.getFileName()).hasDisplay(createExpectedFileName(issue)).hasSort("/path/to/file-1:0000015");
    }

    @Test
    void shouldShowIssueWithoutBlames() {
        Report report = new Report();
        Issue issue = createIssue(1);
        report.add(issue);

        Blames blames = mock(Blames.class);

        BlamesModel model = createModel(blames);

        BlamesRow actualRow = model.getRow(report, issue);
        assertThat(actualRow).hasDescription(EXPECTED_DESCRIPTION)
                .hasAge("1")
                .hasCommit(BlamesModel.UNDEFINED)
                .hasAuthor(BlamesModel.UNDEFINED)
                .hasEmail(BlamesModel.UNDEFINED)
                .hasAddedAt(BlamesModel.UNDEFINED_DATE);
        assertThat(actualRow.getFileName()).hasDisplay(createExpectedFileName(issue)).hasSort("/path/to/file-1:0000015");
    }

    private BlamesModel createModel(final Blames blames) {
        return new BlamesModel(createAgeBuilder(), createFileNameRenderer(), issue -> DESCRIPTION, blames);
    }
}
