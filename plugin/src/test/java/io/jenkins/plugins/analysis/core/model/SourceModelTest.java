package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Severity;
import hudson.console.ConsoleNote;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.SourceModel.IssueState;
import io.jenkins.plugins.analysis.core.model.SourceModel.ToolIssue;
import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileBlame;
import io.jenkins.plugins.prism.Marker;
import io.jenkins.plugins.prism.Marker.Badge;
import io.jenkins.plugins.prism.SourceCodeViewModel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link SourceModel}.
 *
 * @author Ullrich Hafner
 */
class SourceModelTest {
    private static final String FILE_NAME = "path/to/File.java";
    private static final String ICON = "symbol-pmd";
    private static final String TOOL = "PMD";

    /**
     * Verifies that each issue of the affected file is shown as a marker, sorted by the position in the file.
     */
    @Test
    void shouldCreateMarkersForAllIssuesOfFile() {
        Run<?, ?> run = mock(Run.class);
        var selected = createIssue(20, 5, "Selected", Severity.WARNING_NORMAL);
        var first = createIssue(10, 1, "First", Severity.WARNING_NORMAL);
        var action = createAction(mock(Blames.class));

        var model = createModel(
                run,
                run,
                selected,
                List.of(
                        new ToolIssue(selected, action, IssueState.OUTSTANDING),
                        new ToolIssue(first, action, IssueState.OUTSTANDING)));

        assertThat(model)
                .hasDisplayName("File.java")
                .hasFileName(FILE_NAME)
                .hasSelectedIssue(selected)
                .hasSelectedMarkerId("issue-" + selected.getId())
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
     * Verifies that the severity of an issue is mapped to the severity of the marker.
     */
    @Test
    void shouldMapSeverities() {
        assertThat(SourceModel.asMarkerSeverity(Severity.ERROR)).isEqualTo(Marker.Severity.ERROR);
        assertThat(SourceModel.asMarkerSeverity(Severity.WARNING_HIGH)).isEqualTo(Marker.Severity.ERROR);
        assertThat(SourceModel.asMarkerSeverity(Severity.WARNING_NORMAL)).isEqualTo(Marker.Severity.WARNING);
        assertThat(SourceModel.asMarkerSeverity(Severity.WARNING_LOW)).isEqualTo(Marker.Severity.INFO);
    }

    /**
     * Verifies that the badges show the severity, the state, the tool, the category and type, and the blame.
     */
    @Test
    void shouldShowBadges() {
        Run<?, ?> run = mock(Run.class);
        var newIssue = createIssue(10, 1, "New", Severity.WARNING_HIGH);
        var outstanding = createIssue(20, 1, "Outstanding", Severity.WARNING_LOW);
        var blames = mock(Blames.class);
        var fileBlame = mock(FileBlame.class);
        when(blames.contains(FILE_NAME)).thenReturn(true);
        when(blames.getBlame(FILE_NAME)).thenReturn(fileBlame);
        when(fileBlame.getName(10)).thenReturn("Ulli");
        when(fileBlame.getEmail(10)).thenReturn("ulli@example.com");
        when(fileBlame.getCommit(10)).thenReturn("0123456789abcdef");
        when(fileBlame.getName(20)).thenReturn("-");
        when(fileBlame.getEmail(20)).thenReturn("-");
        when(fileBlame.getCommit(20)).thenReturn("-");
        var action = createAction(blames);

        var markers = createModel(
                        run,
                        run,
                        newIssue,
                        List.of(
                                new ToolIssue(newIssue, action, IssueState.NEW),
                                new ToolIssue(outstanding, action, IssueState.OUTSTANDING)))
                .createMarkers();

        assertThat(markers)
                .extracting(Marker::getSeverity)
                .containsExactly(Marker.Severity.ERROR, Marker.Severity.INFO);
        assertThat(markers.get(0).getBadges())
                .extracting(Badge::getText)
                .containsExactly("High", "New", TOOL, "Category · Type", "Ulli · 0123456");
        assertThat(markers.get(0).getBadges().get(4).getTooltip()).contains("0123456789abcdef", "ulli@example.com");
        assertThat(markers.get(1).getBadges())
                .extracting(Badge::getText)
                .containsExactly("Low", TOOL, "Category · Type");
        assertThat(markers.get(0).getData())
                .containsEntry("severity", "high")
                .containsEntry("state", "new")
                .containsEntry("tool", "pmd");
        assertThat(markers.get(1).getData()).containsEntry("severity", "low").containsEntry("state", "outstanding");
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
        var fixed = createIssue(3, 0, "Fixed", Severity.WARNING_NORMAL);

        var model = createModel(
                run,
                reference,
                fixed,
                List.of(new ToolIssue(fixed, createAction(mock(Blames.class)), IssueState.FIXED)));

        assertThat(model).isFixed().hasSourceBuildName("#36").hasSourceBuildUrl("job/x/36/");
        assertThat(model.getObject()).isSameAs(run);
        assertThat(model.createMarkers().get(0).getBadges())
                .extracting(Badge::getText)
                .contains("Fixed");
    }

    private SourceModel createModel(
            final Run<?, ?> run, final Run<?, ?> sourceBuild, final Issue selected, final List<ToolIssue> issues) {
        return new SourceModel(
                run, sourceBuild, selected, issues, StandardCharsets.UTF_8, mock(BuildFolderFacade.class));
    }

    /**
     * Verifies that issues in the console log are shown in an excerpt of the console log: the lines of all issues
     * plus 10 lines of context before and after, without the console notes.
     */
    @Test
    void shouldShowExcerptOfConsoleLog() {
        Run<?, ?> run = mock(Run.class);
        var selected = createConsoleIssue(20);
        var other = createConsoleIssue(15);
        var action = createAction(mock(Blames.class));
        when(action.getLabelProvider().getSmallIconUrl()).thenReturn(StringUtils.EMPTY); // symbols require Jenkins
        var buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.readConsoleLog(run))
                .thenReturn(IntStream.rangeClosed(1, 100)
                        .mapToObj(line ->
                                ConsoleNote.PREAMBLE_STR + "note" + ConsoleNote.POSTAMBLE_STR + "line " + line));

        var model = new SourceModel(
                run,
                run,
                selected,
                List.of(new ToolIssue(selected, action, IssueState.NEW), new ToolIssue(other, action, IssueState.NEW)),
                StandardCharsets.UTF_8,
                buildFolder);

        assertThat(model)
                .isConsoleLog()
                .hasDisplayName(Messages.ConsoleLog_Name())
                .hasFileName(Messages.ConsoleLog_Name());
        assertThat(model.getSourceCode()).isInstanceOfSatisfying(SourceCodeViewModel.class, view -> {
            var code = Jsoup.parse(view.getSourceCode()).selectFirst(".prism-source__raw > code");
            assertThat(code).isNotNull();
            assertThat(code.attr("data-first-line")).isEqualTo("5");
            assertThat(code.attr("data-lines")).isEqualTo("26");
            assertThat(code.classNames()).isEmpty();
            assertThat(code.wholeText()).startsWith("line 5\nline 6\n").endsWith("line 30");
        });
    }

    private Issue createConsoleIssue(final int line) {
        try (var builder = new IssueBuilder()) {
            return builder.setFileName(ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID)
                    .setLineStart(line)
                    .setMessage("Message " + line)
                    .build();
        }
    }

    private ResultAction createAction(final Blames blames) {
        var labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getName()).thenReturn(TOOL);
        when(labelProvider.getSmallIconUrl()).thenReturn(ICON);
        when(labelProvider.getSourceCodeDescription(any(), any()))
                .thenAnswer(invocation ->
                        "Description of " + invocation.<Issue>getArgument(1).getMessage());
        var result = mock(AnalysisResult.class);
        when(result.getBlames()).thenReturn(blames);
        var action = mock(ResultAction.class);
        when(action.getId()).thenReturn("pmd");
        when(action.getLabelProvider()).thenReturn(labelProvider);
        when(action.getResult()).thenReturn(result);
        return action;
    }

    private Issue createIssue(final int line, final int column, final String message, final Severity severity) {
        try (var builder = new IssueBuilder()) {
            return builder.setFileName(FILE_NAME)
                    .setLineStart(line)
                    .setColumnStart(column)
                    .setMessage(message)
                    .setSeverity(severity)
                    .setCategory("Category")
                    .setType("Type")
                    .build();
        }
    }
}
