package io.jenkins.plugins.analysis.warnings.recorder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.JSONWebResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.restapi.AnalysisResultApi;
import io.jenkins.plugins.analysis.core.restapi.ReportApi;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.analysis.warnings.SpotBugs;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Integration tests of the remote API.
 *
 * @author Manuel Hampp
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class RemoteApiITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String CHECKSTYLE_FILE = "checkstyle-filtering.xml";
    private static final String RESULT_REMOTE_API_EXPECTED_XML = "result.xml";
    private static final String ISSUES_REMOTE_API_EXPECTED_XML = "issues.xml";
    private static final String FOLDER_PREFIX = "rest-api/";

    /** Ensures that XML unit does ignore white space. */
    @BeforeClass
    public static void initXmlUnit() {
        XMLUnit.setIgnoreWhitespace(true);
    }

    /**
     * Verifies a top-level REST API call that returns a representation of {@link AnalysisResultApi}.
     */
    @Test
    public void shouldReturnSummaryForTopLevelApiCall() {
        // Skip elements with absolute paths or other platform specific information
        verifyRemoteApi("/checkstyle/api/xml"
                + "?exclude=/*/errorMessage"
                + "&exclude=/*/infoMessage"
                + "&exclude=/*/owner/url", RESULT_REMOTE_API_EXPECTED_XML);
    }

    /**
     * Verifies a REST API call for url "/all" that returns a representation of {@link ReportApi}.
     */
    @Test
    public void shouldReturnIssuesForNewApiCall() {
        verifyRemoteApi("/checkstyle/all/api/xml", ISSUES_REMOTE_API_EXPECTED_XML);
    }

    private void verifyRemoteApi(final String url, final String issuesRemoteApiExpectedXml) {
        Run<?, ?> build = buildCheckStyleJob();

        assertThatRemoteApiEquals(build, url, issuesRemoteApiExpectedXml);
    }

    private void assertThatRemoteApiEquals(final Run<?, ?> build, final String url, final String expectedXml) {
        Document actualDocument = callXmlRemoteApi(build.getUrl() + url);
        Document expectedDocument = readExpectedXml(FOLDER_PREFIX + expectedXml);
        Diff diff = XMLUnit.compareXML(expectedDocument, actualDocument);

        assertThat(diff.identical()).as(diff.toString()).isTrue();
    }

    /**
     * Tests the xpath navigation within the xml api.
     */
    @Test
    public void assertXmlApiWithXPathNavigationMatchesExpected() {
        Run<?, ?> build = buildCheckStyleJob();

        Document actualDocument = callXmlRemoteApi(build.getUrl() + "/checkstyle/api/xml?xpath=/*/qualityGateStatus");

        assertThat(actualDocument.getDocumentElement().getTagName()).isEqualTo("qualityGateStatus");
        assertThat(actualDocument.getDocumentElement().getFirstChild().getNodeValue()).isEqualTo("INACTIVE");
    }

    /**
     * Tests the depth parameter within the xml api.
     *
     * @throws XPathExpressionException
     *         if the path could not be resolved
     */
    @Test
    public void assertXmlApiWithDepthContainsDeepElements() throws XPathExpressionException {
        Run<?, ?> build = buildCheckStyleJob();

        Document actualDocument = callXmlRemoteApi(build.getUrl() + "/checkstyle/api/xml?depth=1");

        // navigate to one deep level element that is not visible at depth 0
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node deepLevelElement = (Node) xpath
                .compile("//analysisResultApi//owner//action//cause//*")
                .evaluate(actualDocument, XPathConstants.NODE);

        assertThat(deepLevelElement).isNotNull();
        assertThat(deepLevelElement.getNodeName()).isEqualTo("shortDescription");
    }

    /**
     * Verifies that the remote API for new, fixed and outstanding warnings is correctly returning the filtered
     * results.
     */
    @Test
    public void shouldFindNewCheckStyleWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle1.xml", "checkstyle2.xml");
        IssuesRecorder recorder = enableWarnings(project, createCheckstyle("**/checkstyle1*"));
        buildWithStatus(project, Result.SUCCESS);
        recorder.setTool(createCheckstyle("**/checkstyle2*"));
        Run<?, ?> build = buildWithStatus(project, Result.SUCCESS);

        assertThatRemoteApiEquals(build, "/checkstyle/all/api/xml", "all-issues.xml");
        assertThatRemoteApiEquals(build, "/checkstyle/new/api/xml", "new-issues.xml");
        assertThatRemoteApiEquals(build, "/checkstyle/fixed/api/xml", "fixed-issues.xml");
        assertThatRemoteApiEquals(build, "/checkstyle/outstanding/api/xml", "outstanding-issues.xml");
    }

    /** Verifies that the remote API for the tools aggregation correctly returns the summary. */
    @Test
    public void shouldReturnAggregation() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle1.xml", "checkstyle2.xml");
        enableWarnings(project, createCheckstyle("**/checkstyle1*"),
                configurePattern(new Pmd()), configurePattern(new SpotBugs()));
        Run<?, ?> build = buildWithStatus(project, Result.SUCCESS);

        JSONWebResponse json = callJsonRemoteApi(build.getUrl() + "warnings-ng/api/json");
        JSONObject result = json.getJSONObject();

        assertThatJson(result).node("tools").isArray().hasSize(3);
        JSONArray tools = result.getJSONArray("tools");

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
        ReportScanningTool tool = createTool(new CheckStyle(), pattern);
        tool.setReportEncoding("UTF-8");
        return tool;
    }

    private Run<?, ?> buildCheckStyleJob() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(CHECKSTYLE_FILE);
        enableCheckStyleWarnings(project);
        return buildWithResult(project, Result.SUCCESS);
    }

    private Document readExpectedXml(final String fileName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            return builder.parse(asInputStream(fileName));
        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }
}
