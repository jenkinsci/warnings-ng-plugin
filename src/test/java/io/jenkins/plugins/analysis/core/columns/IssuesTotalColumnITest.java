package io.jenkins.plugins.analysis.core.columns;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.Issue;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.PyLint;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Integration Test for the Issue counter column.
 *
 * @author Andreas Reiser
 */
public class IssuesTotalColumnITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JAVAC_ONE_WARNING = "javac_1_warning.txt";
    private static final String JAVAC_PYTHON_WARNINGS = "javac_python_3_issues.txt";
    private static final String RESULT_FILE_NAME = "build.log";
    private static final String LOG_FILE_PATTERN = "**/*.log";
    private static final String DASH = "-";
    private static final String HEALTH_LINK_TEXT_XPATH = "div/table/tbody/tr/td/a/text()";
    private static final String JAVA_TOOL_NAME = "Java";
    private static final String PYTHON_TOOL_NAME = "Pylint";

    /**
     * Tests that the issue counter is displayed correctly with 1 issue.
     */
    @Test
    public void shouldShowIssueCounterEqualToOne() {
        buildWithFile(JAVAC_ONE_WARNING);

        assertThat(getIssueCount(getRootPage())).isEqualTo(1);
    }

    /**
     * Tests that the issue counter is displayed correctly with 1 issue.
     */
    @Test @Issue("JENKINS-58420")
    public void shouldShowIssueCounterIfBuildFails() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(JAVAC_ONE_WARNING);
        project.getBuildersList().add(new FailureBuilder());
        enableGenericWarnings(project, issuesRecorder -> issuesRecorder.setEnabledForFailure(true), new Java());

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);
        assertThat(result.getTotalSize()).isEqualTo(1);

        assertThat(getIssueCount(getRootPage())).isEqualTo(1);
    }

    /**
     * Tests that the issue counter is displayed correctly with 2 issues from different tools.
     */
    @Test
    public void shouldShowIssueCounterEqualToSum() {
        buildWithFile(JAVAC_PYTHON_WARNINGS);

        HtmlPage rootPage = getRootPage();

        assertThat(getIssueCount(rootPage)).isEqualTo(3);
        assertThat(getHoverCount(JAVA_TOOL_NAME, rootPage)).isEqualTo(1);
        assertThat(getHoverCount(PYTHON_TOOL_NAME, rootPage)).isEqualTo(2);
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

        assertThat(getIssueString(getRootPage())).isEqualTo(DASH);
    }

    private void buildWithFile(final String fileName) {
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
        try {
            return getWebClient(JavaScriptSupport.JS_ENABLED).goTo("");
        }
        catch (SAXException | IOException e) {
            throw new AssertionError("Unexpected I/O Exception", e);
        }
    }

    private int getHoverCount(final String toolName, final HtmlPage rootPage) {
        List<DomText> text = getColumn(rootPage).getByXPath(HEALTH_LINK_TEXT_XPATH);
        for (DomText content : text) {
            if (content.getWholeText().contains(toolName + " Warnings")) {
                return Integer.parseInt(content.getParentNode().getParentNode().getNextSibling().getTextContent());
            }
        }
        return 0;
    }

    private DomElement getColumn(final HtmlPage rootPage) {
        List<DomElement> elements = rootPage.getByXPath("//td[contains(@class, 'issues-total')]");
        return elements.get(elements.size() - 1);
    }

    private int getIssueCount(final HtmlPage rootPage) {
        return Integer.parseInt(getIssueString(rootPage));
    }

    private String getIssueString(final HtmlPage rootPage) {
        return getColumn(rootPage).asText().replaceAll("\\s+", "");
    }
}
