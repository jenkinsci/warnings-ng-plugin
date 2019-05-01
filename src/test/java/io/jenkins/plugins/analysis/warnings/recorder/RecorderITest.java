package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
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

    private void verifyHealthOfProjectWithWarnings(String file, int warnings, int health, boolean withHealthReport) {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, file, "javac.txt");
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);
        if (withHealthReport) {
            configureHealthReport(project);
        }
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        String buildnumber = String.valueOf(project.getLastBuild().getNumber());
        HtmlJenkinsJavaInfo html = new HtmlJenkinsJavaInfo(project, buildnumber + "/java/info");

        assertThat(result).hasTotalSize(warnings);
        assertThat(result.getInfoMessages()).isEqualTo(html.getInfoMessages());
        assertThat(result.getErrorMessages()).isEqualTo(html.getErrorMessages());

        if (withHealthReport) {
            assertThat(result.getInfoMessages()).contains(
                    "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)");
            assertThat(project.getBuildHealth().getScore()).isEqualTo(health);
        }
        else {
            assertThat(result.getInfoMessages()).contains("Health report is disabled - skipping");
        }
    }

    private void configureHealthReport(Project project) {
        HtmlForm webForm = getWebPage(project, "configure").getFormByName("config");
        ((HtmlNumberInput) webForm.getInputByName("_.healthy")).setText(String.valueOf(1));
        ((HtmlNumberInput) webForm.getInputByName("_.unhealthy")).setText(String.valueOf(9));
        submit(webForm);
    }

    @Test
    public void shouldCreateReportWithZeroWarnings() {
        verifyHealthOfProjectWithWarnings("demoJavacZeroWarnings.txt", 0, 100, true);
    }

    @Test
    public void shouldCreateReportWithOneWarning() {
        verifyHealthOfProjectWithWarnings("demoJavacOneWarning.txt", 1, 90, true);
    }

    @Test
    public void shouldCreateReportWithNineWarnings() {
        verifyHealthOfProjectWithWarnings("demoJavacNineWarnings.txt", 9, 10, true);
    }

    @Test
    public void shouldCreateReportWithTenWarnings() {
        verifyHealthOfProjectWithWarnings("demoJavacTenWarnings.txt", 10, 0, true);
    }

    @Test
    public void shouldCreateReportWithZeroWarningsWithDisabledHealthReport() {
        verifyHealthOfProjectWithWarnings("demoJavacZeroWarnings.txt", 0, 100, false);
    }

    @Test
    public void shouldCreateReportWithOneWarningWithDisabledHealthReport() {
        verifyHealthOfProjectWithWarnings("demoJavacOneWarning.txt", 1, 100, false);
    }

    @Test
    public void shouldCreateReportWithNineWarningsWithDisabledHealthReport() {
        verifyHealthOfProjectWithWarnings("demoJavacNineWarnings.txt", 9, 100, false);
    }

    @Test
    public void shouldCreateReportWithTenWarningsWithDisabledHealthReport() {
        verifyHealthOfProjectWithWarnings("demoJavacTenWarnings.txt", 10, 100, false);
    }

    /**
     * Page object for buildNumber/java/info HtmlPage.
     */
    private class HtmlJenkinsJavaInfo {

        HtmlPage webPage;

        HtmlJenkinsJavaInfo(Project project, String remainingUrl) {
            String buildnumber = String.valueOf(project.getLastBuild().getNumber());
            webPage = getWebPage(project, buildnumber + "/java/info");
        }

        ImmutableList<String> getInfoMessages() {

            return getMessagesById("info");
        }

        ImmutableList<String> getErrorMessages() {

            return getMessagesById("errors");
        }

        private ImmutableList<String> getMessagesById(String idString) {
            List<String> messages = new ArrayList<>();
            DomElement messageElement = webPage.getElementById(idString);
            if (messageElement != null && messageElement.hasChildNodes()) {
                for (DomElement message : messageElement.getChildElements()) {
                    messages.add(message.getTextContent());
                }
            }
            return Lists.immutable.withAll(messages);
        }

    }

}
