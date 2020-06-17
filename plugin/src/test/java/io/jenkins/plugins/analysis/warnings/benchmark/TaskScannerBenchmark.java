package io.jenkins.plugins.analysis.warnings.benchmark;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Benchmark;

import com.google.errorprone.annotations.MustBeClosed;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import jenkins.benchmark.jmh.JmhBenchmark;
import jenkins.benchmark.jmh.JmhBenchmarkState;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScannerBuilder;

/**
 * Benchmark test for {@link TaskScanner}
 *
 * @author Oliver Scholz
 * @author Andreas Riepl
 */
@JmhBenchmark
public class TaskScannerBenchmark {
    private static final IssueBuilder ISSUE_BUILDER = new IssueBuilder();

    public static class JenkinsState extends JmhBenchmarkState {
        private WorkflowJob job;

        @Override
        public void setup() throws Exception {
            job = Jenkins.getInstanceOrNull().createProject(WorkflowJob.class, "benchmark_job");
            configureScanner(job, "checkstyle1");
        }

        private void configureScanner(final WorkflowJob job, final String fileName) {
            job.setDefinition(new CpsFlowDefinition("node {\n"
                    + "  stage ('Integration Test') {\n"
                    + "         def report = scanForIssues tool: checkStyle(pattern: '**/" + fileName + "*')\n"
                    + "         echo '[total=' + report.size() + ']' \n"
                    + "         echo '[id=' + report.getId() + ']' \n"
                    + "         def issues = report.getIssues()\n"
                    + "         issues.each { issue ->\n"
                    + "             echo issue.toString()\n"
                    + "             echo issue.getOrigin()\n"
                    + "             echo issue.getAuthorName()\n"
                    + "         }"
                    + "  }\n"
                    + "}", true));
        }

        public WorkflowJob getJob() {
            return job;
        }
    }

    @Benchmark
    public void scanBenchmark(final JenkinsState state) {
        Report tasks = new TaskScannerBuilder()
                .setHighTasks("WARNING")
                .setNormalTasks("TODO")
                .setLowTasks("@todo")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read("io/jenkins/plugins/analysis/warnings/tasks/tasks-words-test.txt"), ISSUE_BUILDER);
    }

    private Iterator<String> read(final String fileName) {
        return asStream(fileName).iterator();
    }

    @MustBeClosed
    protected Stream<String> asStream(String fileName) {
        return this.asStream(fileName, StandardCharsets.UTF_8);
    }

    @MustBeClosed
    protected Stream<String> asStream(String fileName, Charset charset) {
        try {
            return Files.lines(Paths.get(Thread.currentThread().getContextClassLoader().getResource(fileName).toURI()), charset);
        } catch (URISyntaxException | IOException var4) {
            throw new AssertionError("Can't read resource " + fileName, var4);
        }
    }
}


