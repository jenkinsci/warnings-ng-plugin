package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page object for the console log view.
 *
 * @author Veronika Zwickenpflug
 */
public class ConsoleLogView {
    private List<String> messages = new ArrayList<>();
    private List<String> markedMessages = new ArrayList<>();

    /**
     * Creates a new instance of {@link ConsoleLogView}.
     *
     * @param page
     *         the whole details HTML page
     */
    public ConsoleLogView(final HtmlPage page) {
        for (DomElement td : page.getElementsByTagName("td")) {
            messages.add(td.getTextContent());

            if (td.hasAttribute("style")) {
                markedMessages.add(td.getTextContent());
            }
        }
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getMarkedMessages() {
        return markedMessages;
    }
}
