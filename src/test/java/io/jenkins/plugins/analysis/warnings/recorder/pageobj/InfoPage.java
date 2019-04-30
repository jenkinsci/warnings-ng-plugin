package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.umd.cs.findbugs.Project;

public class InfoPage {

    private final HtmlPage infoWebPage;

    public InfoPage(HtmlPage infoWebPage) {
        this.infoWebPage = infoWebPage;
    }

    private List<String> getMessages(String id) {
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
