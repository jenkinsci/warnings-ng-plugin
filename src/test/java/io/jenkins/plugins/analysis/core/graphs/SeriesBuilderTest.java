package io.jenkins.plugins.analysis.core.graphs;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.util.Lists;
import org.jfree.data.category.CategoryDataset;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static io.jenkins.plugins.analysis.core.graphs.assertj.Assertions.*;
import io.jenkins.plugins.analysis.core.quality.AnalysisBuild;
import io.jenkins.plugins.analysis.core.quality.StaticAnalysisRun;
import static java.util.Arrays.*;
import static org.mockito.Mockito.*;

/**
 * Base class is tested with a test implementation @link{TestBuilder} to not
 * depend on any concrete implementations.
 */
class SeriesBuilderTest {


    private static final GraphConfiguration CONFIG_BUILD_COUNT_NONE = createWithBuildCount(0);
    private static final GraphConfiguration CONFIG_BUILD_COUNT_ONE = createWithBuildCount(1);
    private static final GraphConfiguration CONFIG_BUILD_COUNT_TWO = createWithBuildCount(2);
    private static final GraphConfiguration CONFIG_BUILD_DATE = createWithBuildDate();

    private static final DateTime DAY = new DateTime(0, DateTimeZone.UTC).plusDays(1);
    private static final DateTime PREVIOUS_DAY = DAY.minusDays(1);
    private static final DateTime SAME_DAY = DAY.plusHours(4);
    private static final DateTime NEXT_DAY = DAY.plusDays(1);

    private static final StaticAnalysisRun RUN_PREVIOUS_DAY = createRun(1, PREVIOUS_DAY);
    private static final StaticAnalysisRun RUN_DAY = createRun(2, DAY);
    private static final StaticAnalysisRun RUN_SAME_DAY = createRun(3, SAME_DAY);
    private static final StaticAnalysisRun RUN_NEXT_DAY = createRun(4, NEXT_DAY);

    private static final List<Integer> FIRST_SERIES = series(0, 1, 2);
    private static final List<Integer> SECOND_SERIES = series(3, 4, 5);
    private static final List<Integer> FORTH_SERIES = series(9, 10, 11);
    private static final List<Integer> AVERAGE_SECOND_AND_THIRD_SERIES = series(4, 5, 6);

    private static Iterable<Object> createDataSetData() {
        return asList(
                new TestArgumentsBuilder()
                        .setTestName("build count 0, 2 runs")
                        .setTime(resultTime(false))
                        .setConfig(CONFIG_BUILD_COUNT_NONE)
                        .setRuns(RUN_SAME_DAY, RUN_DAY, RUN_NEXT_DAY)
                        .setExpected(FIRST_SERIES)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build count 1, 2 runs")
                        .setTime(resultTime(false))
                        .setConfig(CONFIG_BUILD_COUNT_ONE)
                        .setRuns(RUN_SAME_DAY, RUN_DAY, RUN_NEXT_DAY)
                        .setExpected(FIRST_SERIES)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build count 2, 2 runs")
                        .setTime(resultTime(false))
                        .setConfig(CONFIG_BUILD_COUNT_TWO)
                        .setRuns(RUN_DAY, RUN_SAME_DAY)
                        .setExpected(FIRST_SERIES, SECOND_SERIES)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build count 2, 0 runs")
                        .setTime(resultTime(false))
                        .setConfig(CONFIG_BUILD_COUNT_TWO)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build count 2, 1 run")
                        .setTime(resultTime(false))
                        .setConfig(CONFIG_BUILD_COUNT_TWO)
                        .setRuns(RUN_DAY)
                        .setExpected(FIRST_SERIES)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build count 2, 2 runs")
                        .setTime(resultTime(false))
                        .setConfig(CONFIG_BUILD_COUNT_TWO)
                        .setRuns(RUN_DAY, RUN_NEXT_DAY)
                        .setExpected(FIRST_SERIES, SECOND_SERIES)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build date, never too old, 0 runs")
                        .setTime(resultTime(false))
                        .setConfig(CONFIG_BUILD_DATE)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build date, never too old, 1 runs")
                        .setTime(resultTime(false))
                        .setConfig(CONFIG_BUILD_DATE)
                        .setRuns(RUN_DAY)
                        .setExpected(FIRST_SERIES)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build date, never too old, 2 runs")
                        .setTime(resultTime(false))
                        .setConfig(CONFIG_BUILD_DATE)
                        .setRuns(RUN_PREVIOUS_DAY, RUN_DAY)
                        .setExpected(FIRST_SERIES, SECOND_SERIES)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build date, always too, 2 runs")
                        .setTime(resultTime(true))
                        .setConfig(CONFIG_BUILD_DATE)
                        .setRuns(RUN_PREVIOUS_DAY, RUN_DAY)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build date, first too old, 2 runs")
                        .setTime(resultTime(false, true))
                        .setConfig(CONFIG_BUILD_DATE)
                        .setRuns(RUN_PREVIOUS_DAY, RUN_DAY)
                        .setExpected(FIRST_SERIES)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("build date, never too old, average same days")
                        .setTime(resultTime(false))
                        .setRuns(RUN_PREVIOUS_DAY, RUN_DAY, RUN_SAME_DAY, RUN_NEXT_DAY)
                        .setConfig(CONFIG_BUILD_DATE)
                        .setExpected(FIRST_SERIES, AVERAGE_SECOND_AND_THIRD_SERIES, FORTH_SERIES)
                        .build()
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("createDataSetData")
    final void testCreateDataSet(final String testName,
            final ResultTime time, final GraphConfiguration config,
            final List<StaticAnalysisRun> runs, final List<List<Integer>> expected) {

        SeriesBuilder sut = new TestBuilder(time);

        CategoryDataset result = sut.createDataSet(config, runs);

        assertThat(result)
                .containsExactly(expected);
    }

    private static GraphConfiguration createWithBuildCount(final int count) {
        GraphConfiguration config = createConfig();
        when(config.isBuildCountDefined()).thenReturn(true);
        when(config.getBuildCount()).thenReturn(count);
        return config;
    }

    private static GraphConfiguration createWithBuildDate() {
        GraphConfiguration config = createConfig();
        when(config.useBuildDateAsDomain()).thenReturn(true);
        return config;
    }

    private static GraphConfiguration createConfig() {
        return mock(GraphConfiguration.class);
    }


    private static StaticAnalysisRun createRun(final int buildNo, final DateTime buildTime) {
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);

        AnalysisBuild build = mock(AnalysisBuild.class);
        when(build.getTimeInMillis()).thenReturn(buildTime.getMillis());
        when(build.getNumber()).thenReturn(buildNo);
        when(build.getDisplayName()).thenReturn(String.format("#%s", buildNo));

        when(run.getBuild()).thenReturn(build);

        return run;
    }

    private static ResultTime resultTime(final Boolean value, final Boolean... continuations) {
        ResultTime time = mock(ResultTime.class);
        when(time.areResultsTooOld(any(GraphConfiguration.class), any(StaticAnalysisRun.class)))
                .thenReturn(value, continuations);
        return time;
    }

    private static List<Integer> series(Integer... values) {
        return asList(values);
    }


    /**
     * Dumb test implementation returning the numbers from 1 to n as series, three at a time.
     */
    private static class TestBuilder extends SeriesBuilder {

        private int count;

        TestBuilder(final ResultTime resultTime) {
            super(resultTime);
        }

        @Override
        protected List<Integer> computeSeries(final StaticAnalysisRun current) {
            return asList(count++, count++, count++);

        }
    }

    /**
     * Helps building arguments to parameterized test.
     */
    private static class TestArgumentsBuilder {

        private String testName;
        private GraphConfiguration config;
        private List<StaticAnalysisRun> runs;
        private List<List<Integer>> series;
        private ResultTime time;


        /**
         * Set the tests configuration.
         *
         * @param config
         *         to use in test
         *
         * @return this
         */
        TestArgumentsBuilder setConfig(final GraphConfiguration config) {
            this.config = config;

            return this;
        }


        /**
         * Set the name displayed as test name.
         *
         * @param name
         *         of the test
         *
         * @return this
         */
        TestArgumentsBuilder setTestName(final String name) {
            testName = name;

            return this;
        }

        /**
         * Set the result time used in test.
         *
         * @param time
         *         used in test.
         *
         * @return this
         */
        TestArgumentsBuilder setTime(final ResultTime time) {
            this.time = time;

            return this;
        }

        /**
         * Set the analysis runs used in test.
         *
         * @param runs
         *         used in test, defaults to empty list
         *
         * @return this
         */
        TestArgumentsBuilder setRuns(final StaticAnalysisRun... runs) {
            this.runs = Arrays.asList(runs);

            return this;
        }


        /**
         * Set the tests expectations.
         *
         * @param series
         *         to use in test, defaults to empty list
         *
         * @return this
         */
        @SafeVarargs
        public final TestArgumentsBuilder setExpected(final List<Integer>... series) {
            this.series = Arrays.asList(series);

            return this;
        }

        /**
         * Builds the tests argument.
         *
         * @return test arg
         */
        public Object build() {
            return Arguments.of(
                    testName,
                    time,
                    config,
                    runs != null ? runs : Lists.emptyList(),
                    series != null ? series : Lists.emptyList()
            );
        }
    }
}
