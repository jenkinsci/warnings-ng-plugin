package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for the issue recorder using Page Object pattern.
 *
 * @author Artem Polovyi
 */
public class RecorderITestWithHtmlUnit extends IntegrationTestWithJenkinsPerSuite {
    private static final int HEALTHY_THRESHOLD = 1;
    private static final int UNHEALTHY_THRESHOLD = 9;

    /**
     * Test the job with health report of 10 warnings.
     */
    @Test
    public void shouldCreateHealthReportOfWarnings10() {
        final int warningsNumber = 10;
        FreeStyleProject project = configureFreeStyleProjectForFileWithWarnings(warningsNumber);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        InfoPage infoPage = new InfoPage(project);

        assertThat(project.getBuildHealth().getScore()).isEqualTo(0);
        assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        assertThat(result).hasTotalSize(warningsNumber);
    }

    /**
     * Test the job with health report of 9 warnings.
     */
    @Test
    public void shouldCreateHealthReportOfWarnings9() {
        final int warningsNumber = 9;
        FreeStyleProject project = configureFreeStyleProjectForFileWithWarnings(warningsNumber);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        InfoPage infoPage = new InfoPage(project);

        assertThat(project.getBuildHealth().getScore()).isEqualTo(10);
        assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        assertThat(result).hasTotalSize(warningsNumber);
    }

    /**
     * Test the job with health report of 1 warning.
     */
    @Test
    public void shouldCreateHealthReportOfWarnings1() {
        final int warningsNumber = 1;
        FreeStyleProject project = configureFreeStyleProjectForFileWithWarnings(warningsNumber);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        InfoPage infoPage = new InfoPage(project);

        assertThat(project.getBuildHealth().getScore()).isEqualTo(90);
        assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        assertThat(result).hasTotalSize(warningsNumber);
    }

    /**
     * Test the job with health report of 0 warnings.
     */
    @Test
    public void shouldCreateHealthReportOfWarnings0() {
        final int warningsNumber = 0;
        FreeStyleProject project = configureFreeStyleProjectForFileWithWarnings(warningsNumber);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        InfoPage infoPage = new InfoPage(project);

        assertThat(project.getBuildHealth().getScore()).isEqualTo(100);
        assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        assertThat(result).hasTotalSize(warningsNumber);
    }

    /**
     * Test the job without health report.
     */
    @Test
    public void shouldCreateNoHealthReport() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-0.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        enableWarnings(project, java);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        InfoPage infoPage = new InfoPage(project);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(100);
        assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        assertThat(result).hasTotalSize(0);
    }

    /**
     * Creates a FreeStyle job with certain number of warnings in file.
     *
     * @param warningsNumber
     *         number of warnings in the file
     *
     * @return FreeStyle job to execute
     */
    private FreeStyleProject configureFreeStyleProjectForFileWithWarnings(final int warningsNumber) {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-" + warningsNumber + ".txt", "java.txt");

        Java java = new Java();
        java.setPattern("java.txt");
        enableWarnings(project, java);

        ConfigPage configPage = new ConfigPage(project);
        configPage.configureThreshold(HEALTHY_THRESHOLD, UNHEALTHY_THRESHOLD);
        return project;
    }

    /**
     * Information Page Object
     */
    private class InfoPage {
        private final HtmlPage infoPage;
        private final List<String> infoMessages;
        private final List<String> errorMessages;

        InfoPage(final FreeStyleProject project) {
            this.infoPage = getWebPage(project, project.getLastBuild().getNumber() + "/java/info");
            this.infoMessages = getMessagesById("info");
            this.errorMessages = getMessagesById("error");
        }

        public List<String> getErrorMessages() {
            return this.errorMessages;
        }

        public List<String> getInfoMessages() {
            return this.infoMessages;
        }

        private List<String> getMessagesById(final String id) {
            DomElement element = this.infoPage.getElementById(id);

            return element == null ? new ArrayList<>()
                    : StreamSupport.stream(element.getChildElements().spliterator(), false)
                    .map(DomElement::asText)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Configuration Page Object
     */
    private class ConfigPage {
        private final HtmlForm configForm;

        ConfigPage(final FreeStyleProject project) {
            configForm = getWebPage(project, "configure").getFormByName("config");
        }

        void configureThreshold(final int healthyThreshold, final int unhealthyThreshold) {
            setHealthyThreshold(healthyThreshold);
            setUnhealthyThreshold(unhealthyThreshold);
            submit(configForm);
        }

        private void setHealthyThreshold(final int healthyThreshold) {
            HtmlNumberInput input = configForm.getInputByName("_.healthy");
            input.setText(String.valueOf(healthyThreshold));
        }

        private void setUnhealthyThreshold(final int unhealthyThreshold) {
            HtmlNumberInput input = configForm.getInputByName("_.unhealthy");
            input.setText(String.valueOf(unhealthyThreshold));
        }
    }

}
