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
     * Runs the Gendarme parser on an output file that contains several issues: the build should report 3 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllGendarmeIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("Gendarme.xml");
        job.setDefinition(parseAndPublish(GhsMulti.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(3);
        assertThat(result.getIssues()).hasSize(3);
    }

    /**
     * Runs the GhsMulti parser on an output file that contains several issues: the build should report 3 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllGhsMultiIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("ghsmulti.txt");
        job.setDefinition(parseAndPublish(GhsMulti.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(3);
        assertThat(result.getIssues()).hasSize(3);
    }

    /**
     * Runs the Gnat parser on an output file that contains several issues: the build should report 9 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllGnatIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("gnat.txt");
        job.setDefinition(parseAndPublish(Gnat.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(9);
        assertThat(result.getIssues()).hasSize(9);
    }

    /**
     * Runs the GnuFortran parser on an output file that contains several issues: the build should report 4 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllGnuFortranIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("GnuFortran.txt");
        job.setDefinition(parseAndPublish(GnuFortran.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(4);
        assertThat(result.getIssues()).hasSize(4);
    }

    /**
     * Runs the GnuMakeGcc parser on an output file that contains several issues: the build should report 15 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllGnuMakeGccIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("gnuMakeGcc.txt");
        job.setDefinition(parseAndPublish(GnuMakeGcc.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(15);
        assertThat(result.getIssues()).hasSize(15);
    }

    /**
     * Runs the Eclipse parser on an output file that contains several issues. Applies an include filter that selects
     * only one issue (in the file AttributeException.java).
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldIncludeJustOneFile() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("eclipse.txt");
        job.setDefinition(asStage(createScanForIssuesStep(Eclipse.class),
                "publishIssues issues:[issues],  "
                        + "filters:[[property: [$class: 'IncludeFile'], pattern: '.*AttributeException.*']]"));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getIssues()).hasSize(1);
    }

    /**
     * Runs the AcuCobol parser on an output file that contains several issues: the build should report 4 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllAcuCobolIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("acu.txt");
        job.setDefinition(parseAndPublish(AcuCobol.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(4);
        assertThat(result.getIssues()).hasSize(4);
    }

    /**
     * Runs the Ajc parser on an output file that contains several issues: the build should report 9 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllAjcIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("ajc.txt");
        job.setDefinition(parseAndPublish(Ajc.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(9);
        assertThat(result.getIssues()).hasSize(9);
    }

    /**
     * Runs the AnsibleLint parser on an output file that contains several issues: the build should report 4 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllAnsibleLintIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("ansibleLint.txt");
        job.setDefinition(parseAndPublish(AnsibleLint.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(4);
        assertThat(result.getIssues()).hasSize(4);
    }

    /**
     * Runs the Perl::Critic parser on an output file that contains several issues: the build should report 105 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllPerlCriticIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("perlcritic.txt");
        job.setDefinition(parseAndPublish(PerlCritic.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(105);
        assertThat(result.getIssues()).hasSize(105);
    }

    /**
     * Runs the Php parser on an output file that contains several issues: the build should report 5 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllPhpIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("php.txt");
        job.setDefinition(parseAndPublish(Php.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(5);
        assertThat(result.getIssues()).hasSize(5);
    }

    /**
     * Runs the Microsoft PREfast parser on an output file that contains several issues: the build should report 11 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllPREfastIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("PREfast.xml");
        job.setDefinition(parseAndPublish(PREfast.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(11);
        assertThat(result.getIssues()).hasSize(11);
    }

    /**
     * Runs the Puppet Lint parser on an output file that contains several issues: the build should report 5 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllPuppetLintIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("puppet-lint.txt");
        job.setDefinition(parseAndPublish(PuppetLint.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(5);
        assertThat(result.getIssues()).hasSize(5);
    }

    /**
     * Runs the Eclipse parser on an output file that contains several issues: the build should report 8 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllEclipseIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("eclipse.txt");
        job.setDefinition(parseAndPublish(Eclipse.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues()).hasSize(8);
    }

    /**
     * Runs the Idea Inspection parser on an output file that contains several issues: the build should report 1 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllIdeaInspectionIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("IdeaInspectionExample.xml");
        job.setDefinition(parseAndPublish(IdeaInspection.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getIssues()).hasSize(1);
    }

    /**
     * Runs the Intel parser on an output file that contains several issues: the build should report 7 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllIntelIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("intelc.txt");
        job.setDefinition(parseAndPublish(Intel.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(7);
        assertThat(result.getIssues()).hasSize(7);
    }

    /**
     * Runs the Oracle Invalids parser on an output file that contains several issues: the build should report 3 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllInvalidsIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("invalids.txt");
        job.setDefinition(parseAndPublish(Invalids.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(3);
        assertThat(result.getIssues()).hasSize(3);
    }

    /**
     * Runs the Java parser on an output file that contains several issues: the build should report 2 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllJavaIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("javac.txt");
        job.setDefinition(parseAndPublish(Java.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(2);
        assertThat(result.getIssues()).hasSize(2);
    }

    /**
     * Runs the CssLint parser on an output file that contains several issues: the build should report 51 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllCssLintIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("csslint.xml");
        job.setDefinition(parseAndPublish(CssLint.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(51);
        assertThat(result.getIssues()).hasSize(51);
    }

    /**
     * Runs the DiabC parser on an output file that contains several issues: the build should report 12 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllDiabCIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("diabc.txt");
        job.setDefinition(parseAndPublish(DiabC.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(12);
        assertThat(result.getIssues()).hasSize(12);
    }

    /**
     * Runs the Doxygen parser on an output file that contains several issues: the build should report 21 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllDoxygenIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("doxygen.txt");
        job.setDefinition(parseAndPublish(Doxygen.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(21);
        assertThat(result.getIssues()).hasSize(21);
    }

    /**
     * Runs the Dr. Memory parser on an output file that contains several issues: the build should report 8 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllDrMemoryIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("drmemory.txt");
        job.setDefinition(parseAndPublish(DrMemory.class));

        AnalysisResult result = scheduleBuild(job);

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
        WorkflowJob job = createJobWithWorkspaceFile("eclipse.txt");
        job.setDefinition(parseAndPublish(Java.class));

        AnalysisResult result = scheduleBuild(job);

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
        WorkflowJob job = createJobWithWorkspaceFile("msbuild.txt");
        job.setDefinition(parseAndPublish(MsBuild.class));

        AnalysisResult result = scheduleBuild(job);

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
        WorkflowJob job = createJobWithWorkspaceFile("NagFortran.txt");
        job.setDefinition(parseAndPublish(NagFortran.class));

        AnalysisResult result = scheduleBuild(job);

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
        WorkflowJob job = createJobWithWorkspaceFile("perforce.txt");
        job.setDefinition(parseAndPublish(Perforce.class));

        AnalysisResult result = scheduleBuild(job);

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
        WorkflowJob job = createJobWithWorkspaceFile("pep8Test.txt");
        job.setDefinition(parseAndPublish(Pep8.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues()).hasSize(8);
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

    /**
     * Runs the Gcc3Compiler parser on an output file that contains several issues: the build should report 8 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllGcc4CompilerIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("gcc4.txt");
        job.setDefinition(parseAndPublish(Gcc4Compiler.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(14);
        assertThat(result.getIssues()).hasSize(14);
    }

    /**
     * Runs the Gcc3Compiler parser on an output file that contains several issues: the build should report 8 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllGcc3CompilerIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("gcc.txt");
        job.setDefinition(parseAndPublish(Gcc.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues()).hasSize(8);
    }

    /**
     * Runs the Gcc4Linker parser on an output file that contains several issues: the build should report 2 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllGcc4LinkerIssues() throws Exception {
        WorkflowJob job = createJobWithWorkspaceFile("gcc4ld.txt");
        job.setDefinition(parseAndPublish(Gcc4Linker.class));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(7);
        assertThat(result.getIssues()).hasSize(7);
    }


}
