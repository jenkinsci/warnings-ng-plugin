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
import jenkins.model.Jenkins;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
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

    /** Verifies that the number of parsers is 64. */
    @Test
    public void shouldFindAllRegisteredParsers() {
        DescriptorExtensionList<StaticAnalysisTool, Descriptor<StaticAnalysisTool>> descriptors
                = Jenkins.getInstance().getDescriptorList(StaticAnalysisTool.class);
        int count = 0;
        for (Descriptor<StaticAnalysisTool> descriptor : descriptors) {
            if (descriptor.clazz.getPackage().equals(Eclipse.class.getPackage())) {
                System.out.format("%s: %s\n", descriptor.getId(), descriptor.getDisplayName());
            }
        }
    }

    /** Runs the Cadence parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllCadenceIssues() {
        shouldFindIssuesOfTool(3, Cadence.class, "CadenceIncisive.txt");
    }

    /** Runs the Erlc parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllErlcIssues() {
        shouldFindIssuesOfTool(2, Erlc.class, "erlc.txt");
    }

    /** Runs the FlexSDK parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllFlexSDKIssues() {
        shouldFindIssuesOfTool(5, FlexSDK.class, "flexsdk.txt");
    }

    /** Runs the FxCop parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllFxcopSDKIssues() {
        shouldFindIssuesOfTool(2, Fxcop.class, "fxcop.xml");
    }

    /** Runs the Gendarme parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllGendarmeIssues() {
        shouldFindIssuesOfTool(3, Gendarme.class, "Gendarme.xml");
    }

    /** Runs the GhsMulti parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllGhsMultiIssues() {
        shouldFindIssuesOfTool(3, GhsMulti.class, "ghsmulti.txt");
    }

    /**
     * Runs the Gnat parser on an output file that contains 9 issues.
     */
    @Test
    public void shouldFindAllGnatIssues() {
        shouldFindIssuesOfTool(9, Gnat.class, "gnat.txt");
    }

    /** Runs the GnuFortran parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllGnuFortranIssues() {
        shouldFindIssuesOfTool(4, GnuFortran.class, "GnuFortran.txt");
    }

    /** Runs the GnuMakeGcc parser on an output file that contains 15 issues. */
    @Test
    public void shouldFindAllGnuMakeGccIssues() {
        shouldFindIssuesOfTool(15, GnuMakeGcc.class, "gnuMakeGcc.txt");
    }

    /** Runs the MsBuild parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllMsBuildIssues() {
        shouldFindIssuesOfTool(6, MsBuild.class, "msbuild.txt");
    }

    /** Runs the NagFortran parser on an output file that contains 10 issues. */
    @Test
    public void shouldFindAllNagFortranIssues() {
        shouldFindIssuesOfTool(10, NagFortran.class, "NagFortran.txt");
    }

    /** Runs the Perforce parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllP4Issues() {
        shouldFindIssuesOfTool(4, Perforce.class, "perforce.txt");
    }

    /** Runs the Pep8 parser on an output file: the build should report 8 issues. */
    @Test
    public void shouldFindAllPep8Issues() {
        shouldFindIssuesOfTool(8, Pep8.class, "pep8Test.txt");
    }

    /** Runs the Gcc3Compiler parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllGcc3CompilerIssues() {
        shouldFindIssuesOfTool(8, Gcc3.class, "gcc.txt");
    }

    /** Runs the Gcc4Compiler and Gcc4Linker parsers on separate output file that contains 14 + 7 issues. */
    @Test
    public void shouldFindAllGcc4Issues() {
        shouldFindIssuesOfTool(14 + 7, Gcc4.class, "gcc4.txt", "gcc4ld.txt");
    }

    /** Runs the Maven console parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllMavenConsoleIssues() {
        shouldFindIssuesOfTool(4, MavenConsole.class, "maven-console.txt");
    }

    /** Runs the MetrowerksCWCompiler parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllMetrowerksCWCompilerIssues() {
        shouldFindIssuesOfTool(5, MetrowerksCWCompiler.class, "MetrowerksCWCompiler.txt");
    }

    /** Runs the MetrowerksCWLinker parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllMetrowerksCWLinkerIssues() {
        shouldFindIssuesOfTool(3, MetrowerksCWLinker.class, "MetrowerksCWLinker.txt");
    }

    /** Runs the AcuCobol parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllAcuCobolIssues() {
        shouldFindIssuesOfTool(4, AcuCobol.class, "acu.txt");
    }

    /** Runs the Ajc parser on an output file that contains 9 issues. */
    @Test
    public void shouldFindAllAjcIssues() {
        shouldFindIssuesOfTool(9, Ajc.class, "ajc.txt");
    }

    /** Runs the AnsibleLint parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllAnsibleLintIssues() {
        shouldFindIssuesOfTool(4, AnsibleLint.class, "ansibleLint.txt");
    }

    /**
     * Runs the Perl::Critic parser on an output file that contains 105 issues. */
    @Test
    public void shouldFindAllPerlCriticIssues() {
        shouldFindIssuesOfTool(105, PerlCritic.class, "perlcritic.txt");
    }

    /** Runs the Php parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllPhpIssues() {
        shouldFindIssuesOfTool(5, Php.class, "php.txt");
    }

    /** Runs the Microsoft PREfast parser on an output file that contains 11 issues. */
    @Test
    public void shouldFindAllPREfastIssues() {
        shouldFindIssuesOfTool(11, PREfast.class, "PREfast.xml");
    }

    /** Runs the Puppet Lint parser on an output file that contains 5 issues.  */
    @Test
    public void shouldFindAllPuppetLintIssues() {
        shouldFindIssuesOfTool(5, PuppetLint.class, "puppet-lint.txt");
    }

    /** Runs the Eclipse parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllEclipseIssues() {
        shouldFindIssuesOfTool(8, Eclipse.class, "eclipse.txt");
    }

    /** Runs the PyLint parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllPyLintParserIssues() {
        shouldFindIssuesOfTool(6, PyLint.class, "pyLint.txt");
    }

    /**
     * Runs the QACSourceCodeAnalyser parser on an output file that contains 9 issues. */
    @Test
    public void shouldFindAllQACSourceCodeAnalyserIssues() {
        shouldFindIssuesOfTool(9, QACSourceCodeAnalyser.class, "QACSourceCodeAnalyser.txt");
    }

    /** Runs the Resharper parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllResharperInspectCodeIssues() {
        shouldFindIssuesOfTool(3, ResharperInspectCode.class, "ResharperInspectCode.xml");
    }

    /** Runs the RFLint parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllRfLintIssues() {
        shouldFindIssuesOfTool(6, RFLint.class, "rflint.txt");
    }

    /** Runs the Robocopy parser on an output file: the build should report 3 issues. */
    @Test
    public void shouldFindAllRobocopyIssues() {
        shouldFindIssuesOfTool(3, Robocopy.class, "robocopy.txt");
    }

    /** Runs the Scala and SbtScala parser on separate output files: the build should report 2+3 issues. */
    @Test
    public void shouldFindAllScalaIssues() {
        shouldFindIssuesOfTool(2 + 3, Scala.class, "scalac.txt", "sbtScalac.txt");
    }

    /** Runs the Sphinx build parser on an output file: the build should report 6 issues. */
    @Test
    public void shouldFindAllSphinxIssues() {
        shouldFindIssuesOfTool(6, SphinxBuild.class, "sphinxbuild.txt");
    }

    /** Runs the Idea Inspection parser on an output file that contains 1 issues. */
    @Test
    public void shouldFindAllIdeaInspectionIssues() {
        shouldFindIssuesOfTool(1, IdeaInspection.class, "IdeaInspectionExample.xml");
    }

    /** Runs the Intel parser on an output file that contains 7 issues. */
    @Test
    public void shouldFindAllIntelIssues() {
        shouldFindIssuesOfTool(7, Intel.class, "intelc.txt");
    }

    /** Runs the Oracle Invalids parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllInvalidsIssues() {
        shouldFindIssuesOfTool(3, Invalids.class, "invalids.txt");
    }

    /** Runs the Java parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllJavaIssues() {
        shouldFindIssuesOfTool(2, Java.class, "javac.txt");
    }

    /** Runs the CssLint parser on an output file that contains 51 issues. */
    @Test
    public void shouldFindAllCssLintIssues() {
        shouldFindIssuesOfTool(51, CssLint.class, "csslint.xml");
    }

    /** Runs the DiabC parser on an output file that contains 12 issues. */
    @Test
    public void shouldFindAllDiabCIssues() {
        shouldFindIssuesOfTool(12, DiabC.class, "diabc.txt");
    }

    /** Runs the Doxygen parser on an output file that contains 21 issues. */
    @Test
    public void shouldFindAllDoxygenIssues() {
        shouldFindIssuesOfTool(21, Doxygen.class, "doxygen.txt");
    }

    /** Runs the Dr. Memory parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllDrMemoryIssues() {
        shouldFindIssuesOfTool(8, DrMemory.class, "drmemory.txt");
    }

    /** Runs the JavaC parser on an output file of the Eclipse compiler: the build should report no issues. */
    @Test
    public void shouldFindNoJavacIssuesInEclipseOutput() {
        shouldFindIssuesOfTool(0, Java.class, "eclipse.txt");
    }

    /** Runs the all Java parsers on three output files: the build should report issues of all tools. */
    @Test
    public void shouldCombineIssuesOfSeveralFiles() {
        WorkflowJob job = createJobWithWorkspaceFiles("eclipse.txt", "javadoc.txt", "javac.txt");
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
        WorkflowJob job = createJobWithWorkspaceFiles("eclipse.txt");
        job.setDefinition(asStage(createScanForIssuesStep(Eclipse.class),
                "publishIssues issues:[issues],  "
                        + "filters:[[property: [$class: 'IncludeFile'], pattern: '.*AttributeException.*']]"));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getIssues()).hasSize(1);
    }

    @SuppressWarnings({"CheckStyle", "OverlyBroadCatchBlock"})
    private void shouldFindIssuesOfTool(final int expectedSizeOfIssues, final Class<? extends StaticAnalysisTool> tool,
            final String... filenames) {
        try {
            WorkflowJob job = createJobWithWorkspaceFiles(filenames);
            job.setDefinition(parseAndPublish(tool));

            AnalysisResult result = scheduleBuild(job);

            assertThat(result.getTotalSize()).isEqualTo(expectedSizeOfIssues);
            assertThat(result.getIssues()).hasSize(expectedSizeOfIssues);

            Issues<BuildIssue> issues = result.getIssues();
            Descriptor<?> descriptor = Jenkins.getInstance().getDescriptor(tool);
            assertThat(issues.filter(issue -> descriptor.getId().equals(issue.getOrigin()))).hasSize(expectedSizeOfIssues);
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

    private WorkflowJob createJobWithWorkspaceFiles(final String... fileNames) {
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
