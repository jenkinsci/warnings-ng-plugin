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
import hudson.model.Project;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;

import static org.assertj.core.api.Assertions.*;

/**
 *  Tests the combination of the health report configuration and the info page via Web-Gui.
 *
 * @author Veronika Zwickenpflug
 */
public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    private static final String EMPTY = "javac_empty.txt";
    private static final String SINGLE_WARNING = "javac_single_warning.txt";
    private static final String NINE_WARNINGS = "javac_9_warnings.txt";
    private static final String TEN_WARNINGS = "javac_10_warnings.txt";

    @Test
    public void noWarnings() throws IOException {
        checkInfoPage(EMPTY, 0, 0, false);
    }

    @Test
    public void noWarningsWithHealthReport() throws IOException {
        checkInfoPage(EMPTY, 0, 100, true);
    }


    @Test
    public void oneWarningWithHealthReport() throws IOException {
        checkInfoPage(SINGLE_WARNING, 1, 90, true);
    }

    @Test
    public void oneWarning() throws IOException {
        checkInfoPage(SINGLE_WARNING, 1, 0, false);
    }

    @Test
    public void nineWarningsWithHealthReport() throws IOException {
        checkInfoPage(NINE_WARNINGS, 9, 10, true);
    }

    @Test
    public void nineWarnings() throws IOException {
        checkInfoPage(NINE_WARNINGS, 9, 0, false);
    }

    @Test
    public void tenWarningsWithHealthReport() throws IOException {
        checkInfoPage(TEN_WARNINGS, 10, 0, true);
    }

    @Test
    public void tenWarnings() throws IOException {
        checkInfoPage(TEN_WARNINGS, 10, 0, false);
    }

    private void checkInfoPage(final String javacFile, final int warnings, final int healthReportScore,
            final boolean healthReport) throws IOException {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, javacFile, "javac.txt");

        Java java = new Java();
        java.setPattern("javac.txt");
        enableWarnings(project, java);

        if (healthReport) {
            ConfigForm config = new ConfigForm(project);
            config.setHealthyThreshold(1);
            config.setUnhealthyThreshold(9);
            config.submit();
        }

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        InfoPage infoPage = new InfoPage(project, 1);

        assertThat(result.getTotalSize()).isEqualTo(warnings);
        assertThat(infoPage.getInformationMessages()).isEqualTo(result.getInfoMessages());
        assertThat(infoPage.getErrorMessages()).isEqualTo(result.getErrorMessages());

        if (healthReport) {
            assertThat(infoPage.getInformationMessages())
                    .contains("Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)");
            assertThat(project.getBuildHealth().getScore()).isEqualTo(healthReportScore);
        }
        else {
            assertThat(infoPage.getInformationMessages()).contains(
                    "Health report is disabled - skipping");
        }
    }

    private class ConfigForm {
        private static final String RELATIVE_URL = "configure";
        private static final String FORM_ID = "config";
        private static final String HEALTHY_ID = "_.healthy";
        private static final String UNHEALTHY_ID = "_.unhealthy";

        private final HtmlForm form;

        ConfigForm(final Project project) {
            form = getWebPage(project, RELATIVE_URL).getFormByName(FORM_ID);
        }

        void setHealthyThreshold(final int healthy) {
            HtmlNumberInput input = form.getInputByName(HEALTHY_ID);
            input.setText(String.valueOf(healthy));
        }

        void setUnhealthyThreshold(final int unhealthy) {
            HtmlNumberInput input = form.getInputByName(UNHEALTHY_ID);
            input.setText(String.valueOf(unhealthy));
        }

        void submit() throws IOException {
            HtmlFormUtil.submit(form);
        }
    }

    private class InfoPage {
        private final HtmlPage infoPage;

        InfoPage(final Project project, final int buildNumber) {
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

            return info == null ? new ArrayList<>() :
                    StreamSupport.stream(info.getChildElements().spliterator(), false)
                            .map(DomElement::asText)
                            .collect(Collectors.toList());
        }

        private HtmlPage getInfoPage() {
            return infoPage;
        }
    }
}