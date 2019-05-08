package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.List;
import java.util.Objects;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsViewCharts;

import static org.assertj.core.api.Assertions.*;

public class ChartsITest extends IntegrationTestWithJenkinsPerSuite {



    @Test
    public void shouldShowFullPieChart() {


        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.FAILURE);


        createWorkspaceFileWithWarnings(project, "javac.txt", 1);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);


        //assertThat(result.getTotalSize()).isEqualTo(2);
        //assertThat(result.getQualityGateStatus()).isEqualTo(QualityGateStatus.PASSED);

        HtmlPage page = getDetailsWebPage(project, result);
        DomElement carousel = page.getElementsById("overview-carousel").get(0);
        DomElement trendChart = page.getElementsById("trend-chart").get(0);
        DomElement severitiesChart = page.getElementsById("severities-chart").get(0);
        DetailsViewCharts charts = new DetailsViewCharts(page);
        charts.getOverviewCarousel();

    }


    private HtmlPage getDetailsWebPage(final FreeStyleProject project, final AnalysisResult result) {
        int buildNumber = result.getBuild().getNumber();
        String pluginId = result.getId();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);
    }

    private void createWorkspaceFileWithWarnings(final FreeStyleProject project, final String name, final int numWarnings) {
        StringBuilder warningText = new StringBuilder();
        for (int i = 0; i < numWarnings; i++) {
            warningText.append(createDeprecationWarning(i)).append("\n");
        }
        createFileInWorkspace(project, name, warningText.toString());
    }

    private String createDeprecationWarning(final int lineNumber) {
        // TODO: make constant
        return String.format("[WARNING] C:\\Path\\SourceFile.java:[%d,42] [deprecation] path.AClass in path has been deprecated\n", lineNumber);
    }
}
