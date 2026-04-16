package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import hudson.model.Run;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        assertThatThrownBy(() -> tab.getDynamic("missing"))
                .isInstanceOf(NoSuchElementException.class);
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
