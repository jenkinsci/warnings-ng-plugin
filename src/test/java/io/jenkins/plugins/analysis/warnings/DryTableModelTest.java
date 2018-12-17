package io.jenkins.plugins.analysis.warnings;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.parser.dry.DuplicationGroup;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.analysis.core.model.DescriptionProvider;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.DryTableModel;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link DryTableModel}.
 *
 * @author Ullrich Hafner
 */
class DryTableModelTest {
    private static final String DESCRIPTION = "DESCRIPTION";

    @Test
    @SuppressFBWarnings("DMI")
    void shouldConvertIssueToArrayOfColumns() {
        Locale.setDefault(Locale.ENGLISH);

        DetailsTableModel model = createModel();

        IssueBuilder builder = new IssueBuilder();
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

        assertThat(model.getHeaders(report)).hasSize(6);
        assertThat(model.getWidths(report)).hasSize(6);
        List<List<String>> rows = model.getContent(report);
        assertThat(rows).hasSize(2);

        List<String> columns = rows.get(0);
        assertThat(columns).hasSize(6);
        assertThat(columns.get(0)).contains(DESCRIPTION);
        assertThat(columns.get(1)).contains("file-1:10").contains(issue.getId().toString());
        assertThat(columns.get(2)).isEqualTo("<a href=\"NORMAL\">Normal</a>");
        assertThat(columns.get(3)).isEqualTo("15");
        assertThat(columns.get(4)).contains("file-2:5").contains(duplicate.getId().toString());
        assertThat(columns.get(5)).isEqualTo("1");
    }

    private DetailsTableModel createModel() {
        DescriptionProvider descriptionProvider = mock(DescriptionProvider.class);
        when(descriptionProvider.getDescription(any())).thenReturn(DESCRIPTION);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
        FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);

        DefaultAgeBuilder ageBuilder = new DefaultAgeBuilder(1, "url");

        return new DryTableModel(ageBuilder, fileNameRenderer, descriptionProvider);
    }
}