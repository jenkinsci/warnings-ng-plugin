package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.hm.hafner.util.NoSuchElementException;

/**
 * Page Object for the Overview Carousel.
 *
 * @author Artem Polovyi
 */
public class OverviewCarousel {
    private final DomElement overviewCarousel;
    private final HtmlAnchor previous;
    private final HtmlAnchor next;
    private DomElement activeItem;

    private static final String CAROUSEL_ITEMS_XPATH = ".//div[contains(@class, 'carousel-item')]";
    private static final String CAROUSEL_NEXT_XPATH = ".//a[contains(@class, 'carousel-control-next')]";
    private static final String CAROUSEL_PREVIOUS_XPATH = ".//a[contains(@class, 'carousel-control-prev')]";

    /**
     * Creates a new instance of {@link OverviewCarousel}.
     *
     * @param page
     *         the whole HTML page.
     */
    public OverviewCarousel(final HtmlPage page) {
        this.overviewCarousel = page.getElementById("overview-carousel");
        this.next = (HtmlAnchor) overviewCarousel.getByXPath(CAROUSEL_NEXT_XPATH).get(0);
        this.previous = (HtmlAnchor) overviewCarousel.getByXPath(CAROUSEL_PREVIOUS_XPATH).get(0);
        this.activeItem = findActiveItem();
    }

    /**
     * TODO: replace DomElement with CarouselElement once it's done. Helper-method to find an active item in overview
     * carousel.
     *
     * @return active overview carousel item.
     */
    private DomElement findActiveItem() {
        for (DomElement item : findAllCarouselItems()) {
            if (item.getAttribute("class").contains("active")) {
                return item;
            }
        }
        throw new NoSuchElementException("No active item in overview carousel found");
    }

    /**
     * TODO: replace DomElement with CarouselElement once it's done. Helper-method to find all items in overview
     * carousel.
     *
     * @return List of all overview carousel items.
     */
    private List<DomElement> findAllCarouselItems() {
        if (overviewCarousel == null) {
            throw new AssertionError("No overview carousel found");
        }
        List<DomElement> itemList = overviewCarousel.getByXPath(CAROUSEL_ITEMS_XPATH);
        if (itemList.size() == 0) {
            throw new AssertionError("No items in overview carousel found");
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
     * Clicks the button to switch to the next element of overview carousel and sets new active item in overview
     * carousel.
     *
     * @return an updated {@link OverviewCarousel} object with new active item.
     */
    public OverviewCarousel clickNext() {
        if (next == null) {
            throw new AssertionError("No next element link found");
        }
        clickOnButton(next);
        this.activeItem = findActiveItem();
        return this;
    }

    /**
     * Clicks the button to switch to the previous element of overview carousel and sets new active item in overview
     * carousel.
     *
     * @return an updated {@link OverviewCarousel} object with new active item.
     */
    public OverviewCarousel clickPrevious() {
        if (previous == null) {
            throw new AssertionError("No previous element link found");
        }
        clickOnButton(previous);
        this.activeItem = findActiveItem();
        return this;
    }

    /**
     * TODO: replace with Carousel Element once it's done. Returns the active item of carousel overview.
     *
     * @return active item.
     */
    public DomElement getActiveItem() {
        return activeItem;
    }
}
