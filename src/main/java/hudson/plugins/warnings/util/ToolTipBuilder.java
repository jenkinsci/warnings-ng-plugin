package hudson.plugins.warnings.util;

/**
 * Builds tooltips for items.
 *
 * @author Ulli Hafner
 */
public class ToolTipBuilder {
    /** Delegate to get the actual tooltips. */
    private final ToolTipProvider provider;

    /**
     * Creates a new instance of <code>ToolTipBuilder</code>.
     *
     * @param provider
     *            the tool tip provider to use
     */
    public ToolTipBuilder(final ToolTipProvider provider) {
        this.provider = provider;
    }

    /**
     * Returns the tooltip for the specified number of items.
     *
     * @param numberOfItems
     *            the number of items to display the tooltip for
     * @return the tooltip for the specified items
     */
    public final String getTooltip(final int numberOfItems) {
        if (numberOfItems == 1) {
            return provider.getSingleItemTooltip();
        }
        else {
            return provider.getMultipleItemsTooltip(numberOfItems);
        }
    }
}

