package io.jenkins.plugins.analysis.warnings.steps;

import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.jvnet.hudson.test.TestExtension;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Severity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.kohsuke.stapler.HttpResponse;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.UnprotectedRootAction;
import hudson.util.HttpResponses;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.portlets.PullRequestMonitoringPortlet;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.FindBugs;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.JavaDoc;
import io.jenkins.plugins.analysis.warnings.JcReport;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.analysis.warnings.groovy.GroovyParser;
import io.jenkins.plugins.analysis.warnings.groovy.ParserConfiguration;
import io.jenkins.plugins.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

/**
 * Integration tests of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.ExcessiveClassLength", "PMD.ExcessivePublicCount", "PMD.CouplingBetweenObjects", "PMD.GodClass", "checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity"})
class StepsITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String NO_QUALITY_GATE = "";

    @Test
    void shouldNotFailWhenJobHasNoWorkspace() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("eclipse.txt");
        job.setDefinition(createPipelineScript("def r; node {r = scanForIssues tool: eclipse(pattern: '*issues.txt')}; publishIssues issues: [r]"));

        var build = buildSuccessfully(job);
        assertThat(build.getAction(ResultAction.class).getResult().getIssues()).hasSize(8);
    }

    @Test
    void shouldParseCheckstyleUsingTheParserRegistry() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("checkstyle1.xml", "checkstyle2.xml");

        job.setDefinition(createPipelineScript("""
                node {
                  stage ('Integration Test') {
                         recordIssues tool: analysisParser(analysisModelId: 'checkstyle', pattern: '**/\
                checkstyle1\
                *')
                  }
                }\
                """));

        var baseline = scheduleSuccessfulBuild(job);
        assertThat(baseline).hasTotalSize(3);

        var action = getResultAction(job.getLastBuild());
        assertThat(action.getDisplayName()).isEqualTo("CheckStyle Warnings");
    }

    /**
     * Runs a pipeline and verifies the {@code scanForIssues} step has some whitelisted methods.
     */
    @Test
    void shouldWhitelistScannerApi() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("checkstyle1.xml", "checkstyle2.xml");

        configureScanner(job, "checkstyle1");
        Run<?, ?> baseline = buildSuccessfully(job);

        assertThat(getConsoleLog(baseline)).contains("[total=" + 3 + "]");
        assertThat(getConsoleLog(baseline)).contains("[id=checkstyle]");

        configureScanner(job, "checkstyle2");
        Run<?, ?> build = buildSuccessfully(job);
        assertThat(getConsoleLog(build)).contains("[total=" + 4 + "]");
        assertThat(getConsoleLog(build)).contains("[id=checkstyle]");
    }

    /**
     * Runs a pipeline and verifies the {@code recordIssues} step has some allowlisted methods.
     */
    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-63109")
    void shouldWhitelistRecorderApi() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("checkstyle1.xml", "checkstyle2.xml");

        configureRecorder(job, "checkstyle1");
        Run<?, ?> baseline = buildSuccessfully(job);

        assertThat(getConsoleLog(baseline)).contains("[reportsSize=" + 1 + "]");
        assertThat(getConsoleLog(baseline)).contains("[totalSize=" + 3 + "]");
        assertThat(getConsoleLog(baseline)).contains("[newSize=" + 0 + "]");
        assertThat(getConsoleLog(baseline)).contains("[fixedSize=" + 0 + "]");
        assertThat(getConsoleLog(baseline)).contains("[qualityGate=" + "PASSED" + "]");
        assertThat(getConsoleLog(baseline)).contains("[id=checkstyle]");
        assertThat(getConsoleLog(baseline))
                .contains("CsharpNamespaceDetector.java(17,5): DesignForExtensionCheck: Design:");
        assertThat(getConsoleLog(baseline))
                .contains("CsharpNamespaceDetector.java(42,0): LineLengthCheck: Sizes:");
        assertThat(getConsoleLog(baseline))
                .contains("CsharpNamespaceDetector.java(22,5): DesignForExtensionCheck: Design:");

        configureRecorder(job, "checkstyle2");
        Run<?, ?> build = buildWithResult(job, Result.UNSTABLE);
        assertThat(getConsoleLog(build)).contains("[reportsSize=" + 1 + "]");
        assertThat(getConsoleLog(build)).contains("[totalSize=" + 4 + "]");
        assertThat(getConsoleLog(build)).contains("[newSize=" + 3 + "]");
        assertThat(getConsoleLog(build)).contains("[fixedSize=" + 2 + "]");
        assertThat(getConsoleLog(build)).contains("[qualityGate=" + "WARNING" + "]");
        assertThat(getConsoleLog(build)).contains("[id=checkstyle]");
        assertThat(getConsoleLog(build)).contains(
                "CsharpNamespaceDetector.java(29,0): LineLengthCheck: Sizes: Zeile ");
        assertThat(getConsoleLog(build)).contains(
                "CsharpNamespaceDetector.java(30,21): RightCurlyCheck: Blocks: '}' sollte in derselben Zeile stehen.");
        assertThat(getConsoleLog(build)).contains(
                "CsharpNamespaceDetector.java(37,9): RightCurlyCheck: Blocks: '}' sollte in derselben Zeile stehen.");
        assertThat(getConsoleLog(build)).contains(
                "CsharpNamespaceDetector.java(22,5): DesignForExtensionCheck: Design: Die Methode 'detectPackageName' ");
    }

    private void configureRecorder(final WorkflowJob job, final String fileName) {
        job.setDefinition(createPipelineScript("node {\n"
                + "  stage ('Integration Test') {\n"
                + "         discoverReferenceBuild()\n"
                + "         def reports = recordIssues tool: checkStyle(pattern: '**/" + fileName
                + "*'), qualityGates: [[threshold: 4, type: 'TOTAL', unstable: true]]\n"
                + "         echo '[reportsSize=' + reports.size() + ']' \n"
                + "         def result = reports.get(0) \n"
                + "         echo '[totalSize=' + result.getTotals().getTotalSize() + ']' \n"
                + "         echo '[newSize=' + result.getTotals().getNewSize() + ']' \n"
                + "         echo '[fixedSize=' + result.getTotals().getFixedSize() + ']' \n"
                + "         echo '[qualityGate=' + result.getQualityGateResult() + ']' \n"
                + "         echo '[id=' + result.getId() + ']' \n"
                + "         result.getIssues().each { issue ->\n"
                + "             echo issue.toString()\n"
                + "         }"
                + "  }\n"
                + "}"));
    }

    private void configureScanner(final WorkflowJob job, final String fileName) {
        job.setDefinition(createPipelineScript("node {\n"
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
                + "}"));
    }

    /**
     * Runs a pipeline and verifies that blames are skipped.
     */
    @Test
    void shouldSkipBlaming() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("checkstyle1.xml");

        job.setDefinition(createPipelineScript("""
                node {
                  stage ('Integration Test') {
                         recordIssues skipBlames: true, tool: checkStyle(pattern: '**/checkstyle1\
                *')
                  }
                }\
                """));
        Run<?, ?> baseline = buildSuccessfully(job);
        assertThat(getConsoleLog(baseline)).contains("Skipping SCM blames as requested");
    }

    @ParameterizedTest(name = "{index} => Logging quiet flag: {0}")
    @ValueSource(booleans = {true, false})
    @DisplayName("Verify quiet property for logger")
    void shouldToggleQuietStatusOfLogger(final boolean quiet) {
        var job = createPipelineWithWorkspaceFilesWithSuffix("checkstyle1.xml");

        job.setDefinition(createPipelineScript("node {\n"
                + "  stage ('Integration Test') {\n"
                + "         recordIssues "
                + "             quiet: " + quiet + ", "
                + "             tool: checkStyle(pattern: '**/" + "checkstyle1" + "*')\n"
                + "  }\n"
                + "}"));
        Run<?, ?> baseline = buildSuccessfully(job);
        var consoleLog = getConsoleLog(baseline);
        var message = "[CheckStyle]";
        if (quiet) {
            assertThat(consoleLog).doesNotContain(message);
        }
        else {
            assertThat(consoleLog).contains(message);
        }
    }

    /**
     * Runs a pipeline and verifies the {@code publishIssues} step has allowlisted methods.
     */
    @Test
    void shouldWhitelistPublisherApi() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("checkstyle1.xml", "checkstyle2.xml");

        configurePublisher(job, "checkstyle1", NO_QUALITY_GATE);
        Run<?, ?> baseline = buildSuccessfully(job);

        verifyApiResults(baseline, 3, 0, 0, "INACTIVE");

        configurePublisher(job, "checkstyle2", NO_QUALITY_GATE);
        Run<?, ?> build = buildSuccessfully(job);
        verifyApiResults(build, 4, 3, 2, "INACTIVE");

        configurePublisher(job, "checkstyle2", "[threshold: 4, type: 'TOTAL', unstable: true]");
        Run<?, ?> unstable = buildWithResult(job, Result.UNSTABLE);
        verifyApiResults(unstable, 4, 0, 0, "WARNING");

        configurePublisher(job, "checkstyle2", "[threshold: 4, type: 'TOTAL', unstable: false]");
        Run<?, ?> failed = buildWithResult(job, Result.FAILURE);
        verifyApiResults(failed, 4, 0, 0, "FAILED");
    }

    private void verifyApiResults(final Run<?, ?> baseline, final int totalSize, final int newSize, final int fixedSize,
            final String qualityGateStatus) {
        assertThat(getConsoleLog(baseline)).contains("[total=" + totalSize + "]");
        assertThat(getConsoleLog(baseline)).contains("[new=" + newSize + "]");
        assertThat(getConsoleLog(baseline)).contains("[fixed=" + fixedSize + "]");
        assertThat(getConsoleLog(baseline)).contains("[id=checkstyle]");
        assertThat(getConsoleLog(baseline)).contains("[name=CheckStyle Warnings]");
        assertThat(getConsoleLog(baseline)).contains("[status=" + qualityGateStatus + "]");
        boolean isSuccessful = QualityGateStatus.valueOf(qualityGateStatus).isSuccessful();
        assertThat(getConsoleLog(baseline)).contains("[isSuccessful=" + isSuccessful + "]");
        assertThat(getConsoleLog(baseline)).contains("[isSuccessfulQualityGate=" + isSuccessful + "]");
    }

    private void configurePublisher(final WorkflowJob job, final String fileName, final String qualityGate) {
        var qualityGateParameter = "qualityGates: [%s]".formatted(qualityGate);
        job.setDefinition(createPipelineScript("node {\n"
                + "  stage ('Integration Test') {\n"
                + "         discoverReferenceBuild()\n"
                + "         def issues = scanForIssues tool: checkStyle(pattern: '**/" + fileName + "*')\n"
                + "         def action = publishIssues issues:[issues], " + qualityGateParameter + "\n"
                + "         echo '[id=' + action.getId() + ']' \n"
                + "         echo '[name=' + action.getDisplayName() + ']' \n"
                + "         echo '[isSuccessful=' + action.isSuccessful() + ']' \n"
                + "         def result = action.getResult()\n"
                + "         def status = result.getQualityGateResult()\n"
                + "         echo '[status=' + status + ']' \n"
                + "         echo '[isSuccessfulQualityGate=' + status.isSuccessful() + ']' \n"
                + "         def totals = result.getTotals()\n"
                + "         echo '[total=' + totals.getTotalSize() + ']' \n"
                + "         echo '[new=' + totals.getNewSize() + ']' \n"
                + "         echo '[fixed=' + totals.getFixedSize() + ']' \n"
                + "  }\n"
                + "}"));
    }

    /** Verifies that a {@link Tool} defines a {@link Symbol}. */
    @Test
    void shouldProvideSymbol() {
        var findBugs = new FindBugs();

        assertThat(findBugs.getSymbolName()).isEqualTo("findBugs");
    }

    /**
     * Creates a declarative Pipeline and scans for a Gcc warning.
     */
    @Test
    void shouldRunInDeclarativePipeline() {
        var job = createPipeline();

        job.setDefinition(createPipelineScript("pipeline {\n"
                + "    agent 'any'\n"
                + "    stages {\n"
                + "        stage ('Create a fake warning') {\n"
                + "            steps {\n"
                + createShellStep("echo \"foo.cc:4:39: error: foo.h: No such file or directory\" >warnings.log")
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "    post {\n"
                + "        always {\n"
                + "            recordIssues tool: gcc4(pattern: 'warnings.log')\n"
                + "        }\n"
                + "    }\n"
                + "}"));

        var result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(1);
    }

    private String createShellStep(final String script) {
        if (isWindows()) {
            return "bat '%s'".formatted(script);
        }
        else {
            return "sh '%s'".formatted(script);
        }
    }

    private String createCatStep(final String arguments) {
        if (isWindows()) {
            return "bat 'type %s'".formatted(arguments);
        }
        else {
            return "sh 'cat %s'".formatted(arguments);
        }
    }

    /** Runs the Clang parser on an output file that contains 1 issue. */
    @Test
    void shouldFindAllClangIssuesIfConsoleIsAnnotatedWithTimeStamps() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("issue56484.txt");
        job.setDefinition(asStage(
                createCatStep("*.txt"),
                "def issues = scanForIssues tool: clang()",
                PUBLISH_ISSUES_STEP));

        var result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(1);
        assertThat(result.getIssues().get(0))
                .hasLineStart(1)
                .hasLineEnd(1)
                .hasColumnStart(2)
                .hasColumnEnd(2)
                .hasMessage("This is an error.")
                .hasFileName("test.c")
                .hasSeverity(Severity.WARNING_HIGH);
    }

    /**
     * Tries to expose JENKINS-64243.
     */
    @Test
    void issue64243() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("maven-console.txt");
        job.setDefinition(
                asStage("def issues = scanForIssues tool: mavenConsole(id: 'id', name: 'MavenConsoleFile', pattern: '*.txt')",
                        PUBLISH_ISSUES_STEP));

        var result = scheduleSuccessfulBuild(job);
        assertThat(result).hasTotalSize(4);
    }

    /** Runs the Clang parser on an output file that contains 1 issue. */
    @Test
    void shouldFindAllGhsIssuesIfConsoleIsAnnotatedWithTimeStamps() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("issue59118.txt");
        job.setDefinition(asStage(
                createCatStep("*.txt"),
                "def issues = scanForIssues tool: ghsMulti()",
                PUBLISH_ISSUES_STEP));

        var result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(2);
        var issues = result.getIssues();
        assertThat(issues.get(0))
                .hasLineStart(19)
                .hasMessage("""
                        operands of logical && or || must be primary expressions
                        
                          #if !defined(_STDARG_H) && !defined(_STDIO_H) && !defined(_GHS_WCHAR_H)""")
                .hasFileName("C:/Path/To/bar.h")
                .hasCategory("#1729-D")
                .hasSeverity(Severity.WARNING_NORMAL);
        assertThat(issues.get(1))
                .hasLineStart(491)
                .hasMessage("""
                        operands of logical && or || must be primary expressions
                        
                                              if(t_deltaInterval != t_u4Interval && t_deltaInterval != 0)""")
                .hasFileName("../../../../Sources/Foo/Bar/Test.c")
                .hasCategory("#1729-D")
                .hasSeverity(Severity.WARNING_NORMAL);
    }

    /**
     * Parses a colored console log that also contains console notes. Verifies that the console notes will be removed
     * before the color codes. Output is from ATH test case
     * {@code WarningsNextGenerationPluginTest#should_show_maven_warnings_in_maven_project}.
     */
    @Test
    void shouldRemoveConsoleLogNotesBeforeRemovingColorCodes() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("ath-colored.log");
        job.setDefinition(asStage(
                createCatStep("*.txt"),
                "recordIssues tool: mavenConsole()"));

        var result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(4).hasTotalErrorsSize(2).hasTotalNormalPrioritySize(2);
        assertThat(result.getIssues().get(0))
                .hasSeverity(Severity.WARNING_NORMAL);
        assertThat(result.getIssues().get(1))
                .hasDescription(
                        "<pre><code>Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!</code></pre>")
                .hasSeverity(Severity.WARNING_NORMAL);
        assertThat(result.getIssues().get(2))
                .hasSeverity(Severity.ERROR);
        assertThat(result.getIssues().get(2).getDescription())
                .contains("Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:2.3.2:compile");
        assertThat(result.getIssues().get(3))
                .hasSeverity(Severity.ERROR);
        assertThat(result.getIssues().get(3).getDescription())
                .contains("Re-run Maven using the -X switch to enable full debug logging.");
    }

    /** Runs the Clang parser on an output file that contains 1 issue. */
    @Test
    void shouldFindAllJavaIssuesIfConsoleIsAnnotatedWithTimeStamps() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("issue56484-maven.txt");
        job.setDefinition(asStage(
                createCatStep("*.txt"),
                "def issues = scanForIssues tool: java()",
                PUBLISH_ISSUES_STEP));

        var result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(6);
    }

    /**
     * Runs the Eclipse parser on the console log that contains 8 issues which are decorated with console notes. The
     * output file is copied to the console log using a shell cat command.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-11675">Issue 11675</a>
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-55368">Issue 55368</a>
     */
    @Test
    void shouldRemoveConsoleNotes() {
        assertThatConsoleNotesAreRemoved("issue11675.txt", 8);
        assertThatConsoleNotesAreRemoved("issue55368.txt", 1);
    }

    private void assertThatConsoleNotesAreRemoved(final String fileName, final int expectedSize) {
        var job = createPipelineWithWorkspaceFilesWithSuffix(fileName);
        job.setDefinition(asStage(
                createCatStep("*.txt"),
                "def issues = scanForIssues tool: eclipse()",
                PUBLISH_ISSUES_STEP));

        var result = scheduleSuccessfulBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(expectedSize);
        assertThat(result.getIssues()).hasSize(expectedSize);

        var report = result.getIssues();
        assertThat(report.filter(issue -> "eclipse".equals(issue.getOrigin()))).hasSize(expectedSize);
        for (Issue annotation : report) {
            assertThat(annotation.getMessage()).matches("[a-zA-Z].*");
        }
    }

    /** Runs the all Java parsers on three output files: the build should report issues of all tools. */
    @Test
    void shouldCombineIssuesOfSeveralFiles() {
        publishResultsWithIdAndName(
                "publishIssues issues:[java, eclipse, javadoc]",
                "analysis", "Static Analysis", "triangle-exclamation");
    }

    /** Runs the JavaDoc parser and uses a message filter to change the number of recorded warnings. */
    @Test
    void shouldFilterByMessage() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("javadoc.txt");
        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), "
                        + "filters:[includeMessage('.*@link.*'), excludeMessage('.*removeSpecChangeListener.*')]")); // 4 @link and one with removeSpecChangeListener

        var result = scheduleSuccessfulBuild(job);
        assertThat(result.getIssues()).hasSize(3);
    }

    /** Verifies that the JavaDoc parser reports just an information message when reading an empty file. */
    @Test
    void javaDocShouldNotReportErrorOnEmptyFiles() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("emptyFile.txt");

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8')"));

        var result = scheduleSuccessfulBuild(job);
        assertThat(result.getIssues()).hasSize(0);
        assertThat(result).hasInfoMessages(
                "Skipping file 'emptyFile-issues.txt' because it's empty");
    }

    /** Verifies that the CheckStyle parser reports an error when reading an empty XML file. */
    @Test
    void checkStyleShouldReportErrorOnEmptyFiles() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("emptyFile.txt");

        job.setDefinition(asStage(
                "recordIssues tool: checkStyle(pattern:'**/*issues.txt', reportEncoding:'UTF-8')"));

        var result = scheduleSuccessfulBuild(job);
        assertThat(result.getIssues()).hasSize(0);
        assertThat(result).hasErrorMessages("Skipping file 'emptyFile-issues.txt' because it's empty");
    }

    /** Runs the JavaDoc parser and enforces quality gates. */
    @Test
    void shouldEnforceQualityGate() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("javadoc.txt");

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), "
                        + "qualityGates: [[threshold: 6, type: 'TOTAL', unstable: true]]"));
        buildWithResult(job, Result.UNSTABLE);

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), "
                        + "qualityGates: [[threshold: 6, type: 'TOTAL', unstable: false]]"));
        buildWithResult(job, Result.FAILURE);

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), "
                        + "qualityGates: [[threshold: 6, type: 'TOTAL_NORMAL', unstable: true]]"));
        buildWithResult(job, Result.UNSTABLE);

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), "
                        + "qualityGates: [[threshold: 6, type: 'TOTAL_NORMAL', unstable: false]]"));
        buildWithResult(job, Result.FAILURE);
    }

    /** Runs the JavaDoc parser and enforces quality gates. */
    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-58253")
    void shouldFailBuildWhenFailBuildOnErrorsIsSet() {
        var job = createPipeline();

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8')"));

        scheduleSuccessfulBuild(job);

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), failOnError: true"));

        scheduleBuildAndAssertStatus(job, Result.FAILURE);
    }

    @ParameterizedTest(name = "{index} => Reading JavaDoc warnings from file \"{0}\"")
    @ValueSource(strings = {"javadoc.txt", "emptyFile.txt"})
    @org.junitpioneer.jupiter.Issue("JENKINS-75344")
    void shouldReportResultWithDifferentIdNameAndIconInStep(final String fileName) {
        var job = createPipelineWithWorkspaceFilesWithSuffix(fileName);

        job.setDefinition(asStage(
                "recordIssues id: 'custom-id', name: 'custom-name', icon: 'custom-icon', "
                        + "tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8')"));

        var action = getResultAction(buildWithResult(job, Result.SUCCESS));
        assertThat(action.getUrlName()).isEqualTo("custom-id");
        assertThat(action.getId()).isEqualTo("custom-id");
        assertThat(action.getDisplayName()).startsWith("custom-name");
        assertThat(action.getIconFileName()).isEqualTo("custom-icon");
    }

    @ParameterizedTest(name = "{index} => Reading JavaDoc warnings from file \"{0}\"")
    @ValueSource(strings = {"javadoc.txt", "emptyFile.txt"})
    @org.junitpioneer.jupiter.Issue("JENKINS-75344")
    void shouldReportResultWithDifferentIdNameAndIconInTool(final String fileName) {
        var job = createPipelineWithWorkspaceFilesWithSuffix(fileName);

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8',"
                        + "id: 'custom-id', name: 'custom-name', icon: 'custom-icon')"));

        var action = getResultAction(buildWithResult(job, Result.SUCCESS));
        assertThat(action.getUrlName()).isEqualTo("custom-id");
        assertThat(action.getId()).isEqualTo("custom-id");
        assertThat(action.getDisplayName()).startsWith("custom-name");
        assertThat(action.getIconFileName()).isEqualTo("custom-icon");
    }

    @ParameterizedTest(name = "{index} => Reading JavaDoc warnings from file \"{0}\"")
    @ValueSource(strings = {"javadoc.txt", "emptyFile.txt"})
    @org.junitpioneer.jupiter.Issue("JENKINS-75391")
    void shouldShowWarningWhenUsingIdForToolAndRecorder(final String fileName) {
        var job = createPipelineWithWorkspaceFilesWithSuffix(fileName);

        job.setDefinition(asStage(
                "recordIssues id: 'custom-id', name: 'custom-name', icon: 'custom-icon', "
                        + "tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8',"
                        + "id: 'custom-id', name: 'custom-name', icon: 'custom-icon')"));

        var action = getResultAction(buildWithResult(job, Result.SUCCESS));
        assertThat(action.getUrlName()).isEqualTo("custom-id");
        assertThat(action.getId()).isEqualTo("custom-id");
        assertThat(action.getDisplayName()).startsWith("custom-name");
        assertThat(action.getIconFileName()).isEqualTo("custom-icon");

        assertThat(getConsoleLog(action.getOwner()))
                .contains("Do not set id, name, or icon for both the tool and the recorder");
    }

    /**
     * Runs the all Java parsers on three output files: the build should report issues of all tools. The results should
     * be aggregated into a new action with the specified ID. Since no name is given, the default name is used.
     */
    @Test
    void shouldProvideADefaultNameIfNoOneIsGiven() {
        publishResultsWithIdAndName(
                "publishIssues issues:[java, eclipse, javadoc], id:'my-id'",
                "my-id", "Static Analysis Warnings", "triangle-exclamation");
    }

    /**
     * Runs the all Java parsers on three output files: the build should report issues of all tools. The results should
     * be aggregated into a new action with the specified ID and the specified name.
     */
    @Test
    void shouldUseSpecifiedName() {
        publishResultsWithIdAndName(
                "publishIssues issues:[java, eclipse, javadoc], id:'my-id', name:'my-name'",
                "my-id", "my-name", "triangle-exclamation");
    }

    /**
     * Runs the all Java parsers on three output files: the build should report issues of all tools. The results should
     * be aggregated into a new action with the specified ID and the specified name.
     */
    @Test
    void shouldUseSpecifiedIcon() {
        publishResultsWithIdAndName(
                "publishIssues issues:[java, eclipse, javadoc], id:'my-id', name:'my-name', icon:'my-icon'",
                "my-id", "my-name", "my-icon");
    }

    private void publishResultsWithIdAndName(final String publishStep, final String expectedId,
            final String expectedName, final String expectedIcon) {
        var job = createPipelineWithWorkspaceFilesWithSuffix("eclipse.txt", "javadoc.txt", "javac.txt");
        job.setDefinition(asStage(createScanForIssuesStep(new Java(), "java"),
                createScanForIssuesStep(new Eclipse(), "eclipse"),
                createScanForIssuesStep(new JavaDoc(), "javadoc"),
                publishStep));

        Run<?, ?> run = buildSuccessfully(job);

        var action = getResultAction(run);
        assertThat(action.getId()).isEqualTo(expectedId);
        assertThat(action.getDisplayName()).contains(expectedName);
        assertThat(action.getIconFileName()).contains(expectedIcon);

        assertThatJavaIssuesArePublished(action.getResult());
    }

    private void assertThatJavaIssuesArePublished(final AnalysisResult result) {
        var report = result.getIssues();
        assertThat(report.filter(issue -> "eclipse".equals(issue.getOrigin()))).hasSize(
                10); // maven eclipse detects to maven javac warnings
        assertThat(report.filter(issue -> "java".equals(issue.getOrigin()))).hasSize(2);
        assertThat(report.filter(issue -> "javadoc-warnings".equals(issue.getOrigin()))).hasSize(6);
        assertThat(report.getTools()).containsExactlyInAnyOrder("java", "javadoc-warnings", "eclipse");
        assertThat(result.getIssues()).hasSize(10 + 2 + 6);
    }

    /**
     * Runs the the Java and JavaDoc parsers on two output files. Both parsers are using a custom ID that should be used
     * for the origin field as well.
     */
    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-57638")
    void shouldUseCustomIdsForOrigin() {
        verifyCustomIdsForOrigin(asStage(
                "def java = scanForIssues tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8', id:'id1', name:'name1')",
                "def javaDoc = scanForIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8', id:'id2', name:'name2')",
                "publishIssues issues:[java, javaDoc]"));
    }

    /**
     * Runs the the Java and JavaDoc parsers on two output files. Both parsers are using a custom ID that should be used
     * for the origin field as well.
     */
    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-57638")
    void shouldUseCustomIdsForOriginSimpleStep() {
        verifyCustomIdsForOrigin(asStage(
                """
                recordIssues(
                                    aggregatingResults: true,\s
                                    tools: [
                                        java(pattern:'**/*issues.txt', reportEncoding:'UTF-8', id:'id1', name:'name1'),
                                        javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8', id:'id2', name:'name2')
                                    ]
                                )"""));
    }

    private void verifyCustomIdsForOrigin(final CpsFlowDefinition stage) {
        var job = createPipelineWithWorkspaceFilesWithSuffix("javadoc.txt", "javac.txt");
        job.setDefinition(stage);
        Run<?, ?> run = buildSuccessfully(job);

        var action = getResultAction(run);
        assertThat(action.getId()).isEqualTo("analysis");
        assertThat(action.getDisplayName()).contains("Static Analysis");

        var result = action.getResult();
        assertThat(result.getIssues()).hasSize(2 + 6);
        assertThat(result.getSizePerOrigin()).contains(entry("id1", 2), entry("id2", 6));

        var report = result.getIssues();
        assertThat(report.filter(issue -> "id1".equals(issue.getOrigin()))).hasSize(2);
        assertThat(report.filter(issue -> "id2".equals(issue.getOrigin()))).hasSize(6);
        assertThat(report.getNameOfOrigin("id1")).isEqualTo("name1");
        assertThat(report.getNameOfOrigin("id2")).isEqualTo("name2");
    }

    /**
     * Runs the Eclipse parsers using the 'tools' property.
     */
    @Test
    void shouldUseToolsProperty() {
        runEclipse("tools: [eclipse(pattern:'**/*issues.txt', reportEncoding:'UTF-8')]");
    }

    /**
     * Runs the Eclipse parsers using the 'toolProxies' property.
     */
    @Test
    void shouldUseToolProxiesProperty() {
        runEclipse("toolProxies: [[tool: eclipse(pattern:'**/*issues.txt', reportEncoding:'UTF-8')]]");
    }

    /**
     * Runs the Eclipse parsers using the 'tool' property.
     */
    @Test
    void shouldUseToolProperty() {
        runEclipse("tool: eclipse(pattern:'**/*issues.txt', reportEncoding:'UTF-8')");
    }

    private void runEclipse(final String property) {
        var job = createPipelineWithWorkspaceFilesWithSuffix("eclipse.txt");
        job.setDefinition(asStage("recordIssues "
                + property));

        var result = scheduleSuccessfulBuild(job);
        assertThat(result.getIssues()).hasSize(8);
    }

    /**
     * Runs the Java parser on an pep8 log file: the build should report no issues. A result should be available with
     * the java ID and name.
     */
    @Test
    void shouldHaveActionWithIdAndNameWithEmptyResults() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("pep8Test.txt");
        job.setDefinition(asStage(createScanForIssuesStep(new Java(), "java"),
                "publishIssues issues:[java]"));

        Run<?, ?> run = buildSuccessfully(job);

        var action = getResultAction(run);
        assertThat(action.getId()).isEqualTo("java");
        assertThat(action.getDisplayName()).contains("Java");

        var result = action.getResult();
        assertThat(result.getIssues()).isEmpty();
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and runs this parser on an
     * error log with 8 issues.
     */
    @Test
    void shouldShowWarningsOfGroovyParserWhenScanningFileInWorkspace() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("pep8Test.txt");
        job.setDefinition(asStage(
                "def groovy = scanForIssues "
                        + "tool: groovyScript(parserId: 'groovy-pep8', pattern:'**/*issues.txt', reportEncoding:'UTF-8')",
                "publishIssues issues:[groovy]"));

        ParserConfiguration configuration = ParserConfiguration.getInstance();
        var id = "groovy-pep8";
        configuration.setParsers(Collections.singletonList(
                new GroovyParser(id, "Groovy Pep8",
                        "(.*):(\\d+):(\\d+): (\\D\\d*) (.*)",
                        toString("groovy/pep8.groovy"), "")));
        testGroovyPep8JobIsSuccessful(job, id);
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and runs this parser on the
     * console that's showing an error log with 8 issues.
     *
     * @throws IOException
     *         if the test fails unexpectedly
     */
    @Test
    void shouldShowWarningsOfGroovyParserWhenScanningConsoleLogWhenThatIsPermitted() throws IOException {
        var job = createPipeline();
        List<String> stages = new ArrayList<>();
        catFileContentsByAddingEchosSteps(stages, "pep8Test.txt");
        stages.add("def groovy = scanForIssues "
                + "tool: groovyScript(parserId: 'groovy-pep8', pattern:'', reportEncoding:'UTF-8')");
        stages.add("publishIssues issues:[groovy]");
        job.setDefinition(asStage(stages.toArray(new String[0])));

        ParserConfiguration configuration = ParserConfiguration.getInstance();
        configuration.setConsoleLogScanningPermitted(true);
        var id = "groovy-pep8";
        configuration.setParsers(Collections.singletonList(
                new GroovyParser(id, "Groovy Pep8",
                        "(.*):(\\d+):(\\d+): (\\D\\d*) (.*)",
                        toString("groovy/pep8.groovy"), "")));
        testGroovyPep8JobIsSuccessful(job, id);
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and runs this parser on the
     * console that's showing an error log with 8 issues.
     *
     * @throws IOException
     *         if the test fails unexpectedly
     */
    @Test
    void shouldShowWarningsOfGroovyParserWhenScanningConsoleLogWhenThatIsPermittedAndUsingAddParser()
            throws IOException {
        var job = createPipeline();
        ArrayList<String> stages = new ArrayList<>();
        catFileContentsByAddingEchosSteps(stages, "pep8Test.txt");
        stages.add("def groovy = scanForIssues "
                + "tool: groovyScript(parserId: 'another-groovy-pep8', pattern:'', reportEncoding:'UTF-8')");
        stages.add("publishIssues issues:[groovy]");
        job.setDefinition(asStage(stages.toArray(new String[0])));

        ParserConfiguration configuration = ParserConfiguration.getInstance();
        configuration.setConsoleLogScanningPermitted(true);
        var id = "another-groovy-pep8";

        configuration.addParser(new GroovyParser(id, "Another Groovy Pep8",
                "(.*):(\\d+):(\\d+): (\\D\\d*) (.*)",
                toString("groovy/pep8.groovy"), ""));
        testGroovyPep8JobIsSuccessful(job, id);
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and runs this parser on the
     * console that's showing an error log with 8 issues ... but when we're configured not to allow groovy parsers to
     * scan the console at all so we expect it to fail.
     *
     * @throws IOException
     *         if the test fails unexpectedly
     */
    @Test
    void shouldFailUsingGroovyParserToScanConsoleLogWhenThatIsForbidden() throws IOException {
        var job = createPipeline();
        ArrayList<String> stages = new ArrayList<>();
        catFileContentsByAddingEchosSteps(stages, "pep8Test.txt");
        stages.add("def groovy = scanForIssues "
                + "tool: groovyScript(parserId: 'groovy-pep8', pattern:'', reportEncoding:'UTF-8')");
        stages.add("publishIssues issues:[groovy]");
        job.setDefinition(asStage(stages.toArray(new String[0])));

        ParserConfiguration configuration = ParserConfiguration.getInstance();
        configuration.setConsoleLogScanningPermitted(false);
        var id = "groovy-pep8";
        configuration.setParsers(Collections.singletonList(
                new GroovyParser(id, "Groovy Pep8",
                        "(.*):(\\d+):(\\d+): (\\D\\d*) (.*)",
                        toString("groovy/pep8.groovy"), "")));
        buildWithResult(job, Result.FAILURE);
    }

    private void testGroovyPep8JobIsSuccessful(final WorkflowJob job, final String id) {
        Run<?, ?> run = buildSuccessfully(job);

        var action = getResultAction(run);
        assertThat(action.getId()).isEqualTo(id);
        assertThat(action.getDisplayName()).contains("Groovy Pep8");

        var result = action.getResult();
        assertThat(result.getIssues()).hasSize(8);
        assertThat(result.getIssues().getPropertyCount(Issue::getOrigin)).containsOnly(entry(id, 8));

        var second = scheduleSuccessfulBuild(job);
        assertThat(second).hasFixedSize(0).hasTotalSize(8).hasNewSize(0);
    }

    private void catFileContentsByAddingEchosSteps(final List<String> stagesToAddTo,
            final String nameOfReportFileToEcho) throws IOException {
        var reportFilePath = getResourceAsFile(nameOfReportFileToEcho);
        List<String> reportFileContents = Files.readAllLines(reportFilePath);
        for (String reportFileLine : reportFileContents) {
            var stage = "echo '" + reportFileLine.replace("'", "\\'") + "'";
            stagesToAddTo.add(stage);
        }
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and uses this parser twice.
     */
    @Test
    void shouldUseGroovyParserTwice() {
        List<AnalysisResult> results = getAnalysisResults(runWith2GroovyParsers(false));
        assertThat(results).hasSize(2);

        Set<String> ids = results.stream().map(AnalysisResult::getId).collect(Collectors.toSet());
        assertThat(ids).containsExactly("groovy-1", "groovy-2");
    }

    /**
     * Verifies that a warning will be logged if the user specified name and id <b>and not</b> {@code isAggregating}.
     */
    @Test
    void shouldLogWarningIfNameIsSetWhenNotAggregating() {
        Run<?, ?> build = runWith2GroovyParsers(false,
                "name: 'name'", "id: 'id'");
        List<AnalysisResult> results = getAnalysisResults(build);
        assertThat(results).hasSize(2);
        Set<String> ids = results.stream().map(AnalysisResult::getId).collect(Collectors.toSet());
        assertThat(ids).containsExactly("groovy-1", "groovy-2");

        assertThat(getConsoleLog(build))
                    .contains("Do not set id, name, or icon of recorder when multiple tools are defined");
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and uses this parser twice.
     * Publishes the results into a single result.
     */
    @Test
    void shouldUseGroovyParserTwiceAndAggregateIntoSingleResult() {
        List<AnalysisResult> results = getAnalysisResults(runWith2GroovyParsers(true));
        assertThat(results).hasSize(1);

        var result = results.get(0);
        assertThat(result.getId()).isEqualTo("analysis");
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and uses this parser twice.
     * Publishes the results into a single result that uses a different ID and name.
     */
    @Test
    void shouldUseGroovyParserTwiceAndAggregateIntoSingleResultWithCustomizableIdAndName() {
        Run<?, ?> build = runWith2GroovyParsers(true,
                "name: 'Custom Name'", "id: 'custom-id'", "icon: 'custom-icon.png'");
        var action = getResultAction(build);

        assertThat(action.getId()).isEqualTo("custom-id");
        assertThat(action.getDisplayName()).isEqualTo("Custom Name Warnings");
        assertThat(action.getIconFileName()).isEqualTo("custom-icon.png");
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and uses this parser twice.
     * Publishes the results into a single result that uses a different ID and name.
     */
    @Test
    void shouldUseGroovyParserTwiceAndAggregateIntoSingleResultWithCustomizableName() {
        Run<?, ?> build = runWith2GroovyParsers(true, "name: 'Custom Name'");
        var action = getResultAction(build);

        assertThat(action.getId()).isEqualTo("analysis");
        assertThat(action.getDisplayName()).isEqualTo("Custom Name Warnings");
    }

    private Run<?, ?> runWith2GroovyParsers(final boolean isAggregating, final String... arguments) {
        var job = createPipelineWithWorkspaceFilesWithSuffix("pep8Test.txt");
        job.setDefinition(asStage(
                "recordIssues aggregatingResults: " + isAggregating + ", tools: ["
                        + "groovyScript(parserId:'groovy-pep8', pattern: '**/*issues.txt', id: 'groovy-1'),"
                        + "groovyScript(parserId:'groovy-pep8', pattern: '**/*issues.txt', id: 'groovy-2')"
                        + "] " + join(arguments)));

        ParserConfiguration configuration = ParserConfiguration.getInstance();
        var id = "groovy-pep8";
        configuration.setParsers(Collections.singletonList(
                new GroovyParser(id, "Groovy Pep8",
                        "(.*):(\\d+):(\\d+): (\\D\\d*) (.*)",
                        toString("groovy/pep8.groovy"), "")));
        return buildSuccessfully(job);
    }

    /**
     * Runs the PMD parser on an output file that contains 16 issues. Applies file filters that select subsets of the
     * issues.
     */
    @Test
    void shouldApplyFileFilters() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("pmd-filtering.xml");

        setFilter(job, "includeFile('File.*.java')");
        assertThat(scheduleSuccessfulBuild(job).getTotalSize()).isEqualTo(16);

        setFilter(job, "excludeFile('File.*.java')");
        assertThat(scheduleSuccessfulBuild(job).getTotalSize()).isZero();

        setFilter(job, "includeFile('')");
        assertThat(scheduleSuccessfulBuild(job).getTotalSize()).isEqualTo(16);

        setFilter(job, "excludeFile('')");
        assertThat(scheduleSuccessfulBuild(job).getTotalSize()).isEqualTo(16);

        setFilter(job, "");
        assertThat(scheduleSuccessfulBuild(job).getTotalSize()).isEqualTo(16);

        verifyIncludeFile(job, "File1.java");
        verifyIncludeFile(job, "File2.java");
        verifyExcludeFile(job, "File1.java", "File2.java");
        verifyExcludeFile(job, "File2.java", "File1.java");
    }

    /**
     * Runs the PMD parser on an output file that contains 16 issues. Combines file filters that select 1 issue
     */
    @Test
    void shouldCombineFilter() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("pmd-filtering.xml");

        setFilter(job, "includeFile('File1.java'), includeCategory('Category1')");
        var result = scheduleSuccessfulBuild(job);
        assertThat(result.getTotalSize()).isEqualTo(8 + 4);

        setFilter(job,
                "includeFile('File1.java'), excludeCategory('Category1'), excludeType('Type1'), excludeNamespace('.*package1') ");
        var oneIssue = scheduleSuccessfulBuild(job);
        assertThat(oneIssue.getIssues().getFiles()).containsExactly("File1.java");
        assertThat(oneIssue.getIssues().getCategories()).containsExactly("Category2");
        assertThat(oneIssue.getIssues().getTypes()).containsExactly("Type2");
        assertThat(oneIssue.getIssues().getPackages()).containsExactly("hm.edu.hafner.package2");
    }

    private void verifyIncludeFile(final WorkflowJob job, final String fileName) {
        setFilter(job, "includeFile('" + fileName + "')");

        var result = scheduleSuccessfulBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues().getFiles()).containsExactly(fileName);
    }

    private void verifyExcludeFile(final WorkflowJob job, final String excludedFileName,
            final String expectedFileName) {
        setFilter(job, "excludeFile('" + excludedFileName + "')");

        var result = scheduleSuccessfulBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues().getFiles()).containsExactly(expectedFileName);
    }

    private void setFilter(final WorkflowJob job, final String filters) {
        var scanWithFilter = createScanForIssuesStep(new Pmd(), "issues", "filters:[%s]".formatted(filters));
        job.setDefinition(asStage(scanWithFilter, "publishIssues issues:[issues]"));
    }

    /**
     * Verifies that when publishIssues marks the build as unstable it also marks the step with WarningAction so that
     * visualizations can display the step as unstable rather than just the whole build.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-39203">Issue 39203</a>
     */
    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-39203")
    void publishIssuesShouldMarkStepWithWarningAction() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("javac.txt");
        job.setDefinition(asStage(createScanForIssuesStep(new Java(), "java"),
                "publishIssues(issues:[java], qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]])"));
        var run = (WorkflowRun) buildWithResult(job, Result.UNSTABLE);
        var publishIssuesNode = new DepthFirstScanner().findFirstMatch(run.getExecution(),
                node -> "publishIssues".equals(Objects.requireNonNull(node).getDisplayFunctionName()));
        assertThat(publishIssuesNode).isNotNull();
        var warningAction = publishIssuesNode.getPersistentAction(WarningAction.class);
        assertThat(warningAction).isNotNull();
        assertThat(warningAction.getMessage()).endsWith(
                "Some quality gates have been missed: overall result is UNSTABLE");
    }

    /**
     * Verifies that when recordIssues marks the build as unstable it also marks the step with WarningAction so that
     * visualizations can display the step as unstable rather than just the whole build.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-39203">Issue 39203</a>
     */
    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-39203")
    void recordIssuesShouldMarkStepWithWarningAction() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("javac.txt");
        job.setDefinition(asStage("recordIssues(tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8'),"
                + "qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]])"));
        var run = (WorkflowRun) buildWithResult(job, Result.UNSTABLE);
        var publishIssuesNode = new DepthFirstScanner().findFirstMatch(run.getExecution(),
                node -> "recordIssues".equals(Objects.requireNonNull(node).getDisplayFunctionName()));
        assertThat(publishIssuesNode).isNotNull();
        var warningAction = publishIssuesNode.getPersistentAction(WarningAction.class);
        assertThat(warningAction).isNotNull();
        assertThat(warningAction.getMessage()).endsWith(
                "Some quality gates have been missed: overall result is UNSTABLE");
    }

    /**
     * Verifies that parsers based on Digester are not vulnerable to an XXE attack. Previous versions allowed any user
     * with an ability to configure a job to read any file from the Jenkins Controller (even on hardened systems where
     * execution on controller is disabled).
     *
     * @see <a href="https://jenkins.io/security/advisory/2018-01-22/">Jenkins Security Advisory 2018-01-22</a>
     */
    @Test
    void showPreventXxeSecurity656() {
        var oobInUserContentLink = getUrl("userContent/oob.xml");
        var triggerLink = getUrl("triggerMe");

        var xxeFileContent = toString("testXxe-xxe.xml");
        var oobFileContent = toString("testXxe-oob.xml");

        write(oobFileContent.replace("$TARGET_URL$", triggerLink));

        var job = createPipeline();
        var adaptedXxeFileContent = xxeFileContent.replace("$OOB_LINK$", oobInUserContentLink);
        createFileInWorkspace(job, "xxe.xml", adaptedXxeFileContent);

        List<AnalysisModelParser> tools = Lists.mutable.of(new CheckStyle(), new Pmd(), new FindBugs(), new JcReport());
        for (AnalysisModelParser tool : tools) {
            job.setDefinition(asStage(
                    "def issues = scanForIssues tool: %s(pattern:'xxe.xml')".formatted(
                            tool.getSymbolName()),
                    "publishIssues issues:[issues]"));

            scheduleSuccessfulBuild(job);

            var urlHandler = getJenkins().jenkins.getExtensionList(UnprotectedRootAction.class)
                    .get(YouCannotTriggerMe.class);
            assertThat(urlHandler).isNotNull();

            assertThat(urlHandler.triggerCount)
                    .as("XXE detected for parser %s: URL has been triggered!", tool)
                    .isEqualTo(0);
        }
    }

    /**
     * Verifies that the json model for the pull request monitoring portlet is generated properly.
     */
    @Test
    void shouldGenerateJsonDataModel() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("checkstyle1.xml");

        configurePublisher(job, "checkstyle1", NO_QUALITY_GATE);
        Run<?, ?> baseline = buildSuccessfully(job);

        var action = baseline.getAction(ResultAction.class);
        var portlet = new PullRequestMonitoringPortlet(action);

        var model = portlet.getWarningsModel();
        assertThatJson(model).node("fixed").isEqualTo(0);
        assertThatJson(model).node("outstanding").isEqualTo(3);
        assertThatJson(model).node("new").node("total").isEqualTo(0);
        assertThatJson(model).node("new").node("low").isEqualTo(0);
        assertThatJson(model).node("new").node("normal").isEqualTo(0);
        assertThatJson(model).node("new").node("high").isEqualTo(0);
        assertThatJson(model).node("new").node("error").isEqualTo(0);

        assertThat(portlet.hasQualityGate()).isFalse();
    }

    private void write(final String adaptedOobFileContent) {
        try {
            var userContentDir = new File(getJenkins().jenkins.getRootDir(), "userContent");
            Files.write(new File(userContentDir, "oob.xml").toPath(), adaptedOobFileContent.getBytes());
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private String getUrl(final String relative) {
        try {
            return getJenkins().getURL() + relative;
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Extension that should not be triggered.
     *
     * @see StepsITest#showPreventXxeSecurity656
     */
    @TestExtension
    public static class YouCannotTriggerMe implements UnprotectedRootAction {
        private int triggerCount;

        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return "triggerMe";
        }

        /**
         * Should not be invoked by the test, otherwise Xxe attack is successful.
         *
         * @return the response
         */
        public HttpResponse doIndex() {
            triggerCount++;
            return HttpResponses.text("triggered");
        }
    }
}
