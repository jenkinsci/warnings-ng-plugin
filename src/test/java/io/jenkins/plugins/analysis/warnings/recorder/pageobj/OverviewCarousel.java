package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.hm.hafner.util.NoSuchElementException;

import net.sf.json.JSONObject;

/**
 * Page Object for the Overview Carousel.
 *
 * @author Artem Polovyi
 */
public class OverviewCarousel extends PageObject {
    private final HtmlAnchor nextButton;
    private final HtmlAnchor previousButton;

    private String activeId;

    /**
     * Creates the trend carousel PageObject for the details view web page. E.g. {buildNr}/java
     *
     * @param detailsViewWebPage
     *         The details view web page to get the trend carousel from.
     */
    public OverviewCarousel(final HtmlPage detailsViewWebPage) {
        super(detailsViewWebPage);

        nextButton = getButton("next");
        previousButton = getButton("prev");
        activeId = getActiveCarouselItemId();
    }

    private HtmlAnchor getButton(final String name) {
        return (HtmlAnchor) getPage().getByXPath(String.format(
                "//div[@id='overview-carousel']/a[contains(@class, 'carousel-control-%s')]", name)).get(0);
    }

    private String getActiveCarouselItemId() {
        HtmlDivision carouselItemActive = (HtmlDivision) getPage().getByXPath(
                "//div[@id='overview-carousel']/div/div[contains(@class, 'carousel-item active')]").get(0);
        return ((HtmlDivision) carouselItemActive.getChildNodes().get(0)).getId();
    }

    private void waitForAjaxCall(final String oldActive) {
        while (oldActive.equals(getActiveCarouselItemId())) {
            System.out.println("Waiting for Ajax call to finish carousel animation...");
            getPage().getEnclosingWindow().getJobManager().waitForJobs(1000);
        }
    }

    /**
     * Clicks on the next button to show the next chart.
     *
     * @return the active chart
     */
    public JSONObject next() {
        return select(nextButton);
    }

    /**
     * Clicks on the previous button to show the next chart.
     *
     * @return the active chart
     */
    public JSONObject previous() {
        return select(previousButton);
    }

    private JSONObject select(final HtmlAnchor anchor) {
        String oldActive = activeId;
        clickOnElement(anchor);
        waitForAjaxCall(oldActive);
        activeId = getActiveCarouselItemId();
        return getActive();
    }

    /**
     * Get the json object of the current trend carousel item.
     *
     * @return json object
     */
    public JSONObject getActive() {
        return new DetailsViewCharts(getPage()).getChartModel(activeId);
    }

    /**
     * Returns the type of the currently visible chart.
     *
     * @return the chart type that is currently visible
     */
    public PieChartType getActiveChartType() {
        return PieChartType.fromId(activeId);
    }

    /**
     * Returns all chart types that are available in the carousel.
     *
     * @return the available set of chart types
     */
    public SortedSet<PieChartType> getChartTypes() {
        return getDivs("overview").stream()
                .map(DomElement::getId)
                .map(PieChartType::fromId)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private List<HtmlDivision> getDivs(final String id) {
        return getPage().getByXPath(String.format(
                "//div[@id='%s-carousel']/div/div[contains(@class, 'carousel-item')]/div", id));
    }

    /**
     * Defines the supported chart types.
     */
    public enum PieChartType {
        SEVERITIES, TREND;

        static PieChartType fromId(final String domId) {
            for (PieChartType type : values()) {
                if (convertDomIdToName(domId).equals(type.name())) {
                    return type;
                }
            }
            throw new NoSuchElementException("No such chart type found with div id '%s'", domId);
        }

        private static String convertDomIdToName(final String domId) {
            return domId.replaceAll("-chart", StringUtils.EMPTY)
                    .replace("-", "_")
                    .toUpperCase(Locale.ENGLISH);
        }
    }
}
