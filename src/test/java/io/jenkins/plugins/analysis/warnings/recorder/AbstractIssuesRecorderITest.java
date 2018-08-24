package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.factory.Lists;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Eclipse;

import hudson.FilePath;
import hudson.Functions;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.tasks.BatchFile;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Shell;
import hudson.util.DescribableList;

/**
 * Base class for integration tests of the warnings plug-in in freestyle jobs. 
 * Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
public class AbstractIssuesRecorderITest extends IntegrationTestWithJenkinsPerTest {
    private static final String WINDOWS_FILE_ACCESS_READ_ONLY = "RX";
    private static final String WINDOWS_FILE_DENY = "/deny";

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
    protected IssuesRecorder enableWarnings(final AbstractProject<?, ?> job, final StaticAnalysisTool tool) {
        return enableWarnings(job, createGenericToolConfiguration(tool));
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param configuration
     *         the tool configuration to use
     * @param additionalConfigurations
     *         the tool configurations to use
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableWarnings(final AbstractProject<?, ?> job,
            final ToolConfiguration configuration,  final ToolConfiguration... additionalConfigurations) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Lists.mutable.of(additionalConfigurations).with(configuration));
        publisher.setReportEncoding("UTF-8");
        job.getPublishersList().add(publisher);
        return publisher;
    }

    @CanIgnoreReturnValue
    protected IssuesRecorder enableEclipseWarnings(final AbstractProject<?, ?> project) {
        return enableWarnings(project, new Eclipse());
    }

    @CanIgnoreReturnValue
    protected IssuesRecorder enableCheckStyleWarnings(final AbstractProject<?, ?> project) {
        return enableWarnings(project, new CheckStyle());
    }

    @CanIgnoreReturnValue
    protected IssuesRecorder enableEclipseWarnings(final FreeStyleProject project,
            final Consumer<IssuesRecorder> configuration) {
        return enableWarnings(project, configuration, createGenericToolConfiguration(new Eclipse()));
    }

    protected HtmlPage getWebPage(final AbstractProject<?, ?> job, final String page) {
        try {
            return createWebClient().getPage(job, page);
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Clicks a link.
     *
     * @param element
     *         a {@link DomElement} which will trigger the redirection to a new page.
     */
    protected HtmlPage clickOnLink(final DomElement element) {
        try {
            return element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    protected HtmlPage getWebPage(final Run<?, ?> build) {
        return getWebPage(build, StringUtils.EMPTY);
    }
    
    protected HtmlPage getWebPage(final AbstractProject<?, ?> job) {
        return getWebPage(job, StringUtils.EMPTY);
    }
    
    protected HtmlPage getWebPage(final Run<?, ?> build, final String page) {
        try {
            return createWebClient().getPage(build, page);
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }
    protected HtmlPage getWebPage(final AnalysisResult result, final String child) {
        return getWebPage(result.getOwner(), result.getId() + "Result/" + child);
    }
    
    protected HtmlPage getWebPage(final AnalysisResult result) {
        return getWebPage(result.getOwner(), result.getId() + "Result");
    }

    private WebClient createWebClient() {
        WebClient webClient = getJenkins().createWebClient();
        webClient.setJavaScriptEnabled(true);
        return webClient;
    }

    protected void submit(final HtmlForm form) {
        try {
            HtmlFormUtil.submit(form);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
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
    protected IssuesRecorder enableWarnings(final AbstractProject<?, ?> job,
            final Consumer<IssuesRecorder> configuration,
            final StaticAnalysisTool tool) {
        return enableWarnings(job, configuration, createGenericToolConfiguration(tool));
    }

    private ToolConfiguration createGenericToolConfiguration(final StaticAnalysisTool tool) {
        return new ToolConfiguration(tool, "**/*issues.txt");
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param recorderConfiguration
     *         configuration of the recorder
     * @param configuration
     *         the tool configuration to use
     * @param additionalConfigurations
     *         the additional tool configurations to use
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableWarnings(final AbstractProject<?, ?> job,
            final Consumer<IssuesRecorder> recorderConfiguration,
            final ToolConfiguration configuration,
            final ToolConfiguration... additionalConfigurations) {
        IssuesRecorder recorder = enableWarnings(job, configuration, additionalConfigurations);
        recorderConfiguration.accept(recorder);
        return recorder;
    }

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
     * Schedules a new build for the specified job and returns the created {@link AnalysisResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result for the build
     * @param assertions
     *         the assertions for the result
     *
     * @return the build
     */
    protected Run<?, ?> scheduleBuildAndAssertStatus(final FreeStyleProject job, final Result status,
            final Consumer<AnalysisResult> assertions) {
        Run<?, ?> build = buildWithStatus(job, status);
        AnalysisResult result = getAnalysisResult(build);
        assertions.accept(result);
        return build;
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link AnalysisResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result for the build
     *
     * @return the created {@link ResultAction}
     */
    protected AnalysisResult scheduleBuildAndAssertStatus(final FreeStyleProject job, final Result status) {
        return getAnalysisResult(buildWithStatus(job, status));
    }

    /**
     * Schedules a new build for the specified job and returns the finished {@link Run}.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result for the build
     *
     * @return the finished {@link Run}.
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    protected Run<?, ?> buildWithStatus(final FreeStyleProject job, final Result status) {
        try {
            return getJenkins().assertBuildStatus(status, job.scheduleBuild2(0));
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
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

    protected void makeFileUnreadable(final Path file) {
        makeFileUnreadable(file.toString());
    }

    protected void makeFileUnreadable(final String absolutePath) {
        File nonReadableFile = new File(absolutePath);
        if (Functions.isWindows()) {
            setAccessMode(absolutePath, WINDOWS_FILE_DENY, WINDOWS_FILE_ACCESS_READ_ONLY);
        }
        else {
            assertThat(nonReadableFile.setReadable(false, false)).isTrue();
            assertThat(nonReadableFile.canRead()).isFalse();
        }
    }

    /**
     * Executed the 'icals' command on the windows command line to remove the read permission of a file.
     *
     * @param path
     *         File to remove from the read permission
     * @param command
     *         part of the icacls command
     * @param accessMode
     *         param for the icacls command
     */
    void setAccessMode(final String path, final String command, final String accessMode) {
        try {
            Process process = Runtime.getRuntime().exec("icacls " + path + " " + command + " *S-1-1-0:" + accessMode);
            process.waitFor();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    protected FilePath getWorkspaceFor(final TopLevelItem project) {
        return getJenkins().jenkins.getWorkspaceFor(project);
    }

    /**
     * Adds a script as a {@link Shell} or {@link BatchFile}.
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

    protected void cleanWorkspace(final FreeStyleProject job) {
        try {
            getWorkspaceFor(job).deleteContents();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    protected Builder addFailureStep(final FreeStyleProject project) {
        return addScriptStep(project, "exit 1");
    }

    void removeBuilder(final FreeStyleProject project, final Builder builder) {
        project.getBuildersList().remove(builder);
    }
}