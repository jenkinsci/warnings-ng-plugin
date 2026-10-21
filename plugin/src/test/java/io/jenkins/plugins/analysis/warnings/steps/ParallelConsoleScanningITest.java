package io.jenkins.plugins.analysis.warnings.steps;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import java.util.List;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

/**
 * Integration and regression tests for JENKINS-55258 / GitHub issue #2707:
 * Support scanning of console log with parallel steps.
 */
class ParallelConsoleScanningITest extends IntegrationTestWithJenkinsPerSuite {
    /**
     * Verifies that scanForIssues properly isolates console log scanning to the enclosing parallel branch.
     * Diagnostics from one branch are not visible to another branch, preventing cross-branch file resolution
     * failures, preserving multiline diagnostic integrity, and avoiding issue duplication.
     */
    @Test
    @Issue("JENKINS-55258")
    @DisplayName(
            "scanForIssues isolates console logs per parallel branch, resolving files and multiline snippets correctly")
    void shouldIsolateParallelBranchConsoleScanning() throws Exception {
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "def issuesList = []\n"
                        + "parallel(\n"
                        + "  'Branch A': {\n"
                        + "    node {\n"
                        + "      dir('wsA') {\n"
                        + "        writeFile file: 'FileA.c', text: 'int foo() { return 0; }\\n'\n"
                        + "        echo 'FileA.c:1:5: warning: message A [-Wextra]'\n"
                        + "        echo '    int foo() { return 0; }'\n"
                        + "        echo '        ^~~'\n"
                        + "        issuesList.add(scanForIssues(tool: gcc(id: 'gcc-a')))\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  'Branch B': {\n"
                        + "    node {\n"
                        + "      dir('wsB') {\n"
                        + "        writeFile file: 'FileB.c', text: 'int bar() { return 0; }\\n'\n"
                        + "        echo 'FileB.c:2:5: warning: message B [-Wextra]'\n"
                        + "        echo '    int bar() { return 0; }'\n"
                        + "        echo '        ^~~'\n"
                        + "        issuesList.add(scanForIssues(tool: gcc(id: 'gcc-b')))\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + ")\n"
                        + "node {\n"
                        + "  publishIssues issues: issuesList\n"
                        + "}\n",
                true));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        // Exactly 2 issues total (1 from Branch A, 1 from Branch B) - no duplication
        assertThat(result).hasTotalSize(2);

        var issueA = result.getIssues().stream()
                .filter(i -> i.getFileName().endsWith("FileA.c"))
                .findFirst()
                .orElseThrow();
        var issueB = result.getIssues().stream()
                .filter(i -> i.getFileName().endsWith("FileB.c"))
                .findFirst()
                .orElseThrow();

        // Multiline diagnostic snippets are preserved correctly within each branch
        assertThat(issueA.getMessage()).contains("int foo()").doesNotContain("int bar()");
        assertThat(issueB.getMessage()).contains("int bar()").doesNotContain("int foo()");

        // Filename resolution and fingerprinting succeed without missing file errors
        String consoleLog = getConsoleLog(result);
        assertThat(consoleLog).contains("Filtering console log to current parallel branch");
        assertThat(consoleLog).doesNotContain("Can't create fingerprints for some files");
    }

    /**
     * Verifies that recordIssues properly isolates console log scanning to the enclosing parallel branch.
     */
    @Test
    @Issue("JENKINS-55258")
    @DisplayName("recordIssues isolates console logs per parallel branch")
    void shouldIsolateParallelBranchesWithRecordIssues() throws Exception {
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "parallel(\n"
                        + "  'Branch A': {\n"
                        + "    node {\n"
                        + "      dir('wsA') {\n"
                        + "        writeFile file: 'FileA.c', text: 'int foo() { return 0; }\\n'\n"
                        + "        echo 'FileA.c:1:5: warning: message A [-Wextra]'\n"
                        + "        recordIssues tool: gcc(id: 'gcc-a')\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  'Branch B': {\n"
                        + "    node {\n"
                        + "      dir('wsB') {\n"
                        + "        writeFile file: 'FileB.c', text: 'int bar() { return 0; }\\n'\n"
                        + "        echo 'FileB.c:2:5: warning: message B [-Wextra]'\n"
                        + "        recordIssues tool: gcc(id: 'gcc-b')\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + ")\n",
                true));

        var build = buildSuccessfully(job);
        List<ResultAction> actions = build.getActions(ResultAction.class);
        assertThat(actions).hasSize(2);

        ResultAction actionA = actions.stream()
                .filter(a -> "gcc-a".equals(a.getId()))
                .findFirst()
                .orElseThrow();
        ResultAction actionB = actions.stream()
                .filter(a -> "gcc-b".equals(a.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(actionA.getResult()).hasTotalSize(1);
        assertThat(actionA.getResult().getIssues().get(0).getFileName()).endsWith("FileA.c");

        assertThat(actionB.getResult()).hasTotalSize(1);
        assertThat(actionB.getResult().getIssues().get(0).getFileName()).endsWith("FileB.c");

        String consoleLog = getConsoleLog(build);
        assertThat(consoleLog).contains("Filtering console log to current parallel branch");
        assertThat(consoleLog).doesNotContain("Can't create fingerprints for some files");
    }

    /**
     * Verifies backward compatibility: sequential (non-parallel) pipeline console scanning
     * continues to work as expected without branch filtering.
     */
    @Test
    @DisplayName("Sequential pipeline console scanning continues to work without branch filtering")
    void shouldKeepSequentialConsoleScanningWorking() throws Exception {
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  dir('ws') {\n"
                        + "    writeFile file: 'FileA.c', text: 'int foo() { return 0; }\\n'\n"
                        + "    echo 'FileA.c:1:5: warning: message A [-Wextra]'\n"
                        + "    recordIssues tool: gcc()\n"
                        + "  }\n"
                        + "}\n",
                true));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(1);
        assertThat(result.getIssues().get(0).getFileName()).endsWith("FileA.c");

        String consoleLog = getConsoleLog(result);
        assertThat(consoleLog).doesNotContain("Filtering console log to current parallel branch");
        assertThat(consoleLog).doesNotContain("Can't create fingerprints for some files");
    }

    /**
     * Demonstrates that file-based report scanning continues to work properly.
     */
    @Test
    @DisplayName("File scanning isolates parallel branch results and resolves files correctly")
    void shouldSucceedWhenUsingIsolatedReportFiles() throws Exception {
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "def issuesList = []\n"
                        + "parallel(\n"
                        + "  'Branch A': {\n"
                        + "    node {\n"
                        + "      dir('partA') {\n"
                        + "        writeFile file: 'FileA.c', text: 'int foo() { return 0; }\\n'\n"
                        + "        writeFile file: 'gccA.log', text: 'FileA.c:1:5: warning: message A [-Wextra]\\n'\n"
                        + "        issuesList.add(scanForIssues(tool: gcc(id: 'gcc-a', pattern: 'gccA.log')))\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  'Branch B': {\n"
                        + "    node {\n"
                        + "      dir('partB') {\n"
                        + "        writeFile file: 'FileB.c', text: 'int bar() { return 0; }\\n'\n"
                        + "        writeFile file: 'gccB.log', text: 'FileB.c:1:5: warning: message B [-Wextra]\\n'\n"
                        + "        issuesList.add(scanForIssues(tool: gcc(id: 'gcc-b', pattern: 'gccB.log')))\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + ")\n"
                        + "node {\n"
                        + "  publishIssues issues: issuesList\n"
                        + "}\n",
                true));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(2);
        assertThat(result.getIssues().stream().anyMatch(i -> i.getFileName().endsWith("FileA.c")))
                .isTrue();
        assertThat(result.getIssues().stream().anyMatch(i -> i.getFileName().endsWith("FileB.c")))
                .isTrue();
        assertThat(getConsoleLog(result)).doesNotContain("Can't create fingerprints");
    }

    /**
     * Verifies that nested parallel branches isolate console scanning to the innermost branch.
     */
    @Test
    @DisplayName("recordIssues isolates nested parallel branches to innermost branch")
    void shouldIsolateNestedParallelBranches() throws Exception {
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "parallel(\n"
                        + "  'Outer A': {\n"
                        + "    node {\n"
                        + "      dir('wsOuterA') {\n"
                        + "        echo 'OuterA.c:1:1: warning: outer message [-Wextra]'\n"
                        + "        parallel(\n"
                        + "          'Inner 1': {\n"
                        + "            dir('wsInner1') {\n"
                        + "              writeFile file: 'File1.c', text: 'int foo() { return 0; }\\n'\n"
                        + "              echo 'File1.c:1:5: warning: message 1 [-Wextra]'\n"
                        + "              recordIssues tool: gcc(id: 'gcc-1')\n"
                        + "            }\n"
                        + "          },\n"
                        + "          'Inner 2': {\n"
                        + "            dir('wsInner2') {\n"
                        + "              writeFile file: 'File2.c', text: 'int bar() { return 0; }\\n'\n"
                        + "              echo 'File2.c:2:5: warning: message 2 [-Wextra]'\n"
                        + "              recordIssues tool: gcc(id: 'gcc-2')\n"
                        + "            }\n"
                        + "          }\n"
                        + "        )\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  'Outer B': {\n"
                        + "    node {\n"
                        + "      echo 'Outer B output'\n"
                        + "    }\n"
                        + "  }\n"
                        + ")\n",
                true));

        var build = buildSuccessfully(job);
        List<ResultAction> actions = build.getActions(ResultAction.class);
        assertThat(actions).hasSize(2);

        ResultAction action1 = actions.stream()
                .filter(a -> "gcc-1".equals(a.getId()))
                .findFirst()
                .orElseThrow();
        ResultAction action2 = actions.stream()
                .filter(a -> "gcc-2".equals(a.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(action1.getResult()).hasTotalSize(1);
        assertThat(action1.getResult().getIssues().get(0).getFileName()).endsWith("File1.c");

        assertThat(action2.getResult()).hasTotalSize(1);
        assertThat(action2.getResult().getIssues().get(0).getFileName()).endsWith("File2.c");

        String consoleLog = getConsoleLog(build);
        assertThat(consoleLog).contains("Filtering console log to current parallel branch");
        assertThat(consoleLog).doesNotContain("Can't create fingerprints for some files");
    }
}
