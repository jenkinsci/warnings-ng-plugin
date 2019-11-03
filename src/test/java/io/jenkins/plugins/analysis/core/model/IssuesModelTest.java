package io.jenkins.plugins.analysis.core.model;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.IssuesModel.IssuesRow;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link DetailsTableModel}.
 *
 * @author Ullrich Hafner
 */
class IssuesModelTest extends AbstractDetailsModelTest {
    private static final String PACKAGE_NAME = "<a href=\"packageName.1802059882/\">package-1</a>";
    private static final int EXPECTED_COLUMNS_SIZE = 7;

    @Test
    void shouldConvertIssuesToArrayWithAllColumns() {
        Report report = new Report();
        Issue issue = createIssue(1);
        report.add(issue);
        report.add(createIssue(2));

        IssuesModel model = createModel(report);
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
                + "{\"data\": \"packageName\"},"
                + "{\"data\": \"category\"},"
                + "{\"data\": \"type\"},"
                + "{\"data\": \"severity\"},"
                + "{\"data\": \"age\"}]");

        IssuesRow actualRow = model.getRow(issue);
        assertThat(actualRow).hasDescription(EXPECTED_DESCRIPTION)
                .hasAge("1")
                .hasPackageName(PACKAGE_NAME)
                .hasCategory("<a href=\"category.1296530210/\">category-1</a>")
                .hasType("<a href=\"type.-858804642/\">type-1</a>")
                .hasSeverity("<a href=\"HIGH\">High</a>");
        assertThatDetailedColumnContains(actualRow.getFileName(),
                createExpectedFileName(issue), "/path/to/file-1:0000015");
    }

    @Test
    void shouldShowOnlyColumnsWithMeaningfulContent() {
        ImmutableList<Issue> issues = Lists.immutable.of(createIssue(1));
        Report report = mock(Report.class);
        when(report.iterator()).thenReturn(issues.iterator());

        DetailsTableModel model = createModel(report);
        assertThat(model.getHeaders()).hasSize(4).doesNotContain("Package", "Category", "Types");
        assertThat(model.getWidths()).hasSize(4);
        assertThat(model.getRows()).hasSize(1);

        when(report.hasPackages()).thenReturn(true);
        assertThat(model.getHeaders()).hasSize(5).contains("Package").doesNotContain("Category", "Type");
        assertThat(model.getWidths()).hasSize(5);

        when(report.hasCategories()).thenReturn(true);
        assertThat(model.getHeaders()).hasSize(6).contains("Package", "Category").doesNotContain("Type");
        assertThat(model.getWidths()).hasSize(6);

        when(report.hasTypes()).thenReturn(true);
        assertThat(model.getHeaders()).hasSize(EXPECTED_COLUMNS_SIZE).contains("Package", "Category", "Type");
        assertThat(model.getWidths()).hasSize(EXPECTED_COLUMNS_SIZE);
    }

    private IssuesModel createModel(final Report report) {
        return new IssuesModel(report, createFileNameRenderer(), createAgeBuilder(), issue -> DESCRIPTION);
    }
}

