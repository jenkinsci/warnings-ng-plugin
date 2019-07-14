package io.jenkins.plugins.analysis.core.model;

import java.util.List;
import java.util.Locale;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileBlame;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ReferenceDetailsModel}.
 *
 * @author Colin Kaschel
 */
class ReferenceDetailsModelTest extends AbstractDetailsModelTest {
    private static final String COMMIT = "commit";
    private static final String NAME = "name";
    private static final String EMAIL = "email";

    @Test
    void shouldConvertIssueToArrayWithAllColumnsAndRows() {
        Locale.setDefault(Locale.ENGLISH);

        Report report = new Report();
        report.add(createIssue(1));
        report.add(createIssue(2));
        Blames blames = mock(Blames.class);

        ReferenceDetailsModel model = createModel(blames);

        assertThat(model.getHeaders(report)).hasSize(6);
        assertThat(model.getWidths(report)).hasSize(6);
        assertThat(model.getContent(report)).hasSize(2);
    }

    @Test
    void shouldShowIssueWithBlameInformation() {
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

        ReferenceDetailsModel model = createModel(blames);

        List<List<String>> rows = model.getContent(report);
        assertThat(rows).hasSize(1);

        List<String> columns = rows.get(0);
        assertThat(columns).hasSize(6);

        assertThat(columns.get(0)).contains(StringEscapeUtils.escapeHtml4(DESCRIPTION));
        assertThat(columns.get(0)).contains(StringEscapeUtils.escapeHtml4(MESSAGE));
        assertThat(columns.get(1)).contains("file-1:15");
        assertThat(columns.get(2)).contains("1");
        assertThat(columns.get(3)).contains(NAME);
        assertThat(columns.get(4)).contains(EMAIL);
        assertThat(columns.get(5)).contains(COMMIT);
    }

    @Test
    void shouldShowIssueWithoutBlameInformation() {
        Locale.setDefault(Locale.ENGLISH);

        Report report = new Report();
        Issue issue = createIssue(1);
        report.add(issue);

        Blames blames = mock(Blames.class);

        ReferenceDetailsModel model = createModel(blames);

        List<List<String>> rows = model.getContent(report);
        assertThat(rows).hasSize(1);

        List<String> columns = rows.get(0);
        assertThat(columns).hasSize(6);

        assertThat(columns.get(0)).contains(StringEscapeUtils.escapeHtml4(DESCRIPTION));
        assertThat(columns.get(0)).contains(StringEscapeUtils.escapeHtml4(MESSAGE));
        assertThat(columns.get(1)).contains("file-1:15");
        assertThat(columns.get(2)).contains("1");
        assertThat(columns.get(3)).contains(ReferenceDetailsModel.UNDEFINED);
        assertThat(columns.get(4)).contains(ReferenceDetailsModel.UNDEFINED);
        assertThat(columns.get(5)).contains(ReferenceDetailsModel.UNDEFINED);
    }

    private ReferenceDetailsModel createModel(final Blames blames) {
        DescriptionProvider descriptionProvider = mock(DescriptionProvider.class);
        when(descriptionProvider.getDescription(any())).thenReturn(DESCRIPTION);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
        FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);
        DefaultAgeBuilder ageBuilder = new DefaultAgeBuilder(1, "url");

        return new ReferenceDetailsModel(ageBuilder, fileNameRenderer, issue -> DESCRIPTION, blames);
    }
}
