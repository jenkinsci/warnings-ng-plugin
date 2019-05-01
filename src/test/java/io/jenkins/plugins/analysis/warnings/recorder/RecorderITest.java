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
import hudson.model.Result;
import hudson.model.Project;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verify the info page of the Java parser.
 *
 * @author Tanja Roithmeier
 */
public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    private static final int HEALTHY = 1;
    private static final int UNHEALTHY = 9;
    private static final String ENABLED_HEALTH_INFO = "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)";

    /**
     * Verifies the info page with one warning as input.
     */
    @Test
    public void shouldCreateReportWithOneWarning() {

        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac_1Warning.txt", "javac.txt");

        enableJavaWarnings(job, "javac.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        InfoPage info = new InfoPage(job);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(info.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(info.getErrorMessages()).isEqualTo(result.getErrorMessages());
    }

    /**
     * Verifies the info page with one warning and enabled health report as input.
     */
    @Test
    public void shouldCreateReportWithOneWarningAndHealthReport90() {

        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac_1Warning.txt", "javac.txt");

        enableJavaWarnings(job, "javac.txt");
        enableHealthReport(job, HEALTHY, UNHEALTHY);

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        InfoPage info = new InfoPage(job);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(info.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(info.getErrorMessages()).isEqualTo(result.getErrorMessages());

        assertThat(job.getBuildHealth().getScore()).isEqualTo(90);
        assertThat(info.getInfoMessages().contains(ENABLED_HEALTH_INFO));
    }

    /**
     * Verifies the info page with warning as input.
     */
    @Test
    public void shouldCreateReportWithouWarnings() {

        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac_0Warning.txt", "javac.txt");

        enableJavaWarnings(job, "javac.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        InfoPage info = new InfoPage(job);

        assertThat(result.getTotalSize()).isEqualTo(0);
        assertThat(info.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(info.getErrorMessages()).isEqualTo(result.getErrorMessages());
    }

    /**
     * Verifies the info page without warnings and enabled health report as input.
     */
    @Test
    public void shouldCreateReportWithoutWarningsAndHealthReport100() {

        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac_0Warning.txt", "javac.txt");

        enableJavaWarnings(job, "javac.txt");
        enableHealthReport(job, HEALTHY, UNHEALTHY);

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        InfoPage info = new InfoPage(job);

        assertThat(result.getTotalSize()).isEqualTo(0);
        assertThat(info.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(info.getErrorMessages()).isEqualTo(result.getErrorMessages());

        assertThat(job.getBuildHealth().getScore()).isEqualTo(100);
        assertThat(info.getInfoMessages().contains(ENABLED_HEALTH_INFO));
    }

    /**
     * Verifies the info page with 9 warnings as input.
     */
    @Test
    public void shouldCreateReportWith9Warnings() {

        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac_9Warnings.txt", "javac.txt");

        enableJavaWarnings(job, "javac.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        InfoPage info = new InfoPage(job);

        assertThat(result.getTotalSize()).isEqualTo(9);
        assertThat(info.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(info.getErrorMessages()).isEqualTo(result.getErrorMessages());
    }

    /**
     * Verifies the info page with nine warnings and enabled health report as input.
     */
    @Test
    public void shouldCreateReportWith9WarningAndHealthReport10() {

        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac_9Warnings.txt", "javac.txt");

        enableJavaWarnings(job, "javac.txt");
        enableHealthReport(job, HEALTHY, UNHEALTHY);

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        InfoPage info = new InfoPage(job);

        assertThat(result.getTotalSize()).isEqualTo(9);
        assertThat(info.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(info.getErrorMessages()).isEqualTo(result.getErrorMessages());

        assertThat(job.getBuildHealth().getScore()).isEqualTo(10);
        assertThat(info.getInfoMessages().contains(ENABLED_HEALTH_INFO));
    }

    /**
     * Verifies the info page with 10 warnings as input.
     */
    @Test
    public void shouldCreateReportWith10Warnings() {

        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac_10Warnings.txt", "javac.txt");

        enableJavaWarnings(job, "javac.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        InfoPage info = new InfoPage(job);

        assertThat(result.getTotalSize()).isEqualTo(10);
        assertThat(info.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(info.getErrorMessages()).isEqualTo(result.getErrorMessages());
    }

    /**
     * Verifies the info page with 10 warnings and enabled health report as input.
     */
    @Test
    public void shouldCreateReportWith10WarningAndHealthReport0() {

        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac_10Warnings.txt", "javac.txt");

        enableJavaWarnings(job, "javac.txt");
        enableHealthReport(job, HEALTHY, UNHEALTHY);

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        InfoPage info = new InfoPage(job);

        assertThat(result.getTotalSize()).isEqualTo(10);
        assertThat(info.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(info.getErrorMessages()).isEqualTo(result.getErrorMessages());

        assertThat(job.getBuildHealth().getScore()).isEqualTo(0);
        assertThat(info.getInfoMessages().contains(ENABLED_HEALTH_INFO));
    }

    private void enableJavaWarnings(Project job, String pattern) {
        Java java = new Java();
        java.setPattern(pattern);
        enableWarnings(job, java);
    }

    private void enableHealthReport(Project job, int healthy, int unhealty) {
        ConfigurationPage configPage = new ConfigurationPage(job);
        configPage.setHealthy(healthy);
        configPage.setUnHealthy(unhealty);
        submit(configPage.GetForm());
    }

    /**
     * Provides access to the HTML configuration page.
     */
    private class ConfigurationPage {
        private static final String HEALTHY = "_.healthy";
        private static final String UNHEALTHY = "_.unhealthy";

        private HtmlForm configForm;

        ConfigurationPage(final Project job) {
            configForm = getWebPage(job, "configure").getFormByName("config");
        }

        int getHealthy() {
            HtmlNumberInput checkBox = configForm.getInputByName(HEALTHY);
            return Integer.parseInt(checkBox.getText());

        }

        void setHealthy(int value) {
            HtmlNumberInput checkBox = configForm.getInputByName(HEALTHY);
            checkBox.setText(String.valueOf(value));
        }

        int getUnHealthy() {
            HtmlNumberInput checkBox = configForm.getInputByName(UNHEALTHY);
            return Integer.parseInt(checkBox.getText());

        }

        void setUnHealthy(int value) {
            HtmlNumberInput checkBox = configForm.getInputByName(UNHEALTHY);
            checkBox.setText(String.valueOf(value));
        }

        HtmlForm getForm() {
            return configForm;
        }
    }

    /**
     * Provides access to the HTML info page.
     */
    private class InfoPage {
        private HtmlPage page;

        InfoPage(final Project job) {
            int buildNumber = job.getLastBuild().getNumber();
            page = getWebPage(job, buildNumber + "/java/info");
        }

        ImmutableList<String> getInfoMessages() {

            return Lists.immutable.withAll(getMessagesbyId("info"));
        }

        ImmutableList<String> getErrorMessages() {

            return Lists.immutable.withAll(getMessagesbyId("errors"));
        }

        private List<String> getMessagesbyId(String id) {
            List<String> messages = new ArrayList<>();
            DomElement messageElement = page.getElementById(id);
            if (messageElement != null && messageElement.hasChildNodes()) {

                for (DomElement child : messageElement.getChildElements()) {
                    messages.add(child.getTextContent());
                }

            }
            return messages;
        }
    }
}



