package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import java.nio.charset.StandardCharsets;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.analysis.core.util.TrendChartType;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ResultAction}.
 *
 * @author Ullrich Hafner
 */
class ResultActionTest {
    @Test
    void shouldRestoreRun() {
        var action = new ResultAction(null, mock(AnalysisResult.class),
                new HealthDescriptor(0, 0, Severity.WARNING_HIGH), "ID", "Name",
                "icon", StandardCharsets.UTF_8, TrendChartType.AGGREGATION_TOOLS);

        assertThat(action.getOwner()).isNull();

        Run<?, ?> run = mock(Run.class);
        action.onAttached(run);
        assertThat(action.getOwner()).isSameAs(run);
    }

    @Test
    void shouldCreateBadgeIfWarningsExist() {
        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(5);

        var action = new ResultAction(null, result,
                new HealthDescriptor(0, 0, Severity.WARNING_HIGH), "ID", "Name",
                "icon", StandardCharsets.UTF_8, TrendChartType.AGGREGATION_TOOLS);

        assertThat(action.getBadge()).isNotNull();
        assertThat(action.getBadge().getText()).isEqualTo("5");
        assertThat(action.getBadge().getTooltip()).isEqualTo(Messages.ResultAction_Badge(5));
        assertThat(action.getBadge().getSeverity()).isEqualTo("warning");
    }

    @Test
    void shouldNotCreateBadgeIfNoWarningsExist() {
        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(0);

        var action = new ResultAction(null, result,
                new HealthDescriptor(0, 0, Severity.WARNING_HIGH), "ID", "Name",
                "icon", StandardCharsets.UTF_8, TrendChartType.AGGREGATION_TOOLS);

        assertThat(action.getBadge()).isNull();
    }
}
