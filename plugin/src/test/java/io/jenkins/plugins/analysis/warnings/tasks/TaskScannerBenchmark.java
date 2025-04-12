package io.jenkins.plugins.analysis.warnings.tasks;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import edu.hm.hafner.util.ResourceTest;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;

/**
 * Benchmark test for the {@link TaskScanner}.
 *
 * @author Oliver Scholz
 * @author Andreas Riepl
 */
@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate")
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1, warmups = 3)
public class TaskScannerBenchmark extends ResourceTest {
    /**
     * BenchmarkRunner - runs all benchmark tests in this class.
     */
    @Test
    public void benchmark() throws RunnerException {
        var opt = new OptionsBuilder()
                .include(getClass().getName() + ".*")
                .addProfiler(StackProfiler.class)
                .build();

        new Runner(opt).run();
    }

    /**
     * Benchmarking for {@link TaskScanner}.
     *
     * @param state
     *         a {@link BenchmarkState} object containing the predefined objects for the test
     * @param blackhole
     *         a {@link Blackhole} to avoid dead code elimination
     */
    @Benchmark
    public void benchmarkTaskScanner(final BenchmarkState state, final Blackhole blackhole) {
        blackhole.consume(state.getScanner().scan(state.getSourceCode(), StandardCharsets.UTF_8));
    }

    /**
     * State for the benchmark containing all preconfigured and necessary objects.
     */
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private Path sourceCode;
        private TaskScanner scanner;

        /**
         * Initializes reports and history for the benchmarks.
         */
        @Setup(Level.Iteration)
        public void doSetup() {
            sourceCode = getResourceAsFile("tasks-words-test.txt");
            scanner = new TaskScannerBuilder()
                    .setHighTasks("WARNING")
                    .setNormalTasks("TODO")
                    .setLowTasks("@todo")
                    .setCaseMode(CaseMode.CASE_SENSITIVE)
                    .setMatcherMode(MatcherMode.STRING_MATCH)
                    .build();
        }

        public TaskScanner getScanner() {
            return scanner;
        }

        public Path getSourceCode() {
            return sourceCode;
        }

        private Path getResourceAsFile(final String fileName) {
            try {
                var resource = TaskScannerBenchmark.class.getResource(fileName);
                if (resource == null) {
                    throw new IllegalArgumentException("Could not find file " + fileName);
                }
                return Path.of(resource.toURI());
            }
            catch (URISyntaxException exception) {
                throw new AssertionError("Can't open file " + fileName, exception);
            }
        }
    }
}
