package io.jenkins.plugins.analysis.warnings.benchmark;

import org.junit.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import jenkins.benchmark.jmh.BenchmarkFinder;


public class BenchmarkRunner {
    @Test
    public void runJmhBenchmarks() throws Exception {
        ChainedOptionsBuilder options = new OptionsBuilder()
                .mode(Mode.AverageTime)
                .forks(2)
                .result("jmh-report.json");

        // Automatically detect benchmark classes annotated with @JmhBenchmark
        new BenchmarkFinder(getClass()).findBenchmarks(options);
        new Runner(options.build()).run();
    }
}