package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Actionable;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.RunTab.NewBuildPageFacade;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
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
    void shouldSortWarningActionsAndResolveDynamicActions() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 3);
        var pmd = createResultAction("pmd", "PMD", 5);
        var spotbugs = createResultAction("spotbugs", "spotbugs", 3);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(spotbugs, checkstyle, pmd));

        var tab = new RunTab(run);

        assertThat(tab.getWarningActions())
                .extracting(ResultAction::getId)
                .containsExactly("pmd", "checkstyle", "spotbugs");
        assertThat(tab.getDynamic("checkstyle")).isSameAs(checkstyle);
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> tab.getDynamic("missing"));
    }

    @Test
    void shouldCreateBadgeWithAggregatedWarningCount() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 2);
        var pmd = createResultAction("pmd", "PMD", 3);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle, pmd));

        var badge = new RunTab(run).getBadge();

        assertThat(badge).isNotNull();
        assertThat(badge.getText()).isEqualTo("5");
        assertThat(badge.getTooltip()).isEqualTo(Messages.ResultAction_Badge(5));
        assertThat(badge.getSeverity()).isEqualTo("warning");
    }

    @Test
    void shouldNotCreateBadgeForZeroWarnings() {
        Run<?, ?> run = mock(Run.class);
        var checkstyle = createResultAction("checkstyle", "CheckStyle", 0);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle));

        assertThat(new RunTab(run).getBadge()).isNull();
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

        new RunTab(mock(Actionable.class), facade).doIndex().generateResponse(createRequest(), response, new Object());

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

        return new RunTab(run, facade);
    }

    private ResultAction createResultAction(final String id, final String displayName, final int totalSize) {
        ResultAction action = mock(ResultAction.class);
        AnalysisResult result = mock(AnalysisResult.class);

        when(action.getId()).thenReturn(id);
        when(action.getUrlName()).thenReturn(id);
        when(action.getDisplayName()).thenReturn(displayName);
        when(action.getResult()).thenReturn(result);
        when(result.getTotalSize()).thenReturn(totalSize);

        return action;
    }
}
