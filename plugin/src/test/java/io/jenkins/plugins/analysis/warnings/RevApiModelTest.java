package io.jenkins.plugins.analysis.warnings;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.RevApiInfoExtension;
import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.model.AbstractDetailsModelTest;
import io.jenkins.plugins.analysis.warnings.RevApi.RevApiModel;
import io.jenkins.plugins.analysis.warnings.RevApi.RevApiModel.RevApiRow;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.assertThat;
import static io.jenkins.plugins.analysis.warnings.RevApiRevApiModelRevApiRowAssert.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

@DefaultLocale("en")
class RevApiModelTest extends AbstractDetailsModelTest {
    private static final String DESCRIPTION = "DESCRIPTION";

    @Test
    void shouldConvertIssueToArrayOfColumns() {
        try (IssueBuilder builder = new IssueBuilder()) {
            Map<String, String> severities = new HashMap<>();
            severities.put("BINARY", "BREAKING");
            severities.put("SOURCE", "NON_BREAKING");
            RevApiInfoExtension additionalData = new RevApiInfoExtension("java.class.added",
                    "java.io.jenkins.plugins.analysis.warnings.RevApiModelTest", "-", severities);
            Issue issue = builder.setFileName("/path/to/file-1")
                    .setCategory("class")
                    .setSeverity(Severity.WARNING_HIGH)
                    .setFileName("RevApiModelTest")
                    .setAdditionalProperties(additionalData)
                    .build();
            Report report = new Report();
            report.add(issue);

            RevApiModel model = new RevApiModel(report, createFileNameRenderer(), createAgeBuilder(), i -> DESCRIPTION,
                    createJenkinsFacade());

            String columnDefinitions = model.getColumnsDefinition();
            assertThatJson(columnDefinitions).isArray().hasSize(9);

            String[] columns = {"description", "issueName", "oldFile", "newFile", "category", "binary", "source", "severity", "age"};
            for (int column = 0; column < columns.length; column++) {
                verifyColumnProperty(model, column, columns[column]);
            }
            assertThat(getLabels(model))
                    .containsExactly("Details", "Name", "Old File", "New File", "Category", "Binary", "Source",
                            "Severity", "Age");

            assertThat(model.getRow(issue)).isInstanceOfSatisfying(RevApiRow.class,
                    row -> assertThat(row)
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
