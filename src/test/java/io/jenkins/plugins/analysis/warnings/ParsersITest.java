package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;

/**
 * Integration tests of all parsers of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 */
public class ParsersITest extends PipelineITest {
    private static final String CODE_FRAGMENT = "<pre><code>#\n"
            + "\n"
            + "    ERROR HANDLING: N/A\n"
            + "    #\n"
            + "    REMARKS: N/A\n"
            + "    #\n"
            + "    ****************************** END HEADER *************************************\n"
            + "    #\n"
            + "\n"
            + "    ***************************** BEGIN PDL ***************************************\n"
            + "    #\n"
            + "    ****************************** END PDL ****************************************\n"
            + "    #\n"
            + "\n"
            + "    ***************************** BEGIN CODE **************************************\n"
            + "    **\n"
            + "    *******************************************************************************\n"
            + "\n"
            + "    *******************************************************************************\n"
            + "    *******************************************************************************\n"
            + "\n"
            + "if [ $# -lt 3 ]\n"
            + "then\n"
            + "exit 1\n"
            + "fi\n"
            + "\n"
            + "    *******************************************************************************\n"
            + "    initialize local variables\n"
            + "    shift input parameter (twice) to leave only files to copy\n"
            + "    *******************************************************************************\n"
            + "\n"
            + "files=\"\"\n"
            + "shift\n"
            + "shift\n"
            + "\n"
            + "    *******************************************************************************\n"
            + "    *******************************************************************************\n"
            + "\n"
            + "for i in $*\n"
            + "do\n"
            + "files=\"$files $directory/$i\"\n"
            + "done</code></pre>";

    /** Runs the Android Lint parser on output files that contains 2 issues. */
    @Test
    public void shouldFindAllAndroidLintIssues() {
        shouldFindIssuesOfTool(2, AndroidLint.class, "android-lint.xml");
    }

    /** Runs the CodeNarc parser on output files that contains 11 issues. */
    @Test
    public void shouldFindAllCodeNArcIssues() {
        shouldFindIssuesOfTool(11, CodeNArc.class, "codeNarc.xml");
    }

    /** Runs the Cppcheck parser on output files that contains 3 issues. */
    @Test
    public void shouldFindAllCppCheckIssues() {
        shouldFindIssuesOfTool(3, CppCheck.class, "cppcheck.xml");
    }

    /** Runs the DocFx parser on output files that contains 3 issues. */
    @Test
    public void shouldFindAllDocFXIssues() {
        shouldFindIssuesOfTool(3, DocFx.class, "docfx.json");
    }

    /** Runs the ErrorProne parser on output files that contains 5 issues. */
    @Test
    public void shouldFindAllErrorProneIssues() {
        shouldFindIssuesOfTool(5, ErrorProne.class, "error-prone.log");
    }

    /** Runs the Flake8 parser on output files that contains 12 issues. */
    @Test
    public void shouldFindAllFlake8Issues() {
        shouldFindIssuesOfTool(12, Flake8.class, "flake8.txt");
    }

    /** Runs the JSHint parser on output files that contains 6 issues. */
    @Test
    public void shouldFindAllJsHintIssues() {
        shouldFindIssuesOfTool(6, JsHint.class, "jshint.xml");
    }

    /** Runs the Klocwork parser on output files that contains 2 issues. */
    @Test
    public void shouldFindAllKlocWorkIssues() {
        shouldFindIssuesOfTool(2, KlocWork.class, "klocwork.xml");
    }

    /** Runs the MyPy parser on output files that contains 5 issues. */
    @Test
    public void shouldFindAllMyPyIssues() {
        shouldFindIssuesOfTool(5, MyPy.class, "mypy.txt");
    }

    /** Runs the PIT parser on output files that contains 25 issues. */
    @Test
    public void shouldFindAllPitIssues() {
        shouldFindIssuesOfTool(25, Pit.class, "pit.xml");
    }

    /** Runs the PyDocStyle parser on output files that contains 33 issues. */
    @Test
    public void shouldFindAllPyDocStyleIssues() {
        shouldFindIssuesOfTool(33, PyDocStyle.class, "pydocstyle.txt");
    }

    /** Runs the XML Lint parser on output files that contains 3 issues. */
    @Test
    public void shouldFindAllXmlLintStyleIssues() {
        shouldFindIssuesOfTool(3, XmlLint.class, "xmllint.txt");
    }

    /** Runs the zptlint parser on output files that contains 2 issues. */
    @Test
    public void shouldFindAllZptLintStyleIssues() {
        shouldFindIssuesOfTool(2, ZptLint.class, "zptlint.log");
    }

    /** Runs the CPD parser on output files that contains 2 issues. */
    @Test
    public void shouldFindAllCpdIssues() {
        Issues<?> issues = shouldFindIssuesOfTool(2, Cpd.class, "cpd.xml");

        assertThat(issues.get(0)).hasDescription(CODE_FRAGMENT);
    }

    /** Runs the Simian parser on output files that contains 4 issues. */
    @Test
    public void shouldFindAllSimianIssues() {
        shouldFindIssuesOfTool(4, Simian.class, "simian.xml");
    }

    /** Runs the DupFinder parser on output files that contains 2 issues. */
    @Test
    public void shouldFindAllDupFinderIssues() {
        Issues<?> issues = shouldFindIssuesOfTool(2, DupFinder.class, "dupfinder.xml");

        assertThat(issues.get(0)).hasDescription("<pre><code>if (items == null) throw new ArgumentNullException(\"items\");</code></pre>");
    }

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
        Issues<?> issues = shouldFindIssuesOfTool(4, Pmd.class, "pmd-warnings.xml");

        Issue issue = issues.get(0);

        StaticAnalysisLabelProvider labelProvider = new Pmd().getLabelProvider();
        assertThat(issue).hasDescription(StringUtils.EMPTY);
        Assertions.assertThat(labelProvider.getDescription(issue)).isEqualTo("\n"
                + "Sometimes two consecutive 'if' statements can be consolidated by separating their conditions with a boolean short-circuit operator.\n"
                + "      <pre>\n  \nvoid bar() {\n\tif (x) {\t\t\t// original implementation\n\t\tif (y) {\n\t\t\t// do stuff\n\t\t}\n"
                + "\t}\n}\n\nvoid bar() {\n\tif (x && y) {\t\t// optimized implementation\n\t\t// do stuff\n\t}\n}\n \n      </pre>");
    }

    /** Runs the CheckStyle parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllCheckStyleIssues() {
        Issues<?> issues = shouldFindIssuesOfTool(6, CheckStyle.class, "checkstyle.xml");

        Issue issue = issues.get(2);

        StaticAnalysisLabelProvider labelProvider = new CheckStyle().getLabelProvider();
        assertThat(issue).hasDescription(StringUtils.EMPTY);
        Assertions.assertThat(labelProvider.getDescription(issue)).contains("finds classes that are designed for extension");
    }

    /** Runs the FindBugs parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllFindBugsIssues() {
        Issues<?> issues = shouldFindIssuesOfTool(2, FindBugs.class, "findbugs-native.xml");

        Issue issue = issues.get(0);

        StaticAnalysisLabelProvider labelProvider = new FindBugs().getLabelProvider();
        assertThat(issue).hasDescription(StringUtils.EMPTY);
        Assertions.assertThat(labelProvider.getDescription(issue))
                .contains(
                        "The fields of this class appear to be accessed inconsistently with respect\n  to synchronization");
    }

    /** Runs the SpotBugs parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllSpotBugsIssues() {
        Issues<?> issues = shouldFindIssuesOfTool(2, SpotBugs.class, "spotbugsXml.xml");

        Issue issue = issues.get(0);

        StaticAnalysisLabelProvider labelProvider = new FindBugs().getLabelProvider();
        assertThat(issue).hasDescription(StringUtils.EMPTY);
        Assertions.assertThat(labelProvider.getDescription(issue))
                .contains("This code calls a method and ignores the return value.");
    }

    /** Runs the Clang-Tidy parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllClangTidyIssues() {
        shouldFindIssuesOfTool(6, ClangTidy.class, "ClangTidy.txt");
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

    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private Issues<?> shouldFindIssuesOfTool(final int expectedSizeOfIssues,
            final Class<? extends StaticAnalysisTool> tool, final String... fileNames) {
        try {
            WorkflowJob job = createJobWithWorkspaceFiles(fileNames);
            job.setDefinition(parseAndPublish(tool));

            AnalysisResult result = scheduleBuild(job, tool);

            assertThat(result.getTotalSize()).isEqualTo(expectedSizeOfIssues);
            assertThat(result.getIssues()).hasSize(expectedSizeOfIssues);

            Issues<?> issues = result.getIssues();
            assertThat(issues.filter(issue -> issue.getOrigin().equals(getIdOf(tool))))
                    .hasSize(expectedSizeOfIssues);

            return issues;
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
