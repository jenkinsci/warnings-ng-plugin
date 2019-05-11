package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import static org.assertj.core.api.Assertions.*;

/**
 * Page Object for the details-tab that shows tables containing details of the issues of a build.
 *
 * @author Nils Engelbrecht
 */
public class DetailsTab {

    private DetailsTabType activeTabType;
    private final HashMap<DetailsTabType, Object> tabs = new HashMap<>();
    private HtmlPage page;

    /**
     * Parse information from the given page and creates a new instance of {@link DetailsTab}.
     *
     * @param page
     *         the whole details HTML page
     */
    public DetailsTab(final HtmlPage page) {
        this.page = page;
        DomElement detailsNav = page.getElementById("tab-details");
        DomNodeList<HtmlElement> navList = detailsNav.getElementsByTagName("a");
        for (HtmlElement navElement : navList) {
            String tabName = navElement.getFirstChild().getTextContent();
            DetailsTabType tabType = DetailsTabType.valueOfIgnoreCase(tabName);
            if (navElement.hasAttribute("aria-selected")) {
                activeTabType = tabType;
            }
            tabs.put(tabType, null);
        }
    }

    /**
     * Returns the page-object of the currently active details-tab.
     *
     * @return the page-object of the active tab
     */
    public Object getActive() {
        return select(activeTabType);
    }

    /**
     * Selects a specific tab and returns the corresponding page-object.
     *
     * @param tapType
     *         tab to be selected.
     *
     * @return the corresponding page-object
     */
    public Object select(final DetailsTabType tapType) {
        assertThat(tabs).containsKey(tapType);

        Object container = tabs.get(tapType);
        if (Objects.isNull(container)) {
            Object retrievedContent = retrieveContent(tapType);
            tabs.put(tapType, retrievedContent);
            container = retrievedContent;
        }
        activeTabType = tapType;
        return container;
    }

    private Object retrieveContent(final DetailsTabType tabType) {
        switch (tabType) {
            case ISSUES:
                return new IssuesTable(page);
            case FOLDERS:
            case FILES:
                return null;
            default:
                return null;
        }
    }

    /**
     * All types of details-tab which are available on current page.
     *
     * @return the set of tab-types of current details-tab
     */
    public Set<DetailsTabType> getTabTypes() {
        return tabs.keySet();
    }

    /**
     * The type of the currently active details-tab.
     *
     * @return activeTabType
     */
    public DetailsTabType getActiveTabType() {
        return activeTabType;
    }

    /**
     * Types for DetailsTab.
     */
    public enum DetailsTabType {
        FILES, FOLDERS, ISSUES;

        private static DetailsTabType valueOfIgnoreCase(final String tabName) {
            return valueOf(tabName.toUpperCase());
        }
    }
}