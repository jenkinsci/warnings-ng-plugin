package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.filter.IncludeMessage;
import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {
    public static final String JAVA_WARNING = "[WARNING] C:/Build/Results/AvaloqDialog.java:[12,39] [deprecation] SubjectControlContentAssistant has been deprecated";

    public static final int HEALTHY_THRESHOLD = 1;
    public static final int UNHEALTHY_THRESHOLD = 9;

    /**
     * First Example Test (Foliensatz 2)
     */
    @Test
    public void shouldCreateFreestyleJobWithJavaWarnings() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../javac.txt", "java.txt");
        List<RegexpFilter> filterList = new ArrayList<>();
        filterList.add(new IncludeMessage("(.*)ContentAssistHandler(.*)"));

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);
        recorder.setFilters(filterList);
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        AnalysisResult result = getAnalysisResult(build);
        HealthReport healthReport = getResultAction(build).getBuildHealth();

        assertThat(result).hasTotalSize(1);
        assertThat(healthReport.getScore()).isEqualTo(90);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.PASSED);
    }

    @Test
    public void javaWarningsHealthReport100() throws IOException, InterruptedException {
        verifyHealthReport(true, 0, 100);
    }

    @Test
    public void javaWarningsHealthReport90() throws IOException, InterruptedException {
        verifyHealthReport(true, 1, 90);
    }

    @Test
    public void javaWarningsHealthReport10() throws IOException, InterruptedException {
        verifyHealthReport(true, 9, 10);
    }

    @Test
    public void javaWarningsHealthReport0() throws IOException, InterruptedException {
        verifyHealthReport(true, 10, 0);
    }

    @Test
    public void javaWarningsNoHealthReport() throws IOException, InterruptedException {
        verifyHealthReport(false, 0, 0);
    }

    private void verifyHealthReport(boolean healthReportEnabled, int numberOfWarnings, int healthScore)
            throws IOException, InterruptedException {
        FreeStyleProject project = createFreeStyleProject();
        getWorkspace(project).createTextTempFile("java", ".txt", getWarnings(numberOfWarnings));

        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        if (healthReportEnabled) {
            WarningsConfigurationPage configPage = new WarningsConfigurationPage(project);
            configPage.setHealthyThreshold(HEALTHY_THRESHOLD);
            configPage.setUnhealthyThreshold(UNHEALTHY_THRESHOLD);
            configPage.submitForm();
        }

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        AnalysisResult result = getAnalysisResult(build);
        HealthReport healthReport = getResultAction(build).getBuildHealth();
        JavaMessagesPage javaMessagesPage = new JavaMessagesPage(build);

        assertThat(result).hasTotalSize(numberOfWarnings);

        assertThat(javaMessagesPage.getNumberOfIssues()).isEqualTo(numberOfWarnings);
        assertThat(javaMessagesPage.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(javaMessagesPage.getErrorMessages()).isEqualTo(result.getErrorMessages());

        if (healthReportEnabled) {
            assertThat(healthReport.getScore()).isEqualTo(healthScore);
            assertThat(javaMessagesPage.getHealthyThreshold()).isEqualTo(HEALTHY_THRESHOLD);
            assertThat(javaMessagesPage.getUnhealthyThreshold()).isEqualTo(UNHEALTHY_THRESHOLD);
        }
        else {
            assertThat(javaMessagesPage.getInfoMessages()).contains("Health report is disabled - skipping");
        }
    }

    private String getWarnings(final int count) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < count; i++) {
            //add the counter to make every warning unique
            builder.append(JAVA_WARNING + i);
            builder.append('\n');
        }

        return builder.toString();
    }

    /** A class representing the Java Messages page following the Object Page Pattern. */
    private class JavaMessagesPage {
        private static final String PAGE_URL = "java/info";
        private static final String INFO_HTML_ELEMENT_ID = "info";
        private static final String ERROR_HTML_ELEMENT_ID = "errors";

        private final Pattern number_issues = Pattern.compile("-> found (\\d+) issue.*");
        private final Pattern healthy_threshold = Pattern.compile(".*Healthy=(\\d+).*");
        private final Pattern unhealthy_threshold = Pattern.compile(".*Unhealthy=(\\d+).*");
        private final HtmlPage page;

        public JavaMessagesPage(Run<?, ?> build) {
            page = getWebPage(build, PAGE_URL);
        }

        public List<String> getInfoMessages() {
            List<String> infoMessagesList = new ArrayList<>();
            DomElement info = page.getElementById(INFO_HTML_ELEMENT_ID);

            info.getChildElements().forEach(child -> infoMessagesList.add(child.asText()));

            return infoMessagesList;
        }

        public List<String> getErrorMessages() {
            List<String> errorMessagesList = new ArrayList<>();
            DomElement errors = page.getElementById(ERROR_HTML_ELEMENT_ID);

            errors.getChildElements().forEach(child -> errorMessagesList.add(child.asText()));

            return errorMessagesList;
        }

        public int getNumberOfIssues() {
            return searchInfoMessagesForInteger(number_issues);
        }

        public int getHealthyThreshold() {
            return searchInfoMessagesForInteger(healthy_threshold);
        }

        public int getUnhealthyThreshold() {
            return searchInfoMessagesForInteger(unhealthy_threshold);
        }

        private int searchInfoMessagesForInteger(Pattern pattern) {
            int searchedInteger = 0;
            List<String> infoMessages = getInfoMessages();

            for (String infoLine : infoMessages) {
                Matcher matcher = pattern.matcher(infoLine);
                if (matcher.matches()) {
                    String IntegerAsString = matcher.group(1);
                    searchedInteger = Integer.parseInt(IntegerAsString);
                    break;
                }
            }

            return searchedInteger;
        }

    }

    /** A class representing the warnings configuration page following the Object Page Pattern. */
    private class WarningsConfigurationPage {
        private static final String PAGE_URL = "configure";
        private static final String FORM_NAME = "config";
        private static final String HEALTHY_THRESHOLD_ID = "_.healthy";
        private static final String UNHEALTHY_THRESHOLD_ID = "_.unhealthy";

        private final HtmlPage configPage;
        private final HtmlForm configForm;

        public WarningsConfigurationPage(AbstractProject<?, ?> job) {
            configPage = getWebPage(job, PAGE_URL);
            configForm = configPage.getFormByName(FORM_NAME);
        }

        public void setHealthyThreshold(int healthyThreshold) {
            HtmlNumberInput healthyThresholdInput = configForm.getInputByName(HEALTHY_THRESHOLD_ID);
            healthyThresholdInput.setText(Integer.toString(healthyThreshold));
        }

        public void setUnhealthyThreshold(int unhealthyThreshold) {
            HtmlNumberInput healthyThresholdInput = configForm.getInputByName(UNHEALTHY_THRESHOLD_ID);
            healthyThresholdInput.setText(Integer.toString(unhealthyThreshold));
        }

        public void submitForm() throws IOException {
            HtmlFormUtil.submit(configForm);
        }

    }
}
