package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.quality.Status;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
public class IssuesRecorderITest extends IntegrationTest {
    /**
     * Verifies that the reference job name can be set to another job.
     */
    @Test
    public void shouldInitializeAndStoreReferenceJobName() {
        FreeStyleProject job = createFreeStyleProject();
        String initialization = "Reference Job";
        enableWarnings(job, tool -> {
            tool.setReferenceJobName(initialization);
        });
        
        HtmlPage configPage = getWebPage(job, "configure");
        HtmlForm form = configPage.getFormByName("config");
        HtmlTextInput referenceJob = form.getInputByName("_.referenceJobName");
        assertThat(referenceJob.getText()).isEqualTo(initialization);
        
        String update = "New Reference Job";
        referenceJob.setText(update);

        submit(form);
        
        assertThat(getRecorder(job).getReferenceJobName()).isEqualTo(update);
    }
    
    private IssuesRecorder getRecorder(final AbstractProject<?, ?> job) {
        DescribableList<Publisher, Descriptor<Publisher>> publishers = job.getPublishersList();
        for (Publisher publisher : publishers) {
            if (publisher instanceof IssuesRecorder) {
                return (IssuesRecorder) publisher;
            }
        }
        throw new AssertionError("No instance of IssuesRecorder found for job " + job);
    }
    
    private void submit(final HtmlForm form) {
        try {
            HtmlFormUtil.submit(form);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private HtmlPage getWebPage(final AbstractProject job, final String page) {
        try {
            WebClient webClient = j.createWebClient();
            webClient.setJavaScriptEnabled(true);
            return webClient.getPage(job, page);
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Runs the Eclipse parser on an empty workspace: the build should report 0 issues and an error message.
     */
    @Test
    public void shouldCreateEmptyResult() {
        FreeStyleProject project = createFreeStyleProject();
        enableEclipseWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages("No files found for pattern '**/*issues.txt'. Configuration error?");
    }

    /**
     * Runs the Eclipse parser on an output file that contains several issues: the build should report 8 issues.
     */
    @Test
    public void shouldCreateResultWithWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableEclipseWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasNewSize(0);
        assertThat(result).hasInfoMessages("Resolved module names for 8 issues",
                "Resolved package names of 4 affected files");
    }

    private void enableEclipseWarnings(final FreeStyleProject project) {
        enableWarnings(project, new Eclipse());
    }

    /**
     * Sets the UNSTABLE threshold to 8 and parse a file that contains exactly 8 warnings: the build should be
     * unstable.
     */
    @Test
    public void shouldCreateUnstableResult() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableWarnings(project, publisher -> publisher.setUnstableTotalAll(7));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.WARNING);

        HtmlPage page = getWebPage(result);
        assertThat(page.getElementsByIdAndOrName("statistics")).hasSize(1);
    }

    /**
     * Runs the CheckStyle parser without specifying a pattern: the default pattern should be used.
     */
    @Test
    public void shouldUseDefaultFileNamePattern() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "checkstyle.xml", "checkstyle-result.xml");
        enableWarnings(project, new CheckStyle(), StringUtils.EMPTY);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(6);
    }

    private HtmlPage getWebPage(final AnalysisResult result) {
        try {
            WebClient webClient = j.createWebClient();
            webClient.setJavaScriptEnabled(false);
            return webClient.getPage(result.getOwner(), "eclipseResult");
        }
        catch (SAXException | IOException e) {
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
    private FreeStyleProject createJobWithWorkspaceFile(final String... fileNames) {
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
    private IssuesRecorder enableWarnings(final AbstractProject<?, ?> job, final StaticAnalysisTool tool) {
        return enableWarnings(job, tool, "**/*issues.txt");
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param tool
     *         the tool to scan the warnings
     * @param pattern
     *         the pattern to use for files
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final AbstractProject<?, ?> job, final StaticAnalysisTool tool,
            final String pattern) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Collections.singletonList(new ToolConfiguration(tool, pattern)));
        job.getPublishersList().add(publisher);
        return publisher;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param configuration
     *         configuration of the recorder
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job, final Consumer<IssuesRecorder> configuration) {
        IssuesRecorder publisher = enableWarnings(job, new Eclipse());
        configuration.accept(publisher);
        return publisher;
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
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private AnalysisResult scheduleBuildAndAssertStatus(final FreeStyleProject job, final Result status) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));

            return getAnalysisResult(build);
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the created {@link AnalysisResult} of a run.
     *
     * @param run
     *         the run that has the action attached
     *
     * @return the created {@link ResultAction}
     */
    protected AnalysisResult getAnalysisResult(final Run<?, ?> run) {
        ResultAction action = run.getAction(ResultAction.class);

        assertThat(action).isNotNull();

        return action.getResult();
    }
}
