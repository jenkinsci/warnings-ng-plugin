package io.jenkins.plugins.analysis.core.model;

import java.util.List;
import java.util.Locale;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import io.jenkins.plugins.analysis.core.scm.BlameRequest;
import io.jenkins.plugins.analysis.core.scm.Blames;

import static j2html.TagCreator.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ReferenceDetailsModel}.
 *
 * @author Colin Kaschel
 */
class ReferenceDetailsModelTest {

    private static final String DESCRIPTION
            = join("Hello description with", a().withHref("url").withText("link")).render();
    private static final String MESSAGE
            = join("Hello message with", a().withHref("url").withText("link")).render();

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

        BlameRequest blameRequest = mock(BlameRequest.class);
        when(blameRequest.getCommit(issue.getLineStart())).thenReturn(COMMIT);
        when(blameRequest.getEmail(issue.getLineStart())).thenReturn(EMAIL);
        when(blameRequest.getName(issue.getLineStart())).thenReturn(NAME);

        Blames blames = mock(Blames.class);
        when(blames.contains(issue.getFileName())).thenReturn(true);
        when(blames.get(issue.getFileName())).thenReturn(blameRequest);

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
        assertThat(columns.get(3)).contains("-");
        assertThat(columns.get(4)).contains("-");
        assertThat(columns.get(5)).contains("-");
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

    private IssueBuilder createBuilder() {
        return new IssueBuilder().setMessage(MESSAGE);
    }

    private Issue createIssue(final int index) {
        IssueBuilder builder = createBuilder();
        builder.setFileName("/path/to/file-" + index)
                .setPackageName("package-" + index)
                .setCategory("category-" + index)
                .setType("type-" + index)
                .setLineStart(15)
                .setSeverity(Severity.WARNING_HIGH)
                .setReference("1");
        return builder.build();
    }

}
