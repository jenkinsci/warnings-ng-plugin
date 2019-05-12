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
    private final DomElement overview;
    private final DomElement prev;
    private final DomElement next;
    private List<DomElement> items;
    private static final String CAROUSEL_ITEMS_XPATH = ".//div[contains(@class, 'carousel-item')]";

    public OverviewCarousel(final HtmlPage page) {
        this.overview = page.getElementById("overview-carousel");
        this.prev = page.getElementById("overview-carousel-prev");
        this.next = page.getElementById("overview-carousel-next");
        this.items = overview.getByXPath(CAROUSEL_ITEMS_XPATH);
    }

    /**
     * TODO: replace with Carousel Element once it's done.
     *
     */
    public DomElement getActiveItem(){
        for (DomElement item: items){
            if (item.getAttribute("class").contains("active"))
                return item;
        }
        return null;
    }

    public void prev() {
        try {
            prev.click();
            this.items = overview.getByXPath(CAROUSEL_ITEMS_XPATH);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void next() {
        try {
            next.click();
            this.items = overview.getByXPath(CAROUSEL_ITEMS_XPATH);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<DomElement> getItems() {
        return items;
    }
}
