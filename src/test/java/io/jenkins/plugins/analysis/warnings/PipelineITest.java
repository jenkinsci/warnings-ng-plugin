package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;
import org.junit.jupiter.api.Tag;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.BuildIssue;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.Result;

/**
 * Integration tests of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
@Tag("IntegrationTest")
public class PipelineITest extends IntegrationTest {
    private static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";

    /** Runs the Armcc parser on output files that contains 3 + 3 issues. */
    @Test
    public void shouldFindAllArmccIssues() {
        shouldFindIssuesOfTool(3 + 3, ArmCc.class, "armcc5.txt", "armcc.txt");
    }

    /** Runs the Buckminster parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllBuckminsterIssues() {
        shouldFindIssuesOfTool(3, Buckminster.class, "buckminster.txt");
    }

    /** Runs the Cadence parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllCadenceIssues() {
        shouldFindIssuesOfTool(3, Cadence.class, "CadenceIncisive.txt");
    }

    /** Runs the PMD parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllPmdIssues() {
        Issues<BuildIssue> issues = shouldFindIssuesOfTool(4, Pmd.class, "pmd-warnings.xml");

        BuildIssue issue = issues.get(0);

        StaticAnalysisLabelProvider labelProvider = new Pmd().getLabelProvider();
        assertThat(issue).hasDescription(StringUtils.EMPTY);
        assertThat(labelProvider.getDescription(issue)).isEqualTo("\n"
                + "Sometimes two consecutive 'if' statements can be consolidated by separating their conditions with a boolean short-circuit operator.\n"
                + "      <pre>\n  \nvoid bar() {\n\tif (x) {\t\t\t// original implementation\n\t\tif (y) {\n\t\t\t// do stuff\n\t\t}\n"
                + "\t}\n}\n\nvoid bar() {\n\tif (x && y) {\t\t// optimized implementation\n\t\t// do stuff\n\t}\n}\n \n      </pre>");
    }

    /** Runs the CheckStyle parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllCheckStyleIssues() {
        Issues<BuildIssue> issues = shouldFindIssuesOfTool(6, CheckStyle.class, "checkstyle.xml");

        BuildIssue issue = issues.get(2);

        StaticAnalysisLabelProvider labelProvider = new CheckStyle().getLabelProvider();
        assertThat(issue).hasDescription(StringUtils.EMPTY);
        assertThat(labelProvider.getDescription(issue)).contains("finds classes that are designed for extension");
    }

    /** Runs the FindBugs parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllFindBugsIssues() {
        Issues<BuildIssue> issues = shouldFindIssuesOfTool(2, FindBugs.class, "findbugs-native.xml");

        BuildIssue issue = issues.get(0);

        StaticAnalysisLabelProvider labelProvider = new FindBugs().getLabelProvider();
        assertThat(issue).hasDescription(StringUtils.EMPTY);
        assertThat(labelProvider.getDescription(issue))
                .contains(
                        "The fields of this class appear to be accessed inconsistently with respect\n  to synchronization");
    }

    /** Runs the SpotBugs parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllSpotBugsIssues() {
        Issues<BuildIssue> issues = shouldFindIssuesOfTool(2, SpotBugs.class, "spotbugsXml.xml");

        BuildIssue issue = issues.get(0);

        StaticAnalysisLabelProvider labelProvider = new FindBugs().getLabelProvider();
        assertThat(issue).hasDescription(StringUtils.EMPTY);
        assertThat(labelProvider.getDescription(issue))
                .contains("This code calls a method and ignores the return value.");
    }

    /** Runs the Clang parser on an output file that contains 9 issues. */
    @Test
    public void shouldFindAllClangIssues() {
        shouldFindIssuesOfTool(9, Clang.class, "apple-llvm-clang.txt");
    }

    /** Runs the Coolflux parser on an output file that contains 1 issues. */
    @Test
    public void shouldFindAllCoolfluxIssues() {
        shouldFindIssuesOfTool(1, Coolflux.class, "coolfluxchesscc.txt");
    }

    /** Runs the CppLint parser on an output file that contains 1031 issues. */
    @Test
    public void shouldFindAllCppLintIssues() {
        shouldFindIssuesOfTool(1031, CppLint.class, "cpplint.txt");
    }

    /** Runs the CodeAnalysis parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllCodeAnalysisIssues() {
        shouldFindIssuesOfTool(3, CodeAnalysis.class, "codeanalysis.txt");
    }

    /** Runs the GoLint parser on an output file that contains 7 issues. */
    @Test
    public void shouldFindAllGoLintIssues() {
        shouldFindIssuesOfTool(7, GoLint.class, "golint.txt");
    }

    /** Runs the GoVet parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllGoVetIssues() {
        shouldFindIssuesOfTool(2, GoVet.class, "govet.txt");
    }

    /** Runs the SunC parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllSunCIssues() {
        shouldFindIssuesOfTool(8, SunC.class, "sunc.txt");
    }

    /** Runs the JcReport parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllJcReportIssues() {
        shouldFindIssuesOfTool(5, JcReport.class, "jcreport.xml");
    }

    /** Runs the LinuxKernel parser on an output file that contains 26 issues. */
    @Test
    public void shouldFindAllLinuxKernelIssues() {
        shouldFindIssuesOfTool(26, LinuxKernelOutput.class, "kernel.log");
    }

    /** Runs the StyleCop parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllStyleCopIssues() {
        shouldFindIssuesOfTool(5, StyleCop.class, "stylecop.xml");
    }

    /** Runs the Tasking VX parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllTaskingVxIssues() {
        shouldFindIssuesOfTool(8, TaskingVx.class, "tasking-vx.txt");
    }

    /** Runs the tnsdl translator parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllTnsdlIssues() {
        shouldFindIssuesOfTool(4, Tnsdl.class, "tnsdl.txt");
    }

    /** Runs the Texas Instruments Code Composer Studio parser on an output file that contains 10 issues. */
    @Test
    public void shouldFindAllTiCssIssues() {
        shouldFindIssuesOfTool(10, TiCss.class, "ticcs.txt");
    }

    /** Runs the IBM XLC compiler and linker parser on an output file that contains 1 + 1 issues. */
    @Test
    public void shouldFindAllXlcIssues() {
        shouldFindIssuesOfTool(2, Xlc.class, "xlc.txt");
    }

    /** Runs the YIU compressor parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllYuiCompressorIssues() {
        shouldFindIssuesOfTool(3, YuiCompressor.class, "yui.txt");
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

    /** Runs the MetrowerksCWCompiler parser on two output files that contains 5 + 3 issues. */
    @Test
    public void shouldFindAllMetrowerksCWCompilerIssues() {
        shouldFindIssuesOfTool(5 + 3, MetrowerksCodeWarrior.class, "MetrowerksCWCompiler.txt",
                "MetrowerksCWLinker.txt");
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
     * Runs the Perl::Critic parser on an output file that contains 105 issues.
     */
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

    /** Runs the Puppet Lint parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllPuppetLintIssues() {
        shouldFindIssuesOfTool(5, PuppetLint.class, "puppet-lint.txt");
    }

    /**
     * Runs the Eclipse parser on the console log that contains 8 issues which are decorated with console notes. The
     * output file is copied to the console log using a shell cat command.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-11675">Issue 11675</a>
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    @Test
    public void issue11675() {
        try {
            WorkflowJob job = createJobWithWorkspaceFiles("issue11675.txt");
            String scanStep = String.format("def %s = scanForIssues tool: 'eclipse'", "issues");
            job.setDefinition(asStage("sh 'cat issue11675-issues.txt'", scanStep, PUBLISH_ISSUES_STEP));

            AnalysisResult result = scheduleBuild(job);

            assertThat(result.getTotalSize()).isEqualTo(8);
            assertThat(result.getIssues()).hasSize(8);

            Issues<BuildIssue> issues = result.getIssues();
            assertThat(issues.filter(issue -> "eclipse".equals(issue.getOrigin()))).hasSize(8);
            for (Issue annotation : issues) {
                assertThat(annotation.getMessage()).matches("[a-zA-Z].*");
            }
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
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
     * Runs the QACSourceCodeAnalyser parser on an output file that contains 9 issues.
     */
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

    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private Issues<BuildIssue> shouldFindIssuesOfTool(final int expectedSizeOfIssues,
            final Class<? extends StaticAnalysisTool> tool,
            final String... fileNames) {
        try {
            WorkflowJob job = createJobWithWorkspaceFiles(fileNames);
            job.setDefinition(parseAndPublish(tool));

            AnalysisResult result = scheduleBuild(job);

            assertThat(result.getTotalSize()).isEqualTo(expectedSizeOfIssues);
            assertThat(result.getIssues()).hasSize(expectedSizeOfIssues);

            Issues<BuildIssue> issues = result.getIssues();
            StaticAnalysisTool analysisTool = tool.newInstance();
            assertThat(issues.filter(issue -> issue.getOrigin().equals(analysisTool.getId())))
                    .hasSize(expectedSizeOfIssues);

            return issues;
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

    private String createScanForIssuesStep(final Class<? extends StaticAnalysisTool> tool, final String issuesName) {
        try {
            StaticAnalysisTool analysisTool = tool.newInstance();
            return String.format(
                    "def %s = scanForIssues tool: '%s', pattern:'**/*issues.txt', defaultEncoding:'UTF-8'",
                    issuesName, analysisTool.getId());
        }
        catch (InstantiationException | IllegalAccessException exception) {
            throw new AssertionError(exception);
        }
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
    @SuppressWarnings("illegalcatch")
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
