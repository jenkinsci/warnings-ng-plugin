package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page Object for the console log view (Maven Console Log parser).
 *
 * @author Veronika Zwickenpflug
 */
public class ConsoleLogView {

    private List<String> messages;
    private List<String> markedMessages;

    public ConsoleLogView(final HtmlPage page) {
        messages = new ArrayList<>();
        markedMessages = new ArrayList<>();
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
