package io.jenkins.plugins.analysis.warnings.benchmark;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import edu.hm.hafner.analysis.FileReaderFactory;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;

import hudson.model.Run;
import jenkins.benchmark.jmh.JmhBenchmark;

import io.jenkins.plugins.analysis.core.model.History;

import static org.mockito.Mockito.*;

/**
 * Performance benchmarks for a {@link CheckStyleParser}.
 *
 * @author Kevin Richter
 * @author Simon Schönwiese
 */
@JmhBenchmark
public class CheckstyleBenchmarkTest {
    /**
     * Benchmarking for parsing an xml file with a {@link CheckStyleParser}.
     *
     * @param state
     *         a {@link BenchmarkState} object containing the FileReaderFactory object
     * @param blackhole
     *         a {@link Blackhole} to avoid dead code elminination
     */
    @Benchmark
    public void benchmarkCheckStyleParser(final BenchmarkState state, final Blackhole blackhole) {
        Report report = new CheckStyleParser().parse(state.getFileReaderFactory());
        blackhole.consume(report);
    }

    /**
     * State for the benchmark containing all preconfigured and necessary objects.
     */
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private History history;
        private static final String RESOURCE_FOLDER = "io/jenkins/plugins/analysis/warnings/recorder/";
        private ReaderFactory fileReaderFactory;

        public History getHistory() {
            return history;
        }

        private ReaderFactory createFileReaderFactory(final String fileName) throws URISyntaxException {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            return new FileReaderFactory(
                    Paths.get(Objects.requireNonNull(contextClassLoader.getResource(fileName)).toURI()));
        }

        public ReaderFactory getFileReaderFactory() {
            return fileReaderFactory;
        }

        /**
         * Initializes history and FileReaderFactory object for the benchmarks.
         */
        @Setup(Level.Iteration)
        public void doSetup() throws URISyntaxException {
            Run<?, ?> run = mock(Run.class);
            when(run.getExternalizableId()).thenReturn("refBuildId");

            history = mock(History.class);
            when(history.getBuild()).thenReturn(Optional.of(run));
            fileReaderFactory = createFileReaderFactory(RESOURCE_FOLDER + "checkstyle-quality-gate.xml");
        }
    }
}
