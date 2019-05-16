package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.hm.hafner.util.NoSuchElementException;

/**
 * Page Object for the Overview Carousel.
 *
 * @author Artem Polovyi
 */
public class OverviewCarousel {
    private final DomElement overviewCarousel;
    private final DomElement previous;
    private final DomElement next;
    private List<DomElement> items;

    private static final String CAROUSEL_ITEMS_XPATH = ".//div[contains(@class, 'carousel-item')]";
    private static final String CAROUSEL_NEXT_XPATH = ".//a[contains(@class, 'carousel-control-next')]";
    private static final String CAROUSEL_PREVIOUS_XPATH = ".//a[contains(@class, 'carousel-control-prev')]";

    /**
     * Creates a new instance of {@link OverviewCarousel}.
     *
     * @param page
     *          the whole HTML page.
     */
    public OverviewCarousel(final HtmlPage page) {
        this.overviewCarousel = page.getElementById("overview-carousel");
        this.next = (DomElement) overviewCarousel.getByXPath(CAROUSEL_NEXT_XPATH).get(0);
        this.previous = (DomElement) overviewCarousel.getByXPath(CAROUSEL_PREVIOUS_XPATH).get(0);
        this.items = retrieveCarouselItems();
    }

    /**
     * Helper-method to retrieve overview carousel items.
     *
     * @return List of overview carousel items.
     */
    private List<DomElement> retrieveCarouselItems() {
        if (overviewCarousel == null) {
            throw new AssertionError("No overview carousel found");
        }
        List<DomElement> itemList = overviewCarousel.getByXPath(CAROUSEL_ITEMS_XPATH);
        if (itemList.size() == 0) {
            throw new AssertionError("No overview carousel items found");
        }
        return itemList;
    }

    /**
     * Helper-method for clicking on a button.
     *
     * @param element
     *         a {@link DomElement} which will trigger the carousel element switch .
     */
    private void clickOnButton(final DomElement element) {
        try {
            element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Clicks the button to switch to the next element of overview carousel and sets new overview carousel items.
     */
    public void clickNext() {
        if (next == null) {
            throw new AssertionError("No next element link found");
        }
        clickOnButton(next);
        this.items = retrieveCarouselItems();
    }

    /**
     * Clicks the button to switch to the previous element of overview carousel and sets new overview carousel items.
     */
    public void clickPrevious() {
        if (previous == null) {
            throw new AssertionError("No previous element link found");
        }
        clickOnButton(previous);
        this.items = retrieveCarouselItems();
    }

    /**
     * TODO: replace with Carousel Element once it's done.
     *
     * @return active overview carousel item.
     */
    public DomElement getActiveItem() {
        for (DomElement item : items) {
            if (item.getAttribute("class").contains("active")) {
                return item;
            }
        }
        throw new NoSuchElementException("No active element in overview carousel found");
    }

    /**
     * Returns the items of carousel overview.
     *
     * @return all items.
     */
    public List<DomElement> getItems() {
        return items;
    }
}
