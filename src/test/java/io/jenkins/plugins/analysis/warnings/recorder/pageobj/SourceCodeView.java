package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page object for the source code view page.
 */
public class SourceCodeView {
    /** List of characters which causing a line break in formatted java code. */
    public static final List<Character> lineEndElements = Arrays.asList(';', '{', '}');
    /** The html element id of the main panel. */
    public static final String mainPanelElementId = "main-panel";

    /** The regex pattern describing the head line of the page (including file name).*/
    public static final Pattern fileNamePattern = Pattern.compile("Content of file (.*)");

    /** The xPath to the file name (starting at the main panel). */
    public static final String fileNameXpath = "//h1";
    /** The xPath to the source code (starting at the main panel). */
    public static final String sourceCodeXpath = "//pre//code";
    /** The xPath to the source code producing the issue (starting at the main panel). */
    public static final String issueSourceCodeXpath = "//pre//code[2]";
    /** The xPath to the message describing the issue (starting at the main panel). */
    public static final String issueMessageXpath = "//pre//div";

    /** The source code view html page.*/
    private HtmlPage sourceCodeViewPage;

    /**
     * Constructor.
     *
     * @param sourceCodeViewPage The source code view html page.
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
        String name = "";

        List<DomNode> headingByXPath = getMainPanel().getByXPath(fileNameXpath);
        if (!headingByXPath.isEmpty()) {
            String heading = headingByXPath.get(0).asText();
            Matcher matcher = fileNamePattern.matcher(heading);
            if (matcher.matches()) {
                name = matcher.group(1);
            }
        }

        return name;
    }

    /**
     * Extracts the whole source code displayed on the html page.
     *
     * @return The source code.
     */
    public String getSourceCode() {
        StringBuilder stringBuilder = new StringBuilder();

        List<DomNode> sourceCodeList = getMainPanel().getByXPath(sourceCodeXpath);

        for (DomNode node : sourceCodeList) {
            String code = node.asText();
            for (char c : code.toCharArray()) {
                stringBuilder.append(c);
                if (lineEndElements.contains(c)) {
                    stringBuilder.append('\n');
                }
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Extracts the source code line that produces the issue.
     *
     * @return The source code line producing the issue.
     */
    public String getIssueSourceCodeLine() {
        String issueSourceCodeLine = "";
        List<DomNode> issueSourceCodeList = getMainPanel().getByXPath(issueSourceCodeXpath);

        if(!issueSourceCodeList.isEmpty()){
           issueSourceCodeLine = issueSourceCodeList.get(0).asText();
        }

        return issueSourceCodeLine;
    }

    /**
     * Extracts the message about the issue from the html page.
     *
     * @return The issue message.
     */
    public String getIssueMessage() {
        String issueMessage = "";
        List<DomNode> issueMessageList = getMainPanel().getByXPath(issueMessageXpath);

        if(!issueMessageList.isEmpty()){
            issueMessage = issueMessageList.get(0).asText();

            //replacing "new line" and "tab" character at the start of the message
            issueMessage = issueMessage.replace("\t", "").replace("\n", "");
        }

        return issueMessage;
    }

    /**
     *
     * @return The main panel element of the html page.
     */
    private DomElement getMainPanel() {
        return sourceCodeViewPage.getElementById(mainPanelElementId);
    }

    /**
     * Just a test method to see if everything works as expected.
     * (Will be deleted later!)
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        WebClient webClient = new WebClient();
        HtmlPage page = webClient.getPage(
                "http://localhost:8081/job/pipeline-analysis-model/5/java/packageName.-423259349/fileName.-1473104080/source.b116f8a7-71d7-4890-a19b-c4eb2dd87947/#112");

        SourceCodeView sourceCodeView = new SourceCodeView(page);
        System.out.println(sourceCodeView.getFileName());
        System.out.println(sourceCodeView.getIssueSourceCodeLine());
        System.out.println(sourceCodeView.getIssueMessage());
        System.out.println(sourceCodeView.getSourceCode());
    }
}
