package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import java.util.List;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.prism.SourceCodeViewModel;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
class StepsOnAgentITest extends IntegrationTestWithJenkinsPerTest {
    private static final String JAVA_CONTENT = "public class Test {}";
    private static final String JAVA_ID = "java-1";

    /**
     * Verifies that affected source files are copied to Jenkins build folder, even if the controller - agent security is
     * active, see JENKINS-56007 for details.
     */
    @Test
    @Issue("JENKINS-56007")
    void shouldCopySourcesIfMasterAgentSecurityIsActive() {
        var agent = createAgentWithEnabledSecurity("agent");

        var project = createPipeline();

        createFileInAgentWorkspace(agent, project, "Test.java", JAVA_CONTENT);

        project.setDefinition(createPipelineScript("""
                node('agent') {
                    echo '[javac] Test.java:39: warning: Test Warning'
                    recordIssues tool: java(), skipBlames: true
                }\
                """));

        var result = scheduleSuccessfulBuild(project);
        assertThat(result).hasNoErrorMessages();
        assertThat(result).hasTotalSize(1);
        assertThat(getConsoleLog(result)).contains("1 copied", "0 not in workspace", "0 not-found", "0 with I/O error");

        // TODO: check for the links in the table model
        assertThat(getSourceCode(result, 0)).contains(JAVA_CONTENT);
    }

    private String getSourceCode(final AnalysisResult result, final int rowIndex) {
        var target = result.getOwner().getAction(ResultAction.class).getTarget();
        var sourceCodeUrl = new FileNameRenderer(result.getOwner()).getSourceCodeUrl(
                result.getIssues().get(rowIndex));
        var dynamic = (SourceCodeViewModel) target.getDynamic(
                sourceCodeUrl.replaceAll("/#.*", ""), null, null);
        return dynamic.getSourceCode();
    }

    /**
     * Creates a JenkinsFile with parallel steps and aggregates the warnings.
     */
    @Test
    void shouldRecordOutputOfParallelSteps() {
        var job = createPipeline();

        copySingleFileToAgentWorkspace(createAgent("node1"), job, "eclipse.txt", "issues.txt");
        copySingleFileToAgentWorkspace(createAgent("node2"), job, "eclipse.txt", "issues.txt");

        job.setDefinition(readJenkinsFile("parallel.jenkinsfile"));

        Run<?, ?> run = buildSuccessfully(job);
        List<ResultAction> actions = run.getActions(ResultAction.class);

        assertThat(actions).hasSize(2);

        ResultAction first;
        ResultAction second;
        if (JAVA_ID.equals(actions.get(0).getId())) {
            first = actions.get(0);
            second = actions.get(1);
        }
        else {
            first = actions.get(1);
            second = actions.get(0);
        }

        assertThat(first.getResult().getIssues()).hasSize(5);
        assertThat(second.getResult().getIssues()).hasSize(3);
    }

    /**
     * Verifies that source files are not retained in the Jenkins build folder when
     * the 'sourceCodeRetention' policy is set to 'NEVER'.
     **/
    @Test
    void shouldNotCopySourcesWhenSourceCodeRetentionIsNever() {
        var agent = createAgentWithEnabledSecurity("agent");

        var project = createPipeline();

        createFileInAgentWorkspace(agent, project, "Test.java", JAVA_CONTENT);

        project.setDefinition(createPipelineScript("""
                node('agent') {
                    echo '[javac] Test.java:39: warning: Test Warning'
                    recordIssues tool: java(), sourceCodeRetention: 'NEVER'
                }\
                """));

        var result = scheduleSuccessfulBuild(project);
        assertThat(result).hasNoErrorMessages();
        assertThat(result).hasTotalSize(1);
        assertThat(getConsoleLog(result)).contains("Skipping copying of affected files");
        assertThat(getSourceCode(result, 0)).contains("FileNotFoundException");
    }

    /**
     * Verifies that the XML parser infrastructure (SAXParserFactory / DocumentBuilderFactory) is pre-warmed at the
     * start of post-processing on the agent, so that Xerces classes are loaded only once via the RemoteClassLoader
     * instead of being fetched repeatedly for each parsed module file.
     *
     * @see <a href="https://issues.jenkins.io/browse/JENKINS-66268">JENKINS-66268</a>
     */
    @Test
    @Issue("JENKINS-66268")
    void shouldPreWarmXmlParsersOnAgentWithoutErrors() {
        var agent = createAgent("xml-warmup-agent");

        var project = createPipeline();

        copySingleFileToAgentWorkspace(agent, project, "eclipse.txt", "issues.txt");

        project.setDefinition(createPipelineScript("""
                node('xml-warmup-agent') {
                    recordIssues tool: eclipse(pattern: 'issues.txt'), skipBlames: true
                }\
                """));

        var result = scheduleSuccessfulBuild(project);

        assertThat(result).hasNoErrorMessages();
        assertThat(result).hasTotalSize(8);
        assertThat(getConsoleLog(result)).doesNotContain("Failed to pre-warm XML parser infrastructure");
        assertThat(result).hasInfoMessages(
                "Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");
    }

    /**
     * Verifies that module name detection via pom.xml parsing works correctly on an agent after the XML parser
     * warm-up (JENKINS-66268), confirming that the warm-up does not interfere with actual XML file parsing.
     *
     * @see <a href="https://issues.jenkins.io/browse/JENKINS-66268">JENKINS-66268</a>
     */
    @Test
    @Issue("JENKINS-66268")
    void shouldCorrectlyDetectMavenModulesOnAgentAfterXmlParserWarmUp() {
        var agent = createAgent("maven-module-agent");

        var project = createPipeline();

        copySingleFileToAgentWorkspace(agent, project, "eclipse.txt", "issues.txt");
        copySingleFileToAgentWorkspace(agent, project,
                "detectors/buildfiles/maven/pom.xml", "pom.xml");

        project.setDefinition(createPipelineScript("""
                node('maven-module-agent') {
                    recordIssues tool: eclipse(pattern: 'issues.txt'), skipBlames: true
                }\
                """));

        var result = scheduleSuccessfulBuild(project);

        assertThat(result).hasNoErrorMessages();
        assertThat(getConsoleLog(result)).doesNotContain("Failed to pre-warm XML parser infrastructure");
        assertThat(result).hasInfoMessages(
                "Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)");
    }
}
