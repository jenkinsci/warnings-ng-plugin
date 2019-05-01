package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Project;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;


/**
 * Verifies the info page of Java Warnings Recorders and their health report configuration via HTML Unit Tests.
 *
 * @author Andreas Neumeier
 */
public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    private static final int HEALTHY = 1;
    private static final int UNHEALTHY = 9;
    private static final String HEALTH_REPORT_ENABLED_MESSAGE = "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)";
    private static final String HEALTH_REPORT_DISABLED_MESSAGE = "Health report is disabled - skipping";

    /***
     * Checks whether a FreestyleJob with HealthReport and no warning scores 100 and the information displayed on the UI is correct.
     * @throws IOException Button not found in HTML.
     */
    @Test
    public void verifyFreestyleJobWithHealthReportAndZeroWarnings() throws IOException {
        verifyFreeStyleProject("java0Warnings.txt", HEALTHY, UNHEALTHY, 0, 100, true);
    }

    /***
     * Checks whether a FreestyleJob with HealthReport and six warning scores 40 and the information displayed on the UI is correct.
     * @throws IOException Button not found in HTML.
     */
    @Test
    public void verifyFreestyleJobWithHealthReportAndSixWarnings() throws IOException {
        verifyFreeStyleProject("java6Warnings.txt", HEALTHY, UNHEALTHY, 6, 40, true);
    }

    /***
     * Checks whether a FreestyleJob with HealthReport and ten warning scores 0 and the information displayed on the UI is correct.
     * @throws IOException Button not found in HTML.
     */
    @Test
    public void verifyFreestyleJobWithHealthReportAndTenWarnings() throws IOException {
        verifyFreeStyleProject("java10Warnings.txt", HEALTHY, UNHEALTHY, 10, 0, true);
    }

    /***
     * Checks whether a FreestyleJob with HealthReport and eleven warning scores 0 and the information displayed on the UI is correct.
     * @throws IOException Button not found in HTML.
     */
    @Test
    public void verifyFreestyleJobWithHealthReportAndElevenWarnings() throws IOException {
        verifyFreeStyleProject("java11Warnings.txt", HEALTHY, UNHEALTHY, 11, 0, true);
    }

    /***
     * Checks whether a FreestyleJob result without HealthReport and no warning shows up correctly on API and UI.
     * @throws IOException Button not found in HTML.
     */
    @Test
    public void verifyFreestyleJobWithZeroWarnings() throws IOException {
        verifyFreeStyleProject("java0Warnings.txt", HEALTHY, UNHEALTHY, 0, 10, false);
    }

    /***
     * Checks whether a FreestyleJob result without HealthReport and six warning shows up correctly on API and UI.
     * @throws IOException Button not found in HTML.
     */
    @Test
    public void verifyFreestyleJobWithSixWarnings() throws IOException {
        verifyFreeStyleProject("java6Warnings.txt", HEALTHY, UNHEALTHY, 6, 40, false);
    }

    /***
     * Checks whether a FreestyleJob result without HealthReport and ten warning shows up correctly on API and UI.
     * @throws IOException Button not found in HTML.
     */
    @Test
    public void verifyFreestyleJobWithTenWarnings() throws IOException {
        verifyFreeStyleProject("java10Warnings.txt", HEALTHY, UNHEALTHY, 10, 0, false);
    }

    /***
     * Checks whether a FreestyleJob result without HealthReport and eleven warning shows up correctly on API and UI.
     * @throws IOException Button not found in HTML.
     */
    @Test
    public void verifyFreestyleJobWithElevenWarnings() throws IOException {
        verifyFreeStyleProject("java11Warnings.txt", HEALTHY, UNHEALTHY, 11, 0, false);
    }

    private void verifyFreeStyleProject(final String file, final int healthy,
            final int unhealthy,
            final int warnings, final int expectedScore, final boolean shouldUseHealthReport) throws IOException {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, file, file);

        Java java = new Java();
        java.setPattern(file);
        enableWarnings(project, java);

        if (shouldUseHealthReport) {
            new FreestyleConfigPage(project)
                    .setHealthyThreshold(healthy)
                    .setUnhealthyThreshold(unhealthy)
                    .submit();
        }

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getTotalSize()).isEqualTo(warnings);
        assertThat(result.getInfoMessages().contains(HEALTH_REPORT_ENABLED_MESSAGE));

        InfoPage infoPage = new InfoPage(project, 1);
        assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        assertThat(result.getErrorMessages()).isEqualTo(infoPage.getErrorMessages());

        if (shouldUseHealthReport) {
            HealthReport healthReport = project.getBuildHealth();
            assertThat(healthReport.getScore()).isEqualTo(expectedScore);
        }
        else {
            assertThat(result.getInfoMessages()).contains(HEALTH_REPORT_DISABLED_MESSAGE);
        }
    }

    /**
     * Encapsulation Object for buildNumber/java/info Page.
     */
    private class InfoPage {
        private final HtmlPage infoPage;

        InfoPage(final Project project, final int buildNumber) {
            infoPage = getWebPage(project, buildNumber + "/java/info");
        }

        List<String> getErrorMessages() {
            return getMessagesById("errors");
        }

        List<String> getInfoMessages() {
            return getMessagesById("info");
        }

        private List<String> getMessagesById(final String id) {
            DomElement info = getInfoPage().getElementById(id);
            return info == null ? new ArrayList<>()
                    : StreamSupport.stream(info.getChildElements().spliterator(), false)
                    .map(DomElement::asText)
                    .collect(Collectors.toList());
        }

        private HtmlPage getInfoPage() {
            return infoPage;
        }
    }

    /**
     * Encapsulation Object for FreeStyle config Page.
     */
    private class FreestyleConfigPage {
        private static final String ID_HEALTHY_THRESHOLD = "_.healthy";
        private static final String ID_UNHEALTHY_THRESHOLD = "_.unhealthy";
        private final HtmlForm form;

        FreestyleConfigPage(final Project project) {
            form = getWebPage(project, "configure").getFormByName("config");
        }

        int getHealthyThreshold() {
            return getNumber(ID_HEALTHY_THRESHOLD);
        }

        FreestyleConfigPage setHealthyThreshold(final int healthy) {
            setInput(ID_HEALTHY_THRESHOLD, healthy);
            return this;
        }

        int getUnhealthyThreshold() {
            return getNumber(ID_UNHEALTHY_THRESHOLD);
        }

        FreestyleConfigPage setUnhealthyThreshold(final int unhealthy) {
            setInput(ID_UNHEALTHY_THRESHOLD, unhealthy);
            return this;
        }

        FreestyleConfigPage submit() throws IOException {
            HtmlFormUtil.submit(getForm());
            return this;
        }

        private void setInput(final String id, final int value) {
            HtmlNumberInput input = getForm().getInputByName(id);
            input.setText(String.valueOf(value));
        }

        private int getNumber(final String id) {
            HtmlNumberInput input = getForm().getInputByName(id);
            return Integer.parseInt(input.getText());
        }

        private HtmlForm getForm() {
            return form;
        }
    }
}