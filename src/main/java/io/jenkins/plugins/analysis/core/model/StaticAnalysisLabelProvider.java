package io.jenkins.plugins.analysis.core.model;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.jvnet.localizer.Localizable;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.IntegerParser;
import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.quality.QualityGateStatus;
import io.jenkins.plugins.analysis.core.views.LocalizedSeverity;
import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.UnescapedText;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import hudson.model.Run;
import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * A generic label provider for static analysis results. Creates pre-defined labels that are parameterized with a string
 * placeholder, that will be replaced with the actual name of the static analysis tool. Moreover, such a default label
 * provider decorates the links and summary boxes with the default icon of the warnings plug-in.
 *
 * @author Ullrich Hafner
 */
public class StaticAnalysisLabelProvider {
    private static final String ICONS_PREFIX = "/plugin/warnings/icons/";
    private static final String SMALL_ICON_URL = ICONS_PREFIX + "analysis-24x24.png";
    private static final String LARGE_ICON_URL = ICONS_PREFIX + "analysis-48x48.png";

    private final String id;
    @CheckForNull
    private String name;
    private final JenkinsFacade jenkins;

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
        this(id, name, new JenkinsFacade());
    }

    @VisibleForTesting
    StaticAnalysisLabelProvider(final String id, @CheckForNull final String name, final JenkinsFacade jenkins) {
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
     * Returns the table headers of the report table.
     *
     * @param report
     *         the report to show
     *
     * @return the table headers
     */
    @SuppressWarnings("unused") // called by Jelly view
    public List<String> getTableHeaders(final Report report) {
        List<String> visibleColumns = new ArrayList<>();
        visibleColumns.add(Messages.Table_Column_Details());
        visibleColumns.add(Messages.Table_Column_File());
        if (report.hasPackages()) {
            visibleColumns.add(Messages.Table_Column_Package());
        }
        if (report.hasCategories()) {
            visibleColumns.add(Messages.Table_Column_Category());
        }
        if (report.hasTypes()) {
            visibleColumns.add(Messages.Table_Column_Type());
        }
        visibleColumns.add(Messages.Table_Column_Priority());
        visibleColumns.add(Messages.Table_Column_Age());
        return visibleColumns;
    }

    /**
     * Returns the widths of the table headers of the report table.
     *
     * @param report
     *         the report to show
     *
     * @return the width of the table headers
     */
    @SuppressWarnings("unused") // called by Jelly view
    public List<Integer> getTableWidths(final Report report) {
        List<Integer> widths = new ArrayList<>();
        widths.add(1);
        widths.add(1);
        if (report.hasPackages()) {
            widths.add(2);
        }
        if (report.hasCategories()) {
            widths.add(1);
        }
        if (report.hasTypes()) {
            widths.add(1);
        }
        widths.add(1);
        widths.add(1);
        return widths;
    }

    /**
     * Converts the specified set of issues into a table.
     *
     * @param report
     *         the report to show in the table
     * @param ageBuilder
     *         produces the age of an issue based on the current build number
     * @param fileNameRenderer
     *         creates a link to the affected file (if accessible)
     *
     * @return the table as String
     */
    public JSONObject toJsonArray(final Report report, final AgeBuilder ageBuilder,
            final FileNameRenderer fileNameRenderer) {
        JSONArray rows = new JSONArray();
        for (Issue issue : report) {
            rows.add(toJson(report, issue, ageBuilder, fileNameRenderer));
        }
        JSONObject data = new JSONObject();
        data.put("data", rows);
        return data;
    }

    /**
     * Returns an JSON array that represents the columns of the issues table.
     *
     * @param report
     *         the report to show in the table
     * @param issue
     *         the issue to get the column properties for
     * @param ageBuilder
     *         age builder to compute the age of a build
     * @param fileNameRenderer
     *         creates a link to the affected file (if accessible)
     *
     * @return the columns
     */
    protected JSONArray toJson(final Report report, final Issue issue,
            final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer) {
        JSONArray columns = new JSONArray();
        columns.add(formatDetails(issue));
        columns.add(formatFileName(issue, fileNameRenderer));
        if (report.hasPackages()) {
            columns.add(formatProperty("packageName", issue.getPackageName()));
        }
        if (report.hasCategories()) {
            columns.add(formatProperty("category", issue.getCategory()));
        }
        if (report.hasTypes()) {
            columns.add(formatProperty("type", issue.getType()));
        }
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
    protected String formatDetails(final Issue issue) {
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
    protected String formatAge(final Issue issue, final AgeBuilder ageBuilder) {
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
    protected String formatSeverity(final Severity severity) {
        return String.format("<a href=\"%s\">%s</a>",
                severity.getName(), LocalizedSeverity.getLocalizedString(severity));
    }

    /**
     * Formats the text of the specified property column. T he text actually is a link to the UI representation of the
     * property.
     *
     * @param property
     *         the property to format
     * @param value
     *         the value of the property
     *
     * @return the formatted column
     */
    protected String formatProperty(final String property, final String value) {
        return String.format("<a href=\"%s.%d/\">%s</a>", property, value.hashCode(), value);
    }

    /**
     * Formats the text of the file name column. The text actually is a link to the UI representation of the file.
     *
     * @param issue
     *         the issue to show the file name for
     * @param fileNameRenderer
     *         creates a link to the affected file (if accessible)
     *
     * @return the formatted file name
     */
    protected String formatFileName(final Issue issue, final FileNameRenderer fileNameRenderer) {
        return fileNameRenderer.renderAffectedFileLink(issue);
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

    /**
     * Sets the human readable name of the tool. 
     * 
     * @param name the name of the tool
     * @return the name
     */
    public StaticAnalysisLabelProvider setName(@CheckForNull final String name) {
        if (StringUtils.isNotBlank(name)) { // don't overwrite with empty
            this.name = name;
        }
        
        return this;
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
    public ContainerTag getTitle(final AnalysisResult result, final boolean hasErrors) {
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
    public ContainerTag getNewIssuesLabel(final int newSize) {
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
    public ContainerTag getFixedIssuesLabel(final int fixedSize) {
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
    public DomContent getNoIssuesSinceLabel(final int currentBuild, final int noIssuesSinceBuild) {
        return join(Messages.Tool_NoIssuesSinceBuild(Messages.Tool_NoIssues(),
                currentBuild - noIssuesSinceBuild + 1,
                a(String.valueOf(noIssuesSinceBuild))
                        .withHref("../" + noIssuesSinceBuild)
                        .withClasses("model-link", "inside").render()));
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

    /**
     * Returns the HTML text showing the result of the quality gate.
     *
     * @param qualityGateStatus
     *         the status of the quality gate
     *
     * @return the legend of the trend chart
     */
    public DomContent getQualityGateResult(final QualityGateStatus qualityGateStatus) {
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
    public DomContent getReferenceBuild(final Run<?, ?> referenceBuild) {
        return join(Messages.Tool_ReferenceBuild(), createReferenceBuildLink(referenceBuild));
    }

    private ContainerTag createReferenceBuildLink(final Run<?, ?> referenceBuild) {
        String absoluteUrl = jenkins.getAbsoluteUrl(referenceBuild.getUrl(), getResultUrl());
        return a(referenceBuild.getFullDisplayName()).withHref(absoluteUrl);
    }

    private UnescapedText getResultIcon(final QualityGateStatus qualityGateStatus) {
        return join(i().withClasses("fa", qualityGateStatus.getIconName())
                        .withAlt(qualityGateStatus.getDescription())
                        .withTitle(qualityGateStatus.getDescription()),
                qualityGateStatus.getDescription());
    }

    public ToolTipProvider getToolTipProvider() {
        return this::getToolTip;
    }

    public String getToolTip(final int numberOfItems) {
        return getToolTipLocalizable(numberOfItems).toString();
    }

    /**
     * Returns a short description describing the total number of issues.
     *
     * @param numberOfItems
     *         the number of issues to report
     *
     * @return the description
     */
    public Localizable getToolTipLocalizable(final int numberOfItems) {
        return new CompositeLocalizable(getName(), createToolTipSuffix(numberOfItems));
    }

    private Localizable createToolTipSuffix(final int numberOfItems) {
        if (numberOfItems == 0) {
            return Messages._Tool_NoIssues();
        }
        if (numberOfItems == 1) {
            return Messages._Tool_OneIssue();
        }
        return Messages._Tool_MultipleIssues(numberOfItems);
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
                String cleanUrl = StringUtils.stripEnd(resultUrl, "/");
                int subDetailsCount = StringUtils.countMatches(cleanUrl, "/");
                String backward = StringUtils.repeat("../", subDetailsCount + 2);
                String detailsUrl = StringUtils.substringBefore(cleanUrl, "/");

                String url = String.format("%s%d/%s", backward, referenceBuild, detailsUrl);
                return a(computeAge(referenceBuild))
                        .withHref(StringUtils.stripEnd(url, "/")).render();
            }
        }

        private String computeAge(final int buildNumber) {
            return String.valueOf(currentBuild - buildNumber + 1);
        }
    }

    /**
     * Creates a {@link Localizable} that is composed of a prefix and suffix.
     */
    static class CompositeLocalizable extends Localizable {
        private static final long serialVersionUID = 2819361593374249688L;

        private final String prefix;
        private final Localizable suffix;

        CompositeLocalizable(final String prefix, final Localizable suffix) {
            super(null, suffix.getKey());
            this.prefix = prefix;
            this.suffix = suffix;
        }

        @Override
        public String toString(final Locale locale) {
            return String.format("%s: %s", prefix, suffix);
        }
    } 
}
