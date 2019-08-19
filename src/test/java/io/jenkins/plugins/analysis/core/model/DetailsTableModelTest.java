package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;

import io.jenkins.plugins.analysis.core.model.DetailsTableModel.TableRow;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link DetailsTableModel}.
 *
 * @author Ullrich Hafner
 */
class DetailsTableModelTest extends AbstractDetailsModelTest {
    @Test
    void shouldCreateSortableFileName() {
        Issue issue = createIssue(1);
        TableRow model = createRow(issue);

        assertThat(model.getFileName()).hasDisplay(createExpectedFileName(issue));
        assertThat(model.getFileName()).hasSort("/path/to/file-1:0000015");
    }

    private TableRow createRow(final Issue issue) {
        return new TableRow(createAgeBuilder(), createFileNameRenderer(), i -> DESCRIPTION, issue);
    }
}
