package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.Test;

import com.parasoft.findings.jenkins.tool.ParasoftTool;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.*;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of all parsers of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.ExcessivePublicCount", "PMD.CyclomaticComplexity", "PMD.GodClass", "PMD.ExcessiveClassLength", "ClassDataAbstractionCoupling", "ClassFanOutComplexity"})
class ParsersITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String CODE_FRAGMENT = """
            <pre><code>#
            
                ERROR HANDLING: N/A
                #
                REMARKS: N/A
                #
                ****************************** END HEADER *************************************
                #
            
                ***************************** BEGIN PDL ***************************************
                #
                ****************************** END PDL ****************************************
                #
            
                ***************************** BEGIN CODE **************************************
                **
                *******************************************************************************
            
                *******************************************************************************
                *******************************************************************************
            
            if [ $# -lt 3 ]
            then
            exit 1
            fi
            
                *******************************************************************************
                initialize local variables
                shift input parameter (twice) to leave only files to copy
                *******************************************************************************
            
            files&#61;&#34;&#34;
            shift
            shift
            
                *******************************************************************************
                *******************************************************************************
            
            for i in $*
            do
            files&#61;&#34;$files $directory/$i&#34;
            done</code></pre>""";

    /** Runs the Parasoft parser (part of the parasoft-findings plugin) on a file that contains 5 issues. */
    @Test
    void shouldReadParasoftWarnings() {
        shouldFindIssuesOfTool(5, new ParasoftTool(), "parasoft.xml");
    }

    /** Runs the native parser on a file that contains 9 issues. */
    @Test
    void shouldFindAllRevapiIssues() {
        shouldFindIssuesOfTool(7, new RevApi(), "revapi-result.json");
    }

    /** Runs the native parser on a file that contains 9 issues.. */
    @Test
    void shouldReadNativeFormats() {
        shouldFindIssuesOfTool(9 + 5 + 5, new WarningsPlugin(), "warnings-issues.xml", "issues.json",
                "json-issues.log");
    }

    /** Runs the BluePearl an output file that contains 7 issues. */
    @Test
    void shouldFindAllBluePearlIssues() {
        shouldFindIssuesOfTool(12, new BluePearl(), "bluepearl.log");
    }

    /** Runs the Dart analysis parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllDartIssues() {
        shouldFindIssuesOfTool(6, new Dart(), "dart.log");
    }

    /** Runs the SARIF parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllSarifIssues() {
        shouldFindIssuesOfTool(2, new Sarif(), "sarif.json");
    }

    /** Runs the native parser on a file that contains 9 issues. */
    @Test
    void shouldReadNativeXmlFormat() {
        shouldFindIssuesOfTool(9, new WarningsPlugin(), "warnings-issues.xml");
    }

    /** Runs the native parser on a file that contains 5 issues. */
    @Test
    void shouldReadNativeJsonFormat() {
        shouldFindIssuesOfTool(5, new WarningsPlugin(), "issues.json");
    }

    /** Runs the native parser on a file that contains 8 issues. */
    @Test
    void shouldReadNativeJsonLogFormat() {
        shouldFindIssuesOfTool(5, new WarningsPlugin(), "json-issues.log");
    }

    /** Verifies that a broken file does not fail. */
    @Test
    void shouldSilentlyIgnoreWrongFile() {
        shouldFindIssuesOfTool(0, new CheckStyle(), "sun_checks.xml");
    }

    /**
     * Runs with several tools that internally delegate to CheckStyle's  parser on an output file that contains 6
     * issues.
     */
    @Test
    void shouldFindAllIssuesForCheckStyleAlias() {
        for (AnalysisModelParser tool : Arrays.asList(new Detekt(), new EsLint(), new KtLint(), new PhpCodeSniffer(),
                new SwiftLint(), new StyleLint(), new TsLint())) {
            shouldFindIssuesOfTool(6, tool, "checkstyle.xml");
        }
    }

    /** Runs the CodeChecker parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllCodeCheckerIssues() {
        var logFile = "CodeChecker_with_linux_paths.txt";
        shouldFindIssuesOfTool(3, new CodeChecker(), logFile);

        var job = createPipeline();
        copyMultipleFilesToWorkspace(job, logFile);
        job.setDefinition(asStage(String.format(
                "recordIssues tool:analysisParser("
                        + "pattern:'**/%s', "
                        + "reportEncoding:'UTF-8', "
                        + "analysisModelId:'code-checker')", logFile)));

        var result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(3);
        var report = result.getIssues();
        assertThat(report.filter(issue -> "code-checker".equals(issue.getOrigin()))).hasSize(3);
    }

    /** Runs the Cmake parser on an output file that contains 8 issues. */
    @Test
    void shouldFindAllCmakeIssues() {
        shouldFindIssuesOfTool(8, new Cmake(), "cmake.txt");
    }

    /** Runs the Cargo parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllCargoIssues() {
        shouldFindIssuesOfTool(2, new Cargo(), "CargoCheck.json");
    }

    /** Runs the Pmd parser on an output file that contains 262 issues. */
    @Test
    void shouldFindAllIssuesForPmdAlias() {
        shouldFindIssuesOfTool(262, new Infer(), "pmd-6.xml");
    }

    /** Runs the MSBuild parser on an output file that contains 262 issues. */
    @Test
    void shouldFindAllIssuesForMsBuildAlias() {
        shouldFindIssuesOfTool(6, new PcLint(), "msbuild.txt");
    }

    /** Runs the YamlLint parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllYamlLintIssues() {
        shouldFindIssuesOfTool(4, new YamlLint(), "yamllint.txt");
    }

    /** Runs the YamlLint parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllYoctoIssues() {
        var action = shouldFindIssuesOfTool(25, new YoctoScanner(), "yocto_scanner_result.json");

        assertThat(action.getIconFileName()).contains("shield");
    }

    /** Runs the Iar parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllIarIssues() {
        shouldFindIssuesOfTool(6, new Iar(), "iar.txt");
    }

    /** Runs the IbLinter parser on an output file that contains 1 issue. */
    @Test
    void shouldFindAllIbLinterIssues() {
        shouldFindIssuesOfTool(1, new IbLinter(), "iblinter.xml");
    }

    /** Runs the IarCStat parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllIarCStatIssues() {
        shouldFindIssuesOfTool(6, new IarCstat(), "iar-cstat.txt");
    }

    /** Runs the TagList parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllOpenTasks() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("file-with-tasks.txt");
        job.setDefinition(asStage(
                "def issues = scanForIssues tool: "
                        + "taskScanner(includePattern:'**/*issues.txt', highTags:'FIXME', normalTags:'TODO')",
                PUBLISH_ISSUES_STEP));

        var result = scheduleSuccessfulBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(2);
        var report = result.getIssues();
        assertThat(report).hasSize(2);
        assertThatReportHasSeverities(report,
                0, 1, 1, 0);
    }

    /** Runs the SonarQube parsers on two files that contains 6 and 31 issues. */
    @Test
    void shouldFindAllSonarQubeIssues() {
        shouldFindIssuesOfTool(32, new SonarQube(), "sonarqube-api.json");
        shouldFindIssuesOfTool(6, new SonarQube(), "sonarqube-differential.json");
        shouldFindIssuesOfTool(38, new SonarQube(), "sonarqube-api.json", "sonarqube-differential.json");
    }

    /** Runs the TagList parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllTagListIssues() {
        shouldFindIssuesOfTool(4, new TagList(), "taglist.xml");
    }

    /** Runs the Ccm parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllCcmIssues() {
        shouldFindIssuesOfTool(6, new Ccm(), "ccm.xml");
    }

    /** Runs the ruboCop parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllRuboCopIssues() {
        shouldFindIssuesOfTool(2, new RuboCop(), "rubocop.log");
    }

    /** Runs the flawfinder parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllFlawfinderIssues() {
        shouldFindIssuesOfTool(3, new Flawfinder(), "flawfinder.log");
    }

    /** Runs the Android Lint parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllAndroidLintIssues() {
        shouldFindIssuesOfTool(2, new AndroidLint(), "android-lint.xml");
    }

    /** Runs the CodeNarc parser on an output file that contains 11 issues. */
    @Test
    void shouldFindAllCodeNArcIssues() {
        shouldFindIssuesOfTool(11, new CodeNarc(), "codeNarc.xml");
    }

    /** Runs the Cppcheck parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllCppCheckIssues() {
        shouldFindIssuesOfTool(3, new CppCheck(), "cppcheck.xml");
    }

    /** Runs the DocFx parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllDocFXIssues() {
        shouldFindIssuesOfTool(3, new DocFx(), "docfx.json");
    }

    /** Runs the ErrorProne parser on output files that contain 9 + 2 issues. */
    @Test
    void shouldFindAllErrorProneIssues() {
        var action = shouldFindIssuesOfTool(9 + 2, new ErrorProne(), "errorprone-maven.log", "gradle-error-prone.log");

        assertThat(action.getIconFileName()).contains("bug");
    }

    /** Runs the Flake8 parser on an output file that contains 12 issues. */
    @Test
    void shouldFindAllFlake8Issues() {
        shouldFindIssuesOfTool(12, new Flake8(), "flake8.txt");
    }

    /** Runs the JSHint parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllJsHintIssues() {
        shouldFindIssuesOfTool(6, new JsHint(), "jshint.xml");
    }

    /**
     * Runs the JUnit parser on an output file that contains 2 and 1 issues.
     */
    @Test
    void shouldFindAllJUnitIssues() {
        shouldFindIssuesOfTool(2, new JUnit(), "junit.xml");

        shouldFindIssuesOfTool(1, new JUnit(), "TEST-org.jenkinsci.plugins.jvctb.perform.JvctbPerformerTest.xml");
    }

    /** Runs the Klocwork parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllKlocWorkIssues() {
        shouldFindIssuesOfTool(2, new KlocWork(), "klocwork.xml");
    }

    /** Runs the MyPy parser on an output file that contains 5 issues. */
    @Test
    void shouldFindAllMyPyIssues() {
        shouldFindIssuesOfTool(5, new MyPy(), "mypy.txt");
    }

    /** Runs the PIT parser on an output file that contains 25 issues. */
    @Test
    void shouldFindAllPitIssues() {
        shouldFindIssuesOfTool(22, new Pit(), "pit.xml");
    }

    /** Runs the PyDocStyle parser on an output file that contains 33 issues. */
    @Test
    void shouldFindAllPyDocStyleIssues() {
        shouldFindIssuesOfTool(33, new PyDocStyle(), "pydocstyle.txt");
    }

    /** Runs the XML Lint parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllXmlLintStyleIssues() {
        shouldFindIssuesOfTool(3, new XmlLint(), "xmllint.txt");
    }

    /** Runs the zptlint parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllZptLintStyleIssues() {
        shouldFindIssuesOfTool(2, new ZptLint(), "zptlint.log");
    }

    /** Runs the CPD parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllCpdIssues() {
        var report = findReportWithoutAnsiColorPlugin(2, new Cpd(), "cpd.xml");
        var reportAnsi = findReportWithAnsiColorPlugin(2, new Cpd(), "cpd.xml");

        assertThatDescriptionOfIssueIsSet(new Cpd(), report.get(0), CODE_FRAGMENT);
        assertThatDescriptionOfIssueIsSet(new Cpd(), reportAnsi.get(0), CODE_FRAGMENT);
    }

    /** Runs the Simian parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllSimianIssues() {
        var action = shouldFindIssuesOfTool(4, new Simian(), "simian.xml");

        assertThat(action.getIconFileName()).contains("clone");
    }

    /** Runs the DupFinder parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllDupFinderIssues() {
        var report = findReportWithoutAnsiColorPlugin(2, new DupFinder(), "dupfinder.xml");
        var reportAnsi = findReportWithAnsiColorPlugin(2, new DupFinder(), "dupfinder.xml");

        assertThatDescriptionOfIssueIsSet(new DupFinder(), report.get(0),
                "<pre><code>if (items &#61;&#61; null) throw new ArgumentNullException(&#34;items&#34;);</code></pre>");
        assertThatDescriptionOfIssueIsSet(new DupFinder(), reportAnsi.get(0),
                "<pre><code>if (items &#61;&#61; null) throw new ArgumentNullException(&#34;items&#34;);</code></pre>");
    }

    /** Runs the Armcc parser on output files that contain 3 + 3 issues. */
    @Test
    void shouldFindAllArmccIssues() {
        var action = shouldFindIssuesOfTool(3 + 3, new ArmCc(), "armcc5.txt", "armcc.txt");

        assertThat(action.getIconFileName()).contains("triangle-exclamation");
    }

    /** Runs the Buckminster parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllBuckminsterIssues() {
        shouldFindIssuesOfTool(3, new Buckminster(), "buckminster.txt");
    }

    /** Runs the Cadence parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllCadenceIssues() {
        shouldFindIssuesOfTool(3, new Cadence(), "CadenceIncisive.txt");
    }

    /** Runs the Mentor parser on an output file that contains 12 issues. */
    @Test
    void shouldFindAllMentorGraphicsIssues() {
        shouldFindIssuesOfTool(12, new MentorGraphics(), "MentorGraphics.log");
    }

    /** Runs the PMD parser on an output file that contains 262 issues (PMD 6.1.0). */
    @Test
    void shouldFindAllPmdIssues() {
        var report = findReportWithoutAnsiColorPlugin(262, new Pmd(), "pmd-6.xml");
        var reportAnsi = findReportWithAnsiColorPlugin(262, new Pmd(), "pmd-6.xml");

        assertThatDescriptionOfIssueIsSet(new Pmd(), report.get(0),
                "A high number of imports can indicate a high degree of coupling within an object.");
        assertThatDescriptionOfIssueIsSet(new Pmd(), reportAnsi.get(0),
                "A high number of imports can indicate a high degree of coupling within an object.");
    }

    /** Runs the Valgrind Pipeline Issues parser on output file that contains 5 issues. */
    @Test
    void shouldFindAllValgrindIssues() {
        shouldFindIssuesOfTool(5, new Valgrind(), "valgrind.xml");
    }

    /** Runs the CheckStyle parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllCheckStyleIssues() {
        var report = findReportWithoutAnsiColorPlugin(6, new CheckStyle(), "checkstyle.xml");
        var reportAnsi = findReportWithAnsiColorPlugin(6, new CheckStyle(), "checkstyle.xml");

        assertThatDescriptionOfIssueIsSet(new CheckStyle(), report.get(2),
                "<p>Since Checkstyle 3.1</p><p>");
        assertThatDescriptionOfIssueIsSet(new CheckStyle(), reportAnsi.get(2),
                "<p>Since Checkstyle 3.1</p><p>");

        var labelProvider = new CheckStyle().getLabelProvider();
        assertThat(labelProvider.getDescription(report.get(2)))
                .contains("The check finds classes that are designed for extension (subclass creation).");
        assertThat(labelProvider.getDescription(reportAnsi.get(2)))
                .contains("The check finds classes that are designed for extension (subclass creation).");
    }

    private void assertThatDescriptionOfIssueIsSet(final Tool tool, final Issue issue,
            final String expectedDescription) {
        var labelProvider = tool.getLabelProvider();
        assertThat(issue).hasDescription("");
        assertThat(labelProvider.getDescription(issue)).contains(expectedDescription);
    }

    /** Runs the FindBugs parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllFindBugsIssues() {
        var report = findReportWithoutAnsiColorPlugin(2, new FindBugs(), "findbugs-native.xml");
        var reportAnsi = findReportWithAnsiColorPlugin(2, new FindBugs(), "findbugs-native.xml");

        assertThatDescriptionOfIssueIsSet(new FindBugs(), report.get(0),
                """
                <p> The fields of this class appear to be accessed inconsistently with respect
                  to synchronization.&nbsp; This bug report indicates that the bug pattern detector
                  judged that
                  </p>
                  <ul>
                  <li> The class contains a mix of locked and unlocked accesses,</li>
                  <li> The class is <b>not</b> annotated as javax.annotation.concurrent.NotThreadSafe,</li>
                  <li> At least one locked access was performed by one of the class's own methods, and</li>
                  <li> The number of unsynchronized field accesses (reads and writes) was no more than
                       one third of all accesses, with writes being weighed twice as high as reads</li>
                  </ul>
                
                  <p> A typical bug matching this bug pattern is forgetting to synchronize
                  one of the methods in a class that is intended to be thread-safe.</p>
                
                  <p> You can select the nodes labeled "Unsynchronized access" to show the
                  code locations where the detector believed that a field was accessed
                  without synchronization.</p>
                
                  <p> Note that there are various sources of inaccuracy in this detector;
                  for example, the detector cannot statically detect all situations in which
                  a lock is held.&nbsp; Also, even when the detector is accurate in
                  distinguishing locked vs. unlocked accesses, the code in question may still
                  be correct.</p>""");
        assertThatDescriptionOfIssueIsSet(new FindBugs(), reportAnsi.get(0),
                """
                <p> The fields of this class appear to be accessed inconsistently with respect
                  to synchronization.&nbsp; This bug report indicates that the bug pattern detector
                  judged that
                  </p>
                  <ul>
                  <li> The class contains a mix of locked and unlocked accesses,</li>
                  <li> The class is <b>not</b> annotated as javax.annotation.concurrent.NotThreadSafe,</li>
                  <li> At least one locked access was performed by one of the class's own methods, and</li>
                  <li> The number of unsynchronized field accesses (reads and writes) was no more than
                       one third of all accesses, with writes being weighed twice as high as reads</li>
                  </ul>
                
                  <p> A typical bug matching this bug pattern is forgetting to synchronize
                  one of the methods in a class that is intended to be thread-safe.</p>
                
                  <p> You can select the nodes labeled "Unsynchronized access" to show the
                  code locations where the detector believed that a field was accessed
                  without synchronization.</p>
                
                  <p> Note that there are various sources of inaccuracy in this detector;
                  for example, the detector cannot statically detect all situations in which
                  a lock is held.&nbsp; Also, even when the detector is accurate in
                  distinguishing locked vs. unlocked accesses, the code in question may still
                  be correct.</p>""");
    }

    /** Runs the SpotBugs parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllSpotBugsIssues() {
        var expectedDescription = """
                <p>This code calls a method and ignores the return value. However our analysis shows that
                the method (including its implementations in subclasses if any) does not produce any effect
                other than return value. Thus this call can be removed.
                </p>
                <p>We are trying to reduce the false positives as much as possible, but in some cases this warning might be wrong.
                Common false-positive cases include:</p>
                <p>- The method is designed to be overridden and produce a side effect in other projects which are out of the scope of the analysis.</p>
                <p>- The method is called to trigger the class loading which may have a side effect.</p>
                <p>- The method is called just to get some exception.</p>
                <p>If you feel that our assumption is incorrect, you can use a @CheckReturnValue annotation
                to instruct SpotBugs that ignoring the return value of this method is acceptable.
                </p>""";

        var report = findReportWithoutAnsiColorPlugin(2, new SpotBugs(), "spotbugsXml.xml");
        assertThatDescriptionOfIssueIsSet(new SpotBugs(), report.get(0), expectedDescription);

        var reportAnsi = findReportWithAnsiColorPlugin(2, new SpotBugs(), "spotbugsXml.xml");
        assertThatDescriptionOfIssueIsSet(new SpotBugs(), reportAnsi.get(0), expectedDescription);
    }

    /** Runs the SpotBugs parser on an output file that contains 2 issues. */
    @Test
    void shouldProvideMessagesAndDescriptionForSecurityIssuesWithSpotBugs() {
        var expectedDescription = """
                <p>A file is opened to read its content. The filename comes from an <b>input</b> parameter.
                If an unfiltered parameter is passed to this file API, files from an arbitrary filesystem location could be read.</p>
                <p>This rule identifies <b>potential</b> path traversal vulnerabilities. In many cases, the constructed file path cannot be controlled
                by the user. If that is the case, the reported instance is a false positive.</p>""";

        var report = findReportWithoutAnsiColorPlugin(1, new SpotBugs(), "issue55707.xml");
        var issue = report.get(0);
        assertThatDescriptionOfIssueIsSet(new SpotBugs(), issue, expectedDescription);
        assertThat(issue).hasMessage(
                "java/nio/file/Paths.get(Ljava/lang/String;[Ljava/lang/String;)Ljava/nio/file/Path; reads a file whose location might be specified by user input");

        var reportAnsi = findReportWithAnsiColorPlugin(1, new SpotBugs(), "issue55707.xml");
        var issueAnsi = reportAnsi.get(0);
        assertThatDescriptionOfIssueIsSet(new SpotBugs(), issueAnsi, expectedDescription);
        assertThat(issueAnsi).hasMessage(
                "java/nio/file/Paths.get(Ljava/lang/String;[Ljava/lang/String;)Ljava/nio/file/Path; reads a file whose location might be specified by user input");
    }

    /** Runs the Clang-Analyzer parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllClangAnalyzerIssues() {
        shouldFindIssuesOfTool(3, new ClangAnalyzer(), "ClangAnalyzer.txt");
    }

    /** Runs the Clang-Tidy parser on an output file that contains 7 issues. */
    @Test
    void shouldFindAllClangTidyIssues() {
        shouldFindIssuesOfTool(7, new ClangTidy(), "ClangTidy.txt");
    }

    /** Runs the Clang parser on an output file that contains 9 issues. */
    @Test
    void shouldFindAllClangIssues() {
        shouldFindIssuesOfTool(9, new Clang(), "apple-llvm-clang.txt");
    }

    /** Runs the Coolflux parser on an output file that contains 1 issues. */
    @Test
    void shouldFindAllCoolfluxIssues() {
        shouldFindIssuesOfTool(1, new Coolflux(), "coolfluxchesscc.txt");
    }

    /** Runs the CppLint parser on an output file that contains 1031 issues. */
    @Test
    void shouldFindAllCppLintIssues() {
        shouldFindIssuesOfTool(1031, new CppLint(), "cpplint.txt");
    }

    /** Runs the CodeAnalysis parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllCodeAnalysisIssues() {
        shouldFindIssuesOfTool(3, new CodeAnalysis(), "codeanalysis.txt");
    }

    /** Runs the DScanner parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllDScannerIssues() {
        shouldFindIssuesOfTool(4, new DScanner(), "dscanner-report.json");
    }

    /** Runs the GoLint parser on an output file that contains 7 issues. */
    @Test
    void shouldFindAllGoLintIssues() {
        shouldFindIssuesOfTool(7, new GoLint(), "golint.txt");
    }

    /** Runs the GoVet parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllGoVetIssues() {
        shouldFindIssuesOfTool(2, new GoVet(), "govet.txt");
    }

    /** Runs the SunC parser on an output file that contains 8 issues. */
    @Test
    void shouldFindAllSunCIssues() {
        shouldFindIssuesOfTool(8, new SunC(), "sunc.txt");
    }

    /** Runs the JcReport parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllJcReportIssues() {
        shouldFindIssuesOfTool(6, new JcReport(), "jcreport.xml");
    }

    /** Runs the StyleCop parser on an output file that contains 5 issues. */
    @Test
    void shouldFindAllStyleCopIssues() {
        shouldFindIssuesOfTool(5, new StyleCop(), "stylecop.xml");
    }

    /** Runs the Tasking VX parser on an output file that contains 8 issues. */
    @Test
    void shouldFindAllTaskingVxIssues() {
        shouldFindIssuesOfTool(8, new TaskingVx(), "tasking-vx.txt");
    }

    /** Runs the tnsdl translator parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllTnsdlIssues() {
        shouldFindIssuesOfTool(4, new Tnsdl(), "tnsdl.txt");
    }

    /** Runs the Texas Instruments Code Composer Studio parser on an output file that contains 10 issues. */
    @Test
    void shouldFindAllTiCssIssues() {
        shouldFindIssuesOfTool(10, new TiCss(), "ticcs.txt");
    }

    /** Runs the IBM XLC compiler and linker parser on an output file that contains 1 + 1 issues. */
    @Test
    void shouldFindAllXlcIssues() {
        shouldFindIssuesOfTool(2, new Xlc(), "xlc.txt");
    }

    /** Runs the YIU compressor parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllYuiCompressorIssues() {
        shouldFindIssuesOfTool(3, new YuiCompressor(), "yui.txt");
    }

    /** Runs the Erlc parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllErlcIssues() {
        shouldFindIssuesOfTool(2, new Erlc(), "erlc.txt");
    }

    /** Runs the FlexSdk parser on an output file that contains 5 issues. */
    @Test
    void shouldFindAllFlexSDKIssues() {
        shouldFindIssuesOfTool(5, new FlexSdk(), "flexsdk.txt");
    }

    /** Runs the FxCop parser on an output file that contains 2 issues. */
    @Test
    void shouldFindAllFxcopSDKIssues() {
        shouldFindIssuesOfTool(2, new Fxcop(), "fxcop.xml");
    }

    /** Runs the Gendarme parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllGendarmeIssues() {
        shouldFindIssuesOfTool(3, new Gendarme(), "Gendarme.xml");
    }

    /** Runs the GhsMulti parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllGhsMultiIssues() {
        shouldFindIssuesOfTool(3, new GhsMulti(), "ghsmulti.txt");
    }

    /**
     * Runs the Gnat parser on an output file that contains 9 issues.
     */
    @Test
    void shouldFindAllGnatIssues() {
        shouldFindIssuesOfTool(9, new Gnat(), "gnat.txt");
    }

    /** Runs the GnuFortran parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllGnuFortranIssues() {
        shouldFindIssuesOfTool(4, new GnuFortran(), "GnuFortran.txt");
    }

    /** Runs the MsBuild parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllMsBuildIssues() {
        shouldFindIssuesOfTool(6, new MsBuild(), "msbuild.txt");
    }

    /** Runs the NagFortran parser on an output file that contains 10 issues. */
    @Test
    void shouldFindAllNagFortranIssues() {
        shouldFindIssuesOfTool(10, new NagFortran(), "NagFortran.txt");
    }

    /** Runs the Perforce parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllP4Issues() {
        shouldFindIssuesOfTool(4, new Perforce(), "perforce.txt");
    }

    /** Runs the Pep8 parser on an output file: the build should report 8 issues. */
    @Test
    void shouldFindAllPep8Issues() {
        shouldFindIssuesOfTool(8, new Pep8(), "pep8Test.txt");
    }

    /** Runs the Gcc3Compiler parser on an output file that contains 8 issues. */
    @Test
    void shouldFindAllGcc3CompilerIssues() {
        shouldFindIssuesOfTool(8, new Gcc3(), "gcc.txt");
    }

    /** Runs the Gcc4Compiler and Gcc4Linker parsers on separate output file that contains 14 + 7 issues. */
    @Test
    void shouldFindAllGcc4Issues() {
        shouldFindIssuesOfTool(14 + 7, new Gcc4(), "gcc4.txt", "gcc4ld.txt");
    }

    /** Runs the Maven console parser on output files that contain 4 + 3 issues. */
    @Test
    void shouldFindAllMavenConsoleIssues() {
        shouldFindIssuesOfTool(4 + 3, new MavenConsole(), "maven-console.txt", "issue13969.txt");
    }

    /** Runs the MetrowerksCWCompiler parser on two output files that contains 5 + 3 issues. */
    @Test
    void shouldFindAllMetrowerksCWCompilerIssues() {
        shouldFindIssuesOfTool(5 + 3, new MetrowerksCodeWarrior(), "MetrowerksCWCompiler.txt",
                "MetrowerksCWLinker.txt");
    }

    /** Runs the AcuCobol parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllAcuCobolIssues() {
        shouldFindIssuesOfTool(4, new AcuCobol(), "acu.txt");
    }

    /** Runs the Ajc parser on an output file that contains 9 issues. */
    @Test
    void shouldFindAllAjcIssues() {
        shouldFindIssuesOfTool(9, new Ajc(), "ajc.txt");
    }

    /** Runs the AnsibleLint parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllAnsibleLintIssues() {
        shouldFindIssuesOfTool(4, new AnsibleLint(), "ansibleLint.txt");
    }

    /**
     * Runs the Perl::Critic parser on an output file that contains 105 issues.
     */
    @Test
    void shouldFindAllPerlCriticIssues() {
        shouldFindIssuesOfTool(105, new PerlCritic(), "perlcritic.txt");
    }

    /** Runs the Php parser on an output file that contains 5 issues. */
    @Test
    void shouldFindAllPhpIssues() {
        shouldFindIssuesOfTool(5, new Php(), "php.txt");
    }

    /**
     * Runs the PHPStan scanner on an output file that contains 14 issues.
     */
    @Test
    void shouldFindAllPhpStanIssues() {
        shouldFindIssuesOfTool(11, new PhpStan(), "phpstan.xml");
    }

    /** Runs the Microsoft PreFast parser on an output file that contains 11 issues. */
    @Test
    void shouldFindAllPREfastIssues() {
        shouldFindIssuesOfTool(11, new PreFast(), "PREfast.xml");
    }

    /** Runs the Puppet Lint parser on an output file that contains 5 issues. */
    @Test
    void shouldFindAllPuppetLintIssues() {
        shouldFindIssuesOfTool(5, new PuppetLint(), "puppet-lint.txt");
    }

    /** Runs the Eclipse parser on an output file that contains 8 issues. */
    @Test
    void shouldFindAllEclipseIssues() {
        shouldFindIssuesOfTool(8, new Eclipse(), "eclipse.txt");

        // FIXME: fails if offline
        shouldFindIssuesOfTool(6, new Eclipse(), "eclipse-withinfo.xml");

        shouldFindIssuesOfTool(8 + 6, new Eclipse(), "eclipse-withinfo.xml", "eclipse.txt");
    }

    /** Runs the PyLint parser on output files that contains 6 + 19 issues. */
    @Test
    void shouldFindAllPyLintParserIssues() {
        var report = findReportWithoutAnsiColorPlugin(6 + 19, new PyLint(), "pyLint.txt", "pylint_parseable.txt");
        var reportAnsi = findReportWithAnsiColorPlugin(6 + 19, new PyLint(), "pyLint.txt",
                "pylint_parseable.txt");

        assertThatDescriptionOfIssueIsSet(new PyLint(), report.get(1),
                "Used when the name doesn't match the regular expression associated to its type(constant, variable, class...).");
        assertThatDescriptionOfIssueIsSet(new PyLint(), report.get(7),
                "Used when a wrong number of spaces is used around an operator, bracket orblock opener.");

        assertThatDescriptionOfIssueIsSet(new PyLint(), reportAnsi.get(1),
                "Used when the name doesn't match the regular expression associated to its type(constant, variable, class...).");
        assertThatDescriptionOfIssueIsSet(new PyLint(), reportAnsi.get(7),
                "Used when a wrong number of spaces is used around an operator, bracket orblock opener.");
    }

    /**
     * Runs the QacSourceCodeAnalyser parser on an output file that contains 9 issues.
     */
    @Test
    void shouldFindAllQACSourceCodeAnalyserIssues() {
        shouldFindIssuesOfTool(9, new QacSourceCodeAnalyser(), "QACSourceCodeAnalyser.txt");
    }

    /** Runs the Resharper parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllResharperInspectCodeIssues() {
        shouldFindIssuesOfTool(3, new ResharperInspectCode(), "ResharperInspectCode.xml");
    }

    /** Runs the RfLint parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllRfLintIssues() {
        shouldFindIssuesOfTool(6, new RfLint(), "rflint.txt");
    }

    /** Runs the Robocopy parser on an output file: the build should report 3 issues. */
    @Test
    void shouldFindAllRobocopyIssues() {
        shouldFindIssuesOfTool(3, new Robocopy(), "robocopy.txt");
    }

    /** Runs the Scala and SbtScala parser on separate output files: the build should report 2+3 issues. */
    @Test
    void shouldFindAllScalaIssues() {
        shouldFindIssuesOfTool(2 + 3, new Scala(), "scalac.txt", "sbtScalac.txt");
    }

    /** Runs the Sphinx build parser on an output file: the build should report 6 issues. */
    @Test
    void shouldFindAllSphinxIssues() {
        shouldFindIssuesOfTool(6, new SphinxBuild(), "sphinxbuild.txt");
    }

    /** Runs the Idea Inspection parser on an output file that contains 1 issues. */
    @Test
    void shouldFindAllIdeaInspectionIssues() {
        shouldFindIssuesOfTool(1, new IdeaInspection(), "IdeaInspectionExample.xml");
    }

    /** Runs the Intel parser on an output file that contains 7 issues. */
    @Test
    void shouldFindAllIntelIssues() {
        shouldFindIssuesOfTool(7, new Intel(), "intelc.txt");
    }

    /** Runs the Oracle Invalids parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllInvalidsIssues() {
        shouldFindIssuesOfTool(3, new Invalids(), "invalids.txt");
    }

    /** Runs the Java parser on several output files that contain 2 + 1 + 1 + 1 + 2 issues. */
    @Test
    void shouldFindAllJavaIssues() {
        shouldFindIssuesOfTool(2 + 1 + 1 + 1 + 2, new Java(), "javac.txt", "gradle.java.log",
                "gradle.another.java.log",
                "ant-javac.txt", "hpi.txt");
    }

    /**
     * Runs the Kotlin parser on several output files that contain 1 issues.
     */
    @Test
    void shouldFindAllKotlinIssues() {
        shouldFindIssuesOfTool(1, new Kotlin(), "kotlin.txt");
    }

    /**
     * Runs the CssLint parser on an output file that contains 51 issues.
     */
    @Test
    void shouldFindAllCssLintIssues() {
        shouldFindIssuesOfTool(51, new CssLint(), "csslint.xml");
    }

    /** Runs the DiabC parser on an output file that contains 12 issues. */
    @Test
    void shouldFindAllDiabCIssues() {
        shouldFindIssuesOfTool(12, new DiabC(), "diabc.txt");
    }

    /** Runs the Polyspace parser on output files that contains 9 + 4 issues. */
    @Test
    void shouldFindAllPolyspaceIssues() {
        shouldFindIssuesOfTool(9 + 4, new Polyspace(), "polyspace.csv", "polyspace_cp.csv");
    }

    /** Runs the Doxygen parser on an output file that contains 18 issues. */
    @Test
    void shouldFindAllDoxygenIssues() {
        shouldFindIssuesOfTool(19, new Doxygen(), "doxygen.txt");
    }

    /** Runs the Dr. Memory parser on an output file that contains 8 issues. */
    @Test
    void shouldFindAllDrMemoryIssues() {
        shouldFindIssuesOfTool(8, new DrMemory(), "drmemory.txt");
    }

    /** Runs the PVS-Studio parser on an output file that contains 33 issues. */
    @Test
    void shouldFindAllPVSStudioIssues() {
        shouldFindIssuesOfTool(33, new PVSStudio(), "TestReport.plog");
    }

    /** Runs the JavaC parser on an output file of the Eclipse compiler: the build should report no issues. */
    @Test
    void shouldFindNoJavacIssuesInEclipseOutput() {
        shouldFindIssuesOfTool(0, new Java(), "eclipse.txt");
    }

    /** Runs the ProtoLint parser on an output file that contains 10 issues. */
    @Test
    void shouldFindAllProtoLintIssues() {
        shouldFindIssuesOfTool(10, new ProtoLint(), "protolint.txt");
    }

    /** Runs the HadoLint parser on an output file that contains 5 issues. */
    @Test
    void shouldFindAllHadoLintIssues() {
        shouldFindIssuesOfTool(5, new HadoLint(), "hadolint.json");
    }

    /** Runs the DockerLint parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllDockerLintIssues() {
        shouldFindIssuesOfTool(3, new DockerLint(), "dockerlint.json");
    }

    /** Runs the Clair parser on an output file that contains 112 issues. */
    @Test
    void shouldFindAllClairIssues() {
        shouldFindIssuesOfTool(112, new Clair(), "clair.json");
    }

    /** Runs the OTDockerLint parser on an output file that contains 5 issues. */
    @Test
    void shouldFindAllOTDockerLintIssues() {
        shouldFindIssuesOfTool(3, new OTDockerLint(), "ot-docker-linter.json");
    }

    /** Runs the OWASP dependency check parser on an output file that contains 2 issues. */
    @Test
    void shouldFindOwaspDependencyCheckIssues() {
        var action = shouldFindIssuesOfTool(2, new OwaspDependencyCheck(), "dependency-check-report.json");

        assertThat(action.getIconFileName()).contains("shield");
    }

    /** Runs the Brakeman parser on an output file that contains 32 issues. */
    @Test
    void shouldFindAllBrakemanIssues() {
        shouldFindIssuesOfTool(32, new Brakeman(), "brakeman.json");
    }

    /** Runs the Simulink Check parser on an output file that contains 12 issues. */
    @Test
    void shouldFindAllSimulinkCheckIssues() {
        shouldFindIssuesOfTool(12, new SimulinkCheck(), "simulink.html");
    }

    /** Runs the Embedded Engineer parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllEmbeddedEngineerIssues() {
        shouldFindIssuesOfTool(8, new EmbeddedEngineer(), "ea.log");
    }

    /** Runs the Embedded Engineer parser on an output file that contains 6 issues. */
    @Test
    void shouldFindAllCrossCoreEmbeddedStudioIssues() {
        shouldFindIssuesOfTool(6, new CrossCoreEmbeddedStudio(), "cces.log");
    }

    /** Runs the Simulink Code Generator parser on an output file that contains 8 issues. */
    @Test
    void shouldFindAllCodeGeneratorIssues() {
        shouldFindIssuesOfTool(8, new CodeGenerator(), "CodeGenerator.log");
    }

    /** Runs the trivy parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllTrivyIssues() {
        shouldFindIssuesOfTool(4, new Trivy(), "trivy_result.json");
    }

    /** Runs the Aqua Scanner parser on an output file that contains 14 issues. */
    @Test
    void shouldFindAllAquaScannerIssues() {
        shouldFindIssuesOfTool(14, new AquaScanner(), "aqua_scanner_result.json");
    }

    /** Runs the Veracode Pipeline Scanner parser on an output file that contains 5 issues. */
    @Test
    void shouldFindAllVeracodePipelineScannerIssues() {
        shouldFindIssuesOfTool(5, new VeraCodePipelineScanner(), "veracode_pipeline_scanner_result.json");
    }

    /** Runs the qt translation parser on an output file that contains 4 issues. */
    @Test
    void shouldFindAllQtTranslationIssues() {
        shouldFindIssuesOfTool(4, new QtTranslation(), "qttranslation.ts");
    }

    /** Runs the oelint-adv parser on an output file that contains 8 issues. */
    @Test
    void shouldFindAllOELintAdvIssues() {
        shouldFindIssuesOfTool(8, new OELintAdv(), "oelint-adv.txt");
    }

    /** Runs the Grype analysis parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllGrypeIssues() {
        shouldFindIssuesOfTool(3, new Grype(), "grype-report.json");
    }

    /** Runs the Vale analysis parser on an output file that contains 3 issues. */
    @Test
    void shouldFindAllValeIssues() {
        shouldFindIssuesOfTool(12, new Vale(), "vale-report.json");
    }

    private ResultAction shouldFindIssuesOfTool(final int expectedSizeOfIssues, final ReportScanningTool tool,
            final String... fileNames) {
        var defaultPipelineDefinition = "recordIssues tool: %s(pattern:'**/%s', reportEncoding:'UTF-8')";

        var action = findIssuesInPipeline(defaultPipelineDefinition,
                expectedSizeOfIssues, tool, fileNames);

        var ansiPipelineDefinition = "wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {\n"
                + "  " + defaultPipelineDefinition + "\n"
                + "}";

        findIssuesInPipeline(ansiPipelineDefinition, expectedSizeOfIssues, tool, fileNames);

        return action;
    }

    private Report findReportWithoutAnsiColorPlugin(final int expectedSizeOfIssues, final ReportScanningTool tool,
            final String... fileNames) {
        return findIssuesWithoutAnsiColorPlugin(expectedSizeOfIssues, tool, fileNames).getResult().getIssues();
    }

    private ResultAction findIssuesWithoutAnsiColorPlugin(final int expectedSizeOfIssues, final ReportScanningTool tool,
            final String... fileNames) {
        return findIssuesInPipeline(
                "recordIssues tool: %s(pattern:'**/%s', reportEncoding:'UTF-8')", expectedSizeOfIssues, tool,
                fileNames);
    }

    private Report findReportWithAnsiColorPlugin(final int expectedSizeOfIssues,
            final ReportScanningTool tool, final String... fileNames) {
        return findIssuesWithAnsiColorPlugin(expectedSizeOfIssues, tool, fileNames).getResult().getIssues();
    }

    private ResultAction findIssuesWithAnsiColorPlugin(final int expectedSizeOfIssues,
            final ReportScanningTool tool, final String... fileNames) {
        var pipelineDefinition = """
                wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
                  recordIssues tool: %s(pattern:'**/%s', reportEncoding:'UTF-8')
                }\
                """;
        return findIssuesInPipeline(pipelineDefinition, expectedSizeOfIssues, tool, fileNames);
    }

    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock", "PMD.LinguisticNaming"})
    private ResultAction findIssuesInPipeline(final String pipelineDefinition, final int expectedSizeOfIssues, final ReportScanningTool tool, final String... fileNames) {
        try {
            var job = createPipeline();
            copyMultipleFilesToWorkspace(job, fileNames);
            job.setDefinition(asStage(pipelineDefinition.formatted(
                    tool.getSymbolName(), createPatternFor(fileNames))));

            var result = scheduleSuccessfulBuild(job);

            assertThat(result).hasTotalSize(expectedSizeOfIssues);
            assertThat(result.getIssues()).hasSize(expectedSizeOfIssues);

            var report = result.getIssues();

            assertThat(report.filter(issue -> issue.getOrigin().equals(tool.getActualId())))
                    .hasSize(expectedSizeOfIssues);

            return result.getOwner().getAction(ResultAction.class);
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private String createPatternFor(final String... fileNames) {
        return Arrays.stream(fileNames).map(s -> "**/" + s).collect(Collectors.joining(", "));
    }
}
