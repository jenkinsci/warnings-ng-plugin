package io.jenkins.plugins.analysis.warnings.benchmark;

import java.io.IOException;

import org.openjdk.jmh.annotations.Benchmark;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import jenkins.benchmark.jmh.JmhBenchmark;
import jenkins.benchmark.jmh.JmhBenchmarkState;
import jenkins.model.Jenkins;

@JmhBenchmark
public class StepsBenchmarkITest {
    public static class JenkinsState extends JmhBenchmarkState {
        private WorkflowJob job;

        @Override
        public void setup() throws Exception {
            Jenkins jenkins = Jenkins.getInstanceOrNull();
            job = jenkins.createProject(WorkflowJob.class, "Benchmark Job");
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
    public void benchmark(final JenkinsState state) throws IOException {
        state.getJob().scheduleBuild2(0);
    }
}