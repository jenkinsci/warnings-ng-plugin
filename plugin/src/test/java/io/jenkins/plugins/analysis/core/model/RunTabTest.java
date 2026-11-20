package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import hudson.model.Actionable;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

/**
 * Tests the class {@link RunTab}.
 */
class RunTabTest {
    @Test
    void shouldShowWarningsTabOnlyIfWarningsExist() {
        Run<?, ?> run = mock(Run.class);
        when(run.getActions(ResultAction.class)).thenReturn(List.of());

        var tab = new RunTab(run);

        assertThat(tab.getDisplayName()).isEqualTo("Warnings");
        assertThat(tab.getUrlName()).isEqualTo("warnings");
        assertThat(tab.getIconFileName()).isNull();

        var checkstyle = createResultAction("checkstyle", "CheckStyle", 1);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle));

        assertThat(tab.getIconFileName()).isEqualTo("symbol-warning-outline plugin-ionicons-api");
    }

    @Test
    void shouldSortWarningActionsAndNotResolveActionsOfTools() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 3);
        var pmd = createResultAction("pmd", "PMD", 5);
        var spotbugs = createResultAction("spotbugs", "spotbugs", 3);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(spotbugs, checkstyle, pmd));

        var tab = new RunTab(run);

        assertThat(tab.getWarningActions())
                .extracting(ResultAction::getId)
                .containsExactly("pmd", "checkstyle", "spotbugs");
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> tab.getDynamic("checkstyle"));
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> tab.getDynamic("missing"));
    }

    @Test
    void shouldCreateBadgeWithNumberOfNewWarnings() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 4);
        when(checkstyle.getResult().getNewSize()).thenReturn(2);
        var pmd = createResultAction("pmd", "PMD", 3);
        when(pmd.getResult().getNewSize()).thenReturn(3);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle, pmd));

        var badge = new RunTab(run).getBadge();

        assertThat(badge).isNotNull();
        assertThat(badge.getText()).isEqualTo("5");
        assertThat(badge.getTooltip()).isEqualTo(Messages.RunTab_Badge_NewWarnings(5));
        assertThat(badge.getSeverity()).isEqualTo("warning");
    }

    @Test
    void shouldCreateBadgeWithNumberOfMissedQualityGates() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 4);
        when(checkstyle.getResult().getNewSize()).thenReturn(2);
        when(checkstyle.getQualityGateResult())
                .thenReturn(createQualityGateResult(QualityGateStatus.WARNING, QualityGateStatus.PASSED));
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle));

        var unstable = new RunTab(run).getBadge();

        assertThat(unstable).isNotNull();
        assertThat(unstable.getText()).isEqualTo("1");
        assertThat(unstable.getTooltip()).isEqualTo(Messages.RunTab_Badge_QualityGates(1));
        assertThat(unstable.getSeverity()).isEqualTo("warning");

        var pmd = createResultAction("pmd", "PMD", 1);
        when(pmd.getQualityGateResult()).thenReturn(createQualityGateResult(QualityGateStatus.FAILED));
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle, pmd));

        var failed = new RunTab(run).getBadge();

        assertThat(failed).isNotNull();
        assertThat(failed.getText()).isEqualTo("2");
        assertThat(failed.getTooltip()).isEqualTo(Messages.RunTab_Badge_QualityGates(2));
        assertThat(failed.getSeverity()).isEqualTo("danger");
    }

    @Test
    void shouldNotCreateBadgeWithoutNewWarnings() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 3);
        when(checkstyle.getQualityGateResult()).thenReturn(createQualityGateResult(QualityGateStatus.PASSED));
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle));

        assertThat(new RunTab(run).getBadge()).isNull();
    }

    /**
     * Creates a quality gate result with a quality gate for each of the specified statuses.
     *
     * @param statuses
     *         the status of each quality gate
     *
     * @return the quality gate result
     */
    private QualityGateResult createQualityGateResult(final QualityGateStatus... statuses) {
        var result = new QualityGateResult();
        for (QualityGateStatus status : statuses) {
            result.add(new WarningsQualityGate(0, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE), status, "1");
        }
        return result;
    }

    /**
     * Verifies that the overview model contains the sorted warning actions of the run.
     */
    @Test
    void shouldCreateOverviewWithSortedActions() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 3);
        var pmd = createResultAction("pmd", "PMD", 5);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle, pmd));

        assertThat(new RunTab(run).getOverview()).hasObject(run).hasActions(pmd, checkstyle);
    }

    /**
     * Verifies that the trend model provides a source for all tools and for each of the sorted warning actions.
     */
    @Test
    void shouldCreateTrendWithSortedActions() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 3);
        var pmd = createResultAction("pmd", "PMD", 5);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle, pmd));

        var trend = new RunTab(run).getTrend();

        assertThat(trend).hasObject(run).hasUrlName("trend");
        assertThat(trend.getSources())
                .extracting(TrendModel.TrendSource::getId)
                .containsExactly(TrendModel.ALL_TOOLS_ID, "pmd", "checkstyle");
    }

    /**
     * Verifies that the issues page is created for the sorted actions of the run.
     */
    @Test
    void shouldCreateIssuesPage() {
        Run<?, ?> run = mock(Run.class);
        var report = new Report();
        try (var builder = new IssueBuilder()) {
            report.add(builder.setFileName("file.txt").build());
        }
        var checkstyle = createResultActionWithIssues("checkstyle", report);
        var pmd = createResultActionWithIssues("pmd", new Report());
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle, pmd));

        var table = new RunTab(run).getTable();

        assertThat(table).hasDisplayName("Issues").hasUrlName("table");
        assertThat(table.getObject()).isSameAs(run);
        assertThat(table.getTools()).containsExactly(checkstyle);
    }

    /**
     * Verifies that the log page shows the messages of the sorted actions of the run, including the tools without
     * issues.
     */
    @Test
    void shouldCreateLogPage() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 0);
        when(checkstyle.getResult().getInfoMessages()).thenReturn(Lists.immutable.of("Parsing checkstyle.xml"));
        when(checkstyle.getResult().getErrorMessages()).thenReturn(Lists.immutable.empty());
        var pmd = createResultAction("pmd", "PMD", 5);
        when(pmd.getResult().getInfoMessages()).thenReturn(Lists.immutable.empty());
        when(pmd.getResult().getErrorMessages()).thenReturn(Lists.immutable.of("No files found"));
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle, pmd));

        var log = new RunTab(run).getLog();

        assertThat(log).hasDisplayName("Log Messages").hasUrlName("log").hasDefaultToolId("pmd");
        assertThat(log.getObject()).isSameAs(run);
        assertThat(log.getTools()).extracting(LogModel.ToolLog::getId).containsExactly("pmd", "checkstyle");
    }

    /**
     * Verifies that the hotspots page shows the sorted actions of the run that contain issues.
     */
    @Test
    void shouldCreateHotspotsPage() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 0);
        var pmd = createResultAction("pmd", "PMD", 2);
        var spotbugs = createResultAction("spotbugs", "SpotBugs", 5);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle, pmd, spotbugs));

        var hotspots = new RunTab(run).getHotspots();

        assertThat(hotspots).hasDisplayName("Hotspots").hasUrlName("hotspots").hasSeveralTools();
        assertThat(hotspots.getObject()).isSameAs(run);
        assertThat(hotspots.getTools()).containsExactly(spotbugs, pmd);
    }

    /**
     * Verifies that the treemap page shows only the tools with issues.
     */
    @Test
    void shouldCreateTreemapPage() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 0);
        var pmd = createResultAction("pmd", "PMD", 2);
        var spotbugs = createResultAction("spotbugs", "SpotBugs", 5);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle, pmd, spotbugs));

        var treemap = new RunTab(run).getTreemap();

        assertThat(treemap).hasDisplayName("Treemap").hasUrlName("treemap").hasSeveralTools();
        assertThat(treemap.getObject()).isSameAs(run);
        assertThat(treemap.getTools()).containsExactly(spotbugs, pmd);
    }

    /**
     * Verifies that the link to the affected file of an issue shows the source code page with all issues of the same
     * file, independent of the tool that reported the issues.
     */
    @Test
    void shouldResolveSourceLinksOfAllTools() {
        Run<?, ?> run = mock(Run.class);
        var selected = createIssue("file.txt", 20);
        var sameFile = createIssue("file.txt", 10);
        var otherFile = createIssue("other.txt", 5);
        var checkstyle = createResultActionWithIssues("checkstyle", createReport(sameFile, otherFile));
        var pmd = createResultActionWithIssues("pmd", createReport(selected));
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle, pmd));

        var tab = new RunTab(run);

        assertThat(tab.getDynamic("source." + selected.getId())).isInstanceOfSatisfying(SourceModel.class, source -> {
            assertThat(source)
                    .hasSelectedIssue(selected)
                    .hasDisplayName("file.txt")
                    .hasSelectedMarkerId("issue-" + selected.getId())
                    .isNotFixed();
            assertThat(source.getObject()).isSameAs(run);
            assertThat(source.getIssues()).containsExactly(sameFile, selected);
            assertThat(source.getIssuesUrl()).startsWith("table#");
        });
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> tab.getDynamic("source.00000000-0000-0000-0000-000000000000"));
    }

    /**
     * Verifies that issues that reference the console log are still shown in the classic view of the tool.
     */
    @Test
    void shouldShowIssuesInConsoleLogInClassicView() {
        Run<?, ?> run = mock(Run.class);
        var issue = createIssue("jenkins-console.log", 10);
        var pmd = createResultActionWithIssues("pmd", createReport(issue));
        when(run.getActions(ResultAction.class)).thenReturn(List.of(pmd));
        var link = "source." + issue.getId();
        var consoleView = new Object();
        IssuesDetail details = mock(IssuesDetail.class);
        when(details.createDetails(link)).thenReturn(consoleView);
        when(pmd.getTarget()).thenReturn(details);

        assertThat(new RunTab(run).getDynamic(link)).isSameAs(consoleView);
    }

    /**
     * Verifies that the link to the affected file of a fixed issue shows the source code of the reference build of
     * the same tool, together with all fixed issues of the same file.
     */
    @Test
    void shouldResolveSourceLinksOfFixedIssuesInReferenceBuild() {
        var selected = createIssue("fixed.txt", 5);
        var sameFile = createIssue("fixed.txt", 1);
        var otherFile = createIssue("other.txt", 1);
        var pmd = createResultActionWithIssues("pmd", new Report());
        when(pmd.getResult().getFixedIssues()).thenReturn(createReport(selected, sameFile, otherFile));
        Run<?, ?> run = mock(Run.class);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(pmd));

        var referenceCheckstyle = createResultActionWithIssues("checkstyle", new Report());
        var referencePmd = createResultActionWithIssues("pmd", createReport(selected, sameFile, otherFile));
        Run<?, ?> reference = mock(Run.class);
        when(reference.getActions(ResultAction.class)).thenReturn(List.of(referenceCheckstyle, referencePmd));
        when(referencePmd.getOwner()).thenAnswer(invocation -> reference);
        var link = "source." + selected.getId();

        var tab = new RunTab(run);

        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> tab.getDynamic(link));

        when(pmd.getResult().getReferenceBuild()).thenAnswer(invocation -> Optional.of(reference));

        assertThat(tab.getDynamic(link)).isInstanceOfSatisfying(SourceModel.class, source -> {
            assertThat(source).hasSelectedIssue(selected).isFixed();
            assertThat(source.getObject()).isSameAs(run);
            assertThat(source.getIssues()).containsExactly(sameFile, selected);
            assertThat(source.getIssuesUrl()).contains("show=fixed");
        });
        verify(referenceCheckstyle, never()).getTarget();
    }

    private Issue createIssue(final String fileName, final int line) {
        try (var builder = new IssueBuilder()) {
            return builder.setFileName(fileName).setLineStart(line).build();
        }
    }

    private Report createReport(final Issue... issues) {
        var report = new Report();
        report.addAll(List.of(issues));
        return report;
    }

    /**
     * Verifies that the index page redirects to the overview page of the run, using an absolute URL that is derived
     * from the URL of the run.
     *
     * @throws IOException
     *         never thrown
     * @throws ServletException
     *         never thrown
     */
    @Test
    void shouldRedirectToOverviewIfNewBuildPageIsEnabled() throws IOException, ServletException {
        var response = mock(StaplerResponse2.class);

        var redirect = createTab(true).doIndex();
        redirect.generateResponse(createRequest(), response, new Object());

        verify(response).sendRedirect(anyInt(), eq("/jenkins/job/x/42/warnings/overview"));
    }

    /**
     * Verifies that the index page is not available if the new build page is disabled.
     *
     * @throws IOException
     *         never thrown
     * @throws ServletException
     *         never thrown
     */
    @Test
    void shouldNotRedirectIfNewBuildPageIsDisabled() throws IOException, ServletException {
        var response = mock(StaplerResponse2.class);

        createTab(false).doIndex().generateResponse(createRequest(), response, new Object());

        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
        verify(response, never()).sendRedirect(anyInt(), anyString());
    }

    /**
     * Verifies that the index page is not available if the tab is not attached to a run.
     *
     * @throws IOException
     *         never thrown
     * @throws ServletException
     *         never thrown
     */
    @Test
    void shouldNotRedirectIfOwnerIsNoRun() throws IOException, ServletException {
        var facade = mock(NewBuildPageFacade.class);
        when(facade.isEnabled()).thenReturn(true);
        var response = mock(StaplerResponse2.class);

        new RunTab(mock(Actionable.class), facade, mock(BuildFolderFacade.class))
                .doIndex()
                .generateResponse(createRequest(), response, new Object());

        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
        verify(response, never()).sendRedirect(anyInt(), anyString());
    }

    /**
     * Creates a request stub with the context path {@code /jenkins}.
     *
     * @return the request
     */
    private StaplerRequest2 createRequest() {
        var request = mock(StaplerRequest2.class);
        when(request.getContextPath()).thenReturn("/jenkins");
        return request;
    }

    /**
     * Creates a tab for a run with one warning action.
     *
     * @param isNewBuildPageEnabled
     *         determines whether the new build page is enabled
     *
     * @return the tab
     */
    private RunTab createTab(final boolean isNewBuildPageEnabled) {
        Run<?, ?> run = mock(Run.class);
        when(run.getUrl()).thenReturn("job/x/42/");
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 3);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle));
        var facade = mock(NewBuildPageFacade.class);
        when(facade.isEnabled()).thenReturn(isNewBuildPageEnabled);

        return new RunTab(run, facade, mock(BuildFolderFacade.class));
    }

    private ResultAction createResultActionWithIssues(final String id, final Report issues) {
        var action = createResultAction(id, id, issues.size());
        var labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getIssuesModel(any(), any(), any())).thenReturn(mock(IssuesModel.class));
        when(action.getLabelProvider()).thenReturn(labelProvider);
        when(action.getResult().getIssues()).thenReturn(issues);
        return action;
    }

    private ResultAction createResultAction(final String id, final String displayName, final int totalSize) {
        ResultAction action = mock(ResultAction.class);
        AnalysisResult result = mock(AnalysisResult.class);

        when(action.getId()).thenReturn(id);
        when(action.getUrlName()).thenReturn(id);
        when(action.getDisplayName()).thenReturn(displayName);
        when(action.getResult()).thenReturn(result);
        when(action.getQualityGateResult()).thenReturn(new QualityGateResult());
        when(result.getTotalSize()).thenReturn(totalSize);
        when(result.getFixedIssues()).thenReturn(new Report());

        return action;
    }
}
