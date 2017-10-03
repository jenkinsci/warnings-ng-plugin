package io.jenkins.plugins.analysis.core.graphs;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jfree.data.category.CategoryDataset;
import org.joda.time.LocalDate;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.analysis.core.HistoryProvider;
import io.jenkins.plugins.analysis.core.steps.BuildResult;

import hudson.model.AbstractBuild;
import hudson.model.Run;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public abstract class SeriesBuilder {
    private static final int A_DAY_IN_MSEC = 24 * 3600 * 1000;

    public CategoryDataset createDataSet(final GraphConfiguration configuration, final Iterable<BuildResult> history) {
        CategoryDataset dataSet;
        if (configuration.useBuildDateAsDomain()) {
            Map<LocalDate, List<Integer>> averagePerDay = averageByDate(createSeriesPerBuild(configuration, history));
            dataSet = createDatasetPerDay(averagePerDay);
        }
        else {
            dataSet = createDatasetPerBuildNumber(createSeriesPerBuild(configuration, history));
        }
        return dataSet;
    }

    /**
     * Creates a series of values per build.
     *
     * @param configuration
     *            the configuration
     * @param history
     *            the build history
     * @return a series of values per build
     */
    @SuppressWarnings("rawtypes")
    private Map<Run, List<Integer>> createSeriesPerBuild(
            final GraphConfiguration configuration, final Iterable<BuildResult> history) {
        int buildCount = 0;
        Map<Run, List<Integer>> valuesPerBuild = Maps.newHashMap();
        String parameterName = configuration.getParameterName();
        String parameterValue = configuration.getParameterValue();

        for (BuildResult current : history) {
            if (isBuildTooOld(configuration, current)) {
                break;
            }
            if (passesFilteringByParameter(current.getOwner(), parameterName, parameterValue)) {
                valuesPerBuild.put(current.getOwner(), computeSeries(current));
            }

            if (configuration.isBuildCountDefined()) {
                buildCount++;
                if (buildCount >= configuration.getBuildCount()) {
                    break;
                }
            }
        }
        return valuesPerBuild;
    }

    protected boolean passesFilteringByParameter(final Run<?, ?> build, final String parameterName, final String parameterValue) {
        if (StringUtils.isBlank(parameterName)) {
            return true;
        }

        Map<String, String> variables;
        if (build instanceof AbstractBuild) {
            variables = ((AbstractBuild<?, ?>) build).getBuildVariables();
        }
        else {
            // There is no comparable method for Run. This means that this feature (using parameters for
            // result graph) will not be available for other than AbstractBuild extending classes (basically
            // all except Workflow builds).
            // So workflow jobs will be never filtered, just show them all.
            return true;
        }
        if (variables == null) {
            return false;
        }

        return Objects.equal(variables.get(parameterName), parameterValue);
    }

    /**
     * Returns the series to plot for the specified build result.
     *
     * @param current the current build result
     * @return the series to plot
     */
    protected abstract List<Integer> computeSeries(BuildResult current);

    /**
     * Creates a data set that contains a series per build number.
     *
     * @param valuesPerBuild
     *            the collected values
     * @return a data set
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private CategoryDataset createDatasetPerBuildNumber(final Map<Run, List<Integer>> valuesPerBuild) {
        hudson.util.DataSetBuilder<String, NumberOnlyBuildLabel> builder = new hudson.util.DataSetBuilder<String, NumberOnlyBuildLabel>();
        List<Run> builds = Lists.newArrayList(valuesPerBuild.keySet());
        Collections.sort(builds);
        for (Run<?, ?> build : builds) {
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
    private CategoryDataset createDatasetPerDay(final Map<LocalDate, List<Integer>> averagePerDay) {
        List<LocalDate> buildDates = Lists.newArrayList(averagePerDay.keySet());
        Collections.sort(buildDates);

        hudson.util.DataSetBuilder<String, LocalDateLabel> builder = new hudson.util.DataSetBuilder<String, LocalDateLabel>();
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
    @SuppressWarnings("rawtypes")
    private Map<LocalDate, List<Integer>> averageByDate(
            final Map<Run, List<Integer>> valuesPerBuild) {
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
            final Map<Run, List<Integer>> valuesPerBuild) {
        Multimap<LocalDate, List<Integer>> valuesPerDate = HashMultimap.create();
        for (Run<?, ?> build : valuesPerBuild.keySet()) {
            valuesPerDate.put(new LocalDate(build.getTimestamp()), valuesPerBuild.get(build));
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

    public CategoryDataset createAggregation(final GraphConfiguration configuration, final Collection<HistoryProvider> resultActions) {
        Set<LocalDate> availableDates = Sets.newHashSet();
        Map<HistoryProvider, Map<LocalDate, List<Integer>>> averagesPerJob = Maps.newHashMap();
        for (HistoryProvider resultAction : resultActions) {
            Map<LocalDate, List<Integer>> averageByDate = averageByDate(
                    createSeriesPerBuild(configuration, resultAction));
            averagesPerJob.put(resultAction, averageByDate);
            availableDates.addAll(averageByDate.keySet());
        }
        return createDatasetPerDay(
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
    @SuppressWarnings("unchecked")
    private Map<LocalDate, List<Integer>> createTotalsForAllAvailableDates(
            final Collection<HistoryProvider> jobs,
            final Set<LocalDate> availableDates,
            final Map<HistoryProvider, Map<LocalDate, List<Integer>>> averagesPerJob) {
        List<LocalDate> sortedDates = Lists.newArrayList(availableDates);
        Collections.sort(sortedDates);

        Map<LocalDate, List<Integer>> totals = Maps.newHashMap();
        for (HistoryProvider jobResult : jobs) {
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
    public static boolean isBuildTooOld(final GraphConfiguration configuration, final BuildResult current) {
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
    public static long computeDayDelta(final Calendar first, final BuildResult second) {
        return computeDayDelta(first, second.getOwner().getTimestamp());
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
    public static boolean areResultsTooOld(final GraphConfiguration configuration, final BuildResult current) {
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
    public static long computeDayDelta(final Calendar first, final Calendar second) {
        return Math.abs((first.getTimeInMillis() - second.getTimeInMillis()) / A_DAY_IN_MSEC);
    }
}
