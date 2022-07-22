package io.jenkins.plugins.analysis.warnings;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.RevApiInfoExtension;
import edu.hm.hafner.analysis.Severity;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.jenkins.plugins.analysis.core.model.AbstractDetailsModelTest;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel.TableRow;
import io.jenkins.plugins.analysis.warnings.RevApi.RevApiModel;
import io.jenkins.plugins.analysis.warnings.RevApi.RevApiModel.RevApiRow;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;

public class RevApiModelTest extends AbstractDetailsModelTest {

    private static final String DESCRIPTION = "DESCRIPTION";

    @Test
    @SuppressFBWarnings("DMI")
    void shouldConvertIssueToArrayOfColumns() {
        try (IssueBuilder builder = new IssueBuilder()) {
            Locale.setDefault(Locale.ENGLISH);
            Map<String, String> severities = new HashMap<>();
            severities.put("BINARY", "BREAKING");
            severities.put("SOURCE", "NON_BREAKING");
            RevApiInfoExtension additionalData = new RevApiInfoExtension("java.class.added",
                    "java.io.jenkins.plugins.analysis.warnings.RevApiModelTest", "null", severities);
            Issue issue = builder.setFileName("/path/to/file-1")
                    .setCategory("class")
                    .setSeverity(Severity.WARNING_HIGH)
                    .setFileName("RevApiModelTest")
                    .setAdditionalProperties(additionalData)
                    .build();
            Report report = new Report();
            report.add(issue);

            RevApiModel model = new RevApiModel(report, createFileNameRenderer(), createAgeBuilder(), i -> DESCRIPTION, createJenkinsFacade());

            String columnDefinitions = model.getColumnsDefinition();
            assertThatJson(columnDefinitions).isArray().hasSize(9);

            String[] columns = {"description", "issueName","oldFile", "newFile", "category", "binary", "source", "severity", "age"};
            for (int column = 0; column < columns.length; column++) {
                verifyColumnProperty(model, column, columns[column]);
            }
            assertThat(getLabels(model))
                    .containsExactly("Details", "Name", "Old File", "New File", "Category", "Binary", "Source", "Severity", "Age");

            TableRow tableRow = model.getRow(issue);
            if (tableRow instanceof RevApiRow) {
                RevApiRow row = (RevApiRow) tableRow;
                assertThat(row.getBinary().equals("BREAKING")).isTrue();
                assertThat(row.getSource().equals("NON_BREAKING")).isTrue();
                assertThat(row.getCategory().equals("class")).isTrue();
                assertThat(row.getIssueName().equals("java.class.added")).isTrue();
                assertThat(row.getSeverity().equals("<a href=\"HIGH\">High</a>")).isTrue();
                assertThat(row.getOldFile().equals("java.io.jenkins.plugins.analysis.warnings.RevApiModelTest")).isTrue();
                assertThat(row.getNewFile().equals("-")).isTrue();
            }
        }
    }
}
