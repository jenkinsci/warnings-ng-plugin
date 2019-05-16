package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import net.sf.json.JSONObject;

/**
 * Page Object for the trend carousel in details views.
 */
public class DetailsViewTrendCarousel {

    private final WebClient webClient;
    private HtmlPage detailsViewWebPage;
    private HtmlAnchor carouselControlNext;
    private HtmlAnchor carouselControlPrev;
    private HtmlDivision carouselItemActive;
    private String carouselItemActiveId;

    /**
     * Creates the trend carousel PageObject for the details view web page. E.g. {buildNr}/java
     *
     * @param detailsViewWebPage
     *         The details view webpage to get the trend carousel from.
     * @param webClient
     *          The web client which handles the web request.
     */
    public DetailsViewTrendCarousel(final HtmlPage detailsViewWebPage, final WebClient webClient) {
        this.webClient = webClient;
        setupDetailsViewTrendCarousel(detailsViewWebPage);

        System.out.println(webClient.getBrowserVersion());
    }

    private void setupDetailsViewTrendCarousel(final HtmlPage page) {
        detailsViewWebPage = page;

        //webClient.waitForBackgroundJavaScript(20000);
        //JavaScriptJobManager manager = page.getEnclosingWindow().getJobManager();
        //while (manager.getJobCount() > 0) {
        //    try {
        //        Thread.sleep(1000);
        //    }
        //    catch (InterruptedException e) {
        //        e.printStackTrace();
        //    }
        //}

        carouselControlNext = (HtmlAnchor) detailsViewWebPage.getByXPath(
                "//a[contains(@class, 'carousel-control-next')]").get(0);
        carouselControlPrev = (HtmlAnchor) detailsViewWebPage.getByXPath(
                "//a[contains(@class, 'carousel-control-prev')]").get(0);
        setCarouselItemActive();
    }

    private void setCarouselItemActive() {
        carouselItemActive = (HtmlDivision) detailsViewWebPage.getByXPath(
                "//div[@id='trend-carousel']/div/div[contains(@class, 'carousel-item active')]").get(0);
        detailsViewWebPage.getByXPath(
                "//div[@id='trend-carousel']/div/div[contains(@class, 'carousel-item active')]").forEach(e -> System.out
                .println(((HtmlDivision) e).asXml()));

        //System.out.println(carouselItemActive.asXml());
        carouselItemActiveId = ((HtmlDivision) carouselItemActive.getChildNodes().get(0)).getId();

        System.out.println(carouselItemActiveId);
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
            //TO DO setter inside or outside of try?
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Click on the next button to show previous carousel item.
     *
     * @return if click was successful
     */
    public boolean clickCarouselControlPrev() {
        try {
            HtmlPage newPage = carouselControlPrev.click();
            setupDetailsViewTrendCarousel(newPage);
            //TO DO setter inside or outside of try?
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
}