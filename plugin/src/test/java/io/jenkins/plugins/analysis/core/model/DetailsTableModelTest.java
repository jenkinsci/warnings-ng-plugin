package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;

import io.jenkins.plugins.analysis.core.model.DetailsTableModel.TableRow;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link DetailsTableModel}.
 *
 * @author Ullrich Hafner
 */
class DetailsTableModelTest extends AbstractDetailsModelTest {
    @Test
    @org.jvnet.hudson.test.Issue("JENKINS-64051")
    void shouldNotRemoveWhitespace() {
        try (IssueBuilder builder = new IssueBuilder()) {
            builder.setMessage("project: Defaults to NumberGroupSeparator on .NET Core except on Windows.");
            TableRow model = createRow(builder.build());

            String actualColumn = model.getDescription();
            assertThat(actualColumn).contains("NumberGroupSeparator on .NET Core except");
        }
    }

    @Test
    void shouldCreateSortableFileName() {
        Issue issue = createIssue(1);
        TableRow model = createRow(issue);

        assertThatDetailedColumnContains(model.getFileName(),
                createExpectedFileName(issue), "/path/to/file-1:0000015");
    }

    @Test
    void shouldReturnClickableProperties() {
        try (IssueBuilder builder = new IssueBuilder()) {
            builder.setCategory("");
            Issue issue = builder.build();
            TableRow model = createRow(issue);
            assertThat(model.formatProperty("category", issue.getCategory()))
                    .isEqualTo("<a href=\"category.0/\">-</a>");
        }
    }

    private TableRow createRow(final Issue issue) {
        return new TableRow(createAgeBuilder(), createFileNameRenderer(), i -> DESCRIPTION, issue,
                createJenkinsFacade());
    }
}
