package io.jenkins.plugins.analysis.warnings.steps;

import java.util.Arrays;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.builder.Input;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.restapi.AnalysisResultApi;
import io.jenkins.plugins.analysis.core.restapi.ReportApi;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.prism.SourceCodeDirectory;
import io.jenkins.plugins.analysis.warnings.SpotBugs;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests of the remote API.
 *
 * @author Manuel Hampp
 */
class RemoteApiITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String CHECKSTYLE_FILE = "checkstyle-filtering.xml";
    private static final String RESULT_REMOTE_API_EXPECTED_XML = "result.xml";
    private static final String ISSUES_REMOTE_API_EXPECTED_XML = "issues.xml";
    private static final String FOLDER_PREFIX = "rest-api/";

    /**
     * Verifies a top-level REST API call that returns a representation of {@link AnalysisResultApi}.
     */
    @Test
    void shouldReturnSummaryForTopLevelApiCall() {
        // Skip elements with absolute paths or other platform-specific information
        verifyRemoteApi("/checkstyle/api/xml"
                + "?exclude=/*/errorMessage"
                + "&exclude=/*/infoMessage"
                + "&exclude=/*/owner/url", RESULT_REMOTE_API_EXPECTED_XML);
    }

    /**
     * Verifies a REST API call for url "/all" that returns a representation of {@link ReportApi}.
     */
    @Test
    void shouldReturnIssuesForNewApiCall() {
        verifyRemoteApi("/checkstyle/all/api/xml", ISSUES_REMOTE_API_EXPECTED_XML);
    }

    /**
     * Verifies that the Remote API returns file paths relative to the configured source directory.
     * This test addresses JENKINS-68856 where file paths should be relative to the sourceDirectory
     * when specified, rather than showing the full workspace path.
     */
    @Test
    void shouldReturnRelativeFilePathsWhenSourceDirectoryIsConfigured() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(CHECKSTYLE_FILE);
        var recorder = enableCheckStyleWarnings(project);
        
        recorder.setSourceDirectories(Arrays.asList(new SourceCodeDirectory("tasks/src")));
        
        Run<?, ?> build = scheduleBuildAndAssertStatus(project, Result.SUCCESS).getOwner();

        var json = callJsonRemoteApi(build.getUrl() + "checkstyle/all/api/json");
        var result = json.getJSONObject();
        
        assertThatJson(result).node("issues").isArray();
        JSONArray issues = result.getJSONArray("issues");
        assertThat(issues.size()).as("Should have issues").isGreaterThan(0);
        
        for (int i = 0; i < issues.size(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            String fileName = issue.getString("fileName");
            
            assertThat(fileName)
                    .as("File name at index %d should be relative to source directory, not include 'tasks/src/' prefix", i)
                    .doesNotStartWith("tasks/src/");
        }
    }

    private void verifyRemoteApi(final String url, final String issuesRemoteApiExpectedXml) {
        Run<?, ?> build = buildCheckStyleJob();

        assertThatRemoteApiEquals(build, url, issuesRemoteApiExpectedXml);
    }

    private void assertThatRemoteApiEquals(final Run<?, ?> build, final String url, final String expectedXml) {
        XmlAssert.assertThat(callXmlRemoteApi(build.getUrl() + url))
                .and(Input.from(readAllBytes(FOLDER_PREFIX + expectedXml)))
                .ignoreChildNodesOrder()
                .normalizeWhitespace()
                .withNodeFilter(node -> "toString".equals(node.getNodeName()))
                .areIdentical();
    }

    /**
     * Tests the xpath navigation within the xml api.
     */
    @Test
    void assertXmlApiWithXPathNavigationMatchesExpected() {
        Run<?, ?> build = buildCheckStyleJob();

        var actualDocument = callXmlRemoteApi(build.getUrl() + "/checkstyle/api/xml?xpath=/*/qualityGates");

        var documentElement = actualDocument.getDocumentElement();
        assertThat(documentElement.getTagName()).isEqualTo("qualityGates");

        var result = documentElement.getFirstChild();
        assertThat(result.getNodeName()).isEqualTo("overallResult");
        assertThat(result.getTextContent()).isEqualTo("INACTIVE");
    }

    /**
     * Tests the depth parameter within the xml api.
     *
     * @throws XPathExpressionException
     *         if the path could not be resolved
     */
    @Test
    void assertXmlApiWithDepthContainsDeepElements() throws XPathExpressionException {
        Run<?, ?> build = buildCheckStyleJob();

        var actualDocument = callXmlRemoteApi(build.getUrl() + "/checkstyle/api/xml?depth=1");

        // navigate to one deep level element that is not visible at depth 0
        var xpath = XPathFactory.newInstance().newXPath();
        var deepLevelElement = (Node) xpath
                .compile("//analysisResultApi//owner//result")
                .evaluate(actualDocument, XPathConstants.NODE);

        assertThat(deepLevelElement).isNotNull();
        assertThat(deepLevelElement.getNodeName()).isEqualTo("result");
        assertThat(deepLevelElement.getTextContent()).isEqualTo("SUCCESS");
    }

    /**
     * Verifies that the remote API for new, fixed and outstanding warnings is correctly returning the filtered
     * results.
     */
    @Test
    void shouldFindNewCheckStyleWarnings() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix("checkstyle1.xml",
                "checkstyle2.xml");
        var recorder = enableWarnings(project, createCheckstyle("**/checkstyle1*"));
        buildWithResult(project, Result.SUCCESS);
        recorder.setTools(createCheckstyle("**/checkstyle2*"));
        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);

        assertThatRemoteApiEquals(build, "/checkstyle/all/api/xml", "all-issues.xml");
        assertThatRemoteApiEquals(build, "/checkstyle/new/api/xml", "new-issues.xml");
        assertThatRemoteApiEquals(build, "/checkstyle/fixed/api/xml", "fixed-issues.xml");
        assertThatRemoteApiEquals(build, "/checkstyle/outstanding/api/xml", "outstanding-issues.xml");
    }

    /** Verifies that the remote API for the tools aggregation correctly returns the summary. */
    @Test
    void shouldReturnAggregation() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix("checkstyle1.xml",
                "checkstyle2.xml");
        enableWarnings(project, createCheckstyle("**/checkstyle1*"),
                configurePattern(new Pmd()), configurePattern(new SpotBugs()));
        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);

        var json = callJsonRemoteApi(build.getUrl() + "warnings-ng/api/json");
        var result = json.getJSONObject();

        assertThatJson(result).node("tools").isArray().hasSize(3);
        var tools = result.getJSONArray("tools");

        assertThatToolsContains(tools, "checkstyle", "CheckStyle Warnings", 3);
        assertThatToolsContains(tools, "pmd", "PMD Warnings", 0);
        assertThatToolsContains(tools, "spotbugs", "SpotBugs Warnings", 0);
    }

    private void assertThatToolsContains(final JSONArray tools,
            final String expectedId, final String expectedName, final int expectedSize) {
        for (int i = 0; i < 3; i++) {
            if (tools.getString(i).contains(expectedId)) {
                assertThatJson(tools.get(i)).node("id").isEqualTo(expectedId);
                assertThatJson(tools.get(i)).node("name").isEqualTo(expectedName);
                assertThatJson(tools.get(i)).node("latestUrl").asString()
                        .matches("http://localhost:\\d+/jenkins/job/test\\d+/1/" + expectedId);
                assertThatJson(tools.get(i)).node("size").isEqualTo(expectedSize);
                return;
            }
        }

        fail("No node found with id %s in %s", expectedId, tools.toString(2));
    }

    private ReportScanningTool createCheckstyle(final String pattern) {
        var tool = createTool(new CheckStyle(), pattern);
        tool.setReportEncoding("UTF-8");
        return tool;
    }

    private Run<?, ?> buildCheckStyleJob() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(CHECKSTYLE_FILE);
        enableCheckStyleWarnings(project);
        return scheduleBuildAndAssertStatus(project, Result.SUCCESS).getOwner();
    }
}
