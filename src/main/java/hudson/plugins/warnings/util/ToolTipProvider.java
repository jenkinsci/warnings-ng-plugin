package hudson.plugins.warnings.util;

/**
 * Provides tooltips for single or multiple items.
 *
 * @author Ulli Hafner
 */
public interface ToolTipProvider {
    /**
     * Returns the tooltip for several items.
     *
     * @param numberOfItems
     *            the number of items to display the tooltip for
     * @return the tooltip for several items
     */
    String getMultipleItemsTooltip(int numberOfItems);

    /**
     * Returns the tooltip for exactly one item.
     *
     * @return the tooltip for exactly one item
     */
    String getSingleItemTooltip();
}
