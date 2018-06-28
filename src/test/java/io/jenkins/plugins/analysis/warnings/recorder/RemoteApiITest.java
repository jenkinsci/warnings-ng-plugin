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

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Integration tests of the remote API.
 *
 * @author Manuel Hampp
 */
public class RemoteApiITest extends AbstractIssuesRecorderITest {
    private static final String CHECKSTYLE_FILE = "checkstyle-filtering.xml";
    private static final String REMOTE_API_EXPECTED_XML = "checkstyle-expected-remote-api.xml";

    /**
     * Compares the basic XML api (without parameters) against a control result.
     */
    @Test
    public void assertXmlApiMatchesExpected() {
        Run<?, ?> build = buildCheckStyleJob();

        // Skip elements with absolute paths or other platform specific information 
        XmlPage page = callRemoteApi(build, "/checkstyleResult/api/xml"
                + "?exclude=/*/errorMessage"
                + "&exclude=/*/infoMessage"
                + "&exclude=/*/owner/*");

        Document actualDocument = page.getXmlDocument();
        Document expectedDocument = readExpectedXml(REMOTE_API_EXPECTED_XML);

        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML(expectedDocument, actualDocument);

        assertThat(diff.identical()).as(diff.toString()).isTrue();
    }

    /**
     * Tests the xpath navigation within the xml api.
     */
    @Test
    public void assertXmlApiWithXPathNavigationMatchesExpected() {
        Run<?, ?> build = buildCheckStyleJob();

        XmlPage page = callRemoteApi(build, "/checkstyleResult/api/xml?xpath=/*/status");

        Document actualDocument = page.getXmlDocument();
        assertThat(actualDocument.getDocumentElement().getTagName()).isEqualTo("status");
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

        XmlPage page = callRemoteApi(build, "/checkstyleResult/api/xml?depth=1");

        Document actualDocument = page.getXmlDocument();
        // navigate to one deep level element that is not visible at depth 0
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node deepLevelElement = (Node) xpath
                .compile("//analysisResult//owner//action//cause//*")
                .evaluate(actualDocument, XPathConstants.NODE);

        assertThat(deepLevelElement).isNotNull();
        assertThat(deepLevelElement.getNodeName()).isEqualTo("shortDescription");
    }

    private Run<?, ?> buildCheckStyleJob() {
        FreeStyleProject project = createJobWithWorkspaceFiles(CHECKSTYLE_FILE);
        enableCheckStyleWarnings(project);
        return buildWithResult(project, Result.SUCCESS);
    }

    private XmlPage callRemoteApi(final Run<?, ?> run, final String url) {
        try {
            return j.createWebClient().goToXml(run.getUrl() + url);
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
