package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

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
}