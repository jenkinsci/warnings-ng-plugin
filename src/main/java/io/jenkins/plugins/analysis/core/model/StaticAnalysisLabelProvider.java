package io.jenkins.plugins.analysis.core.model;

import java.util.function.Function;
import javax.annotation.CheckForNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.kohsuke.stapler.Stapler;

import io.jenkins.plugins.analysis.core.views.LocalizedPriority;

import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import hudson.model.BallColor;
import hudson.model.Result;
import hudson.plugins.analysis.util.ToolTipProvider;

import edu.hm.hafner.analysis.IntegerParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.util.VisibleForTesting;

/**
 * A generic label provider for static analysis runs. Creates pre-defined labels that are parameterized with a string
 * placeholder, that will be replaced with the actual name of the static analysis tool. Moreover, such a default label
 * provider decorates the links and summary boxes with the default icon of the warnings plug-in.
 *
 * @author Ullrich Hafner
 */
public class StaticAnalysisLabelProvider {
    /** Formats a full path: selects the file name portion.  */
    public static final Function<String, String> FILE_NAME_FORMATTER
            = string -> StringUtils.substringAfterLast(string, "/");

    private static final String ICONS_PREFIX = "/plugin/analysis-core/icons/";
    private static final String SMALL_ICON_URL = ICONS_PREFIX + "analysis-24x24.png";
    private static final String LARGE_ICON_URL = ICONS_PREFIX + "analysis-48x48.png";
    private static final String EMPTY_URL = "";

    private final String id;
    @CheckForNull
    private final String name;
    private final IconPathResolver resolver;

    /**
     * Creates a new {@link StaticAnalysisLabelProvider} with the specified ID.
     *
     * @param id
     *         the ID
     */
    public StaticAnalysisLabelProvider(final String id) {
        this(id, StringUtils.EMPTY);
    }

    /**
     * Creates a new {@link StaticAnalysisLabelProvider} with the specified ID.
     *
     * @param id
     *         the ID
     * @param name
     *         the name of the static analysis tool
     */
    public StaticAnalysisLabelProvider(final String id, @CheckForNull final String name) {
        this(id, name, new IconPathResolver());
    }

    /**
     * Creates a new {@link StaticAnalysisLabelProvider} with the specified ID.
     *
     * @param id
     *         the ID
     * @param name
     *         the name of the static analysis tool
     */
    @VisibleForTesting
    StaticAnalysisLabelProvider(final String id, @CheckForNull final String name, final IconPathResolver resolver) {
        this.id = id;
        this.name = name;
        this.resolver = resolver;
    }

    /**
     * Creates a new {@link StaticAnalysisLabelProvider} with the ID 'analysis-core'. This label provider is used as fallback.
     */
    @VisibleForTesting
    StaticAnalysisLabelProvider() {
        this("analysis-core");
    }

    /**
     * Returns the table headers of the issues table.
     *
     * @return the table headers
     */
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

    /**
     * Returns the widths of the table headers of the issues table.
     *
     * @return the width of the table headers
     */
    public int[] getTableWidths() {
        return new int[]{1, 1, 2, 1, 1, 1, 1};
    }

    /**
     * Converts the specified set of issues into a table.
     *
     * @param issues
     *         the issues to show in the table
     * @param ageBuilder
     *         produces the age of an issue based on the current build number
     *
     * @return the table as String
     */
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
    public String getId() {
        return id;
    }

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

    public String getLinkName() {
        return Messages.Tool_Link_Name(getName());
    }

    public String getTrendName() {
        return Messages.Tool_Trend_Name(getName());
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

    public ContainerTag getTitle(final AnalysisResult analysisRun, final boolean hasErrors) {
        String icon = hasErrors ? "fa-exclamation-triangle" : "fa-info-circle";
        return div(join(getName() + ": ",
                getWarningsCount(analysisRun),
                a().withHref(getResultUrl() + "/info").with(i().withClasses("fa", icon))))
                .withId(id + "-title");
    }

    public ContainerTag getNewIssuesLabel(final int newSize) {
        return a(newSize == 1 ? Messages.Tool_OneNewWarning() : Messages.Tool_MultipleNewWarnings(newSize))
                .withHref(getResultUrl() + "/new"); // Make messages overridable
    }

    public ContainerTag getFixedIssuesLabel(final int fixedSize) {
        return a(fixedSize == 1 ? Messages.Tool_OneFixedWarning() : Messages.Tool_MultipleFixedWarnings(fixedSize))
                .withHref(getResultUrl() + "/fixed");
    }

    public DomContent getNoIssuesSinceLabel(final int currentBuild, final int noIssuesSinceBuild) {
        return join(Messages.Tool_NoIssuesSinceBuild(Messages.Tool_NoIssues(),
                currentBuild - noIssuesSinceBuild + 1, linkBuild(noIssuesSinceBuild, EMPTY_URL, EMPTY_URL).render()));
    }

    private Object getWarningsCount(final AnalysisResult analysisRun) {
        int size = analysisRun.getTotalSize();
        if (size == 0) {
            return Messages.Tool_NoIssues();
        }
        if (size == 1) {
            return linkToIssues(Messages.Tool_OneIssue());
        }
        return linkToIssues(Messages.Tool_MultipleIssues(size));
    }

    private ContainerTag linkToIssues(final String linkText) {
        return a(linkText).withHref(getResultUrl());
    }

    public DomContent getQualityGateResult(final Result overallResult, final int referenceBuild) {
        return join(Messages.Tool_QualityGate(),
                getResultIcon(overallResult.color), "-",
                Messages.Tool_ReferenceBuild(linkBuild(referenceBuild, EMPTY_URL, getResultUrl()).render()));
    }

    private ContainerTag getResultIcon(final BallColor color) {
        return a(color.getDescription())
                .withHref(resolver.getImagePath(color))
                .withAlt(color.getDescription())
                .withTitle(color.getDescription());
    }

    public ToolTipProvider getToolTipProvider() {
        return (numberOfItems) -> getTooltip(numberOfItems);
    }

    // FIXME: still required?
    public String getTooltip(final int numberOfItems) {
        if (numberOfItems == 1) {
            return getSingleItemTooltip();
        }
        else {
            return getMultipleItemsTooltip(numberOfItems);
        }
    }

    /**
     * Returns a detailed description of the specified issue.
     *
     * @param issue
     *         the issue to get the description for
     *
     * @return the description
     */
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
        return Messages.Tool_MultipleIssues(numberOfItems);
    }

    /**
     * Returns the tooltip for exactly one item.
     *
     * @return the tooltip for exactly one item
     */
    private String getSingleItemTooltip() {
        return Messages.Tool_OneIssue();
    }

    private static ContainerTag linkBuild(final int referenceBuild, final String currentUrl,
            final String displayName, final String suffix) {
        String url;
        if (StringUtils.isEmpty(currentUrl)) {
            url = String.format("../%d/%s", referenceBuild, suffix);
        }
        else {
            String cleanUrl = StringUtils.stripEnd(currentUrl, "/");
            int subDetailsCount = StringUtils.countMatches(cleanUrl, "/");
            String backward = StringUtils.repeat("../", subDetailsCount + 2);
            String detailsUrl = StringUtils.substringBefore(cleanUrl, "/");

            url = String.format("%s%d/%s%s", backward, referenceBuild, detailsUrl, suffix);
        }
        return a(displayName)
                .withHref(StringUtils.stripEnd(url, "/"))
                .withClasses("model-link", "inside");
    }

    private static ContainerTag linkBuild(final int referenceBuild, final String currentUrl, final String suffix) {
        return linkBuild(referenceBuild, currentUrl, String.valueOf(referenceBuild), suffix);
    }

    public interface AgeBuilder extends Function<Integer, String> {
        // no new methods
    }

    /**
     * Computes the age of a build as a hyper link.
     */
    public static class DefaultAgeBuilder implements AgeBuilder {
        private final int currentBuild;
        private final String resultUrl;

        public DefaultAgeBuilder(final int currentBuild, final String resultUrl) {
            this.currentBuild = currentBuild;
            this.resultUrl = resultUrl;
        }

        @Override
        public String apply(final Integer referenceBuild) {
            if (referenceBuild >= currentBuild) {
                return "1"; // fallback
            }
            else {
                return linkBuild(referenceBuild, resultUrl, computeAge(referenceBuild), "").render();
            }
        }

        private String computeAge(final int buildNumber) {
            return String.valueOf(currentBuild - buildNumber + 1);
        }
    }

    /**
     * Resolves the path to the image of a {@link BallColor} using Staplers {@link Stapler#getCurrentRequest()}.
     */
    static class IconPathResolver {
        String getImagePath(final BallColor color) {
            return color.getImageOf("16");
        }
    }
}
