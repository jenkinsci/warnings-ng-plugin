package io.jenkins.plugins.analysis.warnings;

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

import static org.assertj.core.api.Assertions.*;

/**
 * Verify the info page of the Java parser and the health report configured by the web UI.
 *
 * @author Michael Schmid
 */
public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    private static final int HEALTHY_THRESHOLD = 1;
    private static final int UNHEALTHY_THRESHOLD = 9;

    /**
     * Verify the info page of the Java parser with no warnings as input.
     * Verify the health report (healthy=1, unhealthy=9) configured by the web UI has a score of 100
     * @throws IOException when something went wrong with the web ui access
     */
    @Test
    public void noWarningsWithHealthReport100() throws IOException {
        verifyInfoPage("javac_no_warning.txt", 0, 100, true);
    }

    /**
     * Verify the info page of the Java parser with no warnings as input and no configured health report.
     * @throws IOException when something went wrong with the web ui access
     */
    @Test
    public void noWarningsWithoutHealthReport() throws IOException {
        verifyInfoPage("javac_no_warning.txt", 0, 0, false);
    }

    /**
     * Verify the info page of the Java parser with one warning as input.
     * Verify the health report (healthy=1, unhealthy=9) configured by the web UI has a score of 90
     * @throws IOException when something went wrong with the web ui access
     */
    @Test
    public void oneWarningWithHealthReport90() throws IOException {
        verifyInfoPage("javac_one_warning.txt", 1, 90, true);
    }

    /**
     * Verify the info page of the Java parser with one warning as input and no configured health report.
     * @throws IOException when something went wrong with the web ui access
     */
    @Test
    public void oneWarningWithoutHealthReport() throws IOException {
        verifyInfoPage("javac_one_warning.txt", 1, 0, false);
    }

    /**
     * Verify the info page of the Java parser with nine warnings as input.
     * Verify the health report (healthy=1, unhealthy=9) configured by the web UI has a score of 10
     * @throws IOException when something went wrong with the web ui access
     */
    @Test
    public void nineWarningsWithHealthReport10() throws IOException {
        verifyInfoPage("javac_9_warnings.txt", 9, 10, true);
    }

    /**
     * Verify the info page of the Java parser with nine warnings as input and no configured health report.
     * @throws IOException when something went wrong with the web ui access
     */
    @Test
    public void nineWarningsWithoutHealthReport() throws IOException {
        verifyInfoPage("javac_9_warnings.txt", 9, 0, false);
    }

    /**
     * Verify the info page of the Java parser with ten warnings as input.
     * Verify the health report (healthy=1, unhealthy=9) configured by the web UI has a score of 0
     * @throws IOException when something went wrong with the web ui access
     */
    @Test
    public void tenWarningsWithHealthReport0() throws IOException {
        verifyInfoPage("javac_10_warnings.txt", 10, 0, true);
    }

    /**
     * Verify the info page of the Java parser with ten warnings as input and no configured health report.
     * @throws IOException when something went wrong with the web ui access
     */
    @Test
    public void tenWarningsWithoutHealthReport() throws IOException {
        verifyInfoPage("javac_10_warnings.txt", 10, 0, false);
    }

    private void verifyInfoPage(final String javacFile, final int warnings,
            final int healthReportScore, final boolean withHealthReport)
            throws IOException {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, javacFile, "javac.txt");

        Java java = new Java();
        java.setPattern("javac.txt");
        enableWarnings(project, java);

        if (withHealthReport) {
            WarningsRecorderConfigurationPage config = new WarningsRecorderConfigurationPage(project);
            config.setHealthyThreshold(HEALTHY_THRESHOLD);
            config.setUnhealthyThreshold(UNHEALTHY_THRESHOLD);
            config.submit();
        }

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        JavaInfoPage infoPage = new JavaInfoPage(project, 1);

        assertThat(result.getTotalSize()).isEqualTo(warnings);
        assertThat(infoPage.getInformationMessages()).isEqualTo(result.getInfoMessages());
        assertThat(infoPage.getErrorMessages()).isEqualTo(result.getErrorMessages());

        if (withHealthReport) {
            assertThat(infoPage.getInformationMessages()).contains(
                    "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)");
            HealthReport healthReport = project.getBuildHealth();
            assertThat(healthReport.getScore()).isEqualTo(healthReportScore);
        }
        else {
            assertThat(infoPage.getInformationMessages()).contains(
                    "Health report is disabled - skipping");
        }
    }

    /**
     * Encapsulated access to the Java parser info page by UI - Page Object Pattern.
     */
    private class JavaInfoPage {
        private final HtmlPage infoPage;

        JavaInfoPage(final Project project, final int buildNumber) {
            infoPage = getWebPage(project, buildNumber + "/java/info");
        }

        List<String> getErrorMessages() {
            return getMessagesById("errors");
        }

        List<String> getInformationMessages() {
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
     * Encapsulated access to the warnings recorder configuration page by UI - Page Object Pattern.
     */
    private class WarningsRecorderConfigurationPage {
        private static final String ID_HEALTHY_THRESHOLD = "_.healthy";
        private static final String ID_UNHEALTHY_THRESHOLD = "_.unhealthy";
        private final HtmlForm form;

        WarningsRecorderConfigurationPage(final Project project) {
            form = getWebPage(project, "configure").getFormByName("config");
        }

        int getHealthyThreshold() {
            return getNumber(ID_HEALTHY_THRESHOLD);
        }

        void setHealthyThreshold(final int healthy) {
            setInput(ID_HEALTHY_THRESHOLD, healthy);
        }

        int getUnhealthyThreshold() {
            return getNumber(ID_UNHEALTHY_THRESHOLD);
        }

        void setUnhealthyThreshold(final int unhealthy) {
            setInput(ID_UNHEALTHY_THRESHOLD, unhealthy);
        }

        void submit() throws IOException {
            HtmlFormUtil.submit(getForm());
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
