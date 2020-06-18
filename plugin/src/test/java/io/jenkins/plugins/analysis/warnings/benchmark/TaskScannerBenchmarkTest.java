package io.jenkins.plugins.analysis.warnings.benchmark;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.ResourceTest;

import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScannerBuilder;

import static edu.hm.hafner.analysis.assertions.Assertions.*;

/**
 * Benchmark test for {@link TaskScanner}
 *
 * @author Oliver Scholz
 * @author Andreas Riepl
 */
public class TaskScannerBenchmarkTest extends ResourceTest {
    private static final IssueBuilder ISSUE_BUILDER = new IssueBuilder();
    private static final String RESOURCE_FOLDER = "io/jenkins/plugins/analysis/warnings/tasks/";

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1, warmups = 1)
    public void scanBenchmark() {
        assertThat(read(RESOURCE_FOLDER + "tasks-words-test.txt")).isNotNull();
        Report tasks = new TaskScannerBuilder()
                .setHighTasks("WARNING")
                .setNormalTasks("TODO")
                .setLowTasks("@todo")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read(RESOURCE_FOLDER + "tasks-words-test.txt"), ISSUE_BUILDER);

        assertThat(tasks).hasSize(12);
    }

    private Iterator<String> read(final String fileName) {
        try {
            return Files.lines(Paths.get(Thread.currentThread().getContextClassLoader().getResource(fileName).toURI()),
                StandardCharsets.UTF_8).iterator();
        } catch (IOException | URISyntaxException e) {
            fail(e.toString());
        }
        return null;
    }
}


