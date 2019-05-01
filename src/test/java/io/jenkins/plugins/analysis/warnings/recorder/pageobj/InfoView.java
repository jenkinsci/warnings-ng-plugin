package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.util.Streams;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class InfoView {
    private final HtmlPage infoPage;

    /**
     * Create a new InfoPage Object for the given web Page.
     *
     * @param infoPage
     *         Loaded info web page.
     */
    public InfoView(final HtmlPage infoPage) {
        this.infoPage = infoPage;
    }

    private List<String> getMessages(final String id) {
        DomElement element = infoPage.getElementById(id);
        if (element != null) {
            return Streams.stream(element.getChildElements())
                    .map(DomNode::asText)
                    .collect(Collectors.toList());
        }
        else {
            return new ArrayList<>();
        }
    }

    public List<String> getInfoMessages() {
        return getMessages("info");
    }

    public List<String> getErrorMessages() {
        return getMessages("errors");
    }
}
