package io.jenkins.plugins.analysis.core.model;

import javax.annotation.CheckForNull;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.IntegerParser;
import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.quality.Status;
import io.jenkins.plugins.analysis.core.views.LocalizedSeverity;
import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.UnescapedText;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import hudson.model.BallColor;
import hudson.model.Run;
import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * A generic label provider for static analysis runs. Creates pre-defined labels that are parameterized with a string
 * placeholder, that will be replaced with the actual name of the static analysis tool. Moreover, such a default label
 * provider decorates the links and summary boxes with the default icon of the warnings plug-in.
 *
 * @author Ullrich Hafner
 */
public class StaticAnalysisLabelProvider {
    /** Formats a full path: selects the file name portion. */
    public static final Function<String, String> FILE_NAME_FORMATTER
            = string -> iffElse(IssueParser.SELF.equals(string),
            Messages.ConsoleLog_Name(),
            StringUtils.substringAfterLast(string, "/"));

    private static final String ICONS_PREFIX = "/plugin/analysis-core/icons/";
    private static final String SMALL_ICON_URL = ICONS_PREFIX + "analysis-24x24.png";
    private static final String LARGE_ICON_URL = ICONS_PREFIX + "analysis-48x48.png";

    private final String id;
    @CheckForNull
    private final String name;
    private final JenkinsFacade jenkins;

    /**
     * Creates a new {@link StaticAnalysisLabelProvider} with the specified ID.
     *
     * @param id
     *         the ID
     */
    public StaticAnalysisLabelProvider(String id) {
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
    public StaticAnalysisLabelProvider(String id, @CheckForNull String name) {
        this(id, name, new JenkinsFacade());
    }

    @VisibleForTesting
    StaticAnalysisLabelProvider(String id, @CheckForNull String name, JenkinsFacade jenkins) {
        this.id = id;
        this.name = name;
        this.jenkins = jenkins;
    }

    /**
     * Creates a new {@link StaticAnalysisLabelProvider} with the ID 'analysis-core'. This label provider is used as
     * fallback.
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
     * @param report
     *         the issues to show in the table
     * @param ageBuilder
     *         produces the age of an issue based on the current build number
     *
     * @return the table as String
     */
    public JSONObject toJsonArray(Report report, AgeBuilder ageBuilder) {
        JSONArray rows = new JSONArray();
        for (Issue issue : report) {
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
    protected JSONArray toJson(Issue issue, AgeBuilder ageBuilder) {
        JSONArray columns = new JSONArray();
        columns.add(formatDetails(issue));
        columns.add(formatFileName(issue));
        columns.add(formatProperty("packageName", issue.getPackageName()));
        columns.add(formatProperty("category", issue.getCategory()));
        columns.add(formatProperty("type", issue.getType()));
        columns.add(formatSeverity(issue.getSeverity()));
        columns.add(formatAge(issue, ageBuilder));
        return columns;
    }

    /**
     * Formats the text of the details column. The details column is not directly shown, it rather is a hidden element
     * that is expanded if the corresponding button is selected. The actual text value is stored in the {@code
     * data-description} attribute.
     *
     * @param issue
     *         the issue in a table row
     *
     * @return the formatted column
     */
    protected String formatDetails(Issue issue) {
        return div().withClass("details-control")
                .attr("data-description", join(p(strong(issue.getMessage())), getDescription(issue)).render())
                .render();
    }

    /**
     * Formats the text of the age column. The age shows the number of builds a warning is reported.
     *
     * @param issue
     *         the issue in a table row
     * @param ageBuilder
     *         renders the age
     *
     * @return the formatted column
     */
    protected String formatAge(Issue issue, AgeBuilder ageBuilder) {
        return ageBuilder.apply(new IntegerParser().parseInt(issue.getReference()));
    }

    /**
     * Formats the text of the severity column.
     *
     * @param severity
     *         the severity of the issue
     *
     * @return the formatted column
     */
    protected String formatSeverity(Severity severity) {
        return String.format("<a href=\"%s\">%s</a>",
                severity.getName(), LocalizedSeverity.getLocalizedString(severity));
    }

    private String formatProperty(String property, String value) {
        return String.format("<a href=\"%s.%d/\">%s</a>", property, value.hashCode(), value);
    }

    /**
     * Formats the text of the file name column. The text actually is a link to the UI representation of the file.
     *
     * @param issue
     *         the issue to show the file name for
     *
     * @return the formatted column
     */
    // FIXME: only link if valid file name
    protected String formatFileName(Issue issue) {
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

    /**
     * Returns the human readable name of the tool. If the name has not been set, then the default name is returned.
     *
     * @return the name
     */
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

    /**
     * Returns the name of the link to the results in Jenkins' side panel.
     *
     * @return the name of the side panel link
     */
    public String getLinkName() {
        return Messages.Tool_Link_Name(getName());
    }

    /**
     * Returns the legend for the trend chart in the project overview.
     *
     * @return the legend of the trend chart
     */
    public String getTrendName() {
        return Messages.Tool_Trend_Name(getName());
    }

    /**
     * Returns the absolute URL to the small icon for the tool.
     *
     * @return absolute URL
     */
    public String getSmallIconUrl() {
        return SMALL_ICON_URL;
    }

    /**
     * Returns the absolute URL to the large icon for the tool.
     *
     * @return absolute URL
     */
    public String getLargeIconUrl() {
        return LARGE_ICON_URL;
    }

    /**
     * Returns the URL to the view that shows the results for the tool.
     *
     * @return absolute URL
     */
    public String getResultUrl() {
        return getId() + "Result";
    }

    /**
     * Returns the title for the small information box in the corresponding build page.
     *
     * @param result
     *         the result
     * @param hasErrors
     *         indicates if an error has been reported
     *
     * @return the title div
     */
    public ContainerTag getTitle(AnalysisResult result, boolean hasErrors) {
        String icon = hasErrors ? "fa-exclamation-triangle" : "fa-info-circle";
        return div(join(getName() + ": ",
                getWarningsCount(result),
                a().withHref(getResultUrl() + "/info").with(i().withClasses("fa", icon))))
                .withId(id + "-title");
    }

    /**
     * Returns the HTML label for the link to the new issues of the build.
     *
     * @param newSize
     *         the number of new issues
     *
     * @return the legend of the trend chart
     */
    // TODO: Make messages overridable
    public ContainerTag getNewIssuesLabel(int newSize) {
        return a(newSize == 1 ? Messages.Tool_OneNewWarning() : Messages.Tool_MultipleNewWarnings(newSize))
                .withHref(getResultUrl() + "/new");
    }

    /**
     * Returns the HTML label for the link to the fixed issues of the build.
     *
     * @param fixedSize
     *         the number of fixed issues
     *
     * @return the legend of the trend chart
     */
    public ContainerTag getFixedIssuesLabel(int fixedSize) {
        return a(fixedSize == 1 ? Messages.Tool_OneFixedWarning() : Messages.Tool_MultipleFixedWarnings(fixedSize))
                .withHref(getResultUrl() + "/fixed");
    }

    /**
     * Returns the HTML text showing the number of builds since the project has no issues.
     *
     * @param currentBuild
     *         the current build number
     * @param noIssuesSinceBuild
     *         the build since there are no issues
     *
     * @return the legend of the trend chart
     */
    public DomContent getNoIssuesSinceLabel(int currentBuild, int noIssuesSinceBuild) {
        return join(Messages.Tool_NoIssuesSinceBuild(Messages.Tool_NoIssues(),
                currentBuild - noIssuesSinceBuild + 1,
                a(String.valueOf(noIssuesSinceBuild))
                        .withHref("../" + noIssuesSinceBuild)
                        .withClasses("model-link", "inside").render()));
    }

    private Object getWarningsCount(AnalysisResult analysisRun) {
        int size = analysisRun.getTotalSize();
        if (size == 0) {
            return Messages.Tool_NoIssues();
        }
        if (size == 1) {
            return linkToIssues(Messages.Tool_OneIssue());
        }
        return linkToIssues(Messages.Tool_MultipleIssues(size));
    }

    private ContainerTag linkToIssues(String linkText) {
        return a(linkText).withHref(getResultUrl());
    }

    /**
     * Returns the HTML text showing the result of the quality gate.
     *
     * @param qualityGateStatus
     *         the status of the quality gate
     *
     * @return the legend of the trend chart
     */
    public DomContent getQualityGateResult(Status qualityGateStatus) {
        return join(Messages.Tool_QualityGate(), getResultIcon(qualityGateStatus));
    }

    /**
     * Returns the HTML text showing a link to the reference build.
     *
     * @param referenceBuild
     *         the reference build
     *
     * @return the legend of the trend chart
     */
    public DomContent getReferenceBuild(Run<?, ?> referenceBuild) {
        return join(Messages.Tool_ReferenceBuild(), createReferenceBuildLink(referenceBuild));
    }

    private ContainerTag createReferenceBuildLink(Run<?, ?> referenceBuild) {
        String absoluteUrl = jenkins.getAbsoluteUrl(referenceBuild.getUrl(), getResultUrl());
        return a(referenceBuild.getFullDisplayName()).withHref(absoluteUrl);
    }

    private UnescapedText getResultIcon(Status status) {
        BallColor color = status.getColor();
        return join(img().withSrc(jenkins.getImagePath(color))
                        .withClasses(color.getIconClassName(), "icon-lg")
                        .withAlt(color.getDescription())
                        .withTitle(color.getDescription()),
                color.getDescription());
    }

    public ToolTipProvider getToolTipProvider() {
        return this::getTooltip;
    }

    // FIXME: still required?
    String getTooltip(int numberOfItems) {
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
    public String getDescription(Issue issue) {
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
    private String getMultipleItemsTooltip(int numberOfItems) {
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

    /**
     * Functional interface that maps the age of a build from an integer value to a String value.
     */
    public interface AgeBuilder extends Function<Integer, String> {
        // no new methods
    }

    /**
     * Computes the age of a build as a hyper link.
     */
    public static class DefaultAgeBuilder implements AgeBuilder {
        private final int currentBuild;
        private final String resultUrl;

        /**
         * Creates a new instance of {@link DefaultAgeBuilder}.
         *
         * @param currentBuild
         *         number of the current build
         * @param resultUrl
         *         URL to the results
         */
        public DefaultAgeBuilder(int currentBuild, String resultUrl) {
            this.currentBuild = currentBuild;
            this.resultUrl = resultUrl;
        }

        @Override
        public String apply(Integer referenceBuild) {
            if (referenceBuild >= currentBuild) {
                return "1"; // fallback
            }
            else {
                String cleanUrl = StringUtils.stripEnd(resultUrl, "/");
                int subDetailsCount = StringUtils.countMatches(cleanUrl, "/");
                String backward = StringUtils.repeat("../", subDetailsCount + 2);
                String detailsUrl = StringUtils.substringBefore(cleanUrl, "/");

                String url = String.format("%s%d/%s", backward, referenceBuild, detailsUrl);
                return a(computeAge(referenceBuild))
                        .withHref(StringUtils.stripEnd(url, "/")).render();
            }
        }

        private String computeAge(int buildNumber) {
            return String.valueOf(currentBuild - buildNumber + 1);
        }
    }
}
