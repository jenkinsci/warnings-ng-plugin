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
    @org.junitpioneer.jupiter.Issue("JENKINS-64051")
    void shouldNotRemoveWhitespace() {
        try (var builder = new IssueBuilder()) {
            builder.setMessage("project: Defaults to NumberGroupSeparator on .NET Core except on Windows.");
            var model = createRow(builder.build());

            var actualColumn = model.getDescription();
            assertThat(actualColumn).contains("NumberGroupSeparator on .NET Core except");
        }
    }

    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-62036")
    void shouldEscapeHtmlInMessage() {
        try (var builder = new IssueBuilder()) {
            builder.setMessage("<script>alert('XSS')</script><b>Bold text</b>");
            var model = createRow(builder.build());

            var actualColumn = model.getDescription();
            var actualMessage = model.getMessage();
            
            assertThat(actualColumn)
                    .doesNotContain("<script>alert")
                    .doesNotContain("<b>Bold text</b>")
                    .contains("&amp;lt;script&amp;gt;")
                    .contains("&amp;lt;b&amp;gt;")
                    .contains("Bold text");
            
            assertThat(actualMessage)
                    .doesNotContain("<script>")
                    .doesNotContain("</script>")
                    .doesNotContain("<b>Bold text</b>")
                    .contains("&lt;script&gt;")
                    .contains("&lt;b&gt;")
                    .contains("Bold text");
        }
    }

    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-62036")
    void shouldEscapeHtmlInDescription() {
        try (var builder = new IssueBuilder()) {
            builder.setMessage("Normal message");
            var issue = builder.build();
            var model = createRow(issue, i -> "<img src=x onerror=alert('XSS')><div>Description</div>");

            var actualColumn = model.getDescription();
            
            assertThat(actualColumn)
                    .doesNotContain("<img src=x onerror")  
                    .doesNotContain("<div>Description</div>")
                    .contains("&amp;lt;img")
                    .contains("&amp;lt;div&amp;gt;")
                    .contains("Description");
        }
    }

    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-62036")
    void shouldEscapeSpecialHtmlCharacters() {
        try (var builder = new IssueBuilder()) {
            builder.setMessage("Error: 'value' < 10 && 'name' > \"test\"");
            var model = createRow(builder.build());

            var actualColumn = model.getDescription();
            var actualMessage = model.getMessage();
            
            assertThat(actualColumn).contains("&lt;").contains("&gt;");
            assertThat(actualMessage).contains("&lt;").contains("&gt;");
        }
    }

    @Test
    void shouldCreateSortableFileName() {
        var issue = createIssue(1);
        var model = createRow(issue);

        assertThatDetailedColumnContains(model.getFileName(),
                createExpectedFileName(issue), "/path/to/file-1:0000015");
    }

    @Test
    void shouldReturnClickableProperties() {
        try (var builder = new IssueBuilder()) {
            builder.setCategory("");
            var issue = builder.build();
            var model = createRow(issue);
            assertThat(model.formatProperty("category", issue.getCategory()))
                    .isEqualTo("<a href=\"category.0/\">-</a>");
        }
    }

    private TableRow createRow(final Issue issue) {
        return new TableRow(createAgeBuilder(), createFileNameRenderer(), i -> DESCRIPTION, issue,
                createJenkinsFacade());
    }

    private TableRow createRow(final Issue issue, final DescriptionProvider descriptionProvider) {
        return new TableRow(createAgeBuilder(), createFileNameRenderer(), descriptionProvider, issue,
                createJenkinsFacade());
    }
}
