package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import io.jenkins.plugins.analysis.warnings.Eclipse;

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
   protected void enableEclipseWarnings(final FreeStyleProject project) {
        enableWarnings(project, new Eclipse());
    }

   protected void enableEclipseWarnings(final FreeStyleProject project, final Consumer<IssuesRecorder> configuration) {
        enableWarnings(project, configuration, createGenericToolConfiguration(new Eclipse()));
    }

    protected HtmlPage getWebPage(final AbstractProject job, final String page) {
        try {
            WebClient webClient = j.createWebClient();
            webClient.setJavaScriptEnabled(true);
            return webClient.getPage(job, page);
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
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
    protected FreeStyleProject createJobWithWorkspaceFiles(final String... fileNames) {
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
        return enableWarnings(job, createGenericToolConfiguration(tool));
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
     * @param configuration
     *         configuration of the recorder
     * @param toolConfiguration
     *         the tool configuration to use
     * @param additionalToolConfigurations
     *         the additional tool configurations to use
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableWarnings(final AbstractProject<?, ?> job,
            final Consumer<IssuesRecorder> configuration,
            final ToolConfiguration toolConfiguration,
            final ToolConfiguration... additionalToolConfigurations) {
        IssuesRecorder recorder = enableWarnings(job, ArrayUtils.add(additionalToolConfigurations, toolConfiguration));
        configuration.accept(recorder);
        return recorder;
    }
    
    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param toolConfigurations
     *         the tool configurations to use
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    protected IssuesRecorder enableWarnings(final AbstractProject<?, ?> job,
            final ToolConfiguration... toolConfigurations) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Arrays.asList(toolConfigurations));
        job.getPublishersList().add(publisher);
        return publisher;
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
     *
     * @return the created {@link ResultAction}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    protected AnalysisResult scheduleBuildAndAssertStatus(final FreeStyleProject job, final Result status) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));

            return getAnalysisResult(build);
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link List<AnalysisResult>} after the build
     * has been finished as one result of both tools.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result of both tools for the build
     *
     * @return the created {@link List<ResultAction>}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private List<AnalysisResult> scheduleBuildAndAssertStatusForBothTools(final FreeStyleProject job,
            final Result status) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));
            System.out.println(new String(Files.readAllBytes(build.getLogFile().toPath())));
            List<ResultAction> actions = build.getActions(ResultAction.class);

            List<AnalysisResult> results = new ArrayList<>();
            for (ResultAction elements : actions) {
                results.add(elements.getResult());
            }
            return results;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Schedules a new build for the specified job and checks the console log.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result of both tools for the build
     * @param log
     *         the log string asserted to be in console
     *
     * @return the created {@link FreeStyleBuild}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private FreeStyleBuild scheduleBuildAndAssertLog(final FreeStyleProject job, final Result status,
            final String log) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));
            j.assertLogContains(log, build);
            return build;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a {@link List<AnalysisResult>} of the analysis results of {@link FreeStyleBuild}.
     *
     * @param build
     *         the FreeStyleBuild
     *
     * @return the created {@link List<ResultAction>}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private List<AnalysisResult> getAssertStatusForBothTools(final FreeStyleBuild build) {
        try {
            List<ResultAction> actions = build.getActions(ResultAction.class);

            List<AnalysisResult> results = new ArrayList<>();
            for (ResultAction elements : actions) {
                results.add(elements.getResult());
            }
            return results;
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