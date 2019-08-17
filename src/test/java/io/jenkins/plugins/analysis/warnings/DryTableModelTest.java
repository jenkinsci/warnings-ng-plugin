package io.jenkins.plugins.analysis.warnings;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.parser.dry.DuplicationGroup;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.jenkins.plugins.analysis.core.model.AbstractDetailsModelTest;
import io.jenkins.plugins.analysis.core.model.DescriptionProvider;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.DryModel;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.DryModel.DuplicationRow;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

        DuplicationRow expected = new DuplicationRow();
        expected.setDescription("<div class=\"details-control\" data-description=\"d\"></div>");
        expected.setFileName(getFileNameFor(issue, 1));
        expected.setAge("1");
        expected.setPackageName("<a href=\"packageName.45/\">-</a>");
        expected.setDuplicatedIn(String.format("<ul><li>%s</li></ul>", getFileNameFor(duplicate, 2)));
        expected.setLinesCount("15");
        expected.setSeverity("<a href=\"NORMAL\">Normal</a>");

        assertThat(model.getRow(report, issue, "d")).isEqualToComparingFieldByField(expected);
    }

    private String getFileNameFor(final Issue issue, final int index) {
        return String.format("<a href=\"source.%s/#%d\">file-%d:%d</a>",  issue.getId().toString(),
                issue.getLineStart(), index, issue.getLineStart());
    }

    private DetailsTableModel createModel() {
        DescriptionProvider descriptionProvider = mock(DescriptionProvider.class);
        when(descriptionProvider.getDescription(any())).thenReturn(DESCRIPTION);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
        FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);

        DefaultAgeBuilder ageBuilder = new DefaultAgeBuilder(1, "url");

        return new DryModel(ageBuilder, fileNameRenderer, descriptionProvider);
    }
}
