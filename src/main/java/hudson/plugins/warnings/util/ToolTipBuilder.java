package hudson.plugins.warnings.util;

import org.jfree.data.category.CategoryDataset;

/**
 * Builds tooltips for items.
 *
 * @author Ulli Hafner
 */
public abstract class ToolTipBuilder implements SerializableToolTipGenerator {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 881869231153090533L;
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

    /** {@inheritDoc} */
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append(provider.getTooltip(dataset.getValue(row, column).intValue()));
        tooltip.append(" ");
        tooltip.append(getShortDescription(row));

        return tooltip.toString();
    }

    /**
     * Returns a short description for of the selected row. This text is
     * appended to the number of elements message.
     *
     * @param row
     *            the selected row
     * @return a short description
     */
    protected abstract String getShortDescription(int row);
}

