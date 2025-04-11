package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.DuplicationGroup;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Locale;

import io.jenkins.plugins.analysis.core.model.AbstractDetailsModelTest;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.DryModel;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.DryModel.DuplicationRow;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

/**
 * Tests the class {@link DryModel}.
 *
 * @author Ullrich Hafner
 */
class DryTableModelTest extends AbstractDetailsModelTest {
    private static final String DESCRIPTION = "DESCRIPTION";

    @Test
    @SuppressFBWarnings("DMI")
    void shouldConvertIssueToArrayOfColumns() {
        try (IssueBuilder builder = new IssueBuilder()) {
            Locale.setDefault(Locale.ENGLISH);

            builder.setReference("1");
            DuplicationGroup group = new DuplicationGroup();
            Issue issue = builder.setFileName("/path/to/file-1")
                    .setLineStart(10)
                    .setLineEnd(24)
                    .setAdditionalProperties(group)
                    .build();
            Issue duplicate = builder.setFileName("/path/to/file-2")
                    .setLineStart(5)
                    .setLineEnd(19)
                    .setAdditionalProperties(group)
                    .build();

            group.add(issue);
            group.add(duplicate);

            Report report = new Report();
            report.add(issue).add(duplicate);

            DryModel model = createModel(report);

            String columnDefinitions = model.getColumnsDefinition();
            assertThatJson(columnDefinitions).isArray().hasSize(6);

            String[] columns = {"description", "fileName", "severity", "linesCount", "duplicatedIn", "age"};
            for (int column = 0; column < columns.length; column++) {
                verifyColumnProperty(model, column, columns[column]);
            }
            verifyFileNameColumn(columnDefinitions);

            assertThat(getLabels(model))
                    .containsExactly("Details", "File", "Severity", "#Lines", "Duplicated In", "Age");

            DuplicationRow actualRow = model.getRow(issue);
            assertThat(actualRow)
                    .hasDescription("<div class=\"details-control\" data-description=\"" + DESCRIPTION + "\">"
                            + DETAILS_ICON + "</div>")
                    .hasAge("1");
            assertThatDetailedColumnContains(actualRow.getFileName(),
                    getFileNameFor(issue, 1), "/path/to/file-1:0000010");
            assertThat(actualRow.getPackageName()).isEqualTo("<a href=\"packageName.45/\">-</a>");
            assertThat(actualRow.getDuplicatedIn()).isEqualTo(
                    String.format("<ul><li>%s</li></ul>", getFileNameFor(duplicate, 2)));
            assertThat(actualRow.getLinesCount()).isEqualTo("15");
            assertThat(actualRow.getSeverity()).isEqualTo("<a href=\"NORMAL\">Normal</a>");
        }
    }

    private String getFileNameFor(final Issue issue, final int index) {
        return String.format("<a href=\"source.%s/#%d\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"/path/to/file-"
                        + index + "\">file-%d:%d</a>", issue.getId().toString(),
                issue.getLineStart(), index, issue.getLineStart());
    }

    private DryModel createModel(final Report report) {
        return new DryModel(report, createFileNameRenderer(), createAgeBuilder(), issue -> DESCRIPTION,
                createJenkinsFacade());
    }
}
