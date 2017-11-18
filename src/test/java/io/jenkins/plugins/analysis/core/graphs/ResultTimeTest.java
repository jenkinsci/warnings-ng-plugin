package io.jenkins.plugins.analysis.core.graphs;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.quality.AnalysisBuild;
import io.jenkins.plugins.analysis.core.quality.StaticAnalysisRun;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ResultTime}.
 *
 * @author Ullrich Hafner
 */
class ResultTimeTest {

    private static final int DAY_COUNT = 2;

    /**
     * Verifies that the day count property is not evaluated if {@link GraphConfiguration#isDayCountDefined()} is
     * disabled.
     */
    @Test
    void shouldNotEvaluateDayCountIfOptionIsDeactivated() {
        LocalDate today = LocalDate.now();
        ResultTime time = new ResultTime(today);

        GraphConfiguration configuration = createConfiguration(false);

        StaticAnalysisRun run = createRunAt(today.minusYears(20));

        assertThat(time.areResultsTooOld(configuration, run)).as("Result date marked as too old").isFalse();
        verify(configuration, never()).getDayCount();
    }

    private StaticAnalysisRun createRunAt(final LocalDate now) {
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        AnalysisBuild build = mock(AnalysisBuild.class);
        when(build.getTimeInMillis()).thenReturn(now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        when(run.getBuild()).thenReturn(build);
        return run;
    }

    private GraphConfiguration createConfiguration(final boolean isDayCountDefined) {
        GraphConfiguration configuration = mock(GraphConfiguration.class);
        when(configuration.isDayCountDefined()).thenReturn(isDayCountDefined);
        return configuration;
    }

    /**
     * Verifies that the day count property is correctly evaluated if {@link GraphConfiguration#isDayCountDefined()} is
     * enabled.
     */
    @Test
    void shouldEvaluateDayCountIfOptionIsEnabled() {
        LocalDate today = LocalDate.now();
        ResultTime time = new ResultTime(today);

        assertThatRunIsWithinDayCount(today, time);
        assertThatRunIsWithinDayCount(today.plusDays(1), time);
        assertThatRunIsWithinDayCount(today.plusDays(2), time);

        assertThatRunIsOutsideOfDayCount(today.plusDays(3), time);
        assertThatRunIsOutsideOfDayCount(today.plusDays(4), time);
    }

    private void assertThatRunIsOutsideOfDayCount(final LocalDate runDate, final ResultTime time) {
        GraphConfiguration configuration = createGraphConfigurationWithDayCount();

        StaticAnalysisRun run = createRunAt(runDate);

        assertThat(time.areResultsTooOld(configuration, run)).as("Result date marked as ok").isEqualTo(true);
    }

    private void assertThatRunIsWithinDayCount(final LocalDate runDate, final ResultTime time) {
        GraphConfiguration configuration = createGraphConfigurationWithDayCount();

        StaticAnalysisRun run = createRunAt(runDate);

        assertThat(time.areResultsTooOld(configuration, run)).as("Result date marked as too old").isEqualTo(false);
    }

    private GraphConfiguration createGraphConfigurationWithDayCount() {
        GraphConfiguration configuration = createConfiguration(true);
        when(configuration.getDayCount()).thenReturn(DAY_COUNT);
        return configuration;
    }
}