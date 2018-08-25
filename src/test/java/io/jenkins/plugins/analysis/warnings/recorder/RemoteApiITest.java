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
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.restapi.AnalysisResultApi;
import io.jenkins.plugins.analysis.core.restapi.ReportApi;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.CheckStyle;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Integration tests of the remote API.
 *
 * @author Manuel Hampp
 */
public class RemoteApiITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String CHECKSTYLE_FILE = "checkstyle-filtering.xml";
    private static final String RESULT_REMOTE_API_EXPECTED_XML = "result.xml";
    private static final String ISSUES_REMOTE_API_EXPECTED_XML = "issues.xml";
    private static final String FOLDER_PREFIX = "rest-api/";

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
        XMLUnit.setIgnoreWhitespace(true);
        Document actualDocument = callRemoteApi(build, url);
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

        Document actualDocument = callRemoteApi(build, "/checkstyle/api/xml?xpath=/*/qualityGateStatus");

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

        Document actualDocument = callRemoteApi(build, "/checkstyle/api/xml?depth=1");

        // navigate to one deep level element that is not visible at depth 0
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node deepLevelElement = (Node) xpath
                .compile("//analysisResultApi//owner//action//cause//*")
                .evaluate(actualDocument, XPathConstants.NODE);

        assertThat(deepLevelElement).isNotNull();
        assertThat(deepLevelElement.getNodeName()).isEqualTo("shortDescription");
    }

    /**
     * Verifies that the numbers of new, fixed and outstanding warnings are correctly computed, if the warnings are from
     * the same file but have different properties (e.g. line number). Checks that the fallback-fingerprint is using
     * several properties of the issue if the source code has not been found.
     */
    // TODO: there should be also some tests that use the fingerprinting algorithm on existing source files
    @Test
    public void shouldFindNewCheckStyleWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle1.xml", "checkstyle2.xml");
        IssuesRecorder recorder = enableWarnings(project, new ToolConfiguration(new CheckStyle(), "**/checkstyle1*"));
        buildWithStatus(project, Result.SUCCESS);
        recorder.setTool(new ToolConfiguration(new CheckStyle(), "**/checkstyle2*"));
        Run<?, ?> build = buildWithStatus(project, Result.SUCCESS);

        assertThatRemoteApiEquals(build, "/checkstyle/all/api/xml", "all-issues.xml");
        assertThatRemoteApiEquals(build, "/checkstyle/new/api/xml", "new-issues.xml");
        assertThatRemoteApiEquals(build, "/checkstyle/fixed/api/xml", "fixed-issues.xml");
        assertThatRemoteApiEquals(build, "/checkstyle/outstanding/api/xml", "outstanding-issues.xml");
    }

    private Run<?, ?> buildCheckStyleJob() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(CHECKSTYLE_FILE);
        enableCheckStyleWarnings(project);
        return buildWithResult(project, Result.SUCCESS);
    }

    private Document callRemoteApi(final Run<?, ?> run, final String url) {
        try {
            XmlPage page = getJenkins().createWebClient().goToXml(run.getUrl() + url);
            return page.getXmlDocument();
        }
        catch (IOException | SAXException e) {
            throw new AssertionError(e);
        }
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
