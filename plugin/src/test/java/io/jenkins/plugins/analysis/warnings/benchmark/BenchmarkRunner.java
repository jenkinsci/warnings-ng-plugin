package io.jenkins.plugins.analysis.warnings.benchmark;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Simple Runner to execute Benchmarks and write result to file {@code jmh-report.json}
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
public class BenchmarkRunner {

    @Test
    public void benchmark() throws Exception {
        Options opt = new OptionsBuilder()
                .include(TaskScannerBenchmarkTest.class.getSimpleName())
                .addProfiler(StackProfiler.class)
                .result("jmh-report.json")
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }

}
