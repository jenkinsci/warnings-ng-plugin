package io.jenkins.plugins.analysis.warnings.steps;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Run;
import hudson.model.Slave;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.IssuesDetail;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.prism.SourceCodeViewModel;
import org.junitpioneer.jupiter.Issue;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
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
        Slave agent = createAgentWithEnabledSecurity("agent");

        WorkflowJob project = createPipeline();

        createFileInAgentWorkspace(agent, project, "Test.java", JAVA_CONTENT);

        project.setDefinition(new CpsFlowDefinition("node('agent') {\n"
                + "    echo '[javac] Test.java:39: warning: Test Warning'\n"
                + "    recordIssues tool: java(), skipBlames: true\n"
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).hasNoErrorMessages();
        assertThat(result).hasTotalSize(1);
        assertThat(getConsoleLog(result)).contains("1 copied", "0 not in workspace", "0 not-found", "0 with I/O error");

        // TODO: check for the links in the table model
        assertThat(getSourceCode(result, 0)).contains(JAVA_CONTENT);

    }

    private String getSourceCode(final AnalysisResult result, final int rowIndex) {
        IssuesDetail target = result.getOwner().getAction(ResultAction.class).getTarget();
        String sourceCodeUrl = new FileNameRenderer(result.getOwner()).getSourceCodeUrl(
                result.getIssues().get(rowIndex));
        SourceCodeViewModel dynamic = (SourceCodeViewModel) target.getDynamic(
                sourceCodeUrl.replaceAll("/#.*", ""), null, null);
        return dynamic.getSourceCode();
    }

    /**
     * Creates a JenkinsFile with parallel steps and aggregates the warnings.
     */
    @Test
    void shouldRecordOutputOfParallelSteps() {
        WorkflowJob job = createPipeline();

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
     * the 'sourceCodeRetention' policy is set to 'NEVER'
     **/
    @Test
    void shouldNotCopySourcesWhenSourceCodeRetentionIsNever() {
        Slave agent = createAgentWithEnabledSecurity("agent");

        WorkflowJob project = createPipeline();

        createFileInAgentWorkspace(agent, project, "Test.java", JAVA_CONTENT);

        project.setDefinition(new CpsFlowDefinition("node('agent') {\n"
                + "    echo '[javac] Test.java:39: warning: Test Warning'\n"
                + "    recordIssues tool: java(), sourceCodeRetention: 'NEVER'\n"
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).hasNoErrorMessages();
        assertThat(result).hasTotalSize(1);
        assertThat(getSourceCode(result, 0)).contains("FileNotFoundException");

    }

}
