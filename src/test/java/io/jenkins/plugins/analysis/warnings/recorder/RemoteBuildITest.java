package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Result;
import hudson.model.Slave;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.TabType;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssueRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssuesTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceCodeView;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests if Remote Jobs can successfully record Issues.
 */
public class RemoteBuildITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Tests the basic functionality of running a pipeline job on a slave.
     * The pipeline s configured with a java issue recorder and file inputs.
     */
    @Test
    public void shouldRunBuildOnDumbSlave() {

        Slave agent = createAgentWithEnabledSecurity("slave");

        WorkflowJob project = createPipeline();
        createFileInAgentWorkspace(agent, project, "Hello.java", "public class Hello extends Old {}");
        createFileInAgentWorkspace(agent, project, "javac.txt", "[WARNING] Hello.java:[1,42] [deprecation] Something uses Old.class\n");

        project.setDefinition(new CpsFlowDefinition("node('slave') {recordIssues tool: java(pattern: '**/*.txt')}", true));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(1);
    }

    /**
     * Tests that the results are shown on the web page.
     */
    @Test
    public void shouldShowDumbSlaveResultInDetailsTab() {
        Slave agent = createAgentWithEnabledSecurity("slave");

        WorkflowJob project = createPipeline();

        createFileInAgentWorkspace(agent, project, "Hello.java", "public class Hello extends Old {}");
        createFileInAgentWorkspace(agent, project, "javac.txt", "[WARNING] Hello.java:[1,42] [deprecation] Something uses Old.class\n");

        project.setDefinition(new CpsFlowDefinition("node('slave') {recordIssues tool: java(pattern: '**/*.txt')}", true));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        DetailsTab details = new DetailsTab(getWebPage(JavaScriptSupport.JS_ENABLED, result));

        IssuesTable issues = details.select(TabType.ISSUES);
        assertThat(issues.getRows()).hasSize(1);

        SourceCodeView actualJavaContent = issues.getRow(1).click(IssueRow.FILE);
        assertThat(actualJavaContent.getIssueMessage()).isEqualTo("Something uses Old.class");
    }
}
