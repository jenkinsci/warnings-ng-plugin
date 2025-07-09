package io.jenkins.plugins.analysis.core.testutil;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Tag;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.util.IntegrationTest;

import static org.assertj.core.api.Assertions.*;

/**
 * Base class for integration tests in Jenkins.
 *
 * @author Ullrich Hafner
 */
@Tag("IntegrationTest")
@SuppressWarnings({"ClassDataAbstractionCoupling", "ClassFanOutComplexity", "SameParameterValue", "PMD.SystemPrintln", "PMD.CouplingBetweenObjects"})
public abstract class WarningsIntegrationTest extends IntegrationTest {
    /** Issue log files will be renamed to mach this pattern. */
    private static final String FILE_NAME_PATTERN = "%s-issues.txt";

    /** Step to publish a set of issues. Uses default options for all options. */
    protected static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";
    /** Step to record the reference builds. */
    protected static final String REFERENCE_BUILD = "discoverReferenceBuild()";

    /**
     * Copies the specified files to the workspace using a generated file name that uses the same suffix. So the pattern
     * in the static analysis configuration can use the same fixed regular expression for all types of tools.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     *
     * @see #FILE_NAME_PATTERN
     */
    protected void copyMultipleFilesToWorkspaceWithSuffix(final TopLevelItem job, final String... fileNames) {
        copyWorkspaceFiles(job, fileNames, this::createWorkspaceFileName);
    }

    /**
     * Asserts that the severity distribution or the specified report has been correctly created.
     *
     * @param report
     *         the report
     * @param expectedSizeError
     *         expected number of errors
     * @param expectedSizeHigh
     *         expected number of warnings with severity high
     * @param expectedSizeNormal
     *         expected number of warnings with severity normal
     * @param expectedSizeLow
     *         expected number of warnings with severity low
     */
    protected void assertThatReportHasSeverities(final Report report, final int expectedSizeError,
            final int expectedSizeHigh, final int expectedSizeNormal, final int expectedSizeLow) {
        assertThat(report.getSizeOf(Severity.ERROR)).isEqualTo(expectedSizeError);
        assertThat(report.getSizeOf(Severity.WARNING_HIGH)).isEqualTo(expectedSizeHigh);
        assertThat(report.getSizeOf(Severity.WARNING_NORMAL)).isEqualTo(expectedSizeNormal);
        assertThat(report.getSizeOf(Severity.WARNING_LOW)).isEqualTo(expectedSizeLow);
    }

    protected String createJavaWarning(final String fileName, final int lineNumber) {
        return "[WARNING] %s:[%d,42] [deprecation] path.AClass in path has been deprecated%n".formatted(fileName,
                lineNumber);
    }

    /**
     * Creates a pre-defined filename for a workspace file.
     *
     * @param fileNamePrefix
     *         prefix of the filename
     *
     * @return the whole file name of the workspace file
     */
    protected String createWorkspaceFileName(final String fileNamePrefix) {
        return FILE_NAME_PATTERN.formatted(FilenameUtils.getBaseName(fileNamePrefix));
    }

    /**
     * Creates a new {@link MavenModuleSet maven job}. The job will get a generated name.
     *
     * @return the created job
     */
    protected MavenModuleSet createMavenJob() {
        return createProject(MavenModuleSet.class);
    }

    /**
     * Creates a composite pipeline step that consists of a scanner and publisher.
     *
     * @param tool
     *         the class of the tool to use
     *
     * @return the pipeline script
     */
    protected CpsFlowDefinition createPipelineScriptWithScanAndPublishSteps(final AnalysisModelParser tool) {
        return asStage(REFERENCE_BUILD, createScanForIssuesStep(tool), PUBLISH_ISSUES_STEP);
    }

    /**
     * Creates a pipeline step that scans for issues of the specified tool.
     *
     * @param tool
     *         the class of the tool to use
     *
     * @return the pipeline step
     */
    protected String createScanForIssuesStep(final AnalysisModelParser tool) {
        return createScanForIssuesStep(tool, "issues");
    }

    /**
     * Creates a pipeline step that scans for issues of the specified tool.
     *
     * @param tool
     *         the class of the tool to use
     * @param issuesName
     *         the name of the scanner result variable
     * @param arguments
     *         additional parameters to the {@link AnalysisModelParser}
     *
     * @return the pipeline step
     */
    protected String createScanForIssuesStep(final AnalysisModelParser tool, final String issuesName,
            final String... arguments) {
        return "def %s = scanForIssues tool: %s(pattern:'**/*issues.txt', reportEncoding:'UTF-8')%s".formatted(
                issuesName, tool.getSymbolName(), join(arguments));
    }

    /**
     * Creates a pipeline step that records issues of the specified tool.
     *
     * @param tool
     *         the class of the tool to use
     *
     * @return the pipeline step
     */
    protected String createRecordIssuesStep(final AnalysisModelParser tool) {
        return "recordIssues(tools: [%s(pattern: '**/*issues.txt', reportEncoding:'UTF-8')])".formatted(
                tool.getSymbolName());
    }

    /**
     * Enables an {@link Eclipse} recorder for the specified project.
     *
     * @param project
     *         the project to add the recorder to
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableEclipseWarnings(final AbstractProject<?, ?> project) {
        return enableGenericWarnings(project, new Eclipse());
    }

    /**
     * Enables an {@link Eclipse} recorder for the specified project.
     *
     * @param project
     *         the project to add the recorder to
     * @param configuration
     *         configures the new recorder
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableEclipseWarnings(final FreeStyleProject project,
            final Consumer<IssuesRecorder> configuration) {
        return enableGenericWarnings(project, configuration, configurePattern(new Eclipse()));
    }

    /**
     * Enables a {@link CheckStyle} recorder for the specified project.
     *
     * @param project
     *         the project to add the recorder to
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableCheckStyleWarnings(final AbstractProject<?, ?> project) {
        var tool = new CheckStyle();
        tool.setReportEncoding("UTF-8");
        return enableGenericWarnings(project, tool);
    }

    /**
     * Creates a new tool that uses the specified pattern.
     *
     * @param tool
     *         the tool to add a default pattern
     * @param pattern
     *         the pattern to search for
     *
     * @return the created tool
     */
    protected ReportScanningTool createTool(final ReportScanningTool tool, final String pattern) {
        tool.setPattern(pattern);
        return tool;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param recorderConfiguration
     *         configuration of the recorder
     * @param tool
     *         the tool configuration to use
     * @param additionalTools
     *         the additional tool configurations to use
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableWarnings(final AbstractProject<?, ?> job,
            final Consumer<IssuesRecorder> recorderConfiguration,
            final ReportScanningTool tool, final ReportScanningTool... additionalTools) {
        var recorder = enableWarnings(job, tool, additionalTools);
        recorderConfiguration.accept(recorder);
        return recorder;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param configuration
     *         configuration of the recorder
     * @param tool
     *         the tool to scan the warnings
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableGenericWarnings(final AbstractProject<?, ?> job,
            final Consumer<IssuesRecorder> configuration, final ReportScanningTool tool) {
        configurePattern(tool);

        return enableWarnings(job, configuration, tool);
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param tool
     *         the tool to scan the warnings
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableGenericWarnings(final AbstractProject<?, ?> job, final ReportScanningTool tool) {
        configurePattern(tool);
        return enableWarnings(job, tool);
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param tool
     *         the tool to use
     * @param additionalTools
     *         the tool configurations to use
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableWarnings(final AbstractProject<?, ?> job,
            final Tool tool, final Tool... additionalTools) {
        var publisher = new IssuesRecorder();
        publisher.setTools(tool, additionalTools);
        job.getPublishersList().add(publisher);
        return publisher;
    }

    /**
     * Registers a default pattern for the specified tool.
     *
     * @param tool
     *         the tool to add a default pattern
     *
     * @return the changed tool
     */
    protected ReportScanningTool configurePattern(final ReportScanningTool tool) {
        return createTool(tool, "**/*issues.txt");
    }

    /**
     * Returns the issue recorder instance for the specified job.
     *
     * @param job
     *         the job to get the recorder for
     *
     * @return the issue recorder
     */
    protected IssuesRecorder getRecorder(final AbstractProject<?, ?> job) {
        DescribableList<Publisher, Descriptor<Publisher>> publishers = job.getPublishersList();
        for (Publisher publisher : publishers) {
            if (publisher instanceof IssuesRecorder recorder) {
                return recorder;
            }
        }
        throw new AssertionError("No instance of IssuesRecorder found for job " + job);
    }

    /**
     * Schedules a build for the specified job and waits for the job to finish. After the build has been finished the
     * builds result is checked to be equals to {@link Result#SUCCESS}.
     *
     * @param project
     *         the project to schedule
     *
     * @return the finished build with status {@link Result#SUCCESS}
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected MatrixBuild buildSuccessfully(final MatrixProject project) {
        try {
            var matrixBuild = project.scheduleBuild2(0).get();
            getJenkins().assertBuildStatus(Result.SUCCESS, matrixBuild);
            return matrixBuild;
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    /**
     * Schedules a build for the specified job and waits for the job to finish. After the build has been finished the
     * builds result is checked to be equals to {@link Result#SUCCESS}.
     *
     * @param job
     *         the job to schedule
     *
     * @return the created {@link AnalysisResult}
     */
    protected AnalysisResult scheduleSuccessfulBuild(final ParameterizedJob<?, ?> job) {
        Run<?, ?> run = buildSuccessfully(job);

        var action = getResultAction(run);
        System.out.println("------------------------------------- Infos ------------------------------------");
        action.getResult().getInfoMessages().forEach(System.out::println);
        System.out.println("------------------------------------ Errors ------------------------------------");
        action.getResult().getErrorMessages().forEach(System.out::println);
        System.out.println("--------------------------------------------------------------------------------");

        return action.getResult();
    }

    /**
     * Schedules a build for the specified job and waits for the job to finish. After the build has been finished the
     * builds result is checked to be equals to {@code expectedResult}.
     *
     * @param job
     *         the job to schedule
     * @param expectedResult
     *         the expected result for the build
     *
     * @return the finished build with status {@code expectedResult}
     */
    protected AnalysisResult scheduleBuildAndAssertStatus(final ParameterizedJob<?, ?> job,
            final Result expectedResult) {
        return getAnalysisResult(buildWithResult(job, expectedResult));
    }

    /**
     * Schedules a build for the specified job and waits for the job to finish. After the build has been finished the
     * builds result is checked to be equals to {@code expectedResult}.
     *
     * @param job
     *         the job to schedule
     * @param expectedResult
     *         the expected result for the build
     * @param assertions
     *         additional assertions for the result
     *
     * @return the finished build with status {@code expectedResult}
     */
    protected AnalysisResult scheduleBuildAndAssertStatus(final ParameterizedJob<?, ?> job, final Result expectedResult,
            final Consumer<AnalysisResult> assertions) {
        Run<?, ?> build = buildWithResult(job, expectedResult);
        var result = getAnalysisResult(build);
        assertions.accept(result);
        return result;
    }

    /**
     * Returns the {@link ResultAction} for the specified run. Note that this method does only return the first match,
     * even if a test registered multiple actions.
     *
     * @param build
     *         the build
     *
     * @return the action of the specified build
     */
    protected ResultAction getResultAction(final Run<?, ?> build) {
        var action = build.getAction(ResultAction.class);
        assertThat(action).as("No ResultAction found in run %s", build).isNotNull();
        return action;
    }

    /**
     * Returns the {@link ResultAction} for the specified job. Note that this method does only return the first match,
     * even if a test registered multiple actions.
     *
     * @param job
     *         the job
     *
     * @return the action of the specified build
     */
    protected ResultAction getResultAction(final Job<?, ?> job) {
        Run<?, ?> build = job.getLastCompletedBuild();
        assertThat(build).as("No completed build found for job %s", job).isNotNull();

        var action = build.getAction(ResultAction.class);
        assertThat(action).as("No ResultAction found in run %s", build).isNotNull();

        return action;
    }

    /**
     * Returns the created {@link AnalysisResult analysis result} of a build.
     *
     * @param build
     *         the build that has the action attached
     *
     * @return the created result
     */
    @SuppressWarnings("PMD.SystemPrintln")
    protected AnalysisResult getAnalysisResult(final Run<?, ?> build) {
        List<AnalysisResult> analysisResults = getAnalysisResults(build);

        printConsoleLog(build);

        assertThat(analysisResults).hasSize(1);

        var result = analysisResults.get(0);
        System.out.println("----- Error Messages -----");
        result.getErrorMessages().forEach(System.out::println);
        System.out.println("----- Info Messages -----");
        result.getInfoMessages().forEach(System.out::println);
        System.out.println("-------------------------");

        return result;
    }

    /**
     * Returns the created {@link AnalysisResult analysis results} of a build.
     *
     * @param build
     *         the run that has the actions attached
     *
     * @return the created results
     */
    protected List<AnalysisResult> getAnalysisResults(final Run<?, ?> build) {
        List<ResultAction> actions = build.getActions(ResultAction.class);

        return actions.stream().map(ResultAction::getResult).collect(Collectors.toList());
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job} and copies the specified resources to the workspace folder
     * using the unified suffix. The job will get a generated name.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the created job
     */
    protected FreeStyleProject createFreeStyleProjectWithWorkspaceFilesWithSuffix(final String... fileNames) {
        var job = createFreeStyleProject();
        copyMultipleFilesToWorkspaceWithSuffix(job, fileNames);
        return job;
    }

    /**
     * Creates an empty pipeline job and populates the workspace of that job with copies of the specified files. In
     * order to simplify the scanner pattern, all files follow the filename pattern in
     * {@link #createWorkspaceFileName(String)}.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the pipeline job
     */
    protected WorkflowJob createPipelineWithWorkspaceFilesWithSuffix(final String... fileNames) {
        var job = createPipeline();
        copyMultipleFilesToWorkspaceWithSuffix(job, fileNames);
        return job;
    }

    /**
     * Returns the console log as a String.
     *
     * @param result
     *         the result to get the log for
     *
     * @return the console log
     */
    protected String getConsoleLog(final AnalysisResult result) {
        return getConsoleLog(result.getOwner());
    }

    /**
     * Sets the specified environment variables in Jenkins global configuration.
     *
     * @param vars
     *         the variables to set
     * @see #env(String, String)
     */
    protected void setEnvironmentVariables(final Entry... vars) {
        try {
            getJenkins().getInstance().getNodeProperties().replaceBy(
                    Set.of(new EnvironmentVariablesNodeProperty(vars)));
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    /**
     * Creates a new environment variable.
     *
     * @param key
     *         the key
     * @param value
     *         the value
     *
     * @return the environment variable
     * @see #setEnvironmentVariables(Entry...)
     */
    protected Entry env(final String key, final String value) {
        return new Entry(key, value);
    }
}
