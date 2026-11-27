package io.jenkins.plugins.analysis.warnings.steps;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ConsoleDetail;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;
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
                        + "        issuesList.add(scanForIssues(tool: gcc(id: 'gcc-a'), filterBranchLog: true))\n"
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
                        + "        issuesList.add(scanForIssues(tool: gcc(id: 'gcc-b'), filterBranchLog: true))\n"
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
                        + "        recordIssues tool: gcc(id: 'gcc-a'), filterBranchLog: true\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  'Branch B': {\n"
                        + "    node {\n"
                        + "      dir('wsB') {\n"
                        + "        writeFile file: 'FileB.c', text: 'int bar() { return 0; }\\n'\n"
                        + "        echo 'FileB.c:2:5: warning: message B [-Wextra]'\n"
                        + "        recordIssues tool: gcc(id: 'gcc-b'), filterBranchLog: true\n"
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
                        + "              recordIssues tool: gcc(id: 'gcc-1'), filterBranchLog: true\n"
                        + "            }\n"
                        + "          },\n"
                        + "          'Inner 2': {\n"
                        + "            dir('wsInner2') {\n"
                        + "              writeFile file: 'File2.c', text: 'int bar() { return 0; }\\n'\n"
                        + "              echo 'File2.c:2:5: warning: message 2 [-Wextra]'\n"
                        + "              recordIssues tool: gcc(id: 'gcc-2'), filterBranchLog: true\n"
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

    /**
     * Verifies that by default (filterBranchLog: false), recordIssues inside a parallel branch
     * scans the global build log and preserves warnings emitted before the parallel block.
     * With filterBranchLog: true, only warnings from that branch are captured.
     */
    @Test
    @DisplayName("Default scanning preserves warnings emitted before parallel block; branch isolation is opt-in")
    void shouldScanGlobalLogByDefaultAndPreserveWarningsBeforeParallelBlock() throws Exception {
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "echo 'Before.c:1:1: warning: message before parallel [-Wextra]'\n"
                        + "parallel(\n"
                        + "  'Branch A': {\n"
                        + "    node {\n"
                        + "      dir('wsA') {\n"
                        + "        echo 'FileA.c:1:5: warning: message A [-Wextra]'\n"
                        + "        recordIssues tool: gcc(id: 'gcc-default')\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  'Branch B': {\n"
                        + "    node {\n"
                        + "      dir('wsB') {\n"
                        + "        echo 'FileB.c:2:5: warning: message B [-Wextra]'\n"
                        + "        recordIssues tool: gcc(id: 'gcc-isolated'), filterBranchLog: true\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + ")\n",
                true));

        var build = buildSuccessfully(job);
        List<ResultAction> actions = build.getActions(ResultAction.class);
        assertThat(actions).hasSize(2);

        ResultAction defaultAction = actions.stream()
                .filter(a -> "gcc-default".equals(a.getId()))
                .findFirst()
                .orElseThrow();
        ResultAction isolatedAction = actions.stream()
                .filter(a -> "gcc-isolated".equals(a.getId()))
                .findFirst()
                .orElseThrow();

        // Default recorder scanned the global log and includes the warning emitted before the parallel block
        assertThat(defaultAction.getResult().getIssues().stream()
                        .anyMatch(i -> i.getFileName().endsWith("Before.c")))
                .isTrue();

        // Isolated recorder only has branch B issue (FileB.c), excluding Before.c
        assertThat(isolatedAction.getResult()).hasTotalSize(1);
        assertThat(isolatedAction.getResult().getIssues().get(0).getFileName()).endsWith("FileB.c");
    }

    /**
     * Verifies that GrepParser in parallel branches isolates branch console logs,
     * encodes the branch identity in the issue file name, and reconstructs the branch console log
     * when rendering issue details via ConsoleDetail.
     */
    @Test
    @DisplayName("GrepParser isolates branch logs and renders ConsoleDetail with branch log lines")
    void shouldIsolateBranchConsoleLogForGrepParserAndRenderConsoleDetail() throws Exception {
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "parallel(\n"
                        + "  'Branch A': {\n"
                        + "    node {\n"
                        + "      echo 'MATCH_A: unique branch A output line'\n"
                        + "      recordIssues tool: grepParser(id: 'grep-a', regexp: '^MATCH_A: .*'), filterBranchLog: true\n"
                        + "    }\n"
                        + "  },\n"
                        + "  'Branch B': {\n"
                        + "    node {\n"
                        + "      echo 'MATCH_B: unique branch B output line'\n"
                        + "      recordIssues tool: grepParser(id: 'grep-b', regexp: '^MATCH_B: .*'), filterBranchLog: true\n"
                        + "    }\n"
                        + "  }\n"
                        + ")\n",
                true));

        var build = buildSuccessfully(job);
        List<ResultAction> actions = build.getActions(ResultAction.class);
        assertThat(actions).hasSize(2);

        ResultAction actionA = actions.stream()
                .filter(a -> "grep-a".equals(a.getId()))
                .findFirst()
                .orElseThrow();
        ResultAction actionB = actions.stream()
                .filter(a -> "grep-b".equals(a.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(actionA.getResult()).hasTotalSize(1);
        var issueA = actionA.getResult().getIssues().get(0);
        assertThat(ConsoleLogHandler.isBranchConsoleLog(issueA.getFileName())).isTrue();

        assertThat(actionB.getResult()).hasTotalSize(1);
        var issueB = actionB.getResult().getIssues().get(0);
        assertThat(ConsoleLogHandler.isBranchConsoleLog(issueB.getFileName())).isTrue();

        // Verify issue detail rendering recreates the branch log lines accurately
        var detailA = actionA.getTarget().getDynamic("source." + issueA.getId().toString(), null, null);
        assertThat(detailA).isInstanceOf(ConsoleDetail.class);
        assertThat(((ConsoleDetail) detailA).getSourceCode())
                .contains("MATCH_A: unique branch A output line")
                .doesNotContain("MATCH_B: unique branch B output line");

        var detailB = actionB.getTarget().getDynamic("source." + issueB.getId().toString(), null, null);
        assertThat(detailB).isInstanceOf(ConsoleDetail.class);
        assertThat(((ConsoleDetail) detailB).getSourceCode())
                .contains("MATCH_B: unique branch B output line")
                .doesNotContain("MATCH_A: unique branch A output line");
    }

    /**
     * Verifies that Declarative Pipeline parallel stages support branch log isolation.
     */
    @Test
    @DisplayName("Declarative Pipeline parallel stages support branch log isolation")
    void shouldSupportDeclarativePipelineParallelStages() throws Exception {
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "pipeline {\n"
                        + "  agent any\n"
                        + "  stages {\n"
                        + "    stage('Parallel') {\n"
                        + "      parallel {\n"
                        + "        stage('Branch A') {\n"
                        + "          steps {\n"
                        + "            echo 'FileA.c:1:5: warning: message A [-Wextra]'\n"
                        + "            recordIssues tool: gcc(id: 'gcc-decl-a'), filterBranchLog: true\n"
                        + "          }\n"
                        + "        }\n"
                        + "        stage('Branch B') {\n"
                        + "          steps {\n"
                        + "            echo 'FileB.c:2:5: warning: message B [-Wextra]'\n"
                        + "            recordIssues tool: gcc(id: 'gcc-decl-b'), filterBranchLog: true\n"
                        + "          }\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + "}\n",
                true));

        var build = buildSuccessfully(job);
        List<ResultAction> actions = build.getActions(ResultAction.class);
        assertThat(actions).hasSize(2);

        ResultAction actionA = actions.stream()
                .filter(a -> "gcc-decl-a".equals(a.getId()))
                .findFirst()
                .orElseThrow();
        ResultAction actionB = actions.stream()
                .filter(a -> "gcc-decl-b".equals(a.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(actionA.getResult()).hasTotalSize(1);
        assertThat(actionA.getResult().getIssues().get(0).getFileName()).endsWith("FileA.c");

        assertThat(actionB.getResult()).hasTotalSize(1);
        assertThat(actionB.getResult().getIssues().get(0).getFileName()).endsWith("FileB.c");
    }

    /**
     * Verifies that recordIssues called after a completed parallel block scans the interleaved global log.
     */
    @Test
    @DisplayName("recordIssues after completed parallel block scans the global build log")
    void shouldScanGlobalLogWhenScanningAfterParallelBlock() throws Exception {
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "parallel(\n"
                        + "  'Branch A': {\n"
                        + "    node {\n"
                        + "      echo 'FileA.c:1:5: warning: message A [-Wextra]'\n"
                        + "    }\n"
                        + "  },\n"
                        + "  'Branch B': {\n"
                        + "    node {\n"
                        + "      echo 'FileB.c:2:5: warning: message B [-Wextra]'\n"
                        + "    }\n"
                        + "  }\n"
                        + ")\n"
                        + "node {\n"
                        + "  recordIssues tool: gcc(id: 'gcc-post')\n"
                        + "}\n",
                true));

        var build = buildSuccessfully(job);
        List<ResultAction> actions = build.getActions(ResultAction.class);
        assertThat(actions).hasSize(1);

        ResultAction action = actions.get(0);
        // Post-parallel recorder finds both issues across the build
        assertThat(action.getResult().getIssues().stream()
                        .anyMatch(i -> i.getFileName().endsWith("FileA.c")))
                .isTrue();
        assertThat(action.getResult().getIssues().stream()
                        .anyMatch(i -> i.getFileName().endsWith("FileB.c")))
                .isTrue();
    }
}
