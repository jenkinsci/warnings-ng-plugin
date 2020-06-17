package io.jenkins.plugins.analysis.warnings.benchmark;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import jenkins.benchmark.jmh.BenchmarkFinder;


/**
 * Main class for Benchmark tests
 * Add "-Djmh.separateClasspathJAR=true" to your vm options in the Run Configuration
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
class BenchmarkRunner {

    @Test
    void runJmhBenchmarks() throws Exception {
        ChainedOptionsBuilder options = new OptionsBuilder()
                .mode(Mode.AverageTime)
                .forks(2)
                .threads(1)
                .resultFormat(ResultFormatType.JSON)
                .result("jmh-report.json");

        // Automatically detect benchmark classes annotated with @JmhBenchmark
        new BenchmarkFinder(getClass()).findBenchmarks(options);
        new Runner(options.build()).run();
    }
}
