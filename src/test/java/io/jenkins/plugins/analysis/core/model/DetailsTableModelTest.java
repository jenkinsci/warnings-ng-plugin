package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;

import io.jenkins.plugins.analysis.core.model.DetailsTableModel.TableRow;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link DetailsTableModel}.
 *
 * @author Ullrich Hafner
 */
class DetailsTableModelTest extends AbstractDetailsModelTest {
    @Test
    void shouldCreateSortableFileName() {
        Issue issue = createIssue(1);
        TableRow model = cerateRow(issue);

        assertThat(model.getFileName()).hasDisplay(createExpectedFileName(issue));
        assertThat(model.getFileName()).hasSort("/path/to/file-1:0000015");
    }

    private TableRow cerateRow(final Issue issue) {
        DescriptionProvider descriptionProvider = mock(DescriptionProvider.class);
        when(descriptionProvider.getDescription(any())).thenReturn(DESCRIPTION);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
        FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);

        DefaultAgeBuilder ageBuilder = new DefaultAgeBuilder(1, "url");

        return new TableRow(ageBuilder, fileNameRenderer, i -> DESCRIPTION, issue, "d");
    }
}
