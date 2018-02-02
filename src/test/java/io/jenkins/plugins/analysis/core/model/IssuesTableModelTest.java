package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
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
        IssuesTableModel model = new IssuesTableModel();

        issues.add(createIssue(1));
        JSONObject oneElement = JSONObject.fromObject(model.toJsonArray(issues));

        assertThatJson(oneElement).node("data").isArray().ofLength(1);

        JSONArray singleRow = oneElement.getJSONArray("data");
        assertThatJson(singleRow.get(0)).isArray().ofLength(5);

        JSONArray columns = singleRow.getJSONArray(0);
        BuildIssueTest.assertThatColumnsAreValid(columns, 1);

        issues.add(createIssue(2));
        JSONObject twoElements = JSONObject.fromObject(model.toJsonArray(issues));

        assertThatJson(twoElements).node("data").isArray().ofLength(2);

        JSONArray rows = twoElements.getJSONArray("data");

        assertThatJson(rows.get(0)).isArray().ofLength(5);
        JSONArray columnsFirstRow = rows.getJSONArray(0);
        BuildIssueTest.assertThatColumnsAreValid(columnsFirstRow, 1);

        assertThatJson(rows.get(1)).isArray().ofLength(5);
        JSONArray columnsSecondRow = rows.getJSONArray(1);
        BuildIssueTest.assertThatColumnsAreValid(columnsSecondRow, 2);
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
}