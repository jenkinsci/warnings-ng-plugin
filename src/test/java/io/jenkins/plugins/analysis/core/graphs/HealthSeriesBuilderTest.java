package io.jenkins.plugins.analysis.core.graphs;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
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
    private static final int UNHEALTHY_THRESHOLD = 5;

    @SuppressFBWarnings("UPM")
    @SuppressWarnings("PMD.UnusedPrivateMethod")
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
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTHY_THRESHOLD))
                        .setRun(createRunWithSize(0))
                        .setExpectedSeries(0, 0, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("all healthy when below health threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTHY_THRESHOLD))
                        .setRun(createRunWithSize(1))
                        .setExpectedSeries(HEALTH_THRESHOLD - 1, 0, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("all healthy when at health threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTHY_THRESHOLD))
                        .setRun(createRunWithSize(HEALTH_THRESHOLD))
                        .setExpectedSeries(HEALTH_THRESHOLD, 0, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("one medium when above health, below unhealth threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTHY_THRESHOLD))
                        .setRun(createRunWithSize(HEALTH_THRESHOLD + 1))
                        .setExpectedSeries(2, 1, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("none unhealthy when below unhealth threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTHY_THRESHOLD))
                        .setRun(createRunWithSize(UNHEALTHY_THRESHOLD - 1))
                        .setExpectedSeries(2, 2, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("none unhealthy when at unhealth threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTHY_THRESHOLD))
                        .setRun(createRunWithSize(UNHEALTHY_THRESHOLD))
                        .setExpectedSeries(2, 3, 0)
                        .build(),

                new TestArgumentsBuilder()
                        .setTestName("one unhealthy when above unhealth threshold")
                        .setDescriptor(createEnabledDescriptor(HEALTH_THRESHOLD, UNHEALTHY_THRESHOLD))
                        .setRun(createRunWithSize(UNHEALTHY_THRESHOLD + 1))
                        .setExpectedSeries(2, 3, 1)
                        .build()
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void testComputeSeries(String name, HealthDescriptor descriptor, AnalysisResult run, Iterable<Integer> expectedSeries) {
        HealthSeriesBuilder sut = new HealthSeriesBuilder(descriptor);

        List<Integer> series = sut.computeSeries(run);

        assertThat(series)
                .containsExactlyElementsOf(expectedSeries);
    }

    private static AnalysisResult createRunWithSize(int totalSize) {
        AnalysisResult run = mock(AnalysisResult.class);
        when(run.getTotalSize()).thenReturn(totalSize);
        return run;
    }

    private static HealthDescriptor createDisabledDescriptor() {
        return createDescriptor(false);
    }

    private static HealthDescriptor createEnabledDescriptor(int healthThreshold, int unhealthThreshhold) {
        HealthDescriptor healthDescriptor = createDescriptor(true);
        when(healthDescriptor.getHealthy()).thenReturn(healthThreshold);
        when(healthDescriptor.getUnHealthy()).thenReturn(unhealthThreshhold);
        return healthDescriptor;
    }

    private static HealthDescriptor createDescriptor(boolean isEnabled) {
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
        private AnalysisResult run;
        private HealthDescriptor descriptor;
        private Iterable<Integer> series;


        /**
         * Set the tests name.
         *
         * @param testName
         *         name of the test.
         *
         * @return this
         */
        TestArgumentsBuilder setTestName(String testName) {
            name = testName;

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
        TestArgumentsBuilder setDescriptor(HealthDescriptor descriptor) {
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
        TestArgumentsBuilder setRun(AnalysisResult run) {
            this.run = run;

            return this;
        }

        /**
         * Set test expectation.
         *
         * @param expectedSeries
         *         expected as result.
         *
         * @return this
         */
        TestArgumentsBuilder setExpectedSeries(Integer... expectedSeries) {
            series = asList(expectedSeries);

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
