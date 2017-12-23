package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.Objects;

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
public class PipelineITest extends IntegrationTest {
    private static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";

    /** Runs the Erlc parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllErlcIssues() {
        shouldFindIssuesOfTool(Erlc.class, "erlc.txt", 2);
    }

    /** Runs the FlexSDK parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllFlexSDKIssues() {
        shouldFindIssuesOfTool(FlexSDK.class, "flexsdk.txt", 5);
    }

    /** Runs the FxCop parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllFxcopSDKIssues() {
        shouldFindIssuesOfTool(Fxcop.class, "fxcop.xml", 2);
    }

    /** Runs the Gendarme parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllGendarmeIssues() {
        shouldFindIssuesOfTool(Gendarme.class, "Gendarme.xml", 3);
    }

    /** Runs the GhsMulti parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllGhsMultiIssues() {
        shouldFindIssuesOfTool(GhsMulti.class, "ghsmulti.txt", 3);
    }

    /**
     * Runs the Gnat parser on an output file that contains 9 issues.
     */
    @Test
    public void shouldFindAllGnatIssues() {
        shouldFindIssuesOfTool(Gnat.class, "gnat.txt", 9);
    }

    /** Runs the GnuFortran parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllGnuFortranIssues() {
        shouldFindIssuesOfTool(GnuFortran.class, "GnuFortran.txt", 4);
    }

    /** Runs the GnuMakeGcc parser on an output file that contains 15 issues. */
    @Test
    public void shouldFindAllGnuMakeGccIssues() {
        shouldFindIssuesOfTool(GnuMakeGcc.class, "gnuMakeGcc.txt", 15);
    }

    /** Runs the MsBuild parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllMsBuildIssues() {
        shouldFindIssuesOfTool(MsBuild.class, "msbuild.txt", 6);
    }

    /** Runs the NagFortran parser on an output file that contains 10 issues. */
    @Test
    public void shouldFindAllNagFortranIssues() {
        shouldFindIssuesOfTool(NagFortran.class, "NagFortran.txt", 10);
    }

    /** Runs the Perforce parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllP4Issues() {
        shouldFindIssuesOfTool(Perforce.class, "perforce.txt", 4);
    }

    /** Runs the Pep8 parser on an output file: the build should report 8 issues. */
    @Test
    public void shouldFindAllPep8Issues() {
        shouldFindIssuesOfTool(Pep8.class, "pep8Test.txt", 8);
    }

    /** Runs the Gcc3Compiler parser on an output file that contains 14 issues. */
    @Test
    public void shouldFindAllGcc4CompilerIssues() {
        shouldFindIssuesOfTool(Gcc4Compiler.class, "gcc4.txt", 14);
    }

    /** Runs the Gcc3Compiler parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllGcc3CompilerIssues() {
        shouldFindIssuesOfTool(Gcc.class, "gcc.txt", 8);
    }

    /** Runs the Gcc4Linker parser on an output file that contains 7 issues. */
    @Test
    public void shouldFindAllGcc4LinkerIssues() {
        shouldFindIssuesOfTool(Gcc4Linker.class, "gcc4ld.txt", 7);
    }

    /** Runs the Maven console parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllMavenConsoleIssues() {
        shouldFindIssuesOfTool(MavenConsole.class, "maven-console.txt", 4);
    }

    /** Runs the MetrowerksCWCompiler parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllMetrowerksCWCompilerIssues() {
        shouldFindIssuesOfTool(MetrowerksCWCompiler.class, "MetrowerksCWCompiler.txt", 5);
    }

    /** Runs the MetrowerksCWLinker parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllMetrowerksCWLinkerIssues() {
        shouldFindIssuesOfTool(MetrowerksCWLinker.class, "MetrowerksCWLinker.txt", 3);
    }

    /** Runs the AcuCobol parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllAcuCobolIssues() {
        shouldFindIssuesOfTool(AcuCobol.class, "acu.txt", 4);
    }

    /** Runs the Ajc parser on an output file that contains 9 issues. */
    @Test
    public void shouldFindAllAjcIssues() {
        shouldFindIssuesOfTool(Ajc.class, "ajc.txt", 9);
    }

    /** Runs the AnsibleLint parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllAnsibleLintIssues() {
        shouldFindIssuesOfTool(AnsibleLint.class, "ansibleLint.txt", 4);
    }

    /**
     * Runs the Perl::Critic parser on an output file that contains 105 issues. */
    @Test
    public void shouldFindAllPerlCriticIssues() {
        shouldFindIssuesOfTool(PerlCritic.class, "perlcritic.txt", 105);
    }

    /** Runs the Php parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllPhpIssues() {
        shouldFindIssuesOfTool(Php.class, "php.txt", 5);
    }

    /** Runs the Microsoft PREfast parser on an output file that contains 11 issues. */
    @Test
    public void shouldFindAllPREfastIssues() {
        shouldFindIssuesOfTool(PREfast.class, "PREfast.xml", 11);
    }

    /** Runs the Puppet Lint parser on an output file that contains 5 issues.  */
    @Test
    public void shouldFindAllPuppetLintIssues() {
        shouldFindIssuesOfTool(PuppetLint.class, "puppet-lint.txt", 5);
    }

    /** Runs the Eclipse parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllEclipseIssues() {
        shouldFindIssuesOfTool(Eclipse.class, "eclipse.txt", 8);
    }

    /** Runs the PyLint parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllPyLintParserIssues() {
        shouldFindIssuesOfTool(PyLint.class, "pyLint.txt", 6);
    }

    /**
     * Runs the QACSourceCodeAnalyser parser on an output file that contains 9 issues. */
    @Test
    public void shouldFindAllQACSourceCodeAnalyserIssues() {
        shouldFindIssuesOfTool(QACSourceCodeAnalyser.class, "QACSourceCodeAnalyser.txt", 9);
    }

    /** Runs the Resharper parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllResharperInspectCodeIssues() {
        shouldFindIssuesOfTool(ResharperInspectCode.class, "ResharperInspectCode.xml", 3);
    }

    /** Runs the RFLint parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllRfLintIssues() {
        shouldFindIssuesOfTool(RFLint.class, "rflint.txt", 6);
    }

    /** Runs the Robocopy parser on an output file: the build should report 3 issues. */
    @Test
    public void shouldFindAllRobocopyIssues() {
        shouldFindIssuesOfTool(Robocopy.class, "robocopy.txt", 3);
    }

    /** Runs the ScalaC parser on an output file: the build should report 3 issues. */
    @Test
    public void shouldFindAllScalacIssues() {
        shouldFindIssuesOfTool(Scala.class, "scalac.txt", 3);
    }

    /** Runs the Sphinx build parser on an output file: the build should report 6 issues. */
    @Test
    public void shouldFindAllSphinxIssues() {
        shouldFindIssuesOfTool(SphinxBuild.class, "sphinxbuild.txt", 6);
    }

    /** Runs the SBT scala parser on an output file: the build should report 2 issues. */
    @Test
    public void shouldFindAllSbtScalaCIssues() {
        shouldFindIssuesOfTool(SBTScalaC.class, "sbtScalac.txt", 2);
    }

    /** Runs the Idea Inspection parser on an output file that contains 1 issues. */
    @Test
    public void shouldFindAllIdeaInspectionIssues() {
        shouldFindIssuesOfTool(IdeaInspection.class, "IdeaInspectionExample.xml", 1);
    }

    /** Runs the Intel parser on an output file that contains 7 issues. */
    @Test
    public void shouldFindAllIntelIssues() {
        shouldFindIssuesOfTool(Intel.class, "intelc.txt", 7);
    }

    /** Runs the Oracle Invalids parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllInvalidsIssues() {
        shouldFindIssuesOfTool(Invalids.class, "invalids.txt", 3);
    }

    /** Runs the Java parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllJavaIssues() {
        shouldFindIssuesOfTool(Java.class, "javac.txt", 2);
    }

    /** Runs the CssLint parser on an output file that contains 51 issues. */
    @Test
    public void shouldFindAllCssLintIssues() {
        shouldFindIssuesOfTool(CssLint.class, "csslint.xml", 51);
    }

    /** Runs the DiabC parser on an output file that contains 12 issues. */
    @Test
    public void shouldFindAllDiabCIssues() {
        shouldFindIssuesOfTool(DiabC.class, "diabc.txt", 12);
    }

    /** Runs the Doxygen parser on an output file that contains 21 issues. */
    @Test
    public void shouldFindAllDoxygenIssues() {
        shouldFindIssuesOfTool(Doxygen.class, "doxygen.txt", 21);
    }

    /** Runs the Dr. Memory parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllDrMemoryIssues() {
        shouldFindIssuesOfTool(DrMemory.class, "drmemory.txt", 8);
    }

    /** Runs the JavaC parser on an output file of the Eclipse compiler: the build should report no issues. */
    @Test
    public void shouldFindNoJavacIssuesInEclipseOutput() {
        shouldFindIssuesOfTool(Java.class, "eclipse.txt", 0);
    }

    /** Runs the all Java parsers on three output files: the build should report issues of all tools. */
    @Test
    public void shouldCombineIssuesOfSeveralFiles() {
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
     * Runs the Eclipse parser on an output file that contains several issues. Applies an include filter that selects
     * only one issue (in the file AttributeException.java).
     */
    @Test
    public void shouldIncludeJustOneFile() {
        WorkflowJob job = createJobWithWorkspaceFile("eclipse.txt");
        job.setDefinition(asStage(createScanForIssuesStep(Eclipse.class),
                "publishIssues issues:[issues],  "
                        + "filters:[[property: [$class: 'IncludeFile'], pattern: '.*AttributeException.*']]"));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getIssues()).hasSize(1);
    }

    @SuppressWarnings({"CheckStyle", "OverlyBroadCatchBlock"})
    private void shouldFindIssuesOfTool(final Class<? extends StaticAnalysisTool> tool, final String filename,
            final int expectedSizeOfIssues) {
        try {
            WorkflowJob job = createJobWithWorkspaceFile(filename);
            job.setDefinition(parseAndPublish(tool));

            AnalysisResult result = scheduleBuild(job);

            assertThat(result.getTotalSize()).isEqualTo(expectedSizeOfIssues);
            assertThat(result.getIssues()).hasSize(expectedSizeOfIssues);
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private CpsFlowDefinition parseAndPublish(final Class<? extends StaticAnalysisTool> parserClass) {
        return asStage(createScanForIssuesStep(parserClass), PUBLISH_ISSUES_STEP);
    }

    private String createScanForIssuesStep(final Class<? extends StaticAnalysisTool> parserClass) {
        return createScanForIssuesStep(parserClass, "issues");
    }

    private String createScanForIssuesStep(final Class<? extends StaticAnalysisTool> parserClass,
            final String issuesName) {
        return String.format(
                "def %s = scanForIssues tool: [$class: '%s'], pattern:'**/*issues.txt', defaultEncoding:'UTF-8'",
                issuesName, parserClass.getSimpleName());
    }

    private WorkflowJob createJobWithWorkspaceFile(final String... fileNames) {
        WorkflowJob job = createJob();
        copyFilesToWorkspace(job, fileNames);
        return job;
    }

    private WorkflowJob createJob() {
        try {
            return j.jenkins.createProject(WorkflowJob.class, "Integration-Test");
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
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
    @SuppressWarnings("CheckStyle")
    private AnalysisResult scheduleBuild(final WorkflowJob job) {
        try {
            WorkflowRun run = j.assertBuildStatus(Result.SUCCESS, Objects.requireNonNull(job.scheduleBuild2(0)));

            ResultAction action = run.getAction(ResultAction.class);
            assertThat(action).isNotNull();

            return action.getResult();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
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
