package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page Object for the Overview Carousel.
 *
 * @author Artem Polovyi
 */
public class OverviewCarousel {
    private final DomElement previous;
    private final DomElement next;
    private final List<DomElement> items;
    private final DomElement overview;

    public OverviewCarousel(final HtmlPage page) {
        this.overview = page.getElementById("overview-carousel");
        this.previous = (DomElement) overview.getByXPath(".//a[contains(@class, 'carousel-control-prev')]").get(0);
        this.next = (DomElement) overview.getByXPath(".//a[contains(@class, 'carousel-control-next')]").get(0);
        this.items = overview.getByXPath(".//div[contains(@class, 'carousel-item')]");
    }

    public DomElement getActiveItem(){
        return (DomElement) overview.getByXPath(".//div[contains(@class, 'active')]").get(0);
    }

    public DomElement previous() {
        try {
            previous.click();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return getActiveItem();
    }

    public DomElement next() {
        try {
            next.click();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return getActiveItem();
    }

    public List<DomElement> getItems() {
        return items;
    }
}
