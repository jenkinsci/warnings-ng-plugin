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

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Verify the Java parser's info page and the health report configuration by the web UI.
 *
 * @author Tobias Redl
 */
public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    private static final int HEALTHY_THRESHOLD = 1;
    private static final int UNHEALTHY_THRESHOLD = 9;
    private static final String ENABLED_MESSAGE = "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)";
    private static final String DISABLED_MESSAGE = "Health report is disabled - skipping";

    private void shouldCreateFreeStyleJobAndVerifyJavaInfoPage(final String warningsFile, final int warningsCount,
            final int healthScore, final boolean hasHealthReport) throws IOException {
        final String localFile = "javacWarnings.txt";
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, warningsFile, localFile);

        Java java = new Java();
        java.setPattern(localFile);
        enableWarnings(project, java);

        if (hasHealthReport) {
            new WarningsRecorderConfigurationPageObject(project)
                    .setHealthyThreshold(HEALTHY_THRESHOLD)
                    .setUnhealthyThreshold(UNHEALTHY_THRESHOLD)
                    .submit();
        }

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        JavaInfoPageObject infoPage = new JavaInfoPageObject(project, 1);

        assertThat(result.getTotalSize()).isEqualTo(warningsCount);
        assertThat(infoPage.getInformationMessages()).isEqualTo(result.getInfoMessages());
        assertThat(infoPage.getErrorMessages()).isEqualTo(result.getErrorMessages());

        if (hasHealthReport) {
            assertThat(infoPage.getInformationMessages()).contains(ENABLED_MESSAGE);
            HealthReport healthReport = project.getBuildHealth();
            assertThat(healthReport.getScore()).isEqualTo(healthScore);
        }
        else {
            assertThat(infoPage.getInformationMessages()).contains(DISABLED_MESSAGE);
        }
    }

    /**
     * Verify the Java parser's info page with 0 warnings. Verify the health report (healthy=1, unhealthy=9) configured
     * by the web UI has a score of 100.
     *
     * @throws IOException
     *         occurs when there are problems with the web ui access
     */
    @Test
    public void noWarningsWithHealthReport100() throws IOException {
        shouldCreateFreeStyleJobAndVerifyJavaInfoPage("javac_0_warnings.txt", 0, 100, true);
    }

    /**
     * Verify the Java parser's info page with 1 warnings. Verify the health report (healthy=1, unhealthy=9) configured
     * by the web UI has a score of 90.
     *
     * @throws IOException
     *         occurs when there are problems with the web ui access
     */
    @Test
    public void oneWarningWithHealthReport90() throws IOException {
        shouldCreateFreeStyleJobAndVerifyJavaInfoPage("javac_1_warnings.txt", 1, 90, true);
    }

    /**
     * Verify the Java parser's info page with 9 warnings. Verify the health report (healthy=1, unhealthy=9) configured
     * by the web UI has a score of 10.
     *
     * @throws IOException
     *         occurs when there are problems with the web ui access
     */
    @Test
    public void nineWarningsWithHealthReport10() throws IOException {
        shouldCreateFreeStyleJobAndVerifyJavaInfoPage("javac_9_warnings.txt", 9, 10, true);
    }

    /**
     * Verify the Java parser's info page with 10 warnings. Verify the health report (healthy=1, unhealthy=9) configured
     * by the web UI has a score of 0.
     *
     * @throws IOException
     *         occurs when there are problems with the web ui access
     */
    @Test
    public void tenWarningsWithHealthReport0() throws IOException {
        shouldCreateFreeStyleJobAndVerifyJavaInfoPage("javac_10_warnings.txt", 10, 0, true);
    }

    /**
     * Verify the Java parser's info page with 0 warnings. Health report is not configured in this scenario.
     *
     * @throws IOException
     *         occurs when there are problems with the web ui access
     */
    @Test
    public void noWarningsWithoutHealthReport() throws IOException {
        shouldCreateFreeStyleJobAndVerifyJavaInfoPage("javac_0_warnings.txt", 0, 0, false);
    }

    /**
     * Verify the Java parser's info page with 1 warnings. Health report is not configured in this scenario.
     *
     * @throws IOException
     *         occurs when there are problems with the web ui access
     */
    @Test
    public void oneWarningWithoutHealthReport() throws IOException {
        shouldCreateFreeStyleJobAndVerifyJavaInfoPage("javac_1_warnings.txt", 1, 0, false);
    }

    /**
     * Verify the Java parser's info page with 9 warnings. Health report is not configured in this scenario.
     *
     * @throws IOException
     *         occurs when there are problems with the web ui access
     */
    @Test
    public void nineWarningsWithoutHealthReport() throws IOException {
        shouldCreateFreeStyleJobAndVerifyJavaInfoPage("javac_9_warnings.txt", 9, 0, false);
    }

    /**
     * Verify the Java parser's info page with 10 warnings. Health report is not configured in this scenario.
     *
     * @throws IOException
     *         occurs when there are problems with the web ui access
     */
    @Test
    public void tenWarningsWithoutHealthReport() throws IOException {
        shouldCreateFreeStyleJobAndVerifyJavaInfoPage("javac_10_warnings.txt", 10, 0, false);
    }

    /**
     * Encapsulated access to the Java parser info page by UI - Page Object Pattern.
     */
    private class JavaInfoPageObject {
        private final HtmlPage infoPage;

        JavaInfoPageObject(final Project project, final int buildNumber) {
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
    private class WarningsRecorderConfigurationPageObject {
        private static final String ID_HEALTHY_THRESHOLD = "_.healthy";
        private static final String ID_UNHEALTHY_THRESHOLD = "_.unhealthy";
        private final HtmlForm form;

        WarningsRecorderConfigurationPageObject(final Project project) {
            form = getWebPage(project, "configure").getFormByName("config");
        }

        int getHealthyThreshold() {
            return getNumber(ID_HEALTHY_THRESHOLD);
        }

        WarningsRecorderConfigurationPageObject setHealthyThreshold(final int healthy) {
            setInput(ID_HEALTHY_THRESHOLD, healthy);
            return this;
        }

        int getUnhealthyThreshold() {
            return getNumber(ID_UNHEALTHY_THRESHOLD);
        }

        WarningsRecorderConfigurationPageObject setUnhealthyThreshold(final int unhealthy) {
            setInput(ID_UNHEALTHY_THRESHOLD, unhealthy);
            return this;
        }

        WarningsRecorderConfigurationPageObject submit() throws IOException {
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