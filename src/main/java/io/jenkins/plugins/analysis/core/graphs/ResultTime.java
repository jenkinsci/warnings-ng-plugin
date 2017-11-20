package io.jenkins.plugins.analysis.core.graphs;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

import com.google.common.annotations.VisibleForTesting;

import io.jenkins.plugins.analysis.core.quality.StaticAnalysisRun;

/**
 * Determines whether a build result is too old in order to be considered for a trend graph (based on the properties of
 * a {@link GraphConfiguration}).
 *
 * @author Ullrich Hafner
 */
public class ResultTime {
    private LocalDate today;

    /**
     * Creates a new instance of {@link ResultTime}. The current date from the system clock in the default time-zone is
     * used to initialize this instance.
     */
    public ResultTime() {
        this(LocalDate.now());
    }

    /**
     * Creates a new instance of {@link ResultTime}.
     *
     * @param now
     *         the date representing today
     */
    @VisibleForTesting
    ResultTime(final LocalDate now) {
        today = now;
    }

    /**
     * Returns whether the specified build result is too old in order to be considered for the trend graph.
     *
     * @param configuration
     *         the graph configuration
     * @param analysisRun
     *         the results of a analysis run
     *
     * @return {@code true} if the build is too old
     */
    public boolean areResultsTooOld(final GraphConfiguration configuration, final StaticAnalysisRun analysisRun) {
        return configuration.isDayCountDefined() && computeDayDelta(analysisRun) > configuration.getDayCount();
    }

    private int computeDayDelta(final StaticAnalysisRun analysisRun) {
        return Math.abs(Period.between(toLocalDate(analysisRun.getBuild().getTimeInMillis()), today).getDays());
    }

    private LocalDate toLocalDate(final long timeInMillis) {
        return Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
