package io.jenkins.plugins.analysis.core.testutil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.JSONWebResponse;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import edu.hm.hafner.util.ResourceTest;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.FilePath;
import hudson.Functions;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Slave;
import hudson.model.TopLevelItem;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import hudson.tasks.BatchFile;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Shell;
import hudson.util.DescribableList;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import jenkins.security.s2m.AdminWhitelistRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

/**
 * Base class for integration tests in Jenkins.
 *
 * @author Ullrich Hafner
 */
@Tag("IntegrationTest")
@SuppressWarnings({"ClassDataAbstractionCoupling", "ClassFanOutComplexity", "SameParameterValue", "PMD.SystemPrintln", "PMD.GodClass", "PMD.ExcessiveClassLength", "PMD.ExcessiveImports", "PMD.CouplingBetweenObjects", "PMD.CyclomaticComplexity"})
public abstract class IntegrationTest extends ResourceTest {
    /** Issue log files will be renamed to mach this pattern. */
    private static final String FILE_NAME_PATTERN = "%s-issues.txt";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    /** Step to publish a set of issues. Uses defaults for all options. */
    protected static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";
    private static final String WINDOWS_FILE_ACCESS_READ_ONLY = "RX";
    private static final String WINDOWS_FILE_DENY = "/deny";

    /** Determines whether JavaScript is enabled in the {@link WebClient}. */
    public enum JavaScriptSupport {
        /** JavaScript is disabled. */
        JS_ENABLED,
        /** JavaScript is enabled. */
        JS_DISABLED
    }

    /**
     * Returns the Jenkins rule to manage the Jenkins instance.
     *
     * @return Jenkins rule
     */
    protected abstract JenkinsRule getJenkins();

    /**
     * Returns a {@link WebClient} to access the HTML pages of Jenkins.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     *
     * @return the web client to use
     */
    protected abstract WebClient getWebClient(JavaScriptSupport javaScriptSupport);

    static WebClient create(final JenkinsRule jenkins, final boolean isJavaScriptEnabled) {
        WebClient webClient = jenkins.createWebClient();
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.SEVERE);
        webClient.setIncorrectnessListener((s, o) -> {
        });

        webClient.setJavaScriptEnabled(isJavaScriptEnabled);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getCookieManager().setCookiesEnabled(isJavaScriptEnabled);
        webClient.getOptions().setCssEnabled(isJavaScriptEnabled);

        webClient.getOptions().setDownloadImages(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);

        return webClient;
    }

    /**
     * Creates a file with the specified content in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the files to create
     * @param content
     *         the content of the file
     */
    protected void createFileInWorkspace(final TopLevelItem job, final String fileName, final String content) {
        try {
            FilePath workspace = getWorkspace(job);

            FilePath child = workspace.child(fileName);
            child.copyFrom(new ByteArrayInputStream(content.getBytes(UTF_8)));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

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
     * Copies the specified files to the workspace. The copied files will have the same file name in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     */
    protected void copyMultipleFilesToWorkspace(final TopLevelItem job, final String... fileNames) {
        copyWorkspaceFiles(job, fileNames, file -> Paths.get(file).getFileName().toString());
    }

    /**
     * Copies the specified file to the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the file to copy
     */
    protected void copySingleFileToWorkspace(final TopLevelItem job, final String fileName) {
        FilePath workspace = getWorkspace(job);

        copySingleFileToWorkspace(workspace, fileName, fileName);
    }

    /**
     * Copies the specified files to the workspace. Uses the specified new file name in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param from
     *         the file to copy
     * @param to
     *         the file name in the workspace
     */
    protected void copySingleFileToWorkspace(final TopLevelItem job, final String from, final String to) {
        FilePath workspace = getWorkspace(job);

        copySingleFileToWorkspace(workspace, from, to);
    }

    /**
     * Copies the specified directory recursively to the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param directory
     *         the directory to copy
     */
    protected void copyDirectoryToWorkspace(final TopLevelItem job, final String directory) {
        try {
            URL resource = getTestResourceClass().getResource(directory);
            assertThat(resource).as("No such file: %s", directory).isNotNull();
            FilePath destination = new FilePath(new File(resource.getFile()));
            assertThat(destination.exists()).as("Directory %s does not exist", resource.getFile()).isTrue();
            destination.copyRecursiveTo(getWorkspace(job));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Copies the specified files to the workspace. The file names of the copied files will be determined by the
     * specified mapper.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     * @param fileNameMapper
     *         maps input file names to output file names
     */
    protected void copyWorkspaceFiles(final TopLevelItem job, final String[] fileNames,
            final Function<String, String> fileNameMapper) {
        Arrays.stream(fileNames)
                .forEach(fileName -> copySingleFileToWorkspace(job, fileName, fileNameMapper.apply(fileName)));
    }

    /**
     * Returns the workspace for the specified job.
     *
     * @param job
     *         the job to get the workspace for
     *
     * @return the workspace
     */
    protected FilePath getWorkspace(final TopLevelItem job) {
        FilePath workspace = getJenkins().jenkins.getWorkspaceFor(job);
        assertThat(workspace).isNotNull();
        return workspace;
    }

    private void copySingleFileToWorkspace(final FilePath workspace, final String from, final String to) {
        try {
            workspace.child(to).copyFrom(asInputStream(from));
            System.out.format("Copying file '%s' as workspace file '%s'%n", from, to);
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates an {@link DumbSlave agent} with the specified label.
     *
     * @param label
     *         the label of the agent
     *
     * @return the agent
     */
    @SuppressWarnings("illegalcatch")
    protected Slave createAgent(final String label) {
        try {
            return getJenkins().createOnlineSlave(new LabelAtom(label));
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates an {@link DumbSlave agent} with the specified label. Master - agent security will be enabled.
     *
     * @param label
     *         the label of the agent
     *
     * @return the agent
     */
    protected Slave createAgentWithEnabledSecurity(final String label) {
        try {
            Slave agent = createAgent(label);

            FilePath child = getJenkins().getInstance().getRootPath().child("secrets/filepath-filters.d/30-default.conf");
            child.delete();
            child.write("", "ISO_8859_1");

            Objects.requireNonNull(getJenkins().jenkins.getInjector())
                    .getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false);
            getJenkins().jenkins.save();
            return agent;
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Copies the specified files to the workspace of the specified agent. Uses the specified new file name in the
     * workspace.
     *
     * @param agent
     *         the agent to get the workspace for
     * @param job
     *         the job to get the workspace for
     * @param from
     *         the file to copy
     * @param to
     *         the file name in the workspace
     */
    protected void copySingleFileToAgentWorkspace(final Slave agent, final TopLevelItem job,
            final String from, final String to) {
        FilePath workspace = getAgentWorkspace(agent, job);

        copySingleFileToWorkspace(workspace, from, to);
    }

    private FilePath getAgentWorkspace(final Slave agent, final TopLevelItem job) {
        FilePath workspace = agent.getWorkspaceFor(job);
        assertThat(workspace).isNotNull();
        return workspace;
    }

    /**
     * Creates the specified file with the given content to the workspace of the specified agent.
     *
     * @param agent
     *         the agent to get the workspace for
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the file name
     * @param content
     *         the content to write
     */
    protected void createFileInAgentWorkspace(final Slave agent, final TopLevelItem job, final String fileName,
            final String content) {
        try {
            FilePath workspace = getAgentWorkspace(agent, job);
            FilePath child = workspace.child(fileName);
            child.copyFrom(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
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
        return String.format(FILE_NAME_PATTERN, FilenameUtils.getBaseName(fileNamePrefix));
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job}. The job will get a generated name.
     *
     * @return the created job
     */
    protected FreeStyleProject createFreeStyleProject() {
        return createProject(FreeStyleProject.class);
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job} and copies the specified resources to the workspace folder.
     * The job will get a generated name.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the created job
     */
    protected FreeStyleProject createFreeStyleProjectWithWorkspaceFiles(final String... fileNames) {
        FreeStyleProject job = createFreeStyleProject();
        copyMultipleFilesToWorkspaceWithSuffix(job, fileNames);
        return job;
    }

    /**
     * Creates a new job of the specified type. The job will get a generated name.
     *
     * @param type
     *         type of the job
     * @param <T>
     *         the project type
     *
     * @return the created job
     */
    protected <T extends TopLevelItem> T createProject(final Class<T> type) {
        try {
            return getJenkins().createProject(type);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new job of the specified type.
     *
     * @param type
     *         type of the job
     * @param name
     *         the name of the job
     * @param <T>
     *         the project type
     *
     * @return the created job
     */
    protected <T extends TopLevelItem> T createProject(final Class<T> type, final String name) {
        try {
            return getJenkins().createProject(type, name);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
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
    protected CpsFlowDefinition createPipelineScriptWithScanAndPublishSteps(final ReportScanningTool tool) {
        return asStage(createScanForIssuesStep(tool), PUBLISH_ISSUES_STEP);
    }

    /**
     * Creates a pipeline step that scans for issues of the specified tool.
     *
     * @param tool
     *         the class of the tool to use
     *
     * @return the pipeline step
     */
    protected String createScanForIssuesStep(final ReportScanningTool tool) {
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
     *         additional parameters to the {@link ReportScanningTool}
     *
     * @return the pipeline step
     */
    protected String createScanForIssuesStep(final ReportScanningTool tool, final String issuesName,
            final String... arguments) {
        return String.format(
                "def %s = scanForIssues tool: %s(pattern:'**/*issues.txt', reportEncoding:'UTF-8')%s",
                issuesName, tool.getSymbolName(), join(arguments));
    }

    /**
     * Wraps the specified steps into a stage.
     *
     * @param steps
     *         the steps of the stage
     *
     * @return the pipeline script
     */
    @SuppressWarnings({"UseOfSystemOutOrSystemErr", "PMD.ConsecutiveLiteralAppends"})
    protected CpsFlowDefinition asStage(final String... steps) {
        StringBuilder script = new StringBuilder(1024);
        script.append("node {\n");
        script.append("  stage ('Integration Test') {\n");
        for (String step : steps) {
            script.append("    ");
            script.append(step);
            script.append('\n');
        }
        script.append("  }\n");
        script.append("}\n");

        String jenkinsFile = script.toString();
        logJenkinsFile(jenkinsFile);
        return new CpsFlowDefinition(jenkinsFile, true);
    }

    /**
     * Creates an empty pipeline job and populates the workspace of that job with copies of the specified files. In
     * order to simplify the scanner pattern, all files follow the filename pattern in {@link
     * IntegrationTest#createWorkspaceFileName(String)}.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the pipeline job
     */
    protected WorkflowJob createPipelineWithWorkspaceFiles(final String... fileNames) {
        WorkflowJob job = createPipeline();
        copyMultipleFilesToWorkspaceWithSuffix(job, fileNames);
        return job;
    }

    /**
     * Creates an empty pipeline job.
     *
     * @return the pipeline job
     */
    protected WorkflowJob createPipeline() {
        return createProject(WorkflowJob.class);
    }

    /**
     * Creates an empty pipeline job with the specified name.
     *
     * @param name
     *         the name of the job
     *
     * @return the pipeline job
     */
    protected WorkflowJob createPipeline(final String name) {
        return createProject(WorkflowJob.class, name);
    }

    /**
     * Reads a JenkinsFile (i.e. a {@link FlowDefinition}) from the specified file.
     *
     * @param fileName
     *         path to the JenkinsFile
     *
     * @return the JenkinsFile as {@link FlowDefinition} instance
     */
    protected FlowDefinition readJenkinsFile(final String fileName) {
        String script = toString(fileName);
        logJenkinsFile(script);
        return new CpsFlowDefinition(script, true);
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
        CheckStyle tool = new CheckStyle();
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
        IssuesRecorder recorder = enableWarnings(job, tool, additionalTools);
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
     *         the tool tool to use
     * @param additionalTools
     *         the tool configurations to use
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableWarnings(final AbstractProject<?, ?> job,
            final Tool tool, final Tool... additionalTools) {
        IssuesRecorder publisher = new IssuesRecorder();
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
            if (publisher instanceof IssuesRecorder) {
                return (IssuesRecorder) publisher;
            }
        }
        throw new AssertionError("No instance of IssuesRecorder found for job " + job);
    }

    /**
     * Schedules a build for the specified job and waits for the job to finish. After the build has been finished the
     * builds result is checked to be equals to {@link Result#SUCCESS}.
     *
     * @param job
     *         the job to schedule
     *
     * @return the finished build with status {@link Result#SUCCESS}
     */
    protected Run<?, ?> buildSuccessfully(final ParameterizedJob<?, ?> job) {
        return buildWithResult(job, Result.SUCCESS);
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
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    protected Run<?, ?> buildWithResult(final ParameterizedJob<?, ?> job, final Result expectedResult) {
        try {
            return getJenkins().assertBuildStatus(expectedResult,
                    Objects.requireNonNull(job.scheduleBuild2(0, new Action[0])));
        }
        catch (Exception e) {
            throw new AssertionError(e);
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
    @SuppressWarnings("illegalcatch")
    protected AnalysisResult scheduleSuccessfulBuild(final ParameterizedJob<?, ?> job) {
        try {
            Run<?, ?> run = buildSuccessfully(job);

            ResultAction action = getResultAction(run);

            System.out.println("------------------------------------- Infos ------------------------------------");
            System.out.println(action.getResult().getInfoMessages());
            System.out.println("------------------------------------ Errors ------------------------------------");
            System.out.println(action.getResult().getErrorMessages());
            System.out.println("--------------------------------------------------------------------------------");

            return action.getResult();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
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
        AnalysisResult result = getAnalysisResult(build);
        assertions.accept(result);
        return result;
    }

    /**
     * Prints the content of the JenkinsFile to StdOut.
     *
     * @param script
     *         the script
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private void logJenkinsFile(final String script) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(script);
        System.out.println("----------------------------------------------------------------------");
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
        ResultAction action = build.getAction(ResultAction.class);
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
    protected AnalysisResult getAnalysisResult(final Run<?, ?> build) {
        List<AnalysisResult> analysisResults = getAnalysisResults(build);

        assertThat(analysisResults).hasSize(1);

        AnalysisResult result = analysisResults.get(0);
        System.out.println("----- Console Log -----");
        System.out.println(getConsoleLog(result));
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
     * Clicks the specified DOM element and returns the HTML page content of the page that is the target of the link.
     *
     * @param element
     *         the element that receives the click event
     *
     * @return the HTML page
     */
    protected HtmlPage clickOnLink(final DomElement element) {
        try {
            return element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the HTML page content of the specified URL for a given job.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     * @param job
     *         the job that owns the URL
     * @param relativeUrl
     *         the relative URL within the job
     *
     * @return the HTML page
     */
    protected HtmlPage getWebPage(final JavaScriptSupport javaScriptSupport, final Item job, final String relativeUrl) {
        try {
            return getWebClient(javaScriptSupport).getPage(job, relativeUrl);
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the HTML page content of the specified job.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     * @param job
     *         the job to show the page for
     *
     * @return the HTML page
     */
    protected HtmlPage getWebPage(final JavaScriptSupport javaScriptSupport, final Item job) {
        return getWebPage(javaScriptSupport, job, StringUtils.EMPTY);
    }

    /**
     * Returns the HTML page content of the specified build.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     * @param build
     *         the build to show the page for
     *
     * @return the HTML page
     */
    protected HtmlPage getWebPage(final JavaScriptSupport javaScriptSupport, final Run<?, ?> build) {
        return getWebPage(javaScriptSupport, build, StringUtils.EMPTY);
    }

    /**
     * Returns the HTML page content of the specified URL for a given build.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     * @param build
     *         the build to show the page for
     * @param relativeUrl
     *         the relative URL within the job
     *
     * @return the HTML page
     */
    protected HtmlPage getWebPage(final JavaScriptSupport javaScriptSupport, final Run<?, ?> build,
            final String relativeUrl) {
        try {
            return getWebClient(javaScriptSupport).getPage(build, relativeUrl);
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the HTML page content of the specified URL for a given analysis result.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     * @param result
     *         the analysis result to show the sub page for
     * @param relativeUrl
     *         the relative URL within the job
     *
     * @return the HTML page
     */
    protected HtmlPage getWebPage(final JavaScriptSupport javaScriptSupport,
            final AnalysisResult result, final String relativeUrl) {
        return getWebPage(javaScriptSupport, result.getOwner(), result.getId() + "/" + relativeUrl);
    }

    /**
     * Returns the HTML page content of the specified analysis result.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     * @param result
     *         the analysis result to show
     *
     * @return the HTML page
     */
    protected HtmlPage getWebPage(final JavaScriptSupport javaScriptSupport, final AnalysisResult result) {
        return getWebPage(javaScriptSupport, result.getOwner(), result.getId());
    }

    /**
     * Submit the supplied {@link HtmlForm}. Locates the submit element/button on the form.
     *
     * @param form
     *         the {@link HtmlForm}
     */
    protected void submit(final HtmlForm form) {
        try {
            HtmlFormUtil.submit(form);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Makes the specified file unreadable.
     *
     * @param file
     *         the specified file
     */
    protected void makeFileUnreadable(final Path file) {
        makeFileUnreadable(file.toString());
    }

    /**
     * Makes the specified file unreadable.
     *
     * @param absolutePath
     *         the specified file
     */
    protected void makeFileUnreadable(final String absolutePath) {
        File nonReadableFile = new File(absolutePath);
        if (Functions.isWindows()) {
            setAccessModeOnWindows(absolutePath, WINDOWS_FILE_DENY, WINDOWS_FILE_ACCESS_READ_ONLY);
        }
        else {
            assertThat(nonReadableFile.setReadable(false, false)).isTrue();
            assertThat(nonReadableFile.canRead()).isFalse();
        }
    }

    private void setAccessModeOnWindows(final String path, final String command, final String accessMode) {
        try {
            Process process = Runtime.getRuntime()
                    .exec("icacls \"" + path + "\" " + command + " *S-1-1-0:" + accessMode);
            process.waitFor();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Adds a script as a {@link Shell} or {@link BatchFile} depending on the current OS.
     *
     * @param project
     *         the project
     * @param script
     *         the script to run
     *
     * @return the created script step
     */
    protected Builder addScriptStep(final FreeStyleProject project, final String script) {
        Builder item;
        if (Functions.isWindows()) {
            item = new BatchFile(script);
        }
        else {
            item = new Shell(script);
        }
        project.getBuildersList().add(item);
        return item;
    }

    /**
     * Cleans the workspace of the specified job. Deletes all files in the workspace.
     *
     * @param job
     *         the workspace to clean
     */
    protected void cleanWorkspace(final TopLevelItem job) {
        try {
            getWorkspace(job).deleteContents();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Add a build step that simply fails the build.
     *
     * @param project
     *         the job to add the step
     *
     * @return the created build step
     */
    protected Builder addFailureStep(final FreeStyleProject project) {
        return addScriptStep(project, "exit 1");
    }

    /**
     * Removes the specified builder from the list of registered builders.
     *
     * @param project
     *         the job to add the step
     * @param builder
     *         the builder to remove
     */
    protected void removeBuilder(final FreeStyleProject project, final Builder builder) {
        project.getBuildersList().remove(builder);
    }

    /**
     * Joins the specified arguments as list of comma separated values. Note that the first element is separated with a
     * comma as well.
     * <blockquote>For example,
     * <pre>{@code
     *     String message = join("Java", "is", "cool");
     *     // message returned is: ",Java,is,cool"
     * }</pre></blockquote>
     *
     * @param arguments
     *         th arguments to join
     *
     * @return the concatenated string
     */
    protected String join(final String... arguments) {
        StringBuilder builder = new StringBuilder();
        for (String argument : arguments) {
            builder.append(", ");
            builder.append(argument);
        }
        return builder.toString();
    }

    /**
     * Returns the model of a chart in the specified HTML page.
     *
     * @param page
     *         the HTML page that contains the chart
     * @param id
     *         the element ID of the chart placeholder (that has the EChart instance attached in property @{@code
     *         echart}
     *
     * @return the model (as JSON representation)
     */
    protected String getChartModel(final HtmlPage page, final String id) {
        ScriptResult scriptResult = page.executeJavaScript(
                String.format("JSON.stringify(document.getElementById(\"%s\").echart.getOption());", id));

        return scriptResult.getJavaScriptResult().toString();
    }

    /**
     * Calls Jenkins remote API with the specified URL. Calls the JSON format.
     *
     * @param url
     *         the URL to call
     *
     * @return the JSON response
     */
    protected JSONWebResponse callJsonRemoteApi(final String url) {
        try {
            return getJenkins().getJSON(url);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Calls Jenkins remote API with the specified URL. Calls the XML format.
     *
     * @param url
     *         the URL to call
     *
     * @return the XML response
     */
    protected Document callXmlRemoteApi(final String url) {
        try {
            return getJenkins().createWebClient().goToXml(url).getXmlDocument();
        }
        catch (IOException | SAXException e) {
            throw new AssertionError(e);
        }
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
        try {
            return FileUtils.readFileToString(result.getOwner().getLogFile(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
