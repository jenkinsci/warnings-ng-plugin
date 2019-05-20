package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.hm.hafner.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

/**
 * Page Object for the details-tab that shows tables containing details of the issues of a build.
 *
 * @author Nils Engelbrecht
 */
public class DetailsTab {
    private TabType activeTabType;
    private final SortedSet<TabType> tabs = new TreeSet<>();
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
            assertThat(navElement).isInstanceOf(HtmlAnchor.class);
            TabType tabType = TabType.valueOfReference(((HtmlAnchor) navElement).getHrefAttribute());
            if (navElement.hasAttribute("aria-selected")) {
                activeTabType = tabType;
            }
            tabs.add(tabType);
        }
    }

    /**
     * Returns the page-object of the currently active details-tab.
     *
     * @param <T>
     *         the actual type of the page-object
     *
     * @return the page-object of the active tab
     */
    public <T> T getActive() {
        return select(activeTabType);
    }

    /**
     * Selects a specific tab and returns the corresponding page-object.
     *
     * @param tapType
     *         tab to be selected.
     * @param <T>
     *         the actual type of the page-object
     *
     * @return the corresponding page-object
     */
    @SuppressWarnings("unchecked") // makes tests more readable
    public <T> T select(final TabType tapType) {
        assertThat(tabs).contains(tapType);
        activeTabType = tapType;

        return (T) retrieveContent(tapType);
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
                return new SourceControlTable(page);
        }
        throw new NoSuchElementException("No page object registered for %s", tabType);
    }

    /**
     * Returns all types of details-tab which are available on current page. The order of the elements is the same as
     * in the UI.
     *
     * @return the set of tab-types of current details-tab
     */
    public SortedSet<TabType> getTabTypes() {
        return tabs;
    }

    /**
     * Returns the type of the currently active details-tab.
     *
     * @return currently active details-tab
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

        static TabType valueOfReference(final String hrefAttribute) {
            String actualValue = hrefAttribute.substring(1).replace("Content", StringUtils.EMPTY);
            for (TabType type : values()) {
                if (type.getProperty().equals(actualValue)) {
                    return type;
                }
            }
            throw new NoSuchElementException("No tab found with reference %s", hrefAttribute);
        }

        public String getProperty() {
            return property;
        }
    }
}