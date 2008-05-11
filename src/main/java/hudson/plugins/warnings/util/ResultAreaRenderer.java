package hudson.plugins.warnings.util;


import org.jfree.data.category.CategoryDataset;

/**
 * Renderer that provides direct access to the individual results of a build via
 * links. The renderer also displays tooltips for each selected build.
 * <ul>
 * <li>The tooltip is computed per column (i.e., per build) and shows the total
 * number of annotations for this build.</li>
 * <li>The link is also computed per column and links to the results for this
 * build.</li>
 * </ul>
 *
 * @author Ulli Hafner
 */
public class ResultAreaRenderer extends AbstractAreaRenderer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -4683951507836348304L;

    /**
     * Creates a new instance of <code>ResultAreaRenderer</code>.
     *
     * @param url
     *            base URL of the graph links
     * @param toolTipProvider
     *            tooltip provider for the clickable map
     */
    public ResultAreaRenderer(final String url, final ToolTipProvider toolTipProvider) {
        super(url, toolTipProvider);
    }

    /** {@inheritDoc} */
    @Override
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
        int number = 0;
        for (int index = 0; index < dataset.getRowCount(); index++) {
            final Number value = dataset.getValue(index, column);
            if (value != null) {
                number += value.intValue();
            }
        }
        return getToolTipBuilder().getTooltip(number);
    }
}