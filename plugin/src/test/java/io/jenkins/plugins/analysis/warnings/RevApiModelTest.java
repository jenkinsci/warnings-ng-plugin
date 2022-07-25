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

class RevApiModelTest extends AbstractDetailsModelTest {

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

            String[] columns = {"description", "issueName", "oldFile", "newFile", "category", "binary", "source", "severity", "age"};
            for (int column = 0; column < columns.length; column++) {
                verifyColumnProperty(model, column, columns[column]);
            }
            assertThat(getLabels(model))
                    .containsExactly("Details", "Name", "Old File", "New File", "Category", "Binary", "Source", "Severity", "Age");

            TableRow tableRow = model.getRow(issue);
            if (tableRow instanceof RevApiRow) {
                RevApiRow row = (RevApiRow) tableRow;
                assertThat("BREAKING".equals(row.getBinary())).isTrue();
                assertThat("NON_BREAKING".equals(row.getSource())).isTrue();
                assertThat("class".equals(row.getCategory())).isTrue();
                assertThat("java.class.added".equals(row.getIssueName())).isTrue();
                assertThat("<a href=\"HIGH\">High</a>".equals(row.getSeverity())).isTrue();
                assertThat("java.io.jenkins.plugins.analysis.warnings.RevApiModelTest".equals(row.getOldFile())).isTrue();
                assertThat("-".equals(row.getNewFile())).isTrue();
            }
        }
    }
}
