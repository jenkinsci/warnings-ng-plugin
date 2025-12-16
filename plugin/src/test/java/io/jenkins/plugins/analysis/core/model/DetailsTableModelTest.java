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

    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-76257")
    void shouldFormatCategoryWithExternalUrlWhenAvailable() {
        try (var builder = new IssueBuilder()) {
            builder.setCategory("bugprone-forward-declaration-namespace");
            builder.setDescription("See <a href=\"https://clang.llvm.org/extra/clang-tidy/checks/bugprone/forward-declaration-namespace.html\">Clang-Tidy documentation</a>.");
            var issue = builder.build();
            
            var descriptionProvider = new DescriptionProvider() {
                @Override
                public String getDescription(final Issue issue) {
                    return issue.getDescription();
                }

                @Override
                public String getCategoryUrl(final Issue issue) {
                    // Extract URL from description
                    var description = issue.getDescription();
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(description)) {
                        var hrefPattern = java.util.regex.Pattern.compile("href=[\"']([^\"']+)[\"']");
                        var matcher = hrefPattern.matcher(description);
                        if (matcher.find()) {
                            return matcher.group(1);
                        }
                    }
                    return "";
                }
            };
            
            var model = new TableRow(createAgeBuilder(), createFileNameRenderer(), descriptionProvider, issue,
                    createJenkinsFacade());
            
            assertThat(model.formatPropertyWithUrl("category", issue.getCategory(), issue))
                    .isEqualTo("<a href=\"https://clang.llvm.org/extra/clang-tidy/checks/bugprone/forward-declaration-namespace.html\" target=\"_blank\" rel=\"noopener noreferrer\" title=\"View documentation\">bugprone-forward-declaration-namespace</a>");
        }
    }

    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-76257")
    void shouldFormatCategoryWithInternalLinkWhenNoUrl() {
        try (var builder = new IssueBuilder()) {
            builder.setCategory("some-category");
            var issue = builder.build();
            var model = createRow(issue);
            
            // Should format with internal link (hash code of the value)
            var result = model.formatPropertyWithUrl("category", issue.getCategory(), issue);
            assertThat(result)
                    .startsWith("<a href=\"category.")
                    .endsWith("/\">some-category</a>")
                    .contains("some-category");
        }
    }

    private TableRow createRow(final Issue issue) {
        return new TableRow(createAgeBuilder(), createFileNameRenderer(), i -> DESCRIPTION, issue,
                createJenkinsFacade());
    }
}
