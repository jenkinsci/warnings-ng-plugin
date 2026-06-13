package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link Widget}.
 */
class WidgetTest {
    @Test
    void shouldShowWarningSummaryAndFailedResults() {
        var clean = createResultAction(0);
        var checkstyle = createResultAction(2);
        var pmd = createResultAction(1);

        var widget = new Widget(List.of(clean, checkstyle, pmd));

        assertThat(widget.getSymbol()).isEqualTo("symbol-warning-outline plugin-ionicons-api");
        assertThat(widget.getFailedResults()).containsExactly(checkstyle, pmd);
        assertThat(widget.getLines()).containsExactly(Messages.Widget_WarningsForThisBuild(3));
    }

    @Test
    void shouldShowAllClearIfBuildHasNoWarnings() {
        var checkstyle = createResultAction(0);
        var pmd = createResultAction(0);

        var widget = new Widget(List.of(checkstyle, pmd));

        assertThat(widget.getSymbol()).isEqualTo("symbol-status-blue");
        assertThat(widget.getFailedResults()).isEmpty();
        assertThat(widget.getLines()).containsExactly(
                Messages.Widget_AllClear(),
                Messages.Widget_NoWarningsForThisBuild());
    }

    private ResultAction createResultAction(final int totalSize) {
        ResultAction action = mock(ResultAction.class);
        AnalysisResult result = mock(AnalysisResult.class);

        when(action.getResult()).thenReturn(result);
        when(result.getTotalSize()).thenReturn(totalSize);

        return action;
    }
}
