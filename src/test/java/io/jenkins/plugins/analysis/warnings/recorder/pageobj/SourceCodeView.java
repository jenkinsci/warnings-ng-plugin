package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.List;
import java.util.NoSuchElementException;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page object for the source code view page.
 */
public class SourceCodeView {
    /** The html element id of the main panel. */
    public static final String MAIN_PANEL_ELEMENT_ID = "main-panel";

    /** The xPath to the source code (starting at the main panel). */
    public static final String SOURCE_CODE_XPATH = "//pre//code";
    /** The xPath to the source code producing the issue (starting at the main panel). */
    public static final String AFFECTED_LINES_XPATH = "//pre//code[2]";
    /** The xPath to the message describing the issue (starting at the main panel). */
    public static final String ISSUE_MESSAGE_XPATH = "//pre//div//div";

    /** The source code view html page. */
    private HtmlPage sourceCodeViewPage;

    /**
     * Creates a new instance of {@link SourceCodeView}.
     *
     * @param sourceCodeViewPage
     *         The source code view html page.
     */
    public SourceCodeView(final HtmlPage sourceCodeViewPage) {
        this.sourceCodeViewPage = sourceCodeViewPage;
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
     * Extracts the source code lines affected by the issue.
     *
     * @return The source code lines affected by the issue.
     */
    public String getAffectedLines() {
        StringBuilder affectedLinesBuilder = new StringBuilder();
        List<DomNode> affectedLines = getMainPanel().getByXPath(AFFECTED_LINES_XPATH);

        if (affectedLines.isEmpty()) {
            throw new NoSuchElementException("Can't find affected source code lines on page.");
        }
        else {
            affectedLines.forEach(c -> affectedLinesBuilder.append(c.asText()));
        }

        return affectedLinesBuilder.toString();
    }

    /**
     * Extracts the message about the issue from the html page.
     *
     * @return The issue message.
     */
    public String getIssueMessage() {
        StringBuilder issueMessageBuilder = new StringBuilder();
        List<DomNode> issueMessages = getMainPanel().getByXPath(ISSUE_MESSAGE_XPATH);

        if (issueMessages.isEmpty()) {
            throw new NoSuchElementException("Can't find the issue message on page.");
        }
        else {
            issueMessages.forEach(m -> issueMessageBuilder.append(m.asText()));
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
