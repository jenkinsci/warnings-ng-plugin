package io.jenkins.plugins.analysis.core.model;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.IssuesModel.IssuesRow;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link DetailsTableModel}.
 *
 * @author Ullrich Hafner
 */
class IssuesModelTest extends AbstractDetailsModelTest {
    private static final String PACKAGE_NAME = "<a href=\"packageName.1802059882/\">package-1</a>";

    @Test
    void shouldConvertIssuesToArrayWithAllColumns() {
        Report report = new Report();
        Issue issue = createIssue(1);
        report.add(issue);
        report.add(createIssue(2));

        IssuesModel model = createModel(report);
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

        assertThat(model.getRows()).hasSize(2);

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
        assertThat(getLabels(model))
                .containsExactly("Details", "File", "Severity", "Age");
        assertThat(getWidths(model))
                .containsExactly(1, 1, 1, 1);
        assertThat(model.getRows()).hasSize(1);

        when(report.hasPackages()).thenReturn(true);
        assertThat(getLabels(model))
                .containsExactly("Details", "File", "Package", "Severity", "Age");
        assertThat(getWidths(model))
                .containsExactly(1, 1, 2, 1, 1);

        when(report.hasCategories()).thenReturn(true);
        assertThat(getLabels(model))
                .containsExactly("Details", "File", "Package", "Category", "Severity", "Age");
        assertThat(getWidths(model))
                .containsExactly(1, 1, 2, 1, 1, 1);

        when(report.hasTypes()).thenReturn(true);
        assertThat(getLabels(model))
                .containsExactly("Details", "File", "Package", "Category", "Type", "Severity", "Age");
        assertThat(getWidths(model))
                .containsExactly(1, 1, 2, 1, 1, 1, 1);
    }

    private IssuesModel createModel(final Report report) {
        JenkinsFacade jenkinsFacade = createJenkinsFacade();

        return new IssuesModel(report, createFileNameRenderer(), createAgeBuilder(), issue -> DESCRIPTION,
                jenkinsFacade);
    }
}

