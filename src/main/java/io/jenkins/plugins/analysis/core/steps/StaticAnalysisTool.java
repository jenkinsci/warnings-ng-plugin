package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.text.WordUtils;
import org.kohsuke.stapler.DataBoundSetter;

import jenkins.model.Jenkins;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.plugins.analysis.core.AnnotationParser;
import hudson.plugins.analysis.util.HtmlPrinter;
import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * Describes a static analysis tool that reports issues.
 *
 * @author Ullrich Hafner
 */
public abstract class StaticAnalysisTool extends AbstractDescribableImpl<StaticAnalysisTool> implements AnnotationParser, ExtensionPoint, ToolTipProvider {
    private static final String ICONS_PREFIX = "/plugin/analysis-core/icons/";
    private static final String SMALL_ICON_URL = ICONS_PREFIX + "analysis-24x24.png";
    private static final String LARGE_ICON_URL = ICONS_PREFIX + "analysis-48x48.png";

    /**
     * Finds the static analysis tool with the specified ID.
     *
     * @param id
     *         the ID of the tool to find
     *
     * @return the static analysis tool
     * @throws NoSuchElementException
     *         if the tool could not be found
     */
    public static StaticAnalysisTool find(final String id) {
        for (StaticAnalysisTool parser : all()) {
            if (parser.getId().equals(id)) {
                return parser;
            }
        }
        throw new NoSuchElementException("IssueParser not found: " + id);
    }

    private static Collection<? extends StaticAnalysisTool> all() {
        return Jenkins.getInstance().getExtensionList(StaticAnalysisTool.class);
    }

    private final String id;

    private String defaultEncoding;

    @CheckForNull
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Sets the default encoding used to read files (warnings, source code, etc.).
     *
     * @param defaultEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setDefaultEncoding(final String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Creates a new static analysis tool with the specified ID.
     *
     * @param id
     *         the ID
     */
    // TODO: check that no duplicate IDs are requested
    public StaticAnalysisTool(final String id) {
        this.id = id;
    }

    /**
     * Returns the ID of the tool.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    protected String getSuffix() {
        return String.format(" (%s)", WordUtils.capitalize(getId()));
    }

    protected String getName() {
        return Messages.Tool_Name(getSuffix());
    }

    public String getLinkName() {
        return Messages.Tool_Link_Name(getSuffix());
    }

    public String getTrendName() {
        return Messages.Tool_Trend_Name(getSuffix());
    }

    public String getSmallIconUrl() {
        return SMALL_ICON_URL;
    }

    public String getLargeIconUrl() {
        return LARGE_ICON_URL;
    }

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

    /**
     * Returns the tooltip for several items.
     *
     * @param numberOfItems
     *         the number of items to display the tooltip for
     *
     * @return the tooltip for several items
     */
    protected String getMultipleItemsTooltip(final int numberOfItems) {
        return hudson.plugins.analysis.Messages.ResultAction_MultipleWarnings(numberOfItems);
    }

    /**
     * Returns the tooltip for exactly one item.
     *
     * @return the tooltip for exactly one item
     */
    protected String getSingleItemTooltip() {
        return hudson.plugins.analysis.Messages.ResultAction_OneWarning();
    }

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @param numberOfIssues
     *         the number of issues in the report
     * @param numberOfModules
     *         the number of modules in the report
     *
     * @return the summary message
     */
    public String getSummary(final int numberOfIssues, final int numberOfModules) {
        return getName() + ": " + new ResultSummaryPrinter().createDefaultSummary(getResultUrl(),
                numberOfIssues, numberOfModules);
    }

    /**
     * Creates a default delta message for the build result.
     *
     * @param newSize
     *         number of new issues
     * @param fixedSize
     *         number of fixed issues
     *
     * @return the summary message
     */
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
            return Messages.ResultAction_OneNewWarning();
        }
        else {
            return Messages.ResultAction_MultipleNewWarnings(newWarnings);
        }
    }

    private static String createFixedWarningsLinkName(final int fixedWarnings) {
        if (fixedWarnings == 1) {
            return Messages.ResultAction_OneFixedWarning();
        }
        else {
            return Messages.ResultAction_MultipleFixedWarnings(fixedWarnings);
        }
    }

    @Override
    public String toString() {
        return getId();
    }

    public static class StaticAnalysisToolDescriptor extends Descriptor<StaticAnalysisTool> {
        public StaticAnalysisToolDescriptor(final Class<? extends StaticAnalysisTool> clazz) {
            super(clazz);
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return clazz.getSimpleName();
        }
    }
}
