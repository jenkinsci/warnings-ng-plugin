package io.jenkins.plugins.analysis.core.model;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.IssuesModel.IssuesRow;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
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
        IssuesModel model = createModel();

        Report report = new Report();
        Issue issue = createIssue(1);
        report.add(issue);
        report.add(createIssue(2));

        assertThat(model.getHeaders(report)).hasSize(EXPECTED_COLUMNS_SIZE);
        assertThat(model.getWidths(report)).hasSize(EXPECTED_COLUMNS_SIZE);
        assertThat(model.getColumnsDefinition(report)).isEqualTo("["
                + "{\"data\": \"description\"},"
                + "{\"data\": \"fileName\"},"
                + "{\"data\": \"packageName\"},"
                + "{\"data\": \"category\"},"
                + "{\"data\": \"type\"},"
                + "{\"data\": \"severity\"},"
                + "{\"data\": \"age\"}]");

        IssuesRow expected = new IssuesRow();
        expected.setDescription(EXPECTED_DESCRIPTION);
        expected.setFileName(String.format("<a href=\"source.%s/#15\">file-1:15</a>",  issue.getId().toString()));
        expected.setAge("1");
        expected.setPackageName(PACKAGE_NAME);
        expected.setCategory("<a href=\"category.1296530210/\">category-1</a>");
        expected.setType("<a href=\"type.-858804642/\">type-1</a>");
        expected.setSeverity("<a href=\"HIGH\">High</a>");

        assertThat(model.getRow(report, issue, "d")).isEqualToComparingFieldByField(expected);
    }

    @Test
    void shouldShowOnlyColumnsWithMeaningfulContent() {
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
        assertThat(model.getHeaders(report)).hasSize(EXPECTED_COLUMNS_SIZE).contains("Package", "Category", "Type");
        assertThat(model.getWidths(report)).hasSize(EXPECTED_COLUMNS_SIZE);
    }

    private IssuesModel createModel() {
        DescriptionProvider descriptionProvider = mock(DescriptionProvider.class);
        when(descriptionProvider.getDescription(any())).thenReturn(DESCRIPTION);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
        FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);

        DefaultAgeBuilder ageBuilder = new DefaultAgeBuilder(1, "url");

        return new IssuesModel(ageBuilder, fileNameRenderer, issue -> DESCRIPTION);
    }
}

