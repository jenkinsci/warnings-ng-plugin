package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class HtmlUnitRecorderITest extends IntegrationTestWithJenkinsPerSuite {


    @Test
    public void shouldCreateHealthreportWith0Warnings() {
        FreeStyleProject project = configureFreeStyleProject(0);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        InfoPage infoPage = new InfoPage(project);

        assertThat(project.getBuildHealth().getScore()).isEqualTo(0);
        assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        assertThat(result).hasTotalSize(0);
    }


    @Test
    public void shouldCreateHealthreportWith1Warnings() {
        FreeStyleProject project = configureFreeStyleProject(1);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        InfoPage infoPage = new InfoPage(project);

        assertThat(project.getBuildHealth().getScore()).isEqualTo(0);
        assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        assertThat(result).hasTotalSize(1);
    }


    @Test
    public void shouldCreateHealthreportWith9Warnings() {
        FreeStyleProject project = configureFreeStyleProject(9);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        InfoPage infoPage = new InfoPage(project);

        assertThat(project.getBuildHealth().getScore()).isEqualTo(0);
        assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        assertThat(result).hasTotalSize(9);
    }


    @Test
    public void shouldCreateHealthreportWith10Warnings() {
        FreeStyleProject project = configureFreeStyleProject(10);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        InfoPage infoPage = new InfoPage(project);

        assertThat(project.getBuildHealth().getScore()).isEqualTo(0);
        assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        assertThat(result).hasTotalSize(10);
    }


//--------------------------- Helper Functions ------------------------------

    private FreeStyleProject configureFreeStyleProject(final int warningsNumber) {
        FreeStyleProject project = createFreeStyleProject();
        if(warningsNumber == 0 || warningsNumber == 1 || warningsNumber == 9 || warningsNumber == 10) {
            copySingleFileToWorkspace(project, "warning-test/warnings-" + warningsNumber + ".txt", "java.txt");
        } else {
            System.out.println("Only 0, 1, 9 or 10 Warnings possible ");
            throw new IllegalArgumentException();
        }

        Java java = new Java();
        java.setPattern("java.txt");
        enableWarnings(project, java);

        ConfigPage configPage = new ConfigPage(project);
        configPage.configureThreshold(1, 9);
        return project;
    }

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
}
