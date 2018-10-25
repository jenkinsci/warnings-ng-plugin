package io.jenkins.plugins.analysis.warnings;

import java.util.Arrays;
import java.util.List;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.JenkinsRule;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.util.SymbolNameGenerator;

/**
 * Integration tests of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
@RunWith(Parameterized.class)
public class StepsSymbolTest extends IntegrationTest {

    private StaticAnalysisTool tool;
    @Rule
    public JenkinsRule jenkinsPerTest = new JenkinsRule();

    @Override
    final public  JenkinsRule getJenkins() {
        return jenkinsPerTest;
    }

    @Parameters
    public static List<StaticAnalysisTool> configs(){
        return Arrays.asList(
            new AcuCobol(),
                    new Ajc(),
                    new AndroidLint(),
                    new AnsibleLint(),
                    new ArmCc(),
                    new Buckminster(),
                    new Cadence(),
                    new Ccm(),
                    new CheckStyle(),
                    new Clang(),
                    new ClangTidy(),
                    new CodeAnalysis(),
                    new CodeNArc(),
                    new Coolflux(),
                    new Cpd(),
                    new CppCheck(),
                    new CppLint(),
                    new CssLint(),
                    new Detekt(),
                    new DiabC(),
                    new DocFx(),
                    new Doxygen(),
                    new DrMemory(),
                    new DupFinder(),
                    // new DuplicateCodeScanner(),
                    new Eclipse(),
                    new Erlc(),
                    new ErrorProne(),
                    new EsLint(),
                    new FindBugs(),
//                    new FindBugsMessages(),
                    new Flake8(),
                    new FlexSDK(),
                    new Fxcop(),
                    new Gcc3(),
                    new Gcc4(),
                    new Gendarme(),
                    new GhsMulti(),
                    new Gnat(),
                    new GnuFortran(),
                    new GnuMakeGcc(),
                    new GoLint(),
                    new GoVet(),
                    new GroovyScript(),
                    new Iar(),
//                    // new IconLabelProvider(),
                    new IdeaInspection(),
                    new Infer(),
                    new Intel(),
                    new Invalids(),
                    new Java(),
                    new JavaDoc(),
                    new JcReport(),
                    new JsHint(),
                    new KlocWork(),
                    new KtLint(),
                    new LinuxKernelOutput(),
                    new MavenConsole(),
                    new MetrowerksCodeWarrior(),
                    new MsBuild(),
                    new MyPy(),
                    new NagFortran(),
                    new PREfast(),
                    new Pep8(),
                    new Perforce(),
                    new PerlCritic(),
                    new Php(),
                    new PhpCodeSniffer(),
                    new Pit(),
                    new Pmd(),
//                    new PmdMessages(),
                    new PuppetLint(),
                    new PyDocStyle(),
                    new PyLint(),
                    new QACSourceCodeAnalyser(),
                    new RFLint(),
                    new ResharperInspectCode(),
                    new Robocopy(),
                    new RuboCop(),
                    new Scala(),
                    new Simian(),
                    new SonarQube(),
                    new SphinxBuild(),
                    new SpotBugs(),
                    new StyleCop(),
                    new SunC(),
                    new SwiftLint(),
                    new TaskingVx(),
                    new TiCss(),
                    new Tnsdl(),
                    new TsLint(),
                    new Xlc(),
                    new XmlLint(),
                    new YamlLint(),
                    new YuiCompressor(),
                    new ZptLint()
        );
    }


    public  StepsSymbolTest(StaticAnalysisTool tool) {
        super();
        this.tool = tool;
    }

    @Test
    public void emptyTest(){
        String name = new SymbolNameGenerator().getSymbolName(this.tool.getClass());

        WorkflowJob job = createJob();
        job.setDefinition(asStage(
                "def issues = scanForIssues tool: " + name + "()",
                PUBLISH_ISSUES_STEP));
        AnalysisResult result = scheduleBuild(job, tool.getClass());

        assertThat(result.getTotalSize()).isEqualTo(0);
        assertThat(result.getIssues()).hasSize(0);

    }

}
