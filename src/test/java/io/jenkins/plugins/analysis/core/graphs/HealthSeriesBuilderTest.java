package io.jenkins.plugins.analysis.core.graphs;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.StaticAnalysisRun;
import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests class {@link HealthSeriesBuilder}.
 *
 * @author Florian Pirchmoser
 */
class HealthSeriesBuilderTest {

    private static final int HEALTH_THRESHOLD = 2;
    private static final int UNHEALTH_THRESHOLD = 5;

    private static Iterable<Object> testData() {
        return asList(
                new TestArgumentsBuilder()
                        .setTestName("all healthy when descriptor disabled")
                        .setDescriptor(createDisabledDescriptor())
                        .setRun(createRunWithSize(4))
                        .setExpectedSeries(4)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("no issues")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTH_THRESHOLD))
                        .setRun(createRunWithSize(0))
                        .setExpectedSeries(0, 0, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("all healthy when below health threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTH_THRESHOLD))
                        .setRun(createRunWithSize(1))
                        .setExpectedSeries(HEALTH_THRESHOLD - 1, 0, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("all healthy when at health threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTH_THRESHOLD))
                        .setRun(createRunWithSize(HEALTH_THRESHOLD))
                        .setExpectedSeries(HEALTH_THRESHOLD, 0, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("one medium when above health, below unhealth threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTH_THRESHOLD))
                        .setRun(createRunWithSize(HEALTH_THRESHOLD + 1))
                        .setExpectedSeries(2, 1, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("none unhealthy when below unhealth threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTH_THRESHOLD))
                        .setRun(createRunWithSize(UNHEALTH_THRESHOLD - 1))
                        .setExpectedSeries(2, 2, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("none unhealthy when at unhealth threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTH_THRESHOLD))
                        .setRun(createRunWithSize(UNHEALTH_THRESHOLD))
                        .setExpectedSeries(2, 3, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("one unhealthy when above unhealth threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTH_THRESHOLD))
                        .setRun(createRunWithSize(UNHEALTH_THRESHOLD + 1))
                        .setExpectedSeries(2, 3, 1)
                        .build()
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void testComputeSeries(final String name, final HealthDescriptor descriptor, final StaticAnalysisRun run, final Iterable<Integer> expectedSeries) {
        HealthSeriesBuilder sut = new HealthSeriesBuilder(descriptor);

        List<Integer> series = sut.computeSeries(run);

        assertThat(series)
                .containsExactlyElementsOf(expectedSeries);
    }

    private static StaticAnalysisRun createRunWithSize(final int totalSize) {
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        when(run.getTotalSize()).thenReturn(totalSize);
        return run;
    }

    private static HealthDescriptor createDisabledDescriptor() {
        return createDescriptor(false);
    }

    private static HealthDescriptor createEnabledDescriptor(final int healthThreshold, final int unhealthThreshhold) {
        HealthDescriptor healthDescriptor = createDescriptor(true);
        when(healthDescriptor.getHealthy()).thenReturn(healthThreshold);
        when(healthDescriptor.getUnHealthy()).thenReturn(unhealthThreshhold);
        return healthDescriptor;
    }

    private static HealthDescriptor createDescriptor(final boolean isEnabled) {
        HealthDescriptor descriptor = mock(HealthDescriptor.class);
        if (isEnabled) {
            when(descriptor.isEnabled()).thenReturn(true);
        }
        return descriptor;
    }

    /**
     * Builds arg for the parameterized test.
     */
    private static class TestArgumentsBuilder {
        private String name;
        private StaticAnalysisRun run;
        private HealthDescriptor descriptor;
        private Iterable<Integer> series;


        /**
         * Set the tests name.
         *
         * @param name
         *         name of the test.
         *
         * @return this
         */
        TestArgumentsBuilder setTestName(final String name) {
            this.name = name;

            return this;
        }

        /**
         * Set the health descriptor used in test.
         *
         * @param descriptor
         *         used in test
         *
         * @return this
         */
        TestArgumentsBuilder setDescriptor(final HealthDescriptor descriptor) {
            this.descriptor = descriptor;

            return this;
        }

        /**
         * Set the analysis run used in test.
         *
         * @param run
         *         used in test
         *
         * @return this
         */
        TestArgumentsBuilder setRun(final StaticAnalysisRun run) {
            this.run = run;

            return this;
        }

        /**
         * Set test expectation.
         *
         * @param series
         *         expected as result.
         *
         * @return this
         */
        TestArgumentsBuilder setExpectedSeries(final Integer... series) {
            this.series = Arrays.asList(series);

            return this;
        }

        /**
         * Build the tests argument.
         *
         * @return test arg
         */
        public Object build() {
            return Arguments.of(
                    name,
                    descriptor,
                    run,
                    series
            );
        }
    }
}
