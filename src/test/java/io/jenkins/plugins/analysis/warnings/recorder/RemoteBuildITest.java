package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Slave;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssuesTable;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests if Remote Jobs can successfully record Issues.
 */
public class RemoteBuildITest  extends IntegrationTestWithJenkinsPerSuite {

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
    public void shouldShowDumbSlaveResultsOnWebPage() {

        Slave agent = createAgentWithEnabledSecurity("slave");

        WorkflowJob project = createPipeline();

        createFileInAgentWorkspace(agent, project, "Hello.java", "public class Hello extends Old {}");
        createFileInAgentWorkspace(agent, project, "javac.txt", "[WARNING] Hello.java:[1,42] [deprecation] Something uses Old.class\n");

        project.setDefinition(new CpsFlowDefinition("node('slave') {recordIssues tool: java(pattern: '**/*.txt')}", true));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        HtmlPage detailsPage = getDetailsWebPage(project, result);
        IssuesTable table = new IssuesTable(detailsPage);
        assertThat(table.getRows()).hasSize(1);
    }

    /**
     * Get the details web page of a recent build.
     *
     * @param project
     *         of the build used for web request
     * @param result
     *         of the most recent build to show the charts
     *
     * @return loaded web page which contains the charts
     */
    private HtmlPage getDetailsWebPage(final WorkflowJob project, final AnalysisResult result) {
        int buildNumber = result.getBuild().getNumber();
        String pluginId = "java";
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);
    }
}
