package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.Issues;
import hudson.model.Result;
import io.jenkins.plugins.analysis.core.steps.*;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;
import org.junit.jupiter.api.Tag;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.function.Consumer;

import static edu.hm.hafner.analysis.assertj.Assertions.assertThat;

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
     * @throws Exception in case of an error
     */
    @Test
    public void shouldFindAllEclipseIssues() throws Exception {
        findAllParserToolIssues("eclipse.txt", Eclipse.class, result -> {
            assertIssuesCount(result, 8);
        });
    }

    private void assertIssuesCount(AnalysisResult result, int expected) {
        assertThat(result.getTotalSize()).isEqualTo(expected);
        assertThat(result.getIssues()).hasSize(expected);
    }

    /**
     * Runs the Maven console parser on an output file that contains several issues: the build should report 4 issues.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void shouldFindAllMavenConsoleIssues() throws Exception {
        findAllParserToolIssues("maven-console.txt", MavenConsole.class, result -> {
            assertIssuesCount(result, 4);
        });
    }

    /**
     * Runs the Maven parser on an output file that contains several issues: the build should report 4 issues.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void shouldFindAllMavenIssues() throws Exception {
        findAllParserToolIssues("maven.txt", Maven.class, result -> {
            assertIssuesCount(result, 5);
        });
    }

    /**
     * Runs the MetrowerksCWCompiler parser on an output file that contains several issues: the build should report 4 issues.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void shouldFindAllMetrowerksCWCompilerIssues() throws Exception {
        findAllParserToolIssues("MetrowerksCWCompiler.txt", MetrowerksCWCompiler.class, result -> {
            assertIssuesCount(result, 5);
        });
    }

    /**
     * Runs the MetrowerksCWLinker parser on an output file that contains several issues: the build should report 4 issues.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void shouldFindAllMetrowerksCWLinkerIssues() throws Exception {
        findAllParserToolIssues("MetrowerksCWLinker.txt", MetrowerksCWLinker.class, result -> {
            assertIssuesCount(result, 3);
        });
    }

    /**
     * Runs the JavaC parser on an output file of the Eclipse compiler: the build should report no issues.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void shouldNoJavacIssuesInEclipseOutput() throws Exception {

        findAllParserToolIssues("eclipse.txt", Java.class, result -> {
            assertThat(result.getTotalSize()).isEqualTo(0);
        });
    }

    /**
     * Runs the all Java parsers on three output files: the build should report issues of all tools.
     *
     * @throws Exception in case of an error
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
     * @param job the job to schedule
     * @return the created {@link AnalysisResult}
     */
    private AnalysisResult scheduleBuild(final WorkflowJob job) throws Exception {
        WorkflowRun run = j.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));

        ResultAction action = run.getAction(ResultAction.class);
        assertThat(action).isNotNull();

        return action.getResult();
    }

    /**
     * /**
     * Runs a parser on an output file that contains several issues and executes a test case on it.
     *
     * @param fileName the output file containing the issues
     * @param parserTool the specific parser that shall be used
     * @param assertions a consumer for the test case
     * @param <T> a parser that inherits from {@link StaticAnalysisTool}
     * @throws Exception in case of an error
     */
    private <T extends StaticAnalysisTool> void findAllParserToolIssues(@CheckForNull String fileName,
                                                                        @CheckForNull Class<T> parserTool,
                                                                        @CheckForNull Consumer<AnalysisResult> assertions) throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile(fileName);
        job.setDefinition(parseAndPublish(parserTool));
        AnalysisResult result = scheduleBuild(job);
        assertions.accept(result);
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
