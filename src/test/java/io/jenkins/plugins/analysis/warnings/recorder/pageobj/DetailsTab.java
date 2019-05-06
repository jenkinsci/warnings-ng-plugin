package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.HashMap;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page Object for the details-tab that shows tables containing details of the issues of a build.
 *
 * @author Nils Engelbrecht
 */
public class DetailsTab {

    private final HashMap<String, Object> tabs = new HashMap<>();

    public DetailsTab(final HtmlPage page) {
        DomElement detailsNav = page.getElementById("tab-details");
        DomNodeList<HtmlElement> navList = detailsNav.getElementsByTagName("a");
        for (HtmlElement navElement : navList) {
            tabs.put(navElement.getFirstChild().getTextContent(), null);
        }
    }

    public HashMap<String, Object> getTabs() {
        return tabs;
    }
}