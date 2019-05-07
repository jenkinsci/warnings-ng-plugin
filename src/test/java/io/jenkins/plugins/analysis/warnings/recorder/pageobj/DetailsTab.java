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

    private String activeTab;
    private final HashMap<String, Object> tabs = new HashMap<>();

    /**
     * Parse information from the given page and creates a new instance of {@link DetailsTab}.
     *
     * @param page
     *         the whole details HTML page
     */
    public DetailsTab(final HtmlPage page) {
        DomElement detailsNav = page.getElementById("tab-details");
        DomNodeList<HtmlElement> navList = detailsNav.getElementsByTagName("a");
        for (HtmlElement navElement : navList) {
            String tabName = navElement.getFirstChild().getTextContent();
            if (navElement.hasAttribute("aria-selected")) {
                boolean isActive = Boolean.parseBoolean(navElement.getAttribute("aria-selected"));
                activeTab = tabName;
            }
            tabs.put(tabName, retrieveContent(page, tabName));
        }
    }

    private Object retrieveContent(final HtmlPage page, final String tabName) {
        switch (tabName) {
            case "Issues":
                return new IssuesTable(page); // TODO fix IssueTable#getBodies();
            default:
                return null;
        }
    }

    /**
     * Returns the tabs and corresponding content of navigation bar (tab header).
     *
     * @return the hashmap with title as key and content as value
     */
    public HashMap<String, Object> getTabs() {
        return tabs;
    }

    public String getActiveTab() {
        return activeTab;
    }

    public boolean tabIsActive(final String tabName) {
        return activeTab.equals(tabName);
    }
}