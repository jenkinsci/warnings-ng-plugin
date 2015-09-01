package hudson.plugins.analysis.util;

import org.apache.commons.lang.StringUtils;
import org.jfree.data.category.CategoryDataset;

import hudson.util.ChartUtil.NumberOnlyBuildLabel;

/**
 * Builds a URL for the links in a clickable map. The URL is composed of the
 * build number, a slash, the plug-in name, and an optional detail URL.
 *
 * @author Ulli Hafner
 */
public class CategoryUrlBuilder implements SerializableUrlGenerator {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -3383164939484624157L;
    /** The plug-in name. */
    private final String pluginName;
    /** The root URL that is used as prefix. */
    private final String rootUrl;

    /**
     * Creates a new instance of {@link CategoryUrlBuilder}.
     *
     * @param rootUrl
     *            root URL that is used as prefix
     * @param pluginName
     *            the name of the plug-in
     */
    public CategoryUrlBuilder(final String rootUrl, final String pluginName) {
        this.rootUrl = rootUrl;
        if (isBlank(pluginName)) {
            this.pluginName = StringUtils.EMPTY;
        }
        else {
            this.pluginName = "/" + pluginName + "Result/";
        }
    }

    @Override
    public String generateURL(final CategoryDataset dataset, final int row, final int column) {
        String prefix = rootUrl + getLabel(dataset, column).getRun().getNumber();
        if (isBlank(pluginName)) {
            return prefix;
        }
        else {
            return prefix + pluginName + getDetailUrl(row);
        }
    }

    private boolean isBlank(final String value) {
        return StringUtils.isBlank(value);
    }

    /**
     * Returns the root URL.
     *
     * @return the root URL
     */
    public String getRootUrl() {
        return rootUrl;
    }

    /**
     * Returns the plug-in name.
     *
     * @return the plug-in name
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Returns a relative URL based on the specified row that will be appended
     * to the base URL. This default implementation returns an empty string,
     * indicating that there is no detail URL based on the selected row.
     *
     * @param row
     *            the selected row
     * @return a relative URL based on the specified row.
     */
    protected String getDetailUrl(final int row) {
        return StringUtils.EMPTY;
    }

    /**
     * Returns the build label at the specified column.
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

