package hudson.plugins.warnings.util;

import hudson.plugins.warnings.util.model.Priority;

import org.jfree.data.category.CategoryDataset;

/**
 * Renderer that provides direct access to the individual results of a build via
 * links. The renderer also displays tooltips for each selected build.
 * <ul>
 * <li>The tooltip is computed per column (i.e., per build) and row (i.e., priority) and shows the
 * number of annotations of the selected priority for this build.</li>
 * <li>The link is also computed per column and links to the results for this
 * build.</li>
 * </ul>
 *
 * @author Ulli Hafner
 */
public class PriorityAreaRenderer extends AbstractAreaRenderer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -4683951507836348304L;

    /**
     * Creates a new instance of {@link PriorityAreaRenderer}.
     *
     * @param url
     *            base URL of the graph links
     * @param toolTipProvider
     *            tooltip provider for the clickable map
     */
    public PriorityAreaRenderer(final String url, final ToolTipProvider toolTipProvider) {
        super(url, toolTipProvider);
    }

    /** {@inheritDoc} */
    @Override
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append(getToolTipBuilder().getTooltip(dataset.getValue(row, column).intValue()));
        tooltip.append(" ");
        if (row == 2) {
            tooltip.append(Messages.Trend_PriorityHigh());
        }
        else if (row == 1) {
            tooltip.append(Messages.Trend_PriorityNormal());
        }
        else {
            tooltip.append(Messages.Trend_PriorityLow());
        }
        return tooltip.toString();
    }

    /** {@inheritDoc} */
    @Override
    protected String getDetailUrl(final int row) {
        if (row == 0) {
            return Priority.LOW.name();
        }
        else if (row == 1) {
            return Priority.NORMAL.name();
        }
        else {
            return Priority.HIGH.name();
        }
    }
}