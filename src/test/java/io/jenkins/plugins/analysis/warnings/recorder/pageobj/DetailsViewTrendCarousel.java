package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import net.sf.json.JSONObject;

/**
 * Page Object for the trend carousel in details views.
 */
public class DetailsViewTrendCarousel {

    private final HtmlPage detailsViewWebPage;

    /**
     * Creates the trend carousel PageObject for the details view web page. E.g. {buildNr}/java
     *
     * @param detailsViewWebPage
     *         The details view webpage to get the trend carousel from.
     */
    public DetailsViewTrendCarousel(final HtmlPage detailsViewWebPage) {
        this.detailsViewWebPage = detailsViewWebPage;
    }

    public void nextCarouselChart() {
        final HtmlDivision nextItemButton = detailsViewWebPage.getHtmlElementById("carousel-control-next");
        final HtmlDivision trendCarousel = detailsViewWebPage.getHtmlElementById("trend-carousel");
        final HtmlDivision activeItem = (HtmlDivision) trendCarousel.getByXPath("/div[contains(@class, 'active')]").get(0);
        final String activeItemId = activeItem.getChildNodes().get(0).getNodeName();
        System.out.println(activeItemId);

        //final HtmlDivision acticeItem = tendCarousel.getByXPath("/div[class='carousel-item active']").get(0);

    }

    public void previouseCarouselChart() {


    }

    public JSONObject getCarouselChart() {

    }

    public static void main(String[] args) {

    }
}