package io.jenkins.plugins.analysis.core.model;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link DetailsTableModel}.
 *
 * @author Ullrich Hafner
 */
class DetailsTableModelTest {
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String MESSAGE = "MESSAGE";

    @Test
    void shouldConvertIssuesToArrayWith7Columns() {
        Locale.setDefault(Locale.ENGLISH);

        Report report = new Report();
        report.add(createIssue(1));
        report.add(createIssue(2));

        DescriptionProvider descriptionProvider = mock(DescriptionProvider.class);
        when(descriptionProvider.getDescription(any())).thenReturn(DESCRIPTION);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
        FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);

        DefaultAgeBuilder ageBuilder = new DefaultAgeBuilder(1, "url");

        DetailsTableModel model = new DetailsTableModel(ageBuilder, fileNameRenderer, descriptionProvider);

        assertThat(model.getHeaders(report)).hasSize(7);
        assertThat(model.getWidths(report)).hasSize(7);
        List<List<String>> rows = model.getContent(report);
        assertThat(rows).hasSize(2);

        List<String> columns = rows.get(0);
        assertThat(columns).hasSize(7);
        assertThat(columns.get(0)).contains(DESCRIPTION).contains(MESSAGE);
        assertThat(columns.get(1)).contains("file-1:15");
        assertThat(columns.get(2)).contains("package-1");
        assertThat(columns.get(3)).contains("category-1");
        assertThat(columns.get(4)).contains("type-1");
        assertThat(columns.get(5)).contains("High");
        assertThat(columns.get(6)).contains("1");
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

