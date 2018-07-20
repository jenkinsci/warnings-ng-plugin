package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.ResourceTest;
import static io.jenkins.plugins.analysis.core.model.Assertions.assertThat;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link StaticAnalysisLabelProvider}.
 *
 * @author Ullrich Hafner
 */
class StaticAnalysisLabelProviderTest {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String OTHER_NAME = "other";

    @Test
    void shouldReturnIdAndNameOfConstructorParametersInAllDisplayProperties() {
        StaticAnalysisLabelProvider labelProvider = new StaticAnalysisLabelProvider(ID, NAME);

        assertThat(labelProvider).hasId(ID);
        assertThat(labelProvider).hasName(NAME);
        assertThat(labelProvider.getLinkName()).contains(NAME);
        assertThat(labelProvider.getTrendName()).contains(NAME);
        assertThat(labelProvider.getResultUrl()).contains(ID);
        
        labelProvider.setName(OTHER_NAME);
        assertThat(labelProvider).hasName(OTHER_NAME);
        assertThat(labelProvider.getLinkName()).contains(OTHER_NAME);
        assertThat(labelProvider.getTrendName()).contains(OTHER_NAME);
    }

    @Test
    void shouldReturnIdAndDefaultNameIfNoNameIsGiven() {
        StaticAnalysisLabelProvider emptyNameLabelProvider = new StaticAnalysisLabelProvider(ID, "");

        assertThat(emptyNameLabelProvider).hasId(ID);
        assertThat(emptyNameLabelProvider).hasName(emptyNameLabelProvider.getDefaultName());

        StaticAnalysisLabelProvider nullNameLabelProvider = new StaticAnalysisLabelProvider(ID, null);

        assertThat(nullNameLabelProvider).hasId(ID);
        assertThat(nullNameLabelProvider).hasName(nullNameLabelProvider.getDefaultName());

        StaticAnalysisLabelProvider noNameLabelProvider = new StaticAnalysisLabelProvider(ID);

        assertThat(noNameLabelProvider).hasId(ID);
        assertThat(noNameLabelProvider).hasName(noNameLabelProvider.getDefaultName());
    }

    private void assertThatColumnsAreValid(final Report report, final JSONArray columns, final int index) {
        int column = 0;
        assertThat(columns.get(column++)).isEqualTo(
                "<div class=\"details-control\" data-description=\"&lt;p&gt;&lt;strong&gt;MESSAGE&lt;/strong&gt;&lt;/p&gt; DESCRIPTION\"></div>");
        String actual = columns.getString(column++);
        assertThat(actual).matches(createFileLinkMatcher("file-" + index, 15));
        if (report.hasPackages()) {
            assertThat(columns.get(column++)).isEqualTo(createPropertyLink("packageName", "package-" + index));
        }
        if (report.hasCategories()) {
            assertThat(columns.get(column++)).isEqualTo(createPropertyLink("category", "category-" + index));
        }
        if (report.hasTypes()) {
            assertThat(columns.get(column++)).isEqualTo(createPropertyLink("type", "type-" + index));
        }
        assertThat(columns.get(column++)).isEqualTo("<a href=\"HIGH\">High</a>");
        assertThat(columns.get(column)).isEqualTo("1");
    }

    private String createPropertyLink(final String property, final String value) {
        return String.format("<a href=\"%s.%d/\">%s</a>", property, value.hashCode(), value);
    }

    private String createFileLinkMatcher(final String fileName, final int lineNumber) {
        return "<a href=\\\"source.[0-9a-f-]+/#" + lineNumber + "\\\">"
                + fileName + ":" + lineNumber
                + "</a>";
    }

    private IssueBuilder createBuilder() {
        return new IssueBuilder().setMessage("MESSAGE").setDescription("DESCRIPTION");
    }

    /**
     * Tests the class {@link AgeBuilder}.
     */
    @Nested
    class AgeBuilderTest {
        @Test
        void shouldCreateAgeLinkForFirstBuild() {
            AgeBuilder builder = new DefaultAgeBuilder(1, "checkstyleResult/");

            assertThat(builder.apply(1)).isEqualTo("1");
        }

        @Test
        void shouldCreateAgeLinkForPreviousBuilds() {
            AgeBuilder builder = new DefaultAgeBuilder(10, "checkstyleResult/");
            assertThat(builder.apply(1))
                    .isEqualTo("<a href=\"../../1/checkstyleResult\">10</a>");
            assertThat(builder.apply(9))
                    .isEqualTo("<a href=\"../../9/checkstyleResult\">2</a>");
            assertThat(builder.apply(10))
                    .isEqualTo("1");
        }

        @Test
        void shouldCreateAgeLinkForSubDetails() {
            AgeBuilder builder = new DefaultAgeBuilder(10, "checkstyleResult/package.1234/");
            assertThat(builder.apply(1))
                    .isEqualTo("<a href=\"../../../1/checkstyleResult\">10</a>");
            assertThat(builder.apply(9))
                    .isEqualTo("<a href=\"../../../9/checkstyleResult\">2</a>");
            assertThat(builder.apply(10))
                    .isEqualTo("1");
        }
    }

    /**
     * Tests the dynamic creation of the table model of a {@link Report}, i.e. a list of {@link Issue} instances.
     */
    @Nested
    class TableModelTest {
        @Test
        void shouldConvertIssuesToJsonArray() {
            Locale.setDefault(Locale.ENGLISH);

            Report report = new Report();
            report.add(createIssue(1));
            report.add(createIssue(2));

            StaticAnalysisLabelProvider labelProvider = new StaticAnalysisLabelProvider();
            BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
            when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
            JSONObject twoRows = labelProvider.toJsonArray(report, new DefaultAgeBuilder(1, "url"),
                    new FileNameRenderer(buildFolder));

            assertThatJson(twoRows).node("data").isArray().ofLength(2);

            JSONArray rows = getDataSection(twoRows);
            assertThatColumnsAreValid(report, rows.getJSONArray(0), 1);
            assertThatColumnsAreValid(report, rows.getJSONArray(1), 2);
        }

        private JSONArray getDataSection(final JSONObject oneElement) {
            JSONArray singleRow = oneElement.getJSONArray("data");
            assertThatJson(singleRow.get(0)).isArray().ofLength(IssueModelTest.EXPECTED_NUMBER_OF_COLUMNS);
            return singleRow;
        }

        private Issue createIssue(final int index) {
            IssueBuilder builder = createBuilder();
            builder.setFileName("/path/to/file-" + index)
                    .setPackageName("package-" + index)
                    .setCategory("category-" + index)
                    .setType("type-" + index)
                    .setLineStart(15)
                    .setPriority(Priority.HIGH)
                    .setReference("1");
            return builder.build();
        }
    }

    /**
     * Tests the dynamic creation of the table model for a single {@link Issue}.
     */
    @Nested
    class IssueModelTest extends ResourceTest {
        static final int EXPECTED_NUMBER_OF_COLUMNS = 7;

        @Test
        void shouldConvertIssueToArrayOfColumns() {
            Locale.setDefault(Locale.ENGLISH);

            IssueBuilder builder = createBuilder();
            Issue issue = builder.setFileName("path/to/file-1")
                    .setPackageName("package-1")
                    .setCategory("category-1")
                    .setType("type-1")
                    .setLineStart(15)
                    .setPriority(Priority.HIGH)
                    .setReference("1").build();

            Report report = mock(Report.class);

            StaticAnalysisLabelProvider provider = new StaticAnalysisLabelProvider();

            BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
            when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
            FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);
            JSONArray columns = provider.toJson(report, issue, String::valueOf, fileNameRenderer);
            assertThatJson(columns).isArray().ofLength(EXPECTED_NUMBER_OF_COLUMNS - 3);
            assertThatColumnsAreValid(report, columns, 1);

            when(report.hasPackages()).thenReturn(true);
            columns = provider.toJson(report, issue, String::valueOf, fileNameRenderer);
            assertThatJson(columns).isArray().ofLength(EXPECTED_NUMBER_OF_COLUMNS - 2);
            assertThatColumnsAreValid(report, columns, 1);

            when(report.hasCategories()).thenReturn(true);
            columns = provider.toJson(report, issue, String::valueOf, fileNameRenderer);
            assertThatJson(columns).isArray().ofLength(EXPECTED_NUMBER_OF_COLUMNS - 1);
            assertThatColumnsAreValid(report, columns, 1);

            when(report.hasTypes()).thenReturn(true);
            columns = provider.toJson(report, issue, String::valueOf, fileNameRenderer);
            assertThatJson(columns).isArray().ofLength(EXPECTED_NUMBER_OF_COLUMNS);
            assertThatColumnsAreValid(report, columns, 1);
        }
    }
}