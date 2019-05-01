package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Representation of the HTML Info Page of a job.
 */
public class InfoPage {

    private final HtmlPage infoWebPage;

    /**
     * Create a new InfoPage Object for the given web Page.
     * @param infoWebPage Loaded info web page.
     */
    public InfoPage(final HtmlPage infoWebPage) {
        this.infoWebPage = infoWebPage;
    }

    private List<String> getMessages(final String id) {
        List<String> result = new ArrayList<>();
        DomElement element = infoWebPage.getElementById(id);
        if (element != null) {
            element.getChildElements().forEach(domElement -> result.add(domElement.asText()));
        }
        return result;
    }

    public List<String> getInfoMessages() {
        return getMessages("info");
    }

    public List<String> getErrorMessages() {
        return getMessages("errors");
    }
}
