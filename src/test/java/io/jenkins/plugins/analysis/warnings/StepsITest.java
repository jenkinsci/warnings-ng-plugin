package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.factory.Lists;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import org.kohsuke.stapler.HttpResponse;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.UnprotectedRootAction;
import hudson.util.HttpResponses;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;
import io.jenkins.plugins.analysis.warnings.groovy.GroovyParser;
import io.jenkins.plugins.analysis.warnings.groovy.ParserConfiguration;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
@SuppressWarnings({"PMD.ExcessiveImports", "ClassDataAbstractionCoupling"})
public class StepsITest extends IntegrationTestWithJenkinsPerTest {
    /** Verifies that a {@link Tool} defines a {@link Symbol}. */
    @Test
    public void shouldProvideSymbol() {
        FindBugs findBugs = new FindBugs();

        assertThat(findBugs.getSymbolName()).isEqualTo("findBugs");
    }

    /**
     * Creates a declarative Pipeline and scans for a Gcc warning.
     */
    @Test
    public void shouldRunInDeclarativePipeline() {
        WorkflowJob job = createPipeline();

        job.setDefinition(new CpsFlowDefinition("pipeline {\n"
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
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(1);
    }

    private String createShellStep(final String script) {
        if (isWindows()) {
            return String.format("bat '%s'", script);
        }
        else {
            return String.format("sh '%s'", script);
        }
    }

    private String createCatStep(final String arguments) {
        if (isWindows()) {
            return String.format("bat 'type %s'", arguments);
        }
        else {
            return String.format("sh 'cat %s'", arguments);
        }
    }

    /**
     * Creates a JenkinsFile with parallel steps and aggregates the warnings.
     */
    @Test
    public void shouldRecordOutputOfParallelSteps() {
        WorkflowJob job = createPipeline();

        copySingleFileToAgentWorkspace(createAgent("node1"), job, "eclipse.txt", "issues.txt");
        copySingleFileToAgentWorkspace(createAgent("node2"), job, "eclipse.txt", "issues.txt");

        job.setDefinition(readJenkinsFile("parallel.jenkinsfile"));

        Run<?, ?> run = buildSuccessfully(job);
        List<ResultAction> actions = run.getActions(ResultAction.class);

        assertThat(actions).hasSize(2);

        ResultAction first;
        ResultAction second;
        if (actions.get(0).getId().equals("java-1")) {
            first = actions.get(0);
            second = actions.get(1);
        }
        else {
            first = actions.get(1);
            second = actions.get(0);
        }

        assertThat(first.getResult().getIssues()).hasSize(5);
        assertThat(second.getResult().getIssues()).hasSize(3);
    }

    /** Runs the Clang parser on an output file that contains 1 issue. */
    @Test
    public void shouldFindAllClangIssuesIfConsoleIsAnnotatedWithTimeStamps() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("issue56484.txt");
        job.setDefinition(asStage(
                createCatStep("*.txt"),
                "def issues = scanForIssues tool: clang()",
                PUBLISH_ISSUES_STEP));

        AnalysisResult result = scheduleSuccessfulBuild(job);

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
     * Parses a colored console log that also contains console notes. Verifies that the console notes will be removed
     * before the color codes. Output is from ATH test case
     * {@code WarningsNextGenerationPluginTest#should_show_maven_warnings_in_maven_project}.
     */
    @Test
    public void shouldRemoveConsoleLogNotesBeforeRemovingColorCodes() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("ath-colored.log");
        job.setDefinition(asStage(
                createCatStep("*.txt"),
                "recordIssues tool: mavenConsole()"));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(2);
        assertThat(result.getIssues().get(0))
                .hasSeverity(Severity.WARNING_NORMAL);
        assertThat(result.getIssues().get(1))
                .hasDescription("<pre><code>Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!</code></pre>")
                .hasSeverity(Severity.WARNING_NORMAL);
    }

    /** Runs the Clang parser on an output file that contains 1 issue. */
    @Test
    public void shouldFindAllJavaIssuesIfConsoleIsAnnotatedWithTimeStamps() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("issue56484-maven.txt");
        job.setDefinition(asStage(
                createCatStep("*.txt"),
                "def issues = scanForIssues tool: java()",
                PUBLISH_ISSUES_STEP));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(10);
    }

    /**
     * Runs the Eclipse parser on the console log that contains 8 issues which are decorated with console notes. The
     * output file is copied to the console log using a shell cat command.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-11675">Issue 11675</a>
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-55368">Issue 55368</a>
     */
    @Test
    public void shouldRemoveConsoleNotes() {
        assertThatConsoleNotesAreRemoved("issue11675.txt", 8);
        assertThatConsoleNotesAreRemoved("issue55368.txt", 1);
    }

    private void assertThatConsoleNotesAreRemoved(final String fileName, final int expectedSize) {
        WorkflowJob job = createPipelineWithWorkspaceFiles(fileName);
        job.setDefinition(asStage(
                createCatStep("*.txt"),
                "def issues = scanForIssues tool: eclipse()",
                PUBLISH_ISSUES_STEP));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(expectedSize);
        assertThat(result.getIssues()).hasSize(expectedSize);

        Report report = result.getIssues();
        assertThat(report.filter(issue -> "eclipse".equals(issue.getOrigin()))).hasSize(expectedSize);
        for (Issue annotation : report) {
            assertThat(annotation.getMessage()).matches("[a-zA-Z].*");
        }
    }

    /** Runs the all Java parsers on three output files: the build should report issues of all tools. */
    @Test
    public void shouldCombineIssuesOfSeveralFiles() {
        publishResultsWithIdAndName(
                "publishIssues issues:[java, eclipse, javadoc]",
                "java", "Java Warnings");
    }

    /** Runs the JavaDoc parser and uses a message filter to change the number of recorded warnings. */
    @Test
    public void shouldFilterByMessage() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("javadoc.txt");
        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), "
                        + "filters:[includeMessage('.*@link.*'), excludeMessage('.*removeSpecChangeListener.*')]")); // 4 @link and one with removeSpecChangeListener

        AnalysisResult result = scheduleSuccessfulBuild(job);
        assertThat(result.getIssues()).hasSize(3);
    }

    /** Runs the JavaDoc parser and enforces quality gates. */
    @Test
    public void shouldEnforceQualityGate() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("javadoc.txt");

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
    @Test @org.jvnet.hudson.test.Issue("JENKINS-58253")
    public void shouldFailBuildWhenFailBuildOnErrorsIsSet() {
        WorkflowJob job = createPipeline();

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8')"));

        scheduleSuccessfulBuild(job);

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), failOnError: true"));

        scheduleBuildAndAssertStatus(job, Result.FAILURE);
    }

    /** Runs the JavaDoc parser and enforces quality gates. */
    @Test @org.jvnet.hudson.test.Issue("JENKINS-58253")
    public void shouldSupportDeprecatedAttributesInRecord() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("javadoc.txt");

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), "
                        + "unstableTotalAll: 6"));
        buildWithResult(job, Result.UNSTABLE);

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), "
                        + "failedTotalAll: 6"));
        buildWithResult(job, Result.FAILURE);

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), "
                        + "unstableTotalNormal: 6"));
        buildWithResult(job, Result.UNSTABLE);

        job.setDefinition(asStage(
                "recordIssues tool: javaDoc(pattern:'**/*issues.txt', reportEncoding:'UTF-8'), "
                        + "failedTotalNormal: 6"));
        buildWithResult(job, Result.FAILURE);
    }

    /** Runs the JavaDoc parser and enforces quality gates. */
    @Test @org.jvnet.hudson.test.Issue("JENKINS-58253")
    public void shouldSupportDeprecatedAttributesInPublish() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("javadoc.txt");

        job.setDefinition(asStage(createScanForIssuesStep(new JavaDoc(), "java"),
                "publishIssues issues:[java], unstableTotalAll: 6"));
        buildWithResult(job, Result.UNSTABLE);

        job.setDefinition(asStage(createScanForIssuesStep(new JavaDoc(), "java"),
                "publishIssues issues:[java], failedTotalAll: 6"));
        buildWithResult(job, Result.FAILURE);

        job.setDefinition(asStage(createScanForIssuesStep(new JavaDoc(), "java"),
                "publishIssues issues:[java], unstableTotalNormal: 6"));
        buildWithResult(job, Result.UNSTABLE);

        job.setDefinition(asStage(createScanForIssuesStep(new JavaDoc(), "java"),
                "publishIssues issues:[java], failedTotalNormal: 6"));
        buildWithResult(job, Result.FAILURE);
    }

    /**
     * Runs the all Java parsers on three output files: the build should report issues of all tools. The results should
     * be aggregated into a new action with the specified ID. Since no name is given the default name is used.
     */
    @Test
    public void shouldProvideADefaultNameIfNoOneIsGiven() {
        publishResultsWithIdAndName(
                "publishIssues issues:[java, eclipse, javadoc], id:'my-id'",
                "my-id", "Static Analysis Warnings");
    }

    /**
     * Runs the all Java parsers on three output files: the build should report issues of all tools. The results should
     * be aggregated into a new action with the specified ID and the specified name.
     */
    @Test
    public void shouldUseSpecifiedName() {
        publishResultsWithIdAndName(
                "publishIssues issues:[java, eclipse, javadoc], id:'my-id', name:'my-name'",
                "my-id", "my-name");
    }

    private void publishResultsWithIdAndName(final String publishStep, final String expectedId,
            final String expectedName) {
        WorkflowJob job = createPipelineWithWorkspaceFiles("eclipse.txt", "javadoc.txt", "javac.txt");
        job.setDefinition(asStage(createScanForIssuesStep(new Java(), "java"),
                createScanForIssuesStep(new Eclipse(), "eclipse"),
                createScanForIssuesStep(new JavaDoc(), "javadoc"),
                publishStep));

        Run<?, ?> run = buildSuccessfully(job);

        ResultAction action = getResultAction(run);
        assertThat(action.getId()).isEqualTo(expectedId);
        assertThat(action.getDisplayName()).contains(expectedName);

        assertThatJavaIssuesArePublished(action.getResult());
    }

    private void assertThatJavaIssuesArePublished(final AnalysisResult result) {
        Report report = result.getIssues();
        assertThat(report.filter(issue -> "eclipse".equals(issue.getOrigin()))).hasSize(10); // maven eclipse detects to maven javac warnings
        assertThat(report.filter(issue -> "java".equals(issue.getOrigin()))).hasSize(2);
        assertThat(report.filter(issue -> "javadoc-warnings".equals(issue.getOrigin()))).hasSize(6);
        assertThat(report.getTools()).containsExactlyInAnyOrder("java", "javadoc-warnings", "eclipse");
        assertThat(result.getIssues()).hasSize(10 + 2 + 6);
    }

    /**
     * Runs the Eclipse parsers using the 'tools' property.
     */
    @Test
    public void shouldUseToolsProperty() {
        runEclipse("tools: [eclipse(pattern:'**/*issues.txt', reportEncoding:'UTF-8')]");
    }

    /**
     * Runs the Eclipse parsers using the 'toolProxies' property.
     */
    @Test
    public void shouldUseToolProxiesProperty() {
        runEclipse("toolProxies: [[tool: eclipse(pattern:'**/*issues.txt', reportEncoding:'UTF-8')]]");
    }

    /**
     * Runs the Eclipse parsers using the 'tool' property.
     */
    @Test
    public void shouldUseToolProperty() {
        runEclipse("tool: eclipse(pattern:'**/*issues.txt', reportEncoding:'UTF-8')");
    }

    private void runEclipse(final String property) {
        WorkflowJob job = createPipelineWithWorkspaceFiles("eclipse.txt");
        job.setDefinition(asStage("recordIssues "
                + property));

        AnalysisResult result = scheduleSuccessfulBuild(job);
        assertThat(result.getIssues()).hasSize(8);
    }

    /**
     * Runs the Java parser on an pep8 log file: the build should report no issues. A result should be available with
     * the java ID and name.
     */
    @Test
    public void shouldHaveActionWithIdAndNameWithEmptyResults() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("pep8Test.txt");
        job.setDefinition(asStage(createScanForIssuesStep(new Java(), "java"),
                "publishIssues issues:[java]"));

        Run<?, ?> run = buildSuccessfully(job);

        ResultAction action = getResultAction(run);
        assertThat(action.getId()).isEqualTo("java");
        assertThat(action.getDisplayName()).contains(Messages.Warnings_JavaParser_ParserName());

        AnalysisResult result = action.getResult();
        assertThat(result.getIssues()).isEmpty();
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and runs this parser on an
     * error log with 8 issues.
     */
    @Test
    public void shouldShowWarningsOfGroovyParser() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("pep8Test.txt");
        job.setDefinition(asStage(
                "def groovy = scanForIssues "
                        + "tool: groovyScript(parserId: 'groovy-pep8', pattern:'**/*issues.txt', reportEncoding:'UTF-8')",
                "publishIssues issues:[groovy]"));

        ParserConfiguration configuration = ParserConfiguration.getInstance();
        String id = "groovy-pep8";
        configuration.setParsers(Collections.singletonList(
                new GroovyParser(id, "Groovy Pep8",
                        "(.*):(\\d+):(\\d+): (\\D\\d*) (.*)",
                        toString("groovy/pep8.groovy"), "")));
        Run<?, ?> run = buildSuccessfully(job);

        ResultAction action = getResultAction(run);
        assertThat(action.getId()).isEqualTo(id);
        assertThat(action.getDisplayName()).contains("Groovy Pep8");

        AnalysisResult result = action.getResult();
        assertThat(result.getIssues()).hasSize(8);
        assertThat(result.getIssues().getPropertyCount(Issue::getOrigin)).containsOnly(entry(id, 8));
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and uses this parser twice.
     */
    @Test
    public void shouldUseGroovyParserTwice() {
        List<AnalysisResult> results = getAnalysisResults(runWith2GroovyParsers(false));
        assertThat(results).hasSize(2);

        Set<String> ids = results.stream().map(AnalysisResult::getId).collect(Collectors.toSet());
        assertThat(ids).containsExactly("groovy-1", "groovy-2");
    }

    /**
     * Verifies that a warning will be logged if the user specified name and id <b>and not</b> {@code isAggregating}.
     */
    @Test
    public void shouldLogWarningIfNameIsSetWhenNotAggregating() {
        List<AnalysisResult> results = getAnalysisResults(runWith2GroovyParsers(false,
                "name: 'name'", "id: 'id'"));
        assertThat(results).hasSize(2);
        for (AnalysisResult result : results) {
            assertThat(result.getInfoMessages())
                    .contains("Ignoring name='name' and id='id' when publishing non-aggregating reports");
        }

        Set<String> ids = results.stream().map(AnalysisResult::getId).collect(Collectors.toSet());
        assertThat(ids).containsExactly("groovy-1", "groovy-2");
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and uses this parser twice.
     * Publishes the results into a single result.
     */
    @Test
    public void shouldUseGroovyParserTwiceAndAggregateIntoSingleResult() {
        List<AnalysisResult> results = getAnalysisResults(runWith2GroovyParsers(true));
        assertThat(results).hasSize(1);

        AnalysisResult result = results.get(0);
        assertThat(result.getId()).isEqualTo("analysis");
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and uses this parser twice.
     * Publishes the results into a single result that uses a different ID and name.
     */
    @Test
    public void shouldUseGroovyParserTwiceAndAggregateIntoSingleResultWithCustomizableIdAndName() {
        Run<?, ?> build = runWith2GroovyParsers(true, "name: 'Custom Name'", "id: 'custom-id'");
        ResultAction action = getResultAction(build);

        assertThat(action.getId()).isEqualTo("custom-id");
        assertThat(action.getDisplayName()).isEqualTo("Custom Name Warnings");
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and uses this parser twice.
     * Publishes the results into a single result that uses a different ID and name.
     */
    @Test
    public void shouldUseGroovyParserTwiceAndAggregateIntoSingleResultWithCustomizableName() {
        Run<?, ?> build = runWith2GroovyParsers(true, "name: 'Custom Name'");
        ResultAction action = getResultAction(build);

        assertThat(action.getId()).isEqualTo("analysis");
        assertThat(action.getDisplayName()).isEqualTo("Custom Name Warnings");
    }

    private Run<?, ?> runWith2GroovyParsers(final boolean isAggregating, final String... arguments) {
        WorkflowJob job = createPipelineWithWorkspaceFiles("pep8Test.txt");
        job.setDefinition(asStage(
                "recordIssues aggregatingResults: " + isAggregating + ", tools: ["
                        + "groovyScript(parserId:'groovy-pep8', pattern: '**/*issues.txt', id: 'groovy-1'),"
                        + "groovyScript(parserId:'groovy-pep8', pattern: '**/*issues.txt', id: 'groovy-2')"
                        + "] " + join(arguments)));

        ParserConfiguration configuration = ParserConfiguration.getInstance();
        String id = "groovy-pep8";
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
    public void shouldApplyFileFilters() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("pmd-filtering.xml");

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
    public void shouldCombineFilter() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("pmd-filtering.xml");

        setFilter(job, "includeFile('File1.java'), includeCategory('Category1')");
        AnalysisResult result = scheduleSuccessfulBuild(job);
        assertThat(result.getTotalSize()).isEqualTo(8 + 4);

        setFilter(job,
                "includeFile('File1.java'), excludeCategory('Category1'), excludeType('Type1'), excludeNamespace('.*package1') ");
        AnalysisResult oneIssue = scheduleSuccessfulBuild(job);
        assertThat(oneIssue.getIssues().getFiles()).containsExactly("File1.java");
        assertThat(oneIssue.getIssues().getCategories()).containsExactly("Category2");
        assertThat(oneIssue.getIssues().getTypes()).containsExactly("Type2");
        assertThat(oneIssue.getIssues().getPackages()).containsExactly("hm.edu.hafner.package2");
    }

    private void verifyIncludeFile(final WorkflowJob job, final String fileName) {
        setFilter(job, "includeFile('" + fileName + "')");

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues().getFiles()).containsExactly(fileName);
    }

    private void verifyExcludeFile(final WorkflowJob job, final String excludedFileName,
            final String expectedFileName) {
        setFilter(job, "excludeFile('" + excludedFileName + "')");

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues().getFiles()).containsExactly(expectedFileName);
    }

    private void setFilter(final WorkflowJob job, final String filters) {
        String scanWithFilter = createScanForIssuesStep(new Pmd(), "issues", String.format("filters:[%s]", filters));
        job.setDefinition(asStage(scanWithFilter, "publishIssues issues:[issues]"));
    }

    /**
     * Creates a reference job and starts the analysis for this job. Then another job is created that uses the first one
     * as reference. Verifies that the association is correctly stored.
     */
    @Test
    public void shouldUseOtherJobAsReference() {
        WorkflowJob reference = createPipeline("reference");
        copyMultipleFilesToWorkspaceWithSuffix(reference, "java-start.txt");
        reference.setDefinition(createPipelineScriptWithScanAndPublishSteps(new Java()));

        AnalysisResult referenceResult = scheduleSuccessfulBuild(reference);

        assertThat(referenceResult.getTotalSize()).isEqualTo(2);
        assertThat(referenceResult.getIssues()).hasSize(2);
        assertThat(referenceResult.getReferenceBuild()).isEmpty();

        WorkflowJob job = createPipelineWithWorkspaceFiles("java-start.txt");
        job.setDefinition(asStage(createScanForIssuesStep(new Java()),
                "publishIssues issues:[issues], referenceJobName:'reference'"));

        AnalysisResult result = scheduleSuccessfulBuild(reference);

        assertThat(result.getTotalSize()).isEqualTo(2);
        assertThat(result.getIssues()).hasSize(2);
        assertThat(result.getReferenceBuild()).hasValue(referenceResult.getOwner());

        // TODO: add verification for io.jenkins.plugins.analysis.core.model.IssueDifference
    }

    /**
     * Verifies that when publishIssues marks the build as unstable it also marks the step with
     * WarningAction so that visualizations can display the step as unstable rather than just
     * the whole build.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-39203">Issue 39203</a>
     */
    @Test @org.jvnet.hudson.test.Issue("JENKINS-39203")
    public void publishIssuesShouldMarkStepWithWarningAction() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("javac.txt");
        job.setDefinition(asStage(createScanForIssuesStep(new Java(), "java"),
                "publishIssues(issues:[java], qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]])"));
        WorkflowRun run = (WorkflowRun)buildWithResult(job, Result.UNSTABLE);
        FlowNode publishIssuesNode = new DepthFirstScanner().findFirstMatch(run.getExecution(),
                node -> node.getDisplayFunctionName().equals("publishIssues"));
        assertThat(publishIssuesNode).isNotNull();
        WarningAction warningAction = publishIssuesNode.getPersistentAction(WarningAction.class);
        assertThat(warningAction).isNotNull();
        assertThat(warningAction.getMessage()).isEqualTo("Some quality gates have been missed: overall result is UNSTABLE");
    }

    /**
     * Verifies that when recordIssues marks the build as unstable it also marks the step with
     * WarningAction so that visualizations can display the step as unstable rather than just
     * the whole build.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-39203">Issue 39203</a>
     */
    @Test @org.jvnet.hudson.test.Issue("JENKINS-39203")
    public void recordIssuesShouldMarkStepWithWarningAction() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("javac.txt");
        job.setDefinition(asStage("recordIssues(tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8'),"
                + "qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]])"));
        WorkflowRun run = (WorkflowRun)buildWithResult(job, Result.UNSTABLE);
        FlowNode publishIssuesNode = new DepthFirstScanner().findFirstMatch(run.getExecution(),
                node -> node.getDisplayFunctionName().equals("recordIssues"));
        assertThat(publishIssuesNode).isNotNull();
        WarningAction warningAction = publishIssuesNode.getPersistentAction(WarningAction.class);
        assertThat(warningAction).isNotNull();
        assertThat(warningAction.getMessage()).isEqualTo("Some quality gates have been missed: overall result is UNSTABLE");
    }

    /**
     * Verifies that parsers based on Digester are not vulnerable to an XXE attack. Previous versions allowed any user
     * with an ability to configure a job to read any file from the Jenkins Master (even on hardened systems where
     * execution on master is disabled).
     *
     * @see <a href="https://jenkins.io/security/advisory/2018-01-22/">Jenkins Security Advisory 2018-01-22</a>
     */
    @Test
    public void showPreventXxeSecurity656() {
        String oobInUserContentLink = getUrl("userContent/oob.xml");
        String triggerLink = getUrl("triggerMe");

        String xxeFileContent = toString("testXxe-xxe.xml");
        String oobFileContent = toString("testXxe-oob.xml");

        write(oobFileContent.replace("$TARGET_URL$", triggerLink));

        WorkflowJob job = createPipeline();
        String adaptedXxeFileContent = xxeFileContent.replace("$OOB_LINK$", oobInUserContentLink);
        createFileInWorkspace(job, "xxe.xml", adaptedXxeFileContent);

        List<ReportScanningTool> tools = Lists.mutable.of(new CheckStyle(), new Pmd(), new FindBugs(), new JcReport());
        for (ReportScanningTool tool : tools) {
            job.setDefinition(asStage(
                    String.format("def issues = scanForIssues tool: %s(pattern:'xxe.xml')",
                            tool.getSymbolName()),
                    "publishIssues issues:[issues]"));

            scheduleSuccessfulBuild(job);

            YouCannotTriggerMe urlHandler = getJenkins().jenkins.getExtensionList(UnprotectedRootAction.class)
                    .get(YouCannotTriggerMe.class);
            assertThat(urlHandler).isNotNull();

            assertThat(urlHandler.triggerCount)
                    .as("XXE detected for parser %s: URL has been triggered!", tool)
                    .isEqualTo(0);
        }
    }

    private void write(final String adaptedOobFileContent) {
        try {
            File userContentDir = new File(getJenkins().jenkins.getRootDir(), "userContent");
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
        private int triggerCount = 0;

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
