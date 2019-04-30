package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

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
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link IssuesRecorder}.
 *
 * @author Raphael Furch
 */

@RunWith(Parameterized.class)
public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    private static final int HEALTHY_THRESHOLD = 1;
    private static final int UNHEALTHY_THRESHOLD = 9;
    private static final String INFORMATION_MESSAGE = "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)";
    /**
     * Parameters under test.
     * @return parameter list.
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"javac_0_warnings.txt",   0, 100, true},
                {"javac_1_warnings.txt",   1,  90, true},
                {"javac_9_warnings.txt",   9,  10, true},
                {"javac_10_warnings.txt", 10,   0, true},
                {"javac_0_warnings.txt",   0,  -1, false},
                {"javac_1_warnings.txt",   1,  -1, false},
                {"javac_9_warnings.txt",   9,  -1, false},
                {"javac_10_warnings.txt", 10,  -1, false},
        });
    }

    /**
     * File with warnings.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    @Parameter
    public /* NOT private */ String javacSrcFile;

    /**
     * Expected amount of warnings.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    @Parameter(1)
    public /* NOT private */ int resultTotalSize;

    /**
     * Expected HealthReport score.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    @Parameter(2)
    public /* NOT private */ int healthReportScore;

    /**
     * Flag if the test is performed with or without health report.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    @Parameter(3)
    public /* NOT private */ boolean withHealthReport;

    private String getJavacSrcFile() {
        return javacSrcFile;
    }

    private int getResultTotalSize() {
        return resultTotalSize;
    }

    private int getHealthReportScore() {
        return healthReportScore;
    }

    private boolean isWithHealthReport() {
        return withHealthReport;
    }

    /**
     * Test with GUI.
     * javacSrcFile" should have "resultTotalSize" warnings and a score of "healthReportScore".
     * @throws IOException Submit-Button not found in HTML.
     */
    @Test
    public void verifyInfoPageWithGui() throws IOException {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, getJavacSrcFile(), "javac.txt");

        Java java = new Java();
        java.setPattern("javac.txt");
        enableWarnings(project, java);

        if (isWithHealthReport()) {
            new WarningsRecorderConfigurationPageConfigurator(project)
                    .setHealthyThreshold(HEALTHY_THRESHOLD)
                    .setUnhealthyThreshold(UNHEALTHY_THRESHOLD)
                    .submit();
        }

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        JavaInfoPage infoPageObject = new JavaInfoPage(project, 1);

        assertThat(result.getTotalSize()).isEqualTo(getResultTotalSize());
        assertThat(infoPageObject.getInformationMessages()).isEqualTo(result.getInfoMessages());
        assertThat(infoPageObject.getErrorMessages()).isEqualTo(result.getErrorMessages());

        if (isWithHealthReport()) {
            assertThat(infoPageObject.getInformationMessages()).contains(INFORMATION_MESSAGE);
            HealthReport healthReport = project.getBuildHealth();
            assertThat(healthReport.getScore()).isEqualTo(getHealthReportScore());
        }
    }

    /**
     * Class for java/info page.
     */
    private class JavaInfoPage {
        private static final String CONST_RELATIVE_URL_PART = "/java/info";
        private static final String ERRORS_ID = "errors";
        private static final String INFOS_ID = "info";
        private final HtmlPage infoPage;

        JavaInfoPage(final Project project, final int buildNumber) {
            infoPage = getWebPage(project, buildNumber + CONST_RELATIVE_URL_PART);
        }

        List<String> getErrorMessages() {
            return getMessagesById(ERRORS_ID);
        }

        List<String> getInformationMessages() {
            return getMessagesById(INFOS_ID);
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
     * Configurator for Warnings Recorder Configuration Page.
     */
    private class WarningsRecorderConfigurationPageConfigurator {
        private static final String RELATIVE_URL = "configure";
        private static final String FORM_NAME = "config";
        private static final String HEALTHY_THRESHOLD_NAME = "_.healthy";
        private static final String UNHEALTHY_THRESHOLD_NAME = "_.unhealthy";

        private final HtmlForm form;

        WarningsRecorderConfigurationPageConfigurator(final Project project) {
            form = getWebPage(project, RELATIVE_URL).getFormByName(FORM_NAME);
        }
        @SuppressWarnings("SameParameterValue")
        WarningsRecorderConfigurationPageConfigurator setHealthyThreshold(final int healthy) {
            setInput(HEALTHY_THRESHOLD_NAME, healthy);
            return this;
        }

        @SuppressWarnings("SameParameterValue")
        WarningsRecorderConfigurationPageConfigurator setUnhealthyThreshold(final int unhealthy) {
            setInput(UNHEALTHY_THRESHOLD_NAME, unhealthy);
            return this;
        }

        void submit() throws IOException {
            HtmlFormUtil.submit(getForm());
        }

        private void setInput(final String name, final int value) {
            HtmlNumberInput input = getForm().getInputByName(name);
            input.setText(String.valueOf(value));
        }

        private HtmlForm getForm() {
            return form;
        }
    }

}