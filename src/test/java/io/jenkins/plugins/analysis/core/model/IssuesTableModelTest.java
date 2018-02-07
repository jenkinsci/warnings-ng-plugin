package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import io.jenkins.plugins.analysis.core.model.IssuesTableModel.AgeBuilder;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Tests the class {@link IssuesTableModel}.
 *
 * @author Ullrich Hafner
 */
class IssuesTableModelTest {
    @Test
    void shouldConvertIssuesToJsonArray() {
        Locale.setDefault(Locale.ENGLISH);

        Issues<BuildIssue> issues = new Issues<>();
        IssuesTableModel model = new IssuesTableModel(build -> String.valueOf(build));

        issues.add(createIssue(1));
        JSONObject oneElement = model.toJsonArray(issues);

        assertThatJson(oneElement).node("data").isArray().ofLength(1);

        JSONArray singleRow = getDataSection(oneElement);

        JSONArray columns = singleRow.getJSONArray(0);
        BuildIssueTest.assertThatColumnsAreValid(columns, 1);

        issues.add(createIssue(2));
        JSONObject twoElements = model.toJsonArray(issues);

        assertThatJson(twoElements).node("data").isArray().ofLength(2);

        JSONArray rows = getDataSection(twoElements);

        JSONArray columnsFirstRow = rows.getJSONArray(0);
        BuildIssueTest.assertThatColumnsAreValid(columnsFirstRow, 1);

        assertThatJson(rows.get(1)).isArray().ofLength(BuildIssueTest.EXPECTED_NUMBER_OF_COLUMNS);
        JSONArray columnsSecondRow = rows.getJSONArray(1);
        BuildIssueTest.assertThatColumnsAreValid(columnsSecondRow, 2);
    }

    private JSONArray getDataSection(final JSONObject oneElement) {
        JSONArray singleRow = oneElement.getJSONArray("data");
        assertThatJson(singleRow.get(0)).isArray().ofLength(BuildIssueTest.EXPECTED_NUMBER_OF_COLUMNS);
        return singleRow;
    }

    private BuildIssue createIssue(final int index) {
        IssueBuilder builder = new IssueBuilder();
        Issue issue = builder.setFileName("/path/to/file-" + index)
                .setPackageName("package-" + index)
                .setCategory("category-" + index)
                .setType("type-" + index)
                .setLineStart(15)
                .setPriority(Priority.HIGH).build();
        return new BuildIssue(issue, 1);
    }

    @Test
    void shouldCreateAgeLinkForFirstBuild() {
        AgeBuilder builder = new AgeBuilder(1, "checkstyleResult/");
        assertThat(builder.apply(1))
                .isEqualTo("1");
    }

    @Test
    void shouldCreateAgeLinkForPreviousBuilds() {
        AgeBuilder builder = new AgeBuilder(10, "checkstyleResult/");
        assertThat(builder.apply(1))
                .isEqualTo("<a href=\"../../1/checkstyleResult\" class=\"model-link inside\">10</a>");
        assertThat(builder.apply(9))
                .isEqualTo("<a href=\"../../9/checkstyleResult\" class=\"model-link inside\">2</a>");
        assertThat(builder.apply(10))
                .isEqualTo("1");
    }

    @Test
    void shouldCreateAgeLinkForSubDetails() {
        AgeBuilder builder = new AgeBuilder(10, "checkstyleResult/package.1234/");
        assertThat(builder.apply(1))
                .isEqualTo("<a href=\"../../../1/checkstyleResult\" class=\"model-link inside\">10</a>");
        assertThat(builder.apply(9))
                .isEqualTo("<a href=\"../../../9/checkstyleResult\" class=\"model-link inside\">2</a>");
        assertThat(builder.apply(10))
                .isEqualTo("1");
    }
}