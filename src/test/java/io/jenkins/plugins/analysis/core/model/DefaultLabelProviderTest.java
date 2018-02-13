package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.util.ResourceTest;
import static io.jenkins.plugins.analysis.core.model.Assertions.assertThat;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider.DefaultAgeBuilder;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Tests the class {@link DefaultLabelProvider}.
 *
 * @author Ullrich Hafner
 */
class DefaultLabelProviderTest {
    private static final String ID = "id";
    private static final String NAME = "name";

    @Test
    void shouldReturnIdAndNameOfConstructorParametersInAllDisplayProperties() {
        DefaultLabelProvider labelProvider = new DefaultLabelProvider(ID, NAME);

        assertThat(labelProvider).hasId(ID);
        assertThat(labelProvider).hasName(NAME);
        assertThat(labelProvider.getLinkName()).contains(NAME);
        assertThat(labelProvider.getTrendName()).contains(NAME);
        assertThat(labelProvider.getResultUrl()).contains(ID);
    }

    @Test
    void shouldReturnIdAndDefaultNameIfNoNameIsGiven() {
        DefaultLabelProvider emptyNameLabelProvider = new DefaultLabelProvider(ID, "");

        assertThat(emptyNameLabelProvider).hasId(ID);
        assertThat(emptyNameLabelProvider).hasName(emptyNameLabelProvider.getDefaultName());

        DefaultLabelProvider nullNameLabelProvider = new DefaultLabelProvider(ID, null);

        assertThat(nullNameLabelProvider).hasId(ID);
        assertThat(nullNameLabelProvider).hasName(nullNameLabelProvider.getDefaultName());

        DefaultLabelProvider noNameLabelProvider = new DefaultLabelProvider(ID);

        assertThat(noNameLabelProvider).hasId(ID);
        assertThat(noNameLabelProvider).hasName(noNameLabelProvider.getDefaultName());
    }

    void assertThatColumnsAreValid(final JSONArray columns, int index) {
        assertThat(columns.get(0)).isEqualTo("<div class=\"details-control\" data-description=\"\"/>");
        String actual = columns.getString(1);
        assertThat(actual).matches(createFileLinkMatcher("file-" + index, 15));
        assertThat(columns.get(2)).isEqualTo(createPropertyLink("packageName", "package-" + index));
        assertThat(columns.get(3)).isEqualTo(createPropertyLink("category", "category-" + index));
        assertThat(columns.get(4)).isEqualTo(createPropertyLink("type", "type-" + index));
        assertThat(columns.get(5)).isEqualTo("<a href=\"HIGH\">High</a>");
        assertThat(columns.get(6)).isEqualTo("1");
    }

    private static String createPropertyLink(final String property, final String value) {
        return String.format("<a href=\"%s.%d/\">%s</a>", property, value.hashCode(), value);
    }

    private static String createFileLinkMatcher(final String fileName, final int lineNumber) {
        return "<a href=\\\"source.[0-9a-f-]+/#" + lineNumber + "\\\">"
                + fileName + ":" + lineNumber
                + "</a>";
    }

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
                    .isEqualTo("<a href=\"../../1/checkstyleResult\" class=\"model-link inside\">10</a>");
            assertThat(builder.apply(9))
                    .isEqualTo("<a href=\"../../9/checkstyleResult\" class=\"model-link inside\">2</a>");
            assertThat(builder.apply(10))
                    .isEqualTo("1");
        }
        @Test
        void shouldCreateAgeLinkForSubDetails() {
            AgeBuilder builder = new DefaultAgeBuilder(10, "checkstyleResult/package.1234/");
            assertThat(builder.apply(1))
                    .isEqualTo("<a href=\"../../../1/checkstyleResult\" class=\"model-link inside\">10</a>");
            assertThat(builder.apply(9))
                    .isEqualTo("<a href=\"../../../9/checkstyleResult\" class=\"model-link inside\">2</a>");
            assertThat(builder.apply(10))
                    .isEqualTo("1");
        }
    }

    @Nested
    class TableModelTest {
        @Test
        void shouldConvertIssuesToJsonArray() {
            Locale.setDefault(Locale.ENGLISH);

            Issues<Issue> issues = new Issues<>();
            issues.add(createIssue(1));

            DefaultLabelProvider labelProvider = new DefaultLabelProvider();
            JSONObject oneElement = labelProvider.toJsonArray(issues, new DefaultAgeBuilder(1, "url"));

            assertThatJson(oneElement).node("data").isArray().ofLength(1);

            JSONArray singleRow = getDataSection(oneElement);

            JSONArray columns = singleRow.getJSONArray(0);

            assertThatColumnsAreValid(columns, 1);

            issues.add(createIssue(2));
            JSONObject twoElements = labelProvider.toJsonArray(issues, new DefaultAgeBuilder(1, "url"));

            assertThatJson(twoElements).node("data").isArray().ofLength(2);

            JSONArray rows = getDataSection(twoElements);

            JSONArray columnsFirstRow = rows.getJSONArray(0);
            assertThatColumnsAreValid(columnsFirstRow, 1);

            assertThatJson(rows.get(1)).isArray().ofLength(IssueModelTest.EXPECTED_NUMBER_OF_COLUMNS);
            JSONArray columnsSecondRow = rows.getJSONArray(1);
            assertThatColumnsAreValid(columnsSecondRow, 2);
        }

        private JSONArray getDataSection(final JSONObject oneElement) {
            JSONArray singleRow = oneElement.getJSONArray("data");
            assertThatJson(singleRow.get(0)).isArray().ofLength(IssueModelTest.EXPECTED_NUMBER_OF_COLUMNS);
            return singleRow;
        }

        private Issue createIssue(final int index) {
            IssueBuilder builder = new IssueBuilder();
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

    @Nested
    class IssueModelTest extends ResourceTest {
        static final int EXPECTED_NUMBER_OF_COLUMNS = 7;

        @Test
        void shouldConvertIssueToArrayOfColumns() {
            Locale.setDefault(Locale.ENGLISH);

            IssueBuilder builder = new IssueBuilder();
            Issue issue = builder.setFileName("path/to/file-1")
                    .setPackageName("package-1")
                    .setCategory("category-1")
                    .setType("type-1")
                    .setLineStart(15)
                    .setPriority(Priority.HIGH)
                    .setReference("1").build();

            DefaultLabelProvider provider = new DefaultLabelProvider();
            JSONArray columns = provider.toJson(issue, build -> String.valueOf(build));

            assertThatJson(columns).isArray().ofLength(EXPECTED_NUMBER_OF_COLUMNS);
            assertThatColumnsAreValid(columns, 1);
        }
    }
 }