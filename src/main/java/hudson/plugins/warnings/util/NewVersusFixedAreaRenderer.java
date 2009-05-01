package hudson.plugins.warnings.util;

import org.jfree.data.category.CategoryDataset;

/**
 * Renderer that provides direct access to the individual results of a build via
 * links. The renderer also displays tooltips for each selected build.
 * <ul>
 * <li>The tooltip is computed per column (i.e., per build) and row (i.e.,
 * number of warnings) and shows the number of new or fixed annotations for this
 * build.</li>
 * <li>The link is also computed per column and links to the results for this
 * build.</li>
 * </ul>
 *
 * @author Ulli Hafner
 */
public class NewVersusFixedAreaRenderer extends AbstractBoxRenderer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -2356989151327991247L;

    /**
     * Creates a new instance of {@link NewVersusFixedAreaRenderer}.
     *
     * @param url
     *            base URL of the graph links
     * @param toolTipProvider
     *            tooltip provider for the clickable map
     */
    public NewVersusFixedAreaRenderer(final String url, final ToolTipProvider toolTipProvider) {
        super(url, toolTipProvider);
    }

    /** {@inheritDoc} */
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append(getToolTipBuilder().getTooltip(dataset.getValue(row, column).intValue()));
        tooltip.append(" ");
        if (row == 1) {
            tooltip.append(Messages.Trend_Fixed());
        }
        else {
            tooltip.append(Messages.Trend_New());
        }
        return tooltip.toString();
    }

    /** {@inheritDoc} */
    @Override
    protected String getDetailUrl(final int row) {
        if (row == 1) {
            return "fixed";
        }
        else {
            return "new";
        }
    }
}