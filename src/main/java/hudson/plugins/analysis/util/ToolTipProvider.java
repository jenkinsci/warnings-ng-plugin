package hudson.plugins.analysis.util;

/**
 * Provides tooltips for single or multiple items.
 *
 * @author Ulli Hafner
 */
public interface ToolTipProvider {
    /**
     * Returns the tooltip for the specified number of items.
     *
     * @param numberOfItems
     *            the number of items to display the tooltip for
     * @return the tooltip for the specified items
     */
    String getTooltip(final int numberOfItems);
}
