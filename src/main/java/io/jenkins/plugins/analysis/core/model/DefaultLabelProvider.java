package io.jenkins.plugins.analysis.core.model;

import javax.annotation.CheckForNull;

import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.util.VisibleForTesting;

import hudson.plugins.analysis.util.HtmlPrinter;

/**
 * A generic label provider for static analysis runs. Creates pre-defined labels that are parameterized with a
 * string placeholder, that will be replaced with the actual name of the static analysis tool. Moreover,
 * such a default label provider decorates the links and summary boxes with the default icon of the warnings
 * plug-in.
 *
 * @author Ullrich Hafner
 */
public class DefaultLabelProvider implements StaticAnalysisLabelProvider {
    private static final String ICONS_PREFIX = "/plugin/analysis-core/icons/";
    private static final String SMALL_ICON_URL = ICONS_PREFIX + "analysis-24x24.png";
    private static final String LARGE_ICON_URL = ICONS_PREFIX + "analysis-48x48.png";

    private final String id;
    @CheckForNull
    private final String name;

    /**
     * Creates a new {@link DefaultLabelProvider} with the specified ID.
     *
     * @param id
     *         the ID
     */
    public DefaultLabelProvider(final String id) {
        this(id, StringUtils.EMPTY);
    }

    /**
     * Creates a new {@link DefaultLabelProvider} with the specified ID.
     *
     * @param id
     *         the ID
     * @param name
     *         the name of the static analysis tool
     */
    public DefaultLabelProvider(final String id, @CheckForNull final String name) {
        this.id = id;
        this.name = name;
    }

    @VisibleForTesting
    String getDefaultName() {
        return Messages.Tool_Default_Name();
    }

    /**
     * Returns the ID of the tool.
     *
     * @return the ID
     */
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        if (StringUtils.isNotBlank(name)) {
            return name;
        }
        return getDefaultName();
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getId(), getName());
    }

    @Override
    public String getLinkName() {
        return Messages.Tool_Link_Name(getName());
    }

    @Override
    public String getTrendName() {
        return Messages.Tool_Trend_Name(getName());
    }

    @Override
    public String getSmallIconUrl() {
        return SMALL_ICON_URL;
    }

    @Override
    public String getLargeIconUrl() {
        return LARGE_ICON_URL;
    }

    @Override
    public String getResultUrl() {
        return getId() + "Result";
    }

    @Override
    public String getTooltip(final int numberOfItems) {
        if (numberOfItems == 1) {
            return getSingleItemTooltip();
        }
        else {
            return getMultipleItemsTooltip(numberOfItems);
        }
    }

    @Override
    public String getDescription(final Issue issue) {
        return issue.getDescription();
    }

    /**
     * Returns the tooltip for several items.
     *
     * @param numberOfItems
     *         the number of items to display the tooltip for
     *
     * @return the tooltip for several items
     */
    private String getMultipleItemsTooltip(final int numberOfItems) {
        return Messages.Result_MultipleWarnings(numberOfItems);
    }

    /**
     * Returns the tooltip for exactly one item.
     *
     * @return the tooltip for exactly one item
     */
    private String getSingleItemTooltip() {
        return Messages.Result_OneWarning();
    }

    @Override
    public String getSummary(final int numberOfIssues, final int numberOfModules) {
        return getName() + ": " + new ResultSummaryPrinter().createDefaultSummary(getResultUrl(),
                numberOfIssues, numberOfModules);
    }

    @Override
    public String getDeltaMessage(final int newSize, final int fixedSize) {
        HtmlPrinter summary = new HtmlPrinter();
        if (newSize > 0) {
            summary.append(summary.item(
                    summary.link(getResultUrl() + "/new", createNewWarningsLinkName(newSize))));
        }
        if (fixedSize > 0) {
            summary.append(summary.item(
                    summary.link(getResultUrl() + "/fixed", createFixedWarningsLinkName(fixedSize))));
        }
        return summary.toString();
    }

    private static String createNewWarningsLinkName(final int newWarnings) {
        if (newWarnings == 1) {
            return Messages.Result_OneNewWarning();
        }
        else {
            return Messages.Result_MultipleNewWarnings(newWarnings);
        }
    }

    private static String createFixedWarningsLinkName(final int fixedWarnings) {
        if (fixedWarnings == 1) {
            return Messages.Result_OneFixedWarning();
        }
        else {
            return Messages.Result_MultipleFixedWarnings(fixedWarnings);
        }
    }
}
