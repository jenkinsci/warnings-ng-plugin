package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import net.sf.json.JSONObject;

/**
 * Page object for a Bootstrap Carousel.
 *
 * @author Artem Polovyi
 * @author Andreas Neumeier
 * @author Tobias Redl
 * @author Ullrich Hafner
 */
class CarouselPageObject extends PageObject {
    private final HtmlAnchor nextButton;
    private final HtmlAnchor previousButton;
    private final String carouselId;

    private String activeId;

    CarouselPageObject(final HtmlPage page, final String id) {
        super(page);

        carouselId = id;

        nextButton = getButton("next");
        previousButton = getButton("prev");
        activeId = getActiveCarouselItemId();
    }

    private HtmlAnchor getButton(final String name) {
        return (HtmlAnchor) getPage().getByXPath(String.format(
                "//div[@id='%s-carousel']/a[contains(@class, 'carousel-control-%s')]", carouselId, name)).get(0);
    }

    private String getActiveCarouselItemId() {
        HtmlDivision carouselItemActive = (HtmlDivision) getPage().getByXPath(String.format(
                "//div[@id='%s-carousel']/div/div[contains(@class, 'carousel-item active')]", carouselId)).get(0);
        return ((HtmlDivision) carouselItemActive.getChildNodes().get(0)).getId();
    }

    @SuppressWarnings("PMD.SystemPrintln")
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
     * Clicks multiple times on the next button to show the next chart.
     *
     * @param count
     *         clicks this many times
     *
     * @return the active chart
     */
    public JSONObject next(final int count) {
        JSONObject result = new JSONObject();
        for (int i = 0; i < count; i++) {
            result = select(nextButton);
        }
        return result;
    }

    /**
     * Clicks on the previous button to show the previous chart.
     *
     * @return the active chart
     */
    public JSONObject previous() {
        return select(previousButton);
    }

    /**
     * Clicks multiple times on the previous button to show the previous chart.
     *
     * @param count
     *         clicks this many times
     *
     * @return the active chart
     */
    public JSONObject previous(final int count) {
        JSONObject result = new JSONObject();
        for (int i = 0; i < count; i++) {
            result = select(previousButton);
        }
        return result;
    }

    private JSONObject select(final HtmlAnchor anchor) {
        clickOnElement(anchor);
        waitForAjaxCall(activeId);
        activeId = getActiveCarouselItemId();
        return getActiveChartModel();
    }

    /**
     * Get the json object of the current trend carousel item.
     *
     * @return json object
     */
    public JSONObject getActiveChartModel() {
        return new DetailsViewCharts(getPage()).getChartModel(activeId);
    }

    String getActiveId() {
        return activeId;
    }

    List<HtmlDivision> getDivs() {
        return getPage().getByXPath(String.format(
                "//div[@id='%s-carousel']/div/div[contains(@class, 'carousel-item')]/div", carouselId));
    }
}
