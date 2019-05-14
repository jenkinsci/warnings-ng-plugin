package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page object for the source code view page.
 */
public class SourceCodeView {
    /** The html element id of the main panel. */
    public static final String MAIN_PANEL_ELEMENT_ID = "main-panel";

    /** The regex pattern describing the head line of the page (including file name). */
    public static final Pattern FILE_NAME_PATTERN = Pattern.compile("Content of file (.*)");

    /** The xPath to the file name (starting at the main panel). */
    public static final String FILE_NAME_XPATH = "//h1";
    /** The xPath to the source code (starting at the main panel). */
    public static final String SOURCE_CODE_XPATH = "//pre//code";
    /** The xPath to the source code producing the issue (starting at the main panel). */
    public static final String ISSUE_SOURCE_CODE_XPATH = "//pre//code[2]";
    /** The xPath to the message describing the issue (starting at the main panel). */
    public static final String ISSUE_MESSAGE_XPATH = "//pre//div//div";

    /** The source code view html page. */
    private HtmlPage sourceCodeViewPage;

    /**
     * Constructor.
     *
     * @param sourceCodeViewPage
     *         The source code view html page.
     */
    public SourceCodeView(HtmlPage sourceCodeViewPage) {
        this.sourceCodeViewPage = sourceCodeViewPage;
    }

    /**
     * Extracts the name of the file that is displayed on the html page.
     *
     * @return The file name.
     */
    public String getFileName() {
        String name;

        List<DomNode> headingByXPath = getMainPanel().getByXPath(FILE_NAME_XPATH);
        if (!headingByXPath.isEmpty()) {
            String heading = headingByXPath.get(0).asText();
            Matcher matcher = FILE_NAME_PATTERN.matcher(heading);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        throw new NoSuchElementException("Can't find the file name on page.");
    }

    /**
     * Extracts the whole source code displayed on the html page.
     *
     * @return The source code.
     */
    public String getSourceCode() {
        StringBuilder stringBuilder = new StringBuilder();
        List<DomNode> sourceCodeList = getMainPanel().getByXPath(SOURCE_CODE_XPATH);

        for (DomNode node : sourceCodeList) {
            String code = node.asText();
            stringBuilder.append(code);
        }

        return stringBuilder.toString();
    }

    /**
     * Extracts the source code line that produces the issue.
     *
     * @return The source code line producing the issue.
     */
    public String getIssueSourceCodeLine() {
        StringBuilder sourceCodeLineBuilder = new StringBuilder();
        List<DomNode> issueSourceCodeList = getMainPanel().getByXPath(ISSUE_SOURCE_CODE_XPATH);

        if (!issueSourceCodeList.isEmpty()) {
            issueSourceCodeList.forEach(c -> sourceCodeLineBuilder.append(c.asText()));
        }
        else {
            throw new NoSuchElementException("Can't find the issue source code line on page.");
        }

        return sourceCodeLineBuilder.toString();
    }

    /**
     * Extracts the message about the issue from the html page.
     *
     * @return The issue message.
     */
    public String getIssueMessage() {
        StringBuilder issueMessageBuilder = new StringBuilder();
        List<DomNode> issueMessageList = getMainPanel().getByXPath(ISSUE_MESSAGE_XPATH);

        if (!issueMessageList.isEmpty()) {
            for (DomNode node : issueMessageList) {
                String issueMessage = node.asText();

                //replacing "new line" and "tab" character at the start of the message
                issueMessage = issueMessage.replace("\t", "").replace("\n", "");
                issueMessageBuilder.append(issueMessage);
            }
        }
        else {
            throw new NoSuchElementException("Can't find the issue message on page.");
        }

        return issueMessageBuilder.toString();
    }

    /**
     * Extracts the main panel out of the html page.
     *
     * @return The main panel element of the html page.
     */
    private DomElement getMainPanel() {
        return sourceCodeViewPage.getElementById(MAIN_PANEL_ELEMENT_ID);
    }
}
