package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    private void verifyHealthOfProjectWithWarnings(String file, int warnings, int health, boolean withHealthReport){
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project,file, "javac.txt");
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);
        if (withHealthReport) {
            configureHealthReport(project);
        }
        AnalysisResult result = scheduleBuildAndAssertStatus(project,Result.SUCCESS);
        //String buildnumber =String.valueOf(project.getLastBuild().getNumber());
        //HtmlJenkinsInfo html = new HtmlJenkinsInfo(project, buildnumber+"/java/info");

        assertThat(result).hasTotalSize(warnings);

        if (withHealthReport) {
            assertThat(result.getInfoMessages()).contains("Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)");
            assertThat(project.getBuildHealth().getScore()).isEqualTo(health);
        }
        else {
            assertThat(result.getInfoMessages()).contains("Health report is disabled - skipping");
        }
    }

    private void configureHealthReport(Project project) {
        HtmlForm webForm = getWebPage(project,"configure").getFormByName("config");
        ((HtmlNumberInput)webForm.getInputByName("_.healthy")).setText(String.valueOf(1));
        ((HtmlNumberInput)webForm.getInputByName("_.unhealthy")).setText(String.valueOf(9));
        submit(webForm);
    }

    @Test
    public void shouldCreateReportWithZeroWarnings() {
        verifyHealthOfProjectWithWarnings("demoJavacZeroWarnings.txt",0,100,true);
    }

    @Test
    public void shouldCreateReportWithOneWarning() {
        verifyHealthOfProjectWithWarnings("demoJavacOneWarning.txt",1,90,true);
    }

    @Test
    public void shouldCreateReportWithNineWarnings() {
        verifyHealthOfProjectWithWarnings("demoJavacNineWarnings.txt",9,10,true);
    }

    @Test
    public void shouldCreateReportWithTenWarnings() {
        verifyHealthOfProjectWithWarnings("demoJavacTenWarnings.txt",10,0,true);
    }


    @Test
    public void shouldCreateReportWithZeroWarningsWithDisabledHealthReport() {
        verifyHealthOfProjectWithWarnings("demoJavacZeroWarnings.txt",0,100,false);
    }

    @Test
    public void shouldCreateReportWithOneWarningWithDisabledHealthReport() {
        verifyHealthOfProjectWithWarnings("demoJavacOneWarning.txt",1,100,false);
    }

    @Test
    public void shouldCreateReportWithNineWarningsWithDisabledHealthReport() {
        verifyHealthOfProjectWithWarnings("demoJavacNineWarnings.txt",9,100,false);
    }

    @Test
    public void shouldCreateReportWithTenWarningsWithDisabledHealthReport() {
        verifyHealthOfProjectWithWarnings("demoJavacTenWarnings.txt",10,100,false);
    }


    private class HtmlJenkinsInfo {

        HtmlPage webPage;

        HtmlJenkinsInfo(Project project, String remainingUrl){
            webPage = getWebPage(project,remainingUrl);
        }

    }

    private class HtmlConfiguration {
        private static final String HEALTHY = "_.healthy";
        private static final String UNHEALTHY = "_.unhealthy";
        HtmlForm webForm;

        HtmlConfiguration(Project project) {
            webForm = getWebPage(project,"configure").getFormByName("config");
        }

        void setHealthy(int healthy) {
            ((HtmlNumberInput)webForm.getInputByName(HEALTHY)).setText(String.valueOf(healthy));
        }

        void setUnhealthy(int unhealthy) {
            ((HtmlNumberInput)webForm.getInputByName(UNHEALTHY)).setText(String.valueOf(unhealthy));
        }

        HtmlForm getWebForm() {
            return webForm;
        }


    }


}
