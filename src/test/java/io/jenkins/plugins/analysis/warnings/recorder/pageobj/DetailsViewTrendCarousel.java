package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import net.sf.json.JSONObject;

/**
 * Page Object for the trend carousel in details views.
 */
public class DetailsViewTrendCarousel {
    private HtmlPage detailsViewWebPage;
    private HtmlAnchor carouselControlNext;
    private HtmlAnchor carouselControlPrev;
    private String carouselItemActiveId;

    /**
     * Creates the trend carousel PageObject for the details view web page. E.g. {buildNr}/java
     *
     * @param detailsViewWebPage
     *         The details view web page to get the trend carousel from.
     */
    public DetailsViewTrendCarousel(final HtmlPage detailsViewWebPage) {
        setupDetailsViewTrendCarousel(detailsViewWebPage);
    }

    private void setupDetailsViewTrendCarousel(final HtmlPage page) {
        detailsViewWebPage = page;
        carouselControlNext = (HtmlAnchor) detailsViewWebPage.getByXPath(
                "//div[@id='trend-carousel']/a[contains(@class, 'carousel-control-next')]").get(0);
        carouselControlPrev = (HtmlAnchor) detailsViewWebPage.getByXPath(
                "//div[@id='trend-carousel']/a[contains(@class, 'carousel-control-prev')]").get(0);
        setCarouselItemActive();
    }

    private void setCarouselItemActive() {
        HtmlDivision carouselItemActive = (HtmlDivision) detailsViewWebPage.getByXPath(
                "//div[@id='trend-carousel']/div/div[contains(@class, 'carousel-item active')]").get(0);
        carouselItemActiveId = ((HtmlDivision) carouselItemActive.getChildNodes().get(0)).getId();
    }

    /**
     * Click on the next button to show next carousel item.
     *
     * @return if click was successful
     */
    public boolean clickCarouselControlNext() {
        try {
            HtmlPage newPage = carouselControlNext.click();
            setupDetailsViewTrendCarousel(newPage);
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Click on the previous button to show previous carousel item.
     *
     * @return if click was successful
     */
    public boolean clickCarouselControlPrev() {
        try {
            HtmlPage newPage = carouselControlPrev.click();
            setupDetailsViewTrendCarousel(newPage);
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Get the json object of the current trend carousel item.
     *
     * @return json object
     */
    public JSONObject getCarouselItemActive() {
        return new DetailsViewCharts(detailsViewWebPage).getChartModel(carouselItemActiveId);
    }

    /**
     * Get the id of the chart which is currently active.
     *
     * @return Name of the active chart
     */
    public String getCarouselItemActiveId() {
        return carouselItemActiveId;
    }
}