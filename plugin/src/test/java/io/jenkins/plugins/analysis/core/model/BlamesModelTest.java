package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileBlame;
import io.jenkins.plugins.forensics.util.CommitDecorator.NullDecorator;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
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

    @Test
    void shouldConvertIssueToArrayWithAllColumnsAndRows() {
        var report = new Report();
        report.add(createIssue(1));
        report.add(createIssue(2));
        Blames blames = mock(Blames.class);

        var model = createModel(report, blames);

        var columnDefinitions = model.getColumnsDefinition();
        assertThatJson(columnDefinitions).isArray().hasSize(8);

        String[] columns = {"description", "fileName", "age", "author", "email", "commit", "addedAt", "message"};
        for (int column = 0; column < columns.length; column++) {
            verifyColumnProperty(model, column, columns[column]);
        }
        verifyFileNameColumn(columnDefinitions);

        assertThat(getLabels(model))
                .containsExactly("Details", "File", "Age", "Author", "Email", "Commit", "Added", "Hiddendetails");

        assertThat(model.getRows()).hasSize(2);
    }

    @Test
    void shouldShowIssueWithBlames() {
        var report = new Report();
        var issue = createIssue(1);
        report.add(issue);

        FileBlame blameRequest = mock(FileBlame.class);
        when(blameRequest.getCommit(issue.getLineStart())).thenReturn(COMMIT);
        when(blameRequest.getEmail(issue.getLineStart())).thenReturn(EMAIL);
        when(blameRequest.getName(issue.getLineStart())).thenReturn(NAME);
        when(blameRequest.getTime(issue.getLineStart())).thenReturn(TIME);

        Blames blames = mock(Blames.class);
        when(blames.contains(issue.getFileName())).thenReturn(true);
        when(blames.getBlame(issue.getFileName())).thenReturn(blameRequest);

        var model = createModel(report, blames);

        var actualRow = model.getRow(issue);
        assertThat(actualRow).hasDescription(EXPECTED_DESCRIPTION)
                .hasAge("1")
                .hasCommit(COMMIT)
                .hasAuthor(NAME)
                .hasEmail(EMAIL)
                .hasAddedAt(TIME);
        assertThatDetailedColumnContains(actualRow.getFileName(),
                createExpectedFileName(issue), "/path/to/file-1:0000015");
    }

    @Test
    void shouldShowIssueWithoutBlames() {
        var report = new Report();
        var issue = createIssue(1);
        report.add(issue);

        Blames blames = mock(Blames.class);

        var model = createModel(report, blames);

        var actualRow = model.getRow(issue);
        assertThat(actualRow.getDescription()).isEqualTo(EXPECTED_DESCRIPTION);

        assertThat(actualRow).hasDescription(EXPECTED_DESCRIPTION)
                .hasAge("1")
                .hasCommit(BlamesModel.UNDEFINED)
                .hasAuthor(BlamesModel.UNDEFINED)
                .hasEmail(BlamesModel.UNDEFINED);

        assertThatDetailedColumnContains(actualRow.getFileName(),
                createExpectedFileName(issue), "/path/to/file-1:0000015");
    }

    private BlamesModel createModel(final Report report, final Blames blames) {
        return new BlamesModel(report, blames, createFileNameRenderer(), createAgeBuilder(), issue -> DESCRIPTION,
                new NullDecorator(), createJenkinsFacade());
    }
}
