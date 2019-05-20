package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.HashSet;
import java.util.NoSuchElementException;
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

    private TabType activeTabType;
    private final Set<TabType> tabs = new HashSet<>();
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
            TabType tabType = TabType.valueOfIgnoreCase(tabName);
            if (navElement.hasAttribute("aria-selected")) {
                activeTabType = tabType;
            }
            tabs.add(tabType);
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
    public Object select(final TabType tapType) {
        assertThat(tabs).contains(tapType);
        activeTabType = tapType;
        return retrieveContent(tapType);
    }

    private Object retrieveContent(final TabType tabType) {
        switch (tabType) {
            case TOOLS:
            case MODULES:
            case PACKAGES:
            case FOLDERS:
            case FILES:
            case CATEGORIES:
            case TYPES:
                return new PropertyTable(page, tabType.getProperty());
            case ISSUES:
                return new IssuesTable(page);
            case BLAMES:
                return null;
            // FIXME: return new Blame(page);
            default:
                throw new NoSuchElementException();
        }
    }

    /**
     * All types of details-tab which are available on current page.
     *
     * @return the set of tab-types of current details-tab
     */
    public Set<TabType> getTabTypes() {
        return tabs;
    }

    /**
     * The type of the currently active details-tab.
     *
     * @return activeTabType
     */
    public TabType getActiveTabType() {
        return activeTabType;
    }

    /**
     * Types for DetailsTab.
     */
    public enum TabType {

        TOOLS("origin"),
        MODULES("moduleName"),
        PACKAGES("packageName"),
        FOLDERS("folder"),
        FILES("fileName"),
        CATEGORIES("category"),
        TYPES("type"),
        ISSUES("issues"),
        BLAMES("scm");

        private String property;

        TabType(final String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }

        private static TabType valueOfIgnoreCase(final String tabName) {
            return valueOf(tabName.toUpperCase());
        }
    }
}