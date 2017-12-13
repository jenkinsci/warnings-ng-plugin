package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.function.Consumer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.warnings.ParserConfiguration;
import hudson.plugins.warnings.WarningsPublisher;
import hudson.plugins.warnings.WarningsResult;
import hudson.plugins.warnings.WarningsResultAction;

/**
 * Integration tests for freestyle jobs with checkstyle plugin.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class FreestyleJobITest extends IntegrationTest {
    /**
     * Runs the Eclipse parser on an empty workspace: the build should report 0 issues and an error message.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldCreateEmptyResult() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        enableWarnings(project);

        WarningsResult result = scheduleBuild(project, Result.SUCCESS);

        assertThat(result.getNumberOfAnnotations()).isEqualTo(0);
        assertThat(result.getErrors()).isNotEmpty();
    }

    /**
     * Runs the Eclipse parser on an output file that contains several issues: the build should report 8 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldCreateResultWithWarnings() throws Exception {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableWarnings(project);

        WarningsResult result = scheduleBuild(project, Result.SUCCESS);

        assertThat(result.getNumberOfAnnotations()).isEqualTo(8);
    }

    /**
     * Sets the UNSTABLE threshold to 8 and parse a file that contains exactly 8 warnings: the build should be unstable.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldCreateUnstableResult() throws Exception {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableWarnings(project, publisher -> publisher.setUnstableTotalAll("7"));

        WarningsResult result = scheduleBuild(project, Result.UNSTABLE);

        assertThat(result.getNumberOfAnnotations()).isEqualTo(8);
        // FIXME: SUCCESS?? assertThat(result.getPluginResult()).isEqualTo(Result.UNSTABLE);
    }

    private WarningsPublisher enableWarnings(final FreeStyleProject job) {
        WarningsPublisher publisher = new WarningsPublisher();
        publisher.setParserConfigurations(new ParserConfiguration[] {
                new ParserConfiguration("**/*issues.txt", "Java Compiler (Eclipse)")
        });
        job.getPublishersList().add(publisher);
        return publisher;
    }

    private WarningsPublisher enableWarnings(final FreeStyleProject job, final Consumer<WarningsPublisher> configuration) {
        WarningsPublisher publisher = enableWarnings(job);
        configuration.accept(publisher);
        return publisher;
    }

    private FreeStyleProject createJobWithWorkspaceFile(final String fileName) throws IOException, InterruptedException {
        FreeStyleProject job = j.createFreeStyleProject();
        copyFilesToWorkspace(job, fileName);
        return job;
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link WarningsResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     *
     * @param status the expected result for the build
     * @return the created {@link WarningsResult}
     */
    private WarningsResult scheduleBuild(final FreeStyleProject job, final Result status) throws Exception {
        FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));

        WarningsResultAction action = build.getAction(WarningsResultAction.class);

        assertThat(action).isNotNull();

        return action.getResult();
    }

}
