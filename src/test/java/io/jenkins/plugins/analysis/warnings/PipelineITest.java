package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;
import org.junit.jupiter.api.Tag;

import edu.hm.hafner.analysis.Issues;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import io.jenkins.plugins.analysis.core.steps.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.BuildIssue;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ResultAction;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;

import hudson.model.Result;

/**
 * Integration tests for pipeline support in the warning plug-in.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
@Tag("IntegrationTest")
@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class PipelineITest extends IntegrationTest {
    private static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";

    /**
     * Runs the Eclipse parser on an output file that contains several issues: the build should report 8 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllEclipseIssues() throws Exception {
        AnalysisResult result = runParserInJobContext(Eclipse.class, "eclipse.txt");

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues()).hasSize(8);
    }

    /**
     * Runs the JavaC parser on an output file of the Eclipse compiler: the build should report no issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindNoJavacIssuesInEclipseOutput() throws Exception {
        AnalysisResult result = runParserInJobContext(Java.class, "eclipse.txt");

        assertThat(result.getTotalSize()).isEqualTo(0);
    }

    /**
     * Runs the all Java parsers on three output files: the build should report issues of all tools.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldCombineIssuesOfSeveralFiles() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("eclipse.txt", "javadoc.txt", "javac.txt");
        job.setDefinition(asStage(createScanForIssuesStep(Java.class, "java"),
                createScanForIssuesStep(Eclipse.class, "eclipse"),
                createScanForIssuesStep(JavaDoc.class, "javadoc"),
                "publishIssues issues:[java, eclipse, javadoc]"));

        AnalysisResult result = scheduleBuild(job);

        Issues<BuildIssue> issues = result.getIssues();
        assertThat(issues.filter(issue -> "eclipse".equals(issue.getOrigin()))).hasSize(8);
        assertThat(issues.filter(issue -> "java".equals(issue.getOrigin()))).hasSize(2);
        assertThat(issues.filter(issue -> "javadoc".equals(issue.getOrigin()))).hasSize(6);
        assertThat(issues.getToolNames()).containsExactlyInAnyOrder("java", "javadoc", "eclipse");
        assertThat(result.getIssues()).hasSize(8 + 2 + 6);
    }

    /**
     * Runs the MsBuild parser on an output file: the build should report 6 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllMsBuildIssues() throws Exception {
        AnalysisResult result = runParserInJobContext(MsBuild.class, "msbuild.txt");

        assertThat(result.getTotalSize()).isEqualTo(6);
        assertThat(result.getIssues()).hasSize(6);
    }

    /**
     * Runs the NagFortran parser on an output file: the build should report 10 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllNagFortranIssues() throws Exception {
        AnalysisResult result = runParserInJobContext(NagFortran.class, "NagFortran.txt");

        assertThat(result.getTotalSize()).isEqualTo(10);
        assertThat(result.getIssues()).hasSize(10);
    }

    /**
     * Runs the Perforce parser on an output file: the build should report 4 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllP4Issues() throws Exception {
        AnalysisResult result = runParserInJobContext(Perforce.class, "perforce.txt");

        assertThat(result.getTotalSize()).isEqualTo(4);
        assertThat(result.getIssues()).hasSize(4);
    }

    /**
     * Runs the Pep8 parser on an output file: the build should report 8 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllPep8Issues() throws Exception {
        AnalysisResult result = runParserInJobContext(Pep8.class, "pep8Test.txt");

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues()).hasSize(8);
    }

    /**
     * Runs a specific Parser on a series of output files
     * @param parserClass the class of the parser to use
     * @param fileNames the output files to be parsed
     * @return the  {@link AnalysisResult} of the output-files parsed by the parser in the jenkins-job context
     * @throws Exception
     *         in case of an error
     */
    private AnalysisResult runParserInJobContext(final Class<? extends StaticAnalysisTool> parserClass,
                                                 final String... fileNames) throws Exception{
        WorkflowJob job = createJobWithWorkspaceFile(fileNames);
        job.setDefinition(parseAndPublish(parserClass));

        return scheduleBuild(job);
    }

    private CpsFlowDefinition parseAndPublish(final Class<? extends StaticAnalysisTool> parserClass) {
        return asStage(createScanForIssuesStep(parserClass), PUBLISH_ISSUES_STEP);
    }

    private String createScanForIssuesStep(final Class<? extends StaticAnalysisTool> parserClass) {
        return createScanForIssuesStep(parserClass, "issues");
    }

    private String createScanForIssuesStep(final Class<? extends StaticAnalysisTool> parserClass, final String issuesName) {
        return String.format("def %s = scanForIssues tool: [$class: '%s'], pattern:'**/*issues.txt'", issuesName, parserClass.getSimpleName());
    }

    private WorkflowJob createJobWithWorkspaceFile(final String... fileNames) throws IOException, InterruptedException {
        WorkflowJob job = createJob();
        copyFilesToWorkspace(job, fileNames);
        return job;
    }

    private WorkflowJob createJob() throws IOException {
        return createJob(WorkflowJob.class);
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link AnalysisResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     *
     * @return the created {@link AnalysisResult}
     */
    private AnalysisResult scheduleBuild(final WorkflowJob job) throws Exception {
        WorkflowRun run = j.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));

        ResultAction action = run.getAction(ResultAction.class);
        assertThat(action).isNotNull();

        return action.getResult();
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private CpsFlowDefinition asStage(final String... steps) {
        StringBuilder script = new StringBuilder();
        script.append("node {\n");
        script.append("  stage ('Integration Test') {\n");
        for (String step : steps) {
            script.append("    ");
            script.append(step);
            script.append('\n');
        }
        script.append("  }\n");
        script.append("}\n");


        System.out.println("----------------------------------------------------------------------");
        System.out.println(script);
        System.out.println("----------------------------------------------------------------------");
        return new CpsFlowDefinition(script.toString(), true);
    }
}
