package hudson.plugins.warnings.util;

import hudson.util.StackedAreaRenderer2;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * {@link StackedAreaRenderer} that provides direct access to the individual
 * results of a build via links. This renderer does not render tooltips, these
 * need to be defined in sub-classes. You can provide different URLs based on
 * the selected row by overwriting method {@link #getDetailUrl(int)}.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractAreaRenderer extends StackedAreaRenderer2 {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 1440842055316682192L;
    /** Base URL of the graph links. */
    private final String url;
    /** Tooltip provider for the clickable map. */
    private final ToolTipBuilder toolTipBuilder;

    /**
     * Creates a new instance of <code>AbstractAreaRenderer</code>.
     *
     * @param url
     *            base URL of the graph links
     * @param toolTipProvider
     *            tooltip provider for the clickable map
     */
    public AbstractAreaRenderer(final String url, final ToolTipProvider toolTipProvider) {
        super();
        toolTipBuilder = new ToolTipBuilder(toolTipProvider);
        this.url = "/" + url + "/";
    }

    /** {@inheritDoc} */
    @Override
    public final String generateURL(final CategoryDataset dataset, final int row, final int column) {
        return getLabel(dataset, column).build.getNumber() + url + getDetailUrl(row);
    }

    /**
     * Returns a relative URL based on the specified row that will be appended
     * to the base URL. This default implementation returns an empty string,
     * indicating that there is no detail URL based on the selected row. If not
     * empty this URL should start with an slash.
     *
     * @param row
     *            the selected row
     * @return a relative URL based on the specified row.
     */
    protected String getDetailUrl(final int row) {
        return StringUtils.EMPTY;
    }

    /**
     * Gets the tool tip builder.
     *
     * @return the tool tip builder
     */
    public final ToolTipBuilder getToolTipBuilder() {
        return toolTipBuilder;
    }

    /**
     * Returns the Hudson build label at the specified column.
     *
     * @param dataset
     *            data set of values
     * @param column
     *            the column
     * @return the label of the column
     */
    private NumberOnlyBuildLabel getLabel(final CategoryDataset dataset, final int column) {
        return (NumberOnlyBuildLabel)dataset.getColumnKey(column);
    }
}
