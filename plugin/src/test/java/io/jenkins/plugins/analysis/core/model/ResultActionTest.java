package io.jenkins.plugins.analysis.core.model;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

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
        ResultAction action = new ResultAction(null, mock(AnalysisResult.class),
                new HealthDescriptor(0, 0, Severity.WARNING_HIGH), "ID", "Name",
                "icon", StandardCharsets.UTF_8, TrendChartType.AGGREGATION_TOOLS);

        assertThat(action.getOwner()).isNull();

        Run<?, ?> run = mock(Run.class);
        action.onAttached(run);
        assertThat(action.getOwner()).isSameAs(run);
    }
}
