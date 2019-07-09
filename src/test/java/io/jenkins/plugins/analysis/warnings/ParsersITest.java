package io.jenkins.plugins.analysis.warnings;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of all parsers of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.ExcessivePublicCount", "ClassDataAbstractionCoupling", "ClassFanOutComplexity"})
public class ParsersITest extends IntegrationTestWithJenkinsPerSuite {
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
            + "files&#61;&#34;&#34;\n"
            + "shift\n"
            + "shift\n"
            + "\n"
            + "    *******************************************************************************\n"
            + "    *******************************************************************************\n"
            + "\n"
            + "for i in $*\n"
            + "do\n"
            + "files&#61;&#34;$files $directory/$i&#34;\n"
            + "done</code></pre>";

    /** Runs the native parser on a file that contains 9 issues.. */
    @Test
    public void shouldReadNativeFormats() {
        shouldFindIssuesOfTool(9 + 5 + 5, new WarningsPlugin(), "warnings-issues.xml", "issues.json", "json-issues.log");
    }

    /** Runs the native parser on a file that contains 9 issues.. */
    @Test
    public void shouldReadNativeXmlFormat() {
        shouldFindIssuesOfTool(9, new WarningsPlugin(), "warnings-issues.xml");
    }

    /** Runs the native parser on a file that contains 5 issues.. */
    @Test
    public void shouldReadNativeJsonFormat() {
        shouldFindIssuesOfTool(5, new WarningsPlugin(), "issues.json");
    }

    /** Runs the native parser on a file that contains 8 issues.. */
    @Test
    public void shouldReadNativeJsonLogFormat() {
        shouldFindIssuesOfTool(5, new WarningsPlugin(), "json-issues.log");
    }

    /** Verifies that a broken file does not fail. */
    @Test
    public void shouldSilentlyIgnoreWrongFile() {
        shouldFindIssuesOfTool(0, new CheckStyle(), "sun_checks.xml");
    }

    /**
     * Runs with several tools that internally delegate to CheckStyle's  parser on an output file that contains 6
     * issues.
     */
    @Test
    public void shouldFindAllIssuesForCheckStyleAlias() {
        for (ReportScanningTool tool : Arrays.asList(new Detekt(), new EsLint(), new KtLint(), new PhpCodeSniffer(),
                new SwiftLint(), new TsLint())) {
            shouldFindIssuesOfTool(6, tool, "checkstyle.xml");
        }
    }

    /** Runs the Iar parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllCmakeIssues() {
        shouldFindIssuesOfTool(8, new Cmake(), "cmake.txt");
    }

    /** Runs the Iar parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllCargoIssues() {
        shouldFindIssuesOfTool(2, new Cargo(), "CargoCheck.json");
    }

    /** Runs the Iar parser on an output file that contains 262 issues. */
    @Test
    public void shouldFindAllIssuesForPmdAlias() {
        shouldFindIssuesOfTool(262, new Infer(), "pmd-6.xml");
    }

    /** Runs the Iar parser on an output file that contains 262 issues. */
    @Test
    public void shouldFindAllIssuesForMsBuildAlias() {
        shouldFindIssuesOfTool(6, new PcLint(), "msbuild.txt");
    }

    /** Runs the Iar parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllYamlLintIssues() {
        shouldFindIssuesOfTool(4, new YamlLint(), "yamllint.txt");
    }

    /** Runs the Iar parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllIarIssues() {
        shouldFindIssuesOfTool(6, new Iar(), "iar.txt");
    }

    /** Runs the IbLinter parser on an output file that contains 1 issue. */
    @Test
    public void shouldFindAllIbLinterIssues() {
        shouldFindIssuesOfTool(1, new IbLinter(), "iblinter.xml");
    }

    /** Runs the IarCStat parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllIarCStatIssues() {
        shouldFindIssuesOfTool(6, new IarCstat(), "iar-cstat.txt");
    }

    /** Runs the TagList parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllOpenTasks() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("tasks/file-with-tasks.txt");
        job.setDefinition(asStage(
                "def issues = scanForIssues tool: "
                        + "taskScanner(includePattern:'**/*issues.txt', highTags:'FIXME', normalTags:'TODO')",
                PUBLISH_ISSUES_STEP));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(2);
        assertThat(result.getIssues()).hasSize(2).hasSeverities(0, 1, 1, 0);
    }

    /** Runs the SonarQube parsers on two files that contains 6 and 31 issues. */
    @Test
    public void shouldFindAllSonarQubeIssues() {
        shouldFindIssuesOfTool(32, new SonarQube(), "sonarqube-api.json");
        shouldFindIssuesOfTool(6, new SonarQube(), "sonarqube-differential.json");
        shouldFindIssuesOfTool(38, new SonarQube(), "sonarqube-api.json", "sonarqube-differential.json");
    }

    /** Runs the TagList parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllTagListIssues() {
        shouldFindIssuesOfTool(4, new TagList(), "taglist.xml");
    }

    /** Runs the Ccm parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllCcmIssues() {
        shouldFindIssuesOfTool(6, new Ccm(), "ccm.xml");
    }

    /** Runs the ruboCop parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllRuboCopIssues() {
        shouldFindIssuesOfTool(2, new RuboCop(), "rubocop.log");
    }

    /** Runs the Android Lint parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllAndroidLintIssues() {
        shouldFindIssuesOfTool(2, new AndroidLint(), "android-lint.xml");
    }

    /** Runs the CodeNarc parser on an output file that contains 11 issues. */
    @Test
    public void shouldFindAllCodeNArcIssues() {
        shouldFindIssuesOfTool(11, new CodeNarc(), "codeNarc.xml");
    }

    /** Runs the Cppcheck parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllCppCheckIssues() {
        shouldFindIssuesOfTool(3, new CppCheck(), "cppcheck.xml");
    }

    /** Runs the DocFx parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllDocFXIssues() {
        shouldFindIssuesOfTool(3, new DocFx(), "docfx.json");
    }

    /** Runs the ErrorProne parser on output files that contain 9 + 2 issues. */
    @Test
    public void shouldFindAllErrorProneIssues() {
        shouldFindIssuesOfTool(9 + 2, new ErrorProne(), "errorprone-maven.log", "gradle-error-prone.log");
    }

    /** Runs the Flake8 parser on an output file that contains 12 issues. */
    @Test
    public void shouldFindAllFlake8Issues() {
        shouldFindIssuesOfTool(12, new Flake8(), "flake8.txt");
    }

    /** Runs the JSHint parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllJsHintIssues() {
        shouldFindIssuesOfTool(6, new JsHint(), "jshint.xml");
    }

    /** Runs the Klocwork parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllKlocWorkIssues() {
        shouldFindIssuesOfTool(2, new KlocWork(), "klocwork.xml");
    }

    /** Runs the MyPy parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllMyPyIssues() {
        shouldFindIssuesOfTool(5, new MyPy(), "mypy.txt");
    }

    /** Runs the PIT parser on an output file that contains 25 issues. */
    @Test
    public void shouldFindAllPitIssues() {
        shouldFindIssuesOfTool(22, new Pit(), "pit.xml");
    }

    /** Runs the PyDocStyle parser on an output file that contains 33 issues. */
    @Test
    public void shouldFindAllPyDocStyleIssues() {
        shouldFindIssuesOfTool(33, new PyDocStyle(), "pydocstyle.txt");
    }

    /** Runs the XML Lint parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllXmlLintStyleIssues() {
        shouldFindIssuesOfTool(3, new XmlLint(), "xmllint.txt");
    }

    /** Runs the zptlint parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllZptLintStyleIssues() {
        shouldFindIssuesOfTool(2, new ZptLint(), "zptlint.log");
    }

    /** Runs the CPD parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllCpdIssues() {
        Report report = shouldFindIssuesOfTool(2, new Cpd(), "cpd.xml");

        assertThatDescriptionOfIssueIsSet(new Cpd(), report.get(0), CODE_FRAGMENT);
    }

    /** Runs the Simian parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllSimianIssues() {
        shouldFindIssuesOfTool(4, new Simian(), "simian.xml");
    }

    /** Runs the DupFinder parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllDupFinderIssues() {
        Report report = shouldFindIssuesOfTool(2, new DupFinder(), "dupfinder.xml");

        assertThatDescriptionOfIssueIsSet(new DupFinder(), report.get(0),
                "<pre><code>if (items &#61;&#61; null) throw new ArgumentNullException(&#34;items&#34;);</code></pre>");
    }

    /** Runs the Armcc parser on output files that contain 3 + 3 issues. */
    @Test
    public void shouldFindAllArmccIssues() {
        shouldFindIssuesOfTool(3 + 3, new ArmCc(), "armcc5.txt", "armcc.txt");
    }

    /** Runs the Buckminster parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllBuckminsterIssues() {
        shouldFindIssuesOfTool(3, new Buckminster(), "buckminster.txt");
    }

    /** Runs the Cadence parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllCadenceIssues() {
        shouldFindIssuesOfTool(3, new Cadence(), "CadenceIncisive.txt");
    }

    /** Runs the Mentor parser on an output file that contains 13 issues. */
    @Test
    public void shouldFindAllMentorGraphicsIssues() {
        shouldFindIssuesOfTool(13, new MentorGraphics(), "MentorGraphics.log");
    }

    /** Runs the PMD parser on an output file that contains 262 issues (PMD 6.1.0). */
    @Test
    public void shouldFindAllPmdIssues() {
        Report report = shouldFindIssuesOfTool(262, new Pmd(), "pmd-6.xml");

        assertThatDescriptionOfIssueIsSet(new Pmd(), report.get(0),
                "A high number of imports can indicate a high degree of coupling within an object.");
    }

    /** Runs the CheckStyle parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllCheckStyleIssues() {
        Report report = shouldFindIssuesOfTool(6, new CheckStyle(), "checkstyle.xml");

        assertThatDescriptionOfIssueIsSet(new CheckStyle(), report.get(2),
                "<p>Since Checkstyle 3.1</p><p>");
        StaticAnalysisLabelProvider labelProvider = new CheckStyle().getLabelProvider();
        assertThat(labelProvider.getDescription(report.get(2)))
                .contains("The check finds classes that are designed for extension (subclass creation).");
    }

    private void assertThatDescriptionOfIssueIsSet(final Tool tool, final Issue issue,
            final String expectedDescription) {
        StaticAnalysisLabelProvider labelProvider = tool.getLabelProvider();
        assertThat(issue).hasDescription("");
        assertThat(labelProvider.getDescription(issue)).contains(expectedDescription);
    }

    /** Runs the FindBugs parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllFindBugsIssues() {
        Report report = shouldFindIssuesOfTool(2, new FindBugs(), "findbugs-native.xml");

        assertThatDescriptionOfIssueIsSet(new FindBugs(), report.get(0),
                "<p> The fields of this class appear to be accessed inconsistently with respect\n"
                        + "  to synchronization.&nbsp; This bug report indicates that the bug pattern detector\n"
                        + "  judged that\n"
                        + "  </p>\n"
                        + "  <ul>\n"
                        + "  <li> The class contains a mix of locked and unlocked accesses,</li>\n"
                        + "  <li> The class is <b>not</b> annotated as javax.annotation.concurrent.NotThreadSafe,</li>\n"
                        + "  <li> At least one locked access was performed by one of the class's own methods, and</li>\n"
                        + "  <li> The number of unsynchronized field accesses (reads and writes) was no more than\n"
                        + "       one third of all accesses, with writes being weighed twice as high as reads</li>\n"
                        + "  </ul>\n"
                        + "\n"
                        + "  <p> A typical bug matching this bug pattern is forgetting to synchronize\n"
                        + "  one of the methods in a class that is intended to be thread-safe.</p>\n"
                        + "\n"
                        + "  <p> You can select the nodes labeled \"Unsynchronized access\" to show the\n"
                        + "  code locations where the detector believed that a field was accessed\n"
                        + "  without synchronization.</p>\n"
                        + "\n"
                        + "  <p> Note that there are various sources of inaccuracy in this detector;\n"
                        + "  for example, the detector cannot statically detect all situations in which\n"
                        + "  a lock is held.&nbsp; Also, even when the detector is accurate in\n"
                        + "  distinguishing locked vs. unlocked accesses, the code in question may still\n"
                        + "  be correct.</p>");
    }

    /** Runs the SpotBugs parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllSpotBugsIssues() {
        Report report = shouldFindIssuesOfTool(2, new SpotBugs(), "spotbugsXml.xml");

        assertThatDescriptionOfIssueIsSet(new SpotBugs(), report.get(0),
                "<p>This code calls a method and ignores the return value. However our analysis shows that\n"
                        + "the method (including its implementations in subclasses if any) does not produce any effect \n"
                        + "other than return value. Thus this call can be removed.\n"
                        + "</p>\n"
                        + "<p>We are trying to reduce the false positives as much as possible, but in some cases this warning might be wrong.\n"
                        + "Common false-positive cases include:</p>\n"
                        + "<p>- The method is designed to be overridden and produce a side effect in other projects which are out of the scope of the analysis.</p>\n"
                        + "<p>- The method is called to trigger the class loading which may have a side effect.</p>\n"
                        + "<p>- The method is called just to get some exception.</p>\n"
                        + "<p>If you feel that our assumption is incorrect, you can use a @CheckReturnValue annotation\n"
                        + "to instruct FindBugs that ignoring the return value of this method is acceptable.\n"
                        + "</p>");
    }

    /** Runs the SpotBugs parser on an output file that contains 2 issues. */
    @Test
    public void shouldProvideMessagesAndDescriptionForSecurityIssuesWithSpotBugs() {
        Report report = shouldFindIssuesOfTool(1, new SpotBugs(), "issue55707.xml");

        Issue issue = report.get(0);
        assertThatDescriptionOfIssueIsSet(new SpotBugs(), issue,
                "<p>A file is opened to read its content. The filename comes from an <b>input</b> parameter. \n"
                        + "If an unfiltered parameter is passed to this file API, files from an arbitrary filesystem location could be read.</p>\n");
        assertThat(issue).hasMessage(
                "java/nio/file/Paths.get(Ljava/lang/String;[Ljava/lang/String;)Ljava/nio/file/Path; reads a file whose location might be specified by user input");
    }

    /** Runs the Clang-Tidy parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllClangTidyIssues() {
        shouldFindIssuesOfTool(6, new ClangTidy(), "ClangTidy.txt");
    }

    /** Runs the Clang parser on an output file that contains 9 issues. */
    @Test
    public void shouldFindAllClangIssues() {
        shouldFindIssuesOfTool(9, new Clang(), "apple-llvm-clang.txt");
    }

    /** Runs the Coolflux parser on an output file that contains 1 issues. */
    @Test
    public void shouldFindAllCoolfluxIssues() {
        shouldFindIssuesOfTool(1, new Coolflux(), "coolfluxchesscc.txt");
    }

    /** Runs the CppLint parser on an output file that contains 1031 issues. */
    @Test
    public void shouldFindAllCppLintIssues() {
        shouldFindIssuesOfTool(1031, new CppLint(), "cpplint.txt");
    }

    /** Runs the CodeAnalysis parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllCodeAnalysisIssues() {
        shouldFindIssuesOfTool(3, new CodeAnalysis(), "codeanalysis.txt");
    }

    /** Runs the DScanner parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllDScannerIssues() {
        shouldFindIssuesOfTool(4, new DScanner(), "dscanner-report.json");
    }

    /** Runs the GoLint parser on an output file that contains 7 issues. */
    @Test
    public void shouldFindAllGoLintIssues() {
        shouldFindIssuesOfTool(7, new GoLint(), "golint.txt");
    }

    /** Runs the GoVet parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllGoVetIssues() {
        shouldFindIssuesOfTool(2, new GoVet(), "govet.txt");
    }

    /** Runs the SunC parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllSunCIssues() {
        shouldFindIssuesOfTool(8, new SunC(), "sunc.txt");
    }

    /** Runs the JcReport parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllJcReportIssues() {
        shouldFindIssuesOfTool(6, new JcReport(), "jcreport.xml");
    }

    /** Runs the StyleCop parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllStyleCopIssues() {
        shouldFindIssuesOfTool(5, new StyleCop(), "stylecop.xml");
    }

    /** Runs the Tasking VX parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllTaskingVxIssues() {
        shouldFindIssuesOfTool(8, new TaskingVx(), "tasking-vx.txt");
    }

    /** Runs the tnsdl translator parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllTnsdlIssues() {
        shouldFindIssuesOfTool(4, new Tnsdl(), "tnsdl.txt");
    }

    /** Runs the Texas Instruments Code Composer Studio parser on an output file that contains 10 issues. */
    @Test
    public void shouldFindAllTiCssIssues() {
        shouldFindIssuesOfTool(10, new TiCss(), "ticcs.txt");
    }

    /** Runs the IBM XLC compiler and linker parser on an output file that contains 1 + 1 issues. */
    @Test
    public void shouldFindAllXlcIssues() {
        shouldFindIssuesOfTool(2, new Xlc(), "xlc.txt");
    }

    /** Runs the YIU compressor parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllYuiCompressorIssues() {
        shouldFindIssuesOfTool(3, new YuiCompressor(), "yui.txt");
    }

    /** Runs the Erlc parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllErlcIssues() {
        shouldFindIssuesOfTool(2, new Erlc(), "erlc.txt");
    }

    /** Runs the FlexSdk parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllFlexSDKIssues() {
        shouldFindIssuesOfTool(5, new FlexSdk(), "flexsdk.txt");
    }

    /** Runs the FxCop parser on an output file that contains 2 issues. */
    @Test
    public void shouldFindAllFxcopSDKIssues() {
        shouldFindIssuesOfTool(2, new Fxcop(), "fxcop.xml");
    }

    /** Runs the Gendarme parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllGendarmeIssues() {
        shouldFindIssuesOfTool(3, new Gendarme(), "Gendarme.xml");
    }

    /** Runs the GhsMulti parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllGhsMultiIssues() {
        shouldFindIssuesOfTool(3, new GhsMulti(), "ghsmulti.txt");
    }

    /**
     * Runs the Gnat parser on an output file that contains 9 issues.
     */
    @Test
    public void shouldFindAllGnatIssues() {
        shouldFindIssuesOfTool(9, new Gnat(), "gnat.txt");
    }

    /** Runs the GnuFortran parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllGnuFortranIssues() {
        shouldFindIssuesOfTool(4, new GnuFortran(), "GnuFortran.txt");
    }

    /** Runs the MsBuild parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllMsBuildIssues() {
        shouldFindIssuesOfTool(6, new MsBuild(), "msbuild.txt");
    }

    /** Runs the NagFortran parser on an output file that contains 10 issues. */
    @Test
    public void shouldFindAllNagFortranIssues() {
        shouldFindIssuesOfTool(10, new NagFortran(), "NagFortran.txt");
    }

    /** Runs the Perforce parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllP4Issues() {
        shouldFindIssuesOfTool(4, new Perforce(), "perforce.txt");
    }

    /** Runs the Pep8 parser on an output file: the build should report 8 issues. */
    @Test
    public void shouldFindAllPep8Issues() {
        shouldFindIssuesOfTool(8, new Pep8(), "pep8Test.txt");
    }

    /** Runs the Gcc3Compiler parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllGcc3CompilerIssues() {
        shouldFindIssuesOfTool(8, new Gcc3(), "gcc.txt");
    }

    /** Runs the Gcc4Compiler and Gcc4Linker parsers on separate output file that contains 14 + 7 issues. */
    @Test
    public void shouldFindAllGcc4Issues() {
        shouldFindIssuesOfTool(14 + 7, new Gcc4(), "gcc4.txt", "gcc4ld.txt");
    }

    /** Runs the Maven console parser on output files that contain 4 + 3 issues. */
    @Test
    public void shouldFindAllMavenConsoleIssues() {
        shouldFindIssuesOfTool(4 + 3, new MavenConsole(), "maven-console.txt", "issue13969.txt");
    }

    /** Runs the MetrowerksCWCompiler parser on two output files that contains 5 + 3 issues. */
    @Test
    public void shouldFindAllMetrowerksCWCompilerIssues() {
        shouldFindIssuesOfTool(5 + 3, new MetrowerksCodeWarrior(), "MetrowerksCWCompiler.txt",
                "MetrowerksCWLinker.txt");
    }

    /** Runs the AcuCobol parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllAcuCobolIssues() {
        shouldFindIssuesOfTool(4, new AcuCobol(), "acu.txt");
    }

    /** Runs the Ajc parser on an output file that contains 9 issues. */
    @Test
    public void shouldFindAllAjcIssues() {
        shouldFindIssuesOfTool(9, new Ajc(), "ajc.txt");
    }

    /** Runs the AnsibleLint parser on an output file that contains 4 issues. */
    @Test
    public void shouldFindAllAnsibleLintIssues() {
        shouldFindIssuesOfTool(4, new AnsibleLint(), "ansibleLint.txt");
    }

    /**
     * Runs the Perl::Critic parser on an output file that contains 105 issues.
     */
    @Test
    public void shouldFindAllPerlCriticIssues() {
        shouldFindIssuesOfTool(105, new PerlCritic(), "perlcritic.txt");
    }

    /** Runs the Php parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllPhpIssues() {
        shouldFindIssuesOfTool(5, new Php(), "php.txt");
    }

    /**
     * Runs the PHPStan scanner on an output file that contains 14 issues.
     */
    @Test
    public void shouldFindAllPhpStanIssues() {
        shouldFindIssuesOfTool(11, new PhpStan(), "phpstan.xml");
    }

    /** Runs the Microsoft PreFast parser on an output file that contains 11 issues. */
    @Test
    public void shouldFindAllPREfastIssues() {
        shouldFindIssuesOfTool(11, new PreFast(), "PREfast.xml");
    }

    /** Runs the Puppet Lint parser on an output file that contains 5 issues. */
    @Test
    public void shouldFindAllPuppetLintIssues() {
        shouldFindIssuesOfTool(5, new PuppetLint(), "puppet-lint.txt");
    }

    /** Runs the Eclipse parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllEclipseIssues() {
        shouldFindIssuesOfTool(8, new Eclipse(), "eclipse.txt");

        // FIXME: fails if offline
        shouldFindIssuesOfTool(6, new Eclipse(), "eclipse-withinfo.xml");

        shouldFindIssuesOfTool(8 + 6, new Eclipse(), "eclipse-withinfo.xml", "eclipse.txt");
    }

    /** Runs the PyLint parser on output files that contains 6 + 19 issues. */
    @Test
    public void shouldFindAllPyLintParserIssues() {
        Report report = shouldFindIssuesOfTool(6 + 19, new PyLint(), "pyLint.txt", "pylint_parseable.txt");

        assertThatDescriptionOfIssueIsSet(new PyLint(), report.get(1),
                "Used when the name doesn't match the regular expression associated to its type(constant, variable, class...).");
        assertThatDescriptionOfIssueIsSet(new PyLint(), report.get(7),
                "Used when a wrong number of spaces is used around an operator, bracket orblock opener.");
    }

    /**
     * Runs the QacSourceCodeAnalyser parser on an output file that contains 9 issues.
     */
    @Test
    public void shouldFindAllQACSourceCodeAnalyserIssues() {
        shouldFindIssuesOfTool(9, new QacSourceCodeAnalyser(), "QACSourceCodeAnalyser.txt");
    }

    /** Runs the Resharper parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllResharperInspectCodeIssues() {
        shouldFindIssuesOfTool(3, new ResharperInspectCode(), "ResharperInspectCode.xml");
    }

    /** Runs the RfLint parser on an output file that contains 6 issues. */
    @Test
    public void shouldFindAllRfLintIssues() {
        shouldFindIssuesOfTool(6, new RfLint(), "rflint.txt");
    }

    /** Runs the Robocopy parser on an output file: the build should report 3 issues. */
    @Test
    public void shouldFindAllRobocopyIssues() {
        shouldFindIssuesOfTool(3, new Robocopy(), "robocopy.txt");
    }

    /** Runs the Scala and SbtScala parser on separate output files: the build should report 2+3 issues. */
    @Test
    public void shouldFindAllScalaIssues() {
        shouldFindIssuesOfTool(2 + 3, new Scala(), "scalac.txt", "sbtScalac.txt");
    }

    /** Runs the Sphinx build parser on an output file: the build should report 6 issues. */
    @Test
    public void shouldFindAllSphinxIssues() {
        shouldFindIssuesOfTool(6, new SphinxBuild(), "sphinxbuild.txt");
    }

    /** Runs the Idea Inspection parser on an output file that contains 1 issues. */
    @Test
    public void shouldFindAllIdeaInspectionIssues() {
        shouldFindIssuesOfTool(1, new IdeaInspection(), "IdeaInspectionExample.xml");
    }

    /** Runs the Intel parser on an output file that contains 7 issues. */
    @Test
    public void shouldFindAllIntelIssues() {
        shouldFindIssuesOfTool(7, new Intel(), "intelc.txt");
    }

    /** Runs the Oracle Invalids parser on an output file that contains 3 issues. */
    @Test
    public void shouldFindAllInvalidsIssues() {
        shouldFindIssuesOfTool(3, new Invalids(), "invalids.txt");
    }

    /** Runs the Java parser on several output files that contain 2 + 1 + 1 + 1 + 2 issues. */
    @Test
    public void shouldFindAllJavaIssues() {
        shouldFindIssuesOfTool(2 + 1 + 1 + 1 + 2, new Java(), "javac.txt", "gradle.java.log", "gradle.another.java.log", "ant-javac.txt", "hpi.txt");
    }

    /**
     * Runs the Kotlin parser on several output files that contain 1 issues.
     */
    @Test
    public void shouldFindAllKotlinIssues() {
        shouldFindIssuesOfTool(1, new Kotlin(), "kotlin.txt");
    }

    /**
     * Runs the CssLint parser on an output file that contains 51 issues.
     */
    @Test
    public void shouldFindAllCssLintIssues() {
        shouldFindIssuesOfTool(51, new CssLint(), "csslint.xml");
    }

    /** Runs the DiabC parser on an output file that contains 12 issues. */
    @Test
    public void shouldFindAllDiabCIssues() {
        shouldFindIssuesOfTool(12, new DiabC(), "diabc.txt");
    }

    /** Runs the Doxygen parser on an output file that contains 18 issues. */
    @Test
    public void shouldFindAllDoxygenIssues() {
        shouldFindIssuesOfTool(18, new Doxygen(), "doxygen.txt");
    }

    /** Runs the Dr. Memory parser on an output file that contains 8 issues. */
    @Test
    public void shouldFindAllDrMemoryIssues() {
        shouldFindIssuesOfTool(8, new DrMemory(), "drmemory.txt");
    }

    /** Runs the JavaC parser on an output file of the Eclipse compiler: the build should report no issues. */
    @Test
    public void shouldFindNoJavacIssuesInEclipseOutput() {
        shouldFindIssuesOfTool(0, new Java(), "eclipse.txt");
    }

    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock", "PMD.LinguisticNaming"})
    private Report shouldFindIssuesOfTool(final int expectedSizeOfIssues, final ReportScanningTool tool,
            final String... fileNames) {
        try {
            WorkflowJob job = createPipeline();
            copyMultipleFilesToWorkspace(job, fileNames);
            job.setDefinition(asStage(String.format(
                    "recordIssues tool: %s(pattern:'**/%s', reportEncoding:'UTF-8')",
                    tool.getSymbolName(), createPatternFor(fileNames))));

            AnalysisResult result = scheduleSuccessfulBuild(job);

            assertThat(result).hasTotalSize(expectedSizeOfIssues);
            assertThat(result.getIssues()).hasSize(expectedSizeOfIssues);

            Report report = result.getIssues();
            assertThat(report.filter(issue -> issue.getOrigin().equals(tool.getActualId())))
                    .hasSize(expectedSizeOfIssues);

            return report;
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private String createPatternFor(final String... fileNames) {
        return Arrays.stream(fileNames).map(s -> "**/" + s).collect(Collectors.joining(", "));
    }
}
