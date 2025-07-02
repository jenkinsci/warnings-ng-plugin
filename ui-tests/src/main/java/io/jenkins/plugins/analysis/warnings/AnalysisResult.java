package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.google.inject.Injector;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * {@link PageObject} representing the details page of the static analysis tool results.
 *
 * @author Stephan Plöderl
 * @author Ullrich Hafner
 * @author Mitja Oldenbourg
 */
public class AnalysisResult extends PageObject {
    private static final int MAX_ATTEMPTS = 5;
    private static final String TARGET_HREF = "data-bs-target";
    private static final String ACTIVE_TAB = "//div[contains(@class, 'tab-pane') and contains(@class, 'active')]//";
    private final String id;

    /**
     * Creates an instance of the page displaying the details of the issues for a specific tool.
     *
     * @param parent
     *         a finished build configured with a static analysis tool
     * @param id
     *         the type of the result page (e.g. simian, checkstyle, cpd, etc.)
     */
    public AnalysisResult(final Build parent, final String id) {
        super(parent, parent.url(id));

        this.id = id;
    }

    /**
     * Creates an instance of the page displaying the details of the issues. This constructor is used for injecting a
     * filtered instance of the page (e.g., by clicking on links which open a filtered instance of a AnalysisResult.
     *
     * @param injector
     *         the injector of the page
     * @param url
     *         the url of the page
     * @param id
     *         the id of the result page (e.g., simian or cpd)
     */
    @SuppressWarnings("unused") // Required to dynamically create a page object using reflection
    public AnalysisResult(final Injector injector, final URL url, final String id) {
        super(injector, url);

        this.id = id;
    }

    /**
     * Returns the active and visible tab that has the focus in the tab bar.
     *
     * @return the active tab
     */
    public Tab getActiveTab() {
        WebElement activeTab = find(By.xpath("//a[@role='tab' and contains(@class, 'active')]"));

        return Tab.valueWithHref(extractRelativeUrl(activeTab.getDomAttribute(TARGET_HREF)));
    }

    /**
     * Returns the list of available tabs. These tabs depend on the available properties of the set of shown issues.
     *
     * @return the available tabs
     */
    public Collection<Tab> getAvailableTabs() {
        return all(By.xpath("//a[@role='tab']")).stream()
                .map(tab -> tab.getDomAttribute(TARGET_HREF))
                .map(this::extractRelativeUrl)
                .map(Tab::valueWithHref)
                .collect(Collectors.toList());
    }

    private String extractRelativeUrl(final String absoluteUrl) {
        return "#" + StringUtils.substringAfterLast(absoluteUrl, "#");
    }

    /**
     * Returns the total number of issues. This method requires that one of the tabs is shown that shows the total
     * number of issues in the footer. I.e., the {@link Tab#ISSUES} and {@link Tab#BLAMES}.
     *
     * @return the total number of issues
     */
    public int getTotal() {
        return extractTotalFromFooter(1);
    }

    /**
     * Returns the total number of new issues. This method requires that one of the tabs is shown that shows the total
     * number of issues in the footer. I.e., the {@link Tab#ISSUES} and {@link Tab#BLAMES}.
     *
     * @return the total number of new issues
     */
    public int getTotalNew() {
        return extractTotalFromFooter(2);
    }

    private int extractTotalFromFooter(final int column) {
        String total = find(By.tagName("tfoot")).getText();
        return Integer.parseInt(total.split(" ", -1)[column]);
    }

    /**
     * Reloads the {@link PageObject}.
     */
    public void reload() {
        open();
    }

    /**
     * Opens the analysis details page and selects the specified tab.
     *
     * @param tab
     *         the tab that should be selected
     */
    public void openTab(final Tab tab) {
        open();

        WebElement tabElement = getElement(By.id("tab-details")).findElement(tab.getXpath());
        tabElement.click();
    }

    /**
     * Opens the analysis details page, selects the tab {@link Tab#ISSUES} and returns the {@link PageObject} of the
     * issues table.
     *
     * @return page object of the issues table.
     */
    public IssuesTable openIssuesTable() {
        WebElement issuesTab = selectTab(Tab.ISSUES);
        return new IssuesTable(issuesTab, this);
    }

    /**
     * Opens the analysis details page, selects the tab {@link Tab#ISSUES} and returns the {@link PageObject} of the
     * issues table.
     *
     * @return page object of the issues table.
     */
    public DryTable openDryTable() {
        WebElement issuesTab = selectTab(Tab.ISSUES);
        return new DryTable(issuesTab, this);
    }

    private WebElement selectTab(final Tab issues) {
        openTab(issues);

        return find(By.id(issues.contentId));
    }

    /**
     * Opens the analysis details page, selects the tab {@link Tab#CATEGORIES} and returns the {@link PageObject} of the
     * category table.
     *
     * @param tab
     *         the tab to open
     *
     * @return page object of the categories table.
     */
    public PropertyDetailsTable openPropertiesTable(final Tab tab) {
        WebElement table = selectTab(tab);
        return new PropertyDetailsTable(table, this, tab.property);
    }

    /**
     * Opens the analysis details page, selects the tab {@link Tab#BLAMES} and returns the {@link PageObject} of the
     * blame table.
     *
     * @return page object of the blame table.
     */
    public BlamesTable openBlamesTable() {
        openTab(Tab.BLAMES);

        WebElement blamesTab = find(By.id("blamesContent"));
        return new BlamesTable(blamesTab, this);
    }

    /**
     * Opens the analysis details page, selects the tab {@link Tab#FORENSICS} and returns the {@link PageObject} of the
     * forensics table.
     *
     * @return page object of the forensics table.
     */
    public ForensicsTable openForensicsTable() {
        openTab(Tab.FORENSICS);

        WebElement forensicsTab = find(By.id("forensicsContent"));
        return new ForensicsTable(forensicsTab, this);
    }

    /**
     * Opens a link on the page leading to another page.
     *
     * @param element
     *         the WebElement representing the link to be clicked
     * @param type
     *         the class of the PageObject which represents the page to which the link leads to
     * @param <T>
     *         actual type of the page object
     *
     * @return the instance of the PageObject to which the link leads to
     */
    public <T extends PageObject> T openLinkOnSite(final WebElement element, final Class<T> type) {
        String link = element.getDomAttribute("href");
        T retVal = newInstance(type, injector, url(link));
        element.click();
        return retVal;
    }

    /**
     * Returns the row length select-element of the table in the currently active tab.
     *
     * @return the select-element where the user can choose how many rows should be displayed
     */
    public Select getLengthSelectElementByActiveTab() {
        var element = find(By.xpath(ACTIVE_TAB + "select[@id[starts-with(., 'dt-length')]]"));
        return new Select(element);
    }

    /**
     * Return the information label of the table in the currently active tab.
     * This labels shows information about the current visible number of table elements.
     *
     * @return the element that shows information about the current visible number of table elements
     */
    public WebElement getInfoElementByActiveTab() {
        return find(By.xpath(ACTIVE_TAB + "div[@class='dt-info']"));
    }

    /**
     * Method for getting the pagination control of the table in the currently active tab.
     *
     * @return parent WebElement that contains the pagination buttons for a result table.
     */
    public WebElement getPaginateElementByActiveTab() {
        return find(By.xpath(ACTIVE_TAB + "div[@class='dt-paging paging_numbers']"));
    }

    /**
     * Returns the pagination buttons of the table in the currently active tab.
     *
     * @return the pagination buttons that select the currently visible portion of the issues
     */
    public List<WebElement> getPaginationButtons() {
        return getPaginateElementByActiveTab().findElements(By.cssSelector("ul li"));
    }

    /**
     * Returns the search input field of the table in the currently active tab.
     *
     * @return input-element where a user can filter the table by text
     */
    public WebElement getFilterInputElementByActiveTab() {
        return find(By.xpath(ACTIVE_TAB + "input[@type='search']"));
    }

    /**
     * Opens a link to a filtered version of this AnalysisResult by clicking on a link.
     *
     * @param element
     *         the WebElement representing the link to be clicked
     *
     * @return the instance of the filtered AnalysisResult
     */
    public AnalysisResult openFilterLinkOnSite(final WebElement element) {
        String link = element.getAttribute("href");
        AnalysisResult retVal = newInstance(AnalysisResult.class, injector, url(link), id);
        element.click();
        return retVal;
    }

    /**
     * Returns the TrendChart Carousel DOM Node.
     *
     * @return trendChart Carousel.
     */
    private WebElement getTrendChart() {
        return find(By.id("trend-carousel"));
    }

    /**
     * Clicks the next-button to cycle through the Trend Charts.
     */
    public void clickNextOnTrendCarousel() {
        WebElement trendChart = getTrendChart();
        WebElement activeChart = trendChart.findElement(By.className("active"));
        trendChart.findElement(By.className("carousel-control-next")).click();
        waitFor().until(() -> !activeChart.isDisplayed());
    }

    /**
     * Checks if the trendChart is visible on the Page.
     *
     * @param chartName
     *         id of the Chart we want to evaluate.
     *
     * @return boolean value, that describes the visibility of the Trendchart.
     */
    public boolean trendChartIsDisplayed(final String chartName) {
        WebElement trendChart = getTrendChart();
        return trendChart.findElement(By.id(chartName)).isDisplayed();
    }

    /**
     * Checks if the trendChart is visible on the Page.
     *
     * @param elementId
     *         id of the Chart we want to return.
     *
     * @return TrendChart as JSON String.
     */
    public String getTrendChartById(final String elementId) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            Object result = executeScript(String.format(
                    "delete(window.Array.prototype.toJSON) %n"
                            + "return JSON.stringify(echarts.getInstanceByDom(document.getElementById(\"%s\")).getOption())",
                    elementId));
            if (result != null) {
                return result.toString();
            }
            elasticSleep(1000);
        }
        throw new NoSuchElementException("Found no trend chart with ID '%s''" + elementId);
    }

    /**
     * Enum representing the possible tabs which can be opened in the {@link AnalysisResult} details view.
     */
    public enum Tab {
        TOOLS("origin"),
        MODULES("moduleName"),
        PACKAGES("packageName"),
        FOLDERS("folder"),
        FILES("fileName"),
        CATEGORIES("category"),
        TYPES("type"),
        ISSUES("issues"),
        BLAMES("blames"),
        FORENSICS("forensics");

        private final String contentId;
        private final String property;

        Tab(final String property) {
            this.property = property;
            contentId = property + "Content";
        }

        /**
         * Returns the selenium {@link By} selector to find the specific tab.
         *
         * @return the selenium filter rule
         */
        By getXpath() {
            return By.xpath("//a[@" + TARGET_HREF + "='#" + contentId + "']");
        }

        /**
         * Returns the enum element that has the specified href property.
         *
         * @param href
         *         the href to select the tab
         *
         * @return the tab
         * @throws NoSuchElementException
         *         if the tab could not be found
         */
        static Tab valueWithHref(final String href) {
            for (Tab tab : values()) {
                if (tab.contentId.equals(href.substring(1))) {
                    return tab;
                }
            }
            throw new NoSuchElementException("No such tab with href " + href);
        }
    }
}
