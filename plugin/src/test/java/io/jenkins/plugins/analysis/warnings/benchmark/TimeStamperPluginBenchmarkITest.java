package io.jenkins.plugins.analysis.warnings.benchmark;

import org.openjdk.jmh.annotations.Benchmark;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import jenkins.benchmark.jmh.JmhBenchmark;
import jenkins.benchmark.jmh.JmhBenchmarkState;

@JmhBenchmark
public class TimeStamperPluginBenchmarkITest {
    public static class JenkinsState extends JmhBenchmarkState {
        private WorkflowJob job;

        @Override
        public void setup() throws Exception {
            job = getJenkins().createProject(WorkflowJob.class, "Benchmark Job");
            configureScanner();
        }

        private void configureScanner() {
            job.setDefinition(new CpsFlowDefinition("node {\n"
                    + "    timestamps {\n"
                    + "        echo 'test.c:1:2: error: This is an error.'\n"
                    + "        recordIssues tools: [clang(id: 'clang', name: 'clang')]\n"
                    + "    }\n"
                    + "}", true));
        }

        public WorkflowJob getJob() {
            return job;
        }
    }


    @Benchmark
    public void benchmarkCoverage(final JenkinsState state) {
        state.getJob().scheduleBuild2(0);
    }
}
