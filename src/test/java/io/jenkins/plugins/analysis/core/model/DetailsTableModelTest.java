package io.jenkins.plugins.analysis.core.model;

import java.util.List;
import java.util.Locale;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static j2html.TagCreator.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link DetailsTableModel}.
 *
 * @author Ullrich Hafner
 */
class DetailsTableModelTest {
    private static final String DESCRIPTION
            = join("Hello description with", a().withHref("url").withText("link")).render();
    private static final String MESSAGE
            = join("Hello message with", a().withHref("url").withText("link")).render();

    @Test
    void shouldConvertIssuesToArrayWithAllColumns() {
        Locale.setDefault(Locale.ENGLISH);

        DetailsTableModel model = createModel();

        Report report = new Report();
        report.add(createIssue(1));
        report.add(createIssue(2));

        assertThat(model.getHeaders(report)).hasSize(7);
        assertThat(model.getWidths(report)).hasSize(7);
        List<List<String>> rows = model.getContent(report);
        assertThat(rows).hasSize(2);

        List<String> columns = rows.get(0);
        assertThat(columns).hasSize(7);
        assertThat(columns.get(0)).contains(StringEscapeUtils.escapeHtml4(DESCRIPTION));
        assertThat(columns.get(0)).contains(StringEscapeUtils.escapeHtml4(MESSAGE));
        assertThat(columns.get(1)).contains("file-1:15");
        assertThat(columns.get(2)).contains("package-1");
        assertThat(columns.get(3)).contains("category-1");
        assertThat(columns.get(4)).contains("type-1");
        assertThat(columns.get(5)).contains("High");
        assertThat(columns.get(6)).contains("1");
    }

    private DetailsTableModel createModel() {
        DescriptionProvider descriptionProvider = mock(DescriptionProvider.class);
        when(descriptionProvider.getDescription(any())).thenReturn(DESCRIPTION);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
        FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);

        DefaultAgeBuilder ageBuilder = new DefaultAgeBuilder(1, "url");

        return new DetailsTableModel(ageBuilder, fileNameRenderer, issue -> DESCRIPTION);
    }

    @Test
    void shouldShowOnlyColumnsWithMeaningfulContent() {
        Locale.setDefault(Locale.ENGLISH);

        DetailsTableModel model = createModel();

        ImmutableList<Issue> issues = Lists.immutable.of(createIssue(1));
        Report report = mock(Report.class);
        when(report.iterator()).thenReturn(issues.iterator());

        assertThat(model.getHeaders(report)).hasSize(4).doesNotContain("Package", "Category", "Types");
        assertThat(model.getWidths(report)).hasSize(4);
        assertThat(model.getContent(report)).hasSize(1);

        when(report.hasPackages()).thenReturn(true);
        assertThat(model.getHeaders(report)).hasSize(5).contains("Package").doesNotContain("Category", "Type");
        assertThat(model.getWidths(report)).hasSize(5);

        when(report.hasCategories()).thenReturn(true);
        assertThat(model.getHeaders(report)).hasSize(6).contains("Package", "Category").doesNotContain("Type");
        assertThat(model.getWidths(report)).hasSize(6);

        when(report.hasTypes()).thenReturn(true);
        assertThat(model.getHeaders(report)).hasSize(7).contains("Package", "Category", "Type");
        assertThat(model.getWidths(report)).hasSize(7);
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

