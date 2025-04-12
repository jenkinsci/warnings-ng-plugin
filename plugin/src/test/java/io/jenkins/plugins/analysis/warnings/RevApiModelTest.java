package io.jenkins.plugins.analysis.warnings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.RevApiInfoExtension;
import edu.hm.hafner.analysis.Severity;

import java.util.HashMap;
import java.util.Map;

import io.jenkins.plugins.analysis.core.model.AbstractDetailsModelTest;
import io.jenkins.plugins.analysis.warnings.RevApi.RevApiModel;
import io.jenkins.plugins.analysis.warnings.RevApi.RevApiModel.RevApiRow;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

@DefaultLocale("en")
class RevApiModelTest extends AbstractDetailsModelTest {
    private static final String DESCRIPTION = "DESCRIPTION";

    @Test
    void shouldConvertIssueToArrayOfColumns() {
        try (var builder = new IssueBuilder()) {
            Map<String, String> severities = new HashMap<>();
            severities.put("BINARY", "BREAKING");
            severities.put("SOURCE", "NON_BREAKING");
            var additionalData = new RevApiInfoExtension("java.class.added",
                    "java.io.jenkins.plugins.analysis.warnings.RevApiModelTest", "-", severities);
            var issue = builder.setFileName("/path/to/file-1")
                    .setCategory("class")
                    .setSeverity(Severity.WARNING_HIGH)
                    .setFileName("RevApiModelTest")
                    .setAdditionalProperties(additionalData)
                    .build();
            var report = new Report();
            report.add(issue);

            var model = new RevApiModel(report, createFileNameRenderer(), createAgeBuilder(), i -> DESCRIPTION,
                    createJenkinsFacade());

            var columnDefinitions = model.getColumnsDefinition();
            assertThatJson(columnDefinitions).isArray().hasSize(9);

            String[] columns = {"description", "issueName", "oldFile", "newFile", "category", "binary", "source", "severity", "age"};
            for (int column = 0; column < columns.length; column++) {
                verifyColumnProperty(model, column, columns[column]);
            }
            Assertions.assertThat(getLabels(model))
                    .containsExactly("Details", "Name", "Old File", "New File", "Category", "Binary", "Source",
                            "Severity", "Age");

            Assertions.assertThat(model.getRow(issue)).isInstanceOfSatisfying(RevApiRow.class,
                    row -> RevApiRevApiModelRevApiRowAssert.assertThat(row)
                            .hasBinary("BREAKING")
                            .hasSource("NON_BREAKING")
                            .hasCategory("class")
                            .hasIssueName("java.class.added")
                            .hasSeverity("<a href=\"HIGH\">High</a>")
                            .hasOldFile("java.io.jenkins.plugins.analysis.warnings.RevApiModelTest")
                            .hasNewFile("-")
            );
        }
    }
}
