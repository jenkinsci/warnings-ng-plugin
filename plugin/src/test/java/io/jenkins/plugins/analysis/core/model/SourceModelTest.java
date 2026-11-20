package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.SourceModel.ToolIssue;
import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.prism.Marker;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link SourceModel}.
 *
 * @author Ullrich Hafner
 */
class SourceModelTest {
    private static final String FILE_NAME = "path/to/File.java";
    private static final String ICON = "symbol-pmd";

    /**
     * Verifies that each issue of the affected file is shown as a marker, sorted by the position in the file.
     */
    @Test
    void shouldCreateMarkersForAllIssuesOfFile() {
        Run<?, ?> run = mock(Run.class);
        var selected = createIssue(20, 5, "Selected");
        var first = createIssue(10, 1, "First");
        var labelProvider = createLabelProvider();

        var model = createModel(
                run,
                run,
                selected,
                List.of(new ToolIssue(selected, labelProvider), new ToolIssue(first, labelProvider)));

        assertThat(model)
                .hasDisplayName("File.java")
                .hasFileName(FILE_NAME)
                .hasSelectedIssue(selected)
                .hasSelectedMarkerId("issue-" + selected.getId())
                .hasIssuesUrl("table")
                .isNotFixed();
        assertThat(model.getObject()).isSameAs(run);
        assertThat(model.getIssues()).containsExactly(first, selected);
        assertThat(model.createMarkers())
                .extracting(
                        Marker::getId,
                        Marker::getTitle,
                        Marker::getDescription,
                        Marker::getIcon,
                        Marker::getLineStart,
                        Marker::getColumnStart)
                .containsExactly(
                        tuple("issue-" + first.getId(), "First", "Description of First", ICON, 10, 1),
                        tuple("issue-" + selected.getId(), "Selected", "Description of Selected", ICON, 20, 5));
    }

    /**
     * Verifies that the source code of the reference build is shown for fixed issues.
     */
    @Test
    void shouldShowSourceCodeOfReferenceBuildForFixedIssues() {
        Run<?, ?> run = mock(Run.class);
        Run<?, ?> reference = mock(Run.class);
        when(reference.getDisplayName()).thenReturn("#36");
        when(reference.getUrl()).thenReturn("job/x/36/");
        var fixed = createIssue(3, 0, "Fixed");

        var model = createModel(run, reference, fixed, List.of(new ToolIssue(fixed, createLabelProvider())));

        assertThat(model).isFixed().hasSourceBuildName("#36").hasSourceBuildUrl("job/x/36/");
        assertThat(model.getObject()).isSameAs(run);
    }

    private SourceModel createModel(
            final Run<?, ?> run, final Run<?, ?> sourceBuild, final Issue selected, final List<ToolIssue> issues) {
        return new SourceModel(
                run, sourceBuild, selected, issues, StandardCharsets.UTF_8, "table", mock(BuildFolderFacade.class));
    }

    private StaticAnalysisLabelProvider createLabelProvider() {
        var labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getSmallIconUrl()).thenReturn(ICON);
        when(labelProvider.getSourceCodeDescription(any(), any()))
                .thenAnswer(invocation ->
                        "Description of " + invocation.<Issue>getArgument(1).getMessage());
        return labelProvider;
    }

    private Issue createIssue(final int line, final int column, final String message) {
        try (var builder = new IssueBuilder()) {
            return builder.setFileName(FILE_NAME)
                    .setLineStart(line)
                    .setColumnStart(column)
                    .setMessage(message)
                    .build();
        }
    }
}
