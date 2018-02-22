package io.jenkins.plugins.analysis.core.model;

import javax.annotation.CheckForNull;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import edu.hm.hafner.analysis.IntegerParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.analysis.core.util.HtmlBuilder;
import static io.jenkins.plugins.analysis.core.views.IssuesDetail.*;
import io.jenkins.plugins.analysis.core.views.LocalizedPriority;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import hudson.plugins.analysis.util.HtmlPrinter;

/**
 * A generic label provider for static analysis runs. Creates pre-defined labels that are parameterized with a string
 * placeholder, that will be replaced with the actual name of the static analysis tool. Moreover, such a default label
 * provider decorates the links and summary boxes with the default icon of the warnings plug-in.
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

    /**
     * Creates a new {@link DefaultLabelProvider} with the ID 'analysis-core'. This label provider is used as fallback.
     */
    public DefaultLabelProvider() {
        this("analysis-core");
    }

    @Override
    public String[] getTableHeaders() {
        return new String[]{
                Messages.Table_Column_Details(),
                Messages.Table_Column_File(),
                Messages.Table_Column_Package(),
                Messages.Table_Column_Category(),
                Messages.Table_Column_Type(),
                Messages.Table_Column_Priority(),
                Messages.Table_Column_Age()
        };
    }

    @Override
    public int[] getTableWidths() {
        return new int[]{1, 1, 2, 1, 1, 1, 1};
    }

    @Override
    public JSONObject toJsonArray(final Issues<?> issues, final AgeBuilder ageBuilder) {
        JSONArray rows = new JSONArray();
        for (Issue issue : issues) {
            rows.add(toJson(issue, ageBuilder));
        }
        JSONObject data = new JSONObject();
        data.put("data", rows);
        return data;
    }

    /**
     * Returns an JSON array that represents the columns of the issues table.
     *
     * @param issue
     *         the issue to get the column properties for
     * @param ageBuilder
     *         age builder to compute the age of a build
     *
     * @return the columns
     */
    protected JSONArray toJson(final Issue issue, final AgeBuilder ageBuilder) {
        JSONArray columns = new JSONArray();
        columns.add(formatDetails(issue));
        columns.add(formatFileName(issue));
        columns.add(formatProperty("packageName", issue.getPackageName()));
        columns.add(formatProperty("category", issue.getCategory()));
        columns.add(formatProperty("type", issue.getType()));
        columns.add(formatPriority(issue.getPriority()));
        columns.add(formatAge(issue, ageBuilder));
        return columns;
    }

    protected String formatDetails(final Issue issue) {
        return String.format("<div class=\"details-control\" data-description=\"%s\"/>",
                StringEscapeUtils.escapeHtml4(getDescription(issue)));
    }

    protected String formatAge(final Issue issue, final AgeBuilder ageBuilder) {
        return ageBuilder.apply(new IntegerParser().parseInt(issue.getReference()));
    }

    protected String formatPriority(final Priority priority) {
        return String.format("<a href=\"%s\">%s</a>",
                priority.name(), LocalizedPriority.getLocalizedString(priority));
    }

    private String formatProperty(final String property, final String value) {
        return String.format("<a href=\"%s.%d/\">%s</a>", property, value.hashCode(), value);
    }

    // FIXME: only link if valid file name
    protected String formatFileName(final Issue issue) {
        return String.format("<a href=\"source.%s/#%d\">%s:%d</a>", issue.getId(), issue.getLineStart(),
                FILE_NAME_FORMATTER.apply(issue.getFileName()), issue.getLineStart());
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

    public interface AgeBuilder extends Function<Integer, String> {
        // no new methods
    }

    public static class DefaultAgeBuilder implements AgeBuilder {
        private final String plugin;
        private final String backward;
        private final int currentBuild;

        public DefaultAgeBuilder(final int currentBuild, final String resultUrl) {
            this.currentBuild = currentBuild;
            String cleanUrl = StringUtils.stripEnd(resultUrl, "/");
            plugin = StringUtils.substringBefore(cleanUrl, "/");
            int subDetailsCount = StringUtils.countMatches(cleanUrl, "/");

            backward = StringUtils.repeat("../", subDetailsCount + 2);
        }

        @Override
        public String apply(final Integer origin) {
            if (origin >= currentBuild) {
                return "1"; // fallback
            }
            else {
                return new HtmlBuilder().linkWithClass(String.format("%s%d/%s", backward, origin, plugin),
                        computeAge(origin), "model-link inside").build();
            }
        }

        private String computeAge(final int buildNumber) {
            return String.valueOf(currentBuild - buildNumber + 1);
        }
    }
}
