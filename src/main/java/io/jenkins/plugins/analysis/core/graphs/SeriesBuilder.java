package io.jenkins.plugins.analysis.core.graphs;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.category.CategoryDataset;
import org.joda.time.LocalDate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.analysis.core.history.ResultHistory;
import io.jenkins.plugins.analysis.core.quality.AnalysisBuild;
import io.jenkins.plugins.analysis.core.quality.StaticAnalysisRun;

import hudson.util.DataSetBuilder;

/**
 * Provides the base algorithms to create a data set for a static analysis graph. The actual series for each
 * result needs to be implemented by sub classes in method {@link #computeSeries}.
 *
 * @author Ullrich Hafner
 */
public abstract class SeriesBuilder {
    private static final int A_DAY_IN_MSEC = 24 * 3600 * 1000;

    public CategoryDataset createDataSet(final GraphConfiguration configuration,
            final Iterable<? extends StaticAnalysisRun> results) {
        CategoryDataset dataSet;
        if (configuration.useBuildDateAsDomain()) {
            Map<LocalDate, List<Integer>> averagePerDay = averageByDate(createSeriesPerBuild(configuration, results));
            dataSet = createDataSetPerDay(averagePerDay);
        }
        else {
            dataSet = createDataSetPerBuildNumber(createSeriesPerBuild(configuration, results));
        }
        return dataSet;
    }

    @SuppressWarnings("rawtypes")
    private Map<AnalysisBuild, List<Integer>> createSeriesPerBuild(
            final GraphConfiguration configuration, final Iterable<? extends StaticAnalysisRun> results) {
        int buildCount = 0;
        Map<AnalysisBuild, List<Integer>> valuesPerBuildNumber = Maps.newHashMap();
        for (StaticAnalysisRun current : results) {
            if (isBuildTooOld(configuration, current)) {
                break;
            }
            valuesPerBuildNumber.put(current.getBuild(), computeSeries(current));

            if (configuration.isBuildCountDefined()) {
                buildCount++;
                if (buildCount >= configuration.getBuildCount()) {
                    break;
                }
            }
        }
        return valuesPerBuildNumber;
    }

    /**
     * Returns the series to plot for the specified build result.
     *
     * @param current the current build result
     * @return the series to plot
     */
    protected abstract List<Integer> computeSeries(StaticAnalysisRun current);

    /**
     * Creates a data set that contains a series per build number.
     *
     * @param valuesPerBuild
     *            the collected values
     * @return a data set
     */
    private CategoryDataset createDataSetPerBuildNumber(final Map<AnalysisBuild, List<Integer>> valuesPerBuild) {
        DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<>();
        List<AnalysisBuild> builds = Lists.newArrayList(valuesPerBuild.keySet());
        Collections.sort(builds);
        for (AnalysisBuild build : builds) {
            List<Integer> series = valuesPerBuild.get(build);
            int level = 0;
            for (Integer integer : series) {
                builder.add(integer, getRowId(level), new NumberOnlyBuildLabel(build));
                level++;
            }
        }
        return builder.build();
    }

    /**
     * Creates a data set that contains one series of values per day.
     *
     * @param averagePerDay
     *            the collected values averaged by day
     * @return a data set
     */
    @SuppressWarnings("unchecked")
    private CategoryDataset createDataSetPerDay(final Map<LocalDate, List<Integer>> averagePerDay) {
        List<LocalDate> buildDates = Lists.newArrayList(averagePerDay.keySet());
        Collections.sort(buildDates);

        DataSetBuilder<String, LocalDateLabel> builder = new DataSetBuilder<>();
        for (LocalDate date : buildDates) {
            int level = 0;
            for (Integer average : averagePerDay.get(date)) {
                builder.add(average, getRowId(level), new LocalDateLabel(date));
                level++;
            }
        }
        return builder.build();
    }

    /**
     * Aggregates multiple series per day to one single series per day by
     * computing the average value.
     *
     * @param multiSeriesPerDate
     *            the values given as multiple series per day
     * @return the values as one series per day (average)
     */
    private Map<LocalDate, List<Integer>> createSeriesPerDay(
            final Multimap<LocalDate, List<Integer>> multiSeriesPerDate) {
        Map<LocalDate, List<Integer>> seriesPerDate = Maps.newHashMap();

        for (LocalDate date : multiSeriesPerDate.keySet()) {
            Iterator<List<Integer>> perDayIterator = multiSeriesPerDate.get(date).iterator();
            List<Integer> total = perDayIterator.next();
            int seriesCount = 1;
            while (perDayIterator.hasNext()) {
                List<Integer> additional = perDayIterator.next();
                seriesCount++;

                List<Integer> sum = Lists.newArrayList();
                for (int i = 0; i < total.size(); i++) {
                    sum.add(total.get(i) + additional.get(i));
                }

                total = sum;
            }
            List<Integer> series = Lists.newArrayList();
            for (Integer totalValue : total) {
                series.add(totalValue / seriesCount);
            }
            seriesPerDate.put(date, series);
        }
        return seriesPerDate;
    }

    /**
     * Aggregates the series per build to a series per date.
     *
     * @param valuesPerBuild
     *            the series per build
     * @return the series per date
     */
    private Map<LocalDate, List<Integer>> averageByDate(
            final Map<AnalysisBuild, List<Integer>> valuesPerBuild) {
        return createSeriesPerDay(createMultiSeriesPerDay(valuesPerBuild));
    }

    /**
     * Creates a mapping of values per day.
     *
     * @param valuesPerBuild
     *            the values per build
     * @return the multi map with the values per day
     */
    @SuppressWarnings("rawtypes")
    @SuppressFBWarnings("WMI")
    private Multimap<LocalDate, List<Integer>> createMultiSeriesPerDay(
            final Map<AnalysisBuild, List<Integer>> valuesPerBuild) {
        Multimap<LocalDate, List<Integer>> valuesPerDate = HashMultimap.create();
        for (AnalysisBuild build : valuesPerBuild.keySet()) {
            valuesPerDate.put(new LocalDate(build.getTimeInMillis()), valuesPerBuild.get(build));
        }
        return valuesPerDate;
    }

    /**
     * Returns the row identifier for the specified level. This identifier will
     * be used in the legend.
     *
     * @param level
     *            the level
     * @return the row identifier
     */
    protected String getRowId(final int level) {
        return String.valueOf(level);
    }

    public CategoryDataset createAggregation(final GraphConfiguration configuration, final Collection<ResultHistory> resultActions) {
        Set<LocalDate> availableDates = Sets.newHashSet();
        Map<ResultHistory, Map<LocalDate, List<Integer>>> averagesPerJob = Maps.newHashMap();
        for (ResultHistory resultAction : resultActions) {
            Map<LocalDate, List<Integer>> averageByDate = averageByDate(
                    createSeriesPerBuild(configuration, resultAction));
            averagesPerJob.put(resultAction, averageByDate);
            availableDates.addAll(averageByDate.keySet());
        }
        return createDataSetPerDay(
                createTotalsForAllAvailableDates(resultActions, availableDates, averagesPerJob));
    }

    /**
     * Creates the totals for all available dates. If a job has no results for a
     * given day then the previous value is used.
     *
     * @param jobs
     *            the result actions belonging to the jobs
     * @param availableDates
     *            the available dates in all jobs
     * @param averagesPerJob
     *            the averages per day, mapped by job
     * @return the aggregated values
     */
    private Map<LocalDate, List<Integer>> createTotalsForAllAvailableDates(
            final Collection<ResultHistory> jobs,
            final Set<LocalDate> availableDates,
            final Map<ResultHistory, Map<LocalDate, List<Integer>>> averagesPerJob) {
        List<LocalDate> sortedDates = Lists.newArrayList(availableDates);
        Collections.sort(sortedDates);

        Map<LocalDate, List<Integer>> totals = Maps.newHashMap();
        for (ResultHistory jobResult : jobs) {
            Map<LocalDate, List<Integer>> availableResults = averagesPerJob.get(jobResult);
            List<Integer> lastResult = Collections.emptyList();
            for (LocalDate buildDate : sortedDates) {
                if (availableResults.containsKey(buildDate)) {
                    List<Integer> additionalResult = availableResults.get(buildDate);
                    addValues(buildDate, totals, additionalResult);
                    lastResult = additionalResult;
                }
                else if (!lastResult.isEmpty()) {
                    addValues(buildDate, totals, lastResult);
                }
            }
        }
        return totals;
    }

    private void addValues(final LocalDate buildDate, final Map<LocalDate, List<Integer>> totals,
                           final List<Integer> additionalResult) {
        if (totals.containsKey(buildDate)) {
            List<Integer> existingResult = totals.get(buildDate);
            List<Integer> sum = Lists.newArrayList();
            for (int i = 0; i < existingResult.size(); i++) {
                sum.add(existingResult.get(i) + additionalResult.get(i));
            }
            totals.put(buildDate, sum);
        }
        else {
            totals.put(buildDate, additionalResult);
        }
    }

    /**
     * Returns whether the specified build result is too old in order to be
     * considered for the trend graph.
     *
     * @param configuration
     *            the graph configuration
     * @param current
     *            the current build
     * @return <code>true</code> if the build is too old
     */
    public static boolean isBuildTooOld(final GraphConfiguration configuration, final StaticAnalysisRun current) {
        return areResultsTooOld(configuration, current);
    }
    /**
     * Computes the delta between two dates in days.
     *
     * @param first
     *            the first date
     * @param second
     *            the second date (given by the build result)
     * @return the delta between two dates in days
     */
    public static long computeDayDelta(final Calendar first, final StaticAnalysisRun second) {
        return computeDayDelta(first, second.getBuild().getTimeInMillis());
    }

    /**
     * Returns whether the specified build result is too old in order to be
     * considered for the trend graph.
     *
     * @param configuration
     *            the graph configuration
     * @param current
     *            the current build
     * @return <code>true</code> if the build is too old
     */
    public static boolean areResultsTooOld(final GraphConfiguration configuration, final StaticAnalysisRun current) {
        Calendar today = new GregorianCalendar();

        return configuration.isDayCountDefined()
                && computeDayDelta(today, current) >= configuration.getDayCount();
    }

    /**
     * Computes the delta between two dates in days.
     *
     * @param first
     *            the first date
     * @param second
     *            the second date
     * @return the delta between two dates in days
     */
    public static long computeDayDelta(final Calendar first, final long second) {
        return Math.abs((first.getTimeInMillis() - second) / A_DAY_IN_MSEC);
    }
}
