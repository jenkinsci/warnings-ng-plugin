package io.jenkins.plugins.analysis.core.columns;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.PyLint;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Integration Test for the Issue counter column.
 */
public class IssueCounterITest extends IntegrationTestWithJenkinsPerTest {

    private static final String ISSUE_ELEMENT_ID = "issues-total";
    private static final String JAVAC_ONE_WARNING = "javac_1_warning.txt";
    private static final String JAVAC_PYTHON_WARNINGS = "javac_python_3_issues.txt";
    private static final String RESULT_FILE_NAME = "build.log";
    private static final String LOG_FILE_PATTERN = "**/*.log";
    private static final String DASH = "-";
    private static final String HEALTH_LINK_TEXT_XPATH = "//div/table/tbody/tr/td/a/text()";
    private static final String JAVA_TOOL_NAME = "Java";
    private static final String PYTHON_TOOL_NAME = "Pylint";

    /**
     * Tests that the issue counter is displayed correctly with 1 issue.
     */
    @Test
    public void shouldShowIssueCounterEqualToOne() {
        createAndBuildProjectWithFile(JAVAC_ONE_WARNING);

        assertThat(getIssueCount(getRootPage())).isEqualTo(1);
    }

    /**
     * Tests that the issue counter is displayed correctly with 2 issues from different tools.
     */
    @Test
    public void shouldShowIssueCounterEqualToSum() {
        createAndBuildProjectWithFile(JAVAC_PYTHON_WARNINGS);

        assertThat(getIssueCount(getRootPage())).isEqualTo(3);

        assertThat(getHoverCount(JAVA_TOOL_NAME)).isEqualTo(1);
        assertThat(getHoverCount(PYTHON_TOOL_NAME)).isEqualTo(2);
    }

    /**
     * Tests that the issue counter is displayed correctly with no issues.
     */
    @Test
    public void shouldShowIssueCounterEqualToZero() {
        FreeStyleProject project = createFreeStyleProject();

        enableWarnings(project, new Java());
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(getIssueCount(getRootPage())).isEqualTo(0);
    }

    /**
     * Tests that the issue counter is displayed correctly with no issues.
     */
    @Test
    public void shouldShowIssueCounterEqualToDash() {
        FreeStyleProject project = createFreeStyleProject();

        buildWithResult(project, Result.SUCCESS);

        assertThat(getIssueString()).isEqualTo(DASH);
    }

    private void createAndBuildProjectWithFile(final String fileName) {
        FreeStyleProject project = createFreeStyleProject();

        Java javaAnalysis = new Java();
        copySingleFileToWorkspace(project, fileName, RESULT_FILE_NAME);
        javaAnalysis.setPattern(LOG_FILE_PATTERN);

        PyLint pyLint = new PyLint();
        pyLint.setPattern(LOG_FILE_PATTERN);

        enableWarnings(project, javaAnalysis, pyLint);

        buildSuccessfully(project);
    }

    private HtmlPage getRootPage() {

        WebClient webClient = getJenkins().createWebClient();
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        assertThat(jenkins).isNotNull();

        try {
            return webClient.getPage(jenkins.getRootUrl());
        }
        catch (IOException e) {
            throw new IllegalStateException("Unexpected I/O Exception", e);
        }
    }

    private int getHoverCount(final String toolName) {
        List<DomText> text = getRootPage().getElementById(ISSUE_ELEMENT_ID)
                .getByXPath(HEALTH_LINK_TEXT_XPATH);
        for (DomText content : text) {
            if (content.getWholeText().contains(toolName + " Warnings")) {
                return Integer.parseInt(content.getParentNode().getParentNode().getNextSibling().getTextContent());
            }
        }
        return 0;
    }

    private int getIssueCount(final HtmlPage rootPage) {
        return Integer.parseInt(rootPage.getElementById(ISSUE_ELEMENT_ID).asText().replaceAll("\\s+", ""));
    }

    private String getIssueString() {
        return getRootPage().getElementById(ISSUE_ELEMENT_ID).asText().replaceAll("\\s+", "");
    }
}
