package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Slave;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.TabType;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssueRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssuesTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceCodeView;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for builds running on a 'DumbSlave' worker node.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
public class DumbSlaveITest extends IntegrationTestWithJenkinsPerTest {
    private static final String JAVA_CONTENT = "public class Test {}";
    private static final String C_SHARP_CONTENT = "hallo welt";

    /**
     * Verifies that affected source files are copied to Jenkins build folder, even if the master - agent security
     * is active, see JENKINS-56007 for details.
     */
    @Test @Issue("JENKINS-56007")
    public void shouldCopySourcesIfMasterAgentSecurityIsActive() {
        Slave agent = createAgentWithEnabledSecurity("agent");

        WorkflowJob project = createPipeline();

        createFileInAgentWorkspace(agent, project, "Test.java", JAVA_CONTENT);
        createFileInAgentWorkspace(agent, project, "MediaPortal.cs", C_SHARP_CONTENT);

        project.setDefinition(new CpsFlowDefinition("node('agent') {\n"
                + "    echo '[javac] Test.java:39: warning: Test Warning'\n"
                + "    echo 'MediaPortal.cs(3001,5): warning CS0162: Hier kommt der Warnings Text'\n"
                + "    recordIssues tools: [msBuild(), java()], aggregatingResults: true\n"
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).hasNoErrorMessages();
        assertThat(result).hasTotalSize(2);

        DetailsTab details = new DetailsTab(getWebPage(JavaScriptSupport.JS_ENABLED, result));

        IssuesTable issues = details.select(TabType.ISSUES);
        assertThat(issues.getRows()).hasSize(2);

        SourceCodeView actualCSharpContent = issues.getRow(0).click(IssueRow.FILE);
        assertThat(actualCSharpContent.getSourceCode()).isEqualTo(C_SHARP_CONTENT);

        SourceCodeView actualJavaContent = issues.getRow(1).click(IssueRow.FILE);
        assertThat(actualJavaContent.getSourceCode()).isEqualTo(JAVA_CONTENT);
    }
}
