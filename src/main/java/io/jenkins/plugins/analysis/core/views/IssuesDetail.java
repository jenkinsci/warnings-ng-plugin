package io.jenkins.plugins.analysis.core.views;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.set.ImmutableSet;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.ExportedBean;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.charts.PieModel;
import io.jenkins.plugins.analysis.core.charts.SeverityChart;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ByIdResultSelector;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.PropertyStatistics;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.restapi.AnalysisResultApi;
import io.jenkins.plugins.analysis.core.restapi.ReportApi;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import hudson.markup.MarkupFormatter;
import hudson.markup.RawHtmlMarkupFormatter;
import hudson.model.Api;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.core.GlobalSettings;

/**
 * Build view that shows the details for a subset of issues.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.ExcessiveImports")
@ExportedBean
public class IssuesDetail implements ModelObject {
    private final Run<?, ?> owner;

    private final Report report;
    private final Report newIssues;
    private final Report outstandingIssues;
    private final Report fixedIssues;

    private final Charset sourceEncoding;
    private final String displayName;
    private final String url;
    private final StaticAnalysisLabelProvider labelProvider;
    private final List<String> errorMessages = new ArrayList<>();
    private final List<String> infoMessages = new ArrayList<>();

    /** Sanitizes HTML elements in warning messages and tooltips. Use this formatter if raw HTML should be shown. */
    private final MarkupFormatter sanitizer = new RawHtmlMarkupFormatter(true);

    private final AnalysisResult result;

    /**
     * Creates a new detail model with the corresponding view {@code IssuesDetail/index.jelly}.
     *
     * @param owner
     *         the associated build/run of this view
     * @param result
     *         the analysis result
     * @param report
     *         all issues that should be shown in this details view
     * @param outstandingIssues
     *         all outstanding issues
     * @param newIssues
     *         all new issues
     * @param fixedIssues
     *         all fixed issues
     * @param url
     *         the relative URL of this view
     * @param displayName
     *         the human readable name of this view (shown in breadcrumb)
     * @param labelProvider
     *         the label provider for the static analysis tool
     * @param sourceEncoding
     *         the encoding to use when displaying source files
     */
    @SuppressWarnings("ParameterNumber")
    public IssuesDetail(final Run<?, ?> owner, final AnalysisResult result,
            final Report report, final Report newIssues,
            final Report outstandingIssues, final Report fixedIssues,
            final String displayName, final String url, final StaticAnalysisLabelProvider labelProvider,
            final Charset sourceEncoding) {
        this.owner = owner;
        this.result = result;

        this.report = report;
        this.fixedIssues = fixedIssues;
        this.newIssues = newIssues;
        this.outstandingIssues = outstandingIssues;

        this.sourceEncoding = sourceEncoding;
        this.displayName = displayName;
        this.labelProvider = labelProvider;
        this.url = url;
    }

    /**
     * Creates a new detail model with the corresponding view {@code IssuesDetail/index.jelly}.
     *
     * @param owner
     *         the associated build/run of this view
     * @param result
     *         the analysis result
     * @param labelProvider
     *         the label provider for the static analysis tool
     * @param sourceEncoding
     *         the charset to visualize source files with
     */
    public IssuesDetail(final Run<?, ?> owner, final AnalysisResult result,
            final StaticAnalysisLabelProvider labelProvider, final Charset sourceEncoding) {
        this(owner, result, result.getIssues(), result.getNewIssues(), result.getOutstandingIssues(),
                result.getFixedIssues(), labelProvider.getLinkName(), labelProvider.getId(),
                labelProvider, sourceEncoding);
        infoMessages.addAll(result.getInfoMessages().castToList());
        errorMessages.addAll(result.getErrorMessages().castToList());
    }

    /**
     * Returns the error messages of the static analysis run.
     *
     * @return the error messages
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Collection<String> getErrorMessages() {
        return errorMessages;
    }

    /**
     * Returns the information messages of the static analysis run.
     *
     * @return the information messages
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Collection<String> getInfoMessages() {
        return infoMessages;
    }

    /**
     * Gets the remote API for this action. Depending on the path, a different result is selected.
     *
     * @return the remote API
     */
    public Api getApi() {
        if (getUrl().endsWith(labelProvider.getId())) {
            return new Api(new AnalysisResultApi(result));
        }
        return new Api(new ReportApi(getIssues()));
    }

    // ------------------------------------ UI entry points for Stapler --------------------------------

    /**
     * Returns the label provider to render the localized labels.
     *
     * @return the label provider
     */
    public StaticAnalysisLabelProvider getLabelProvider() {
        return labelProvider;
    }

    /**
     * Returns the model for the details table.
     *
     * @return the table model
     */
    public DetailsTableModel getIssuesModel() {
        return labelProvider.getIssuesModel(owner, getUrl());
    }

    /**
     * Returns the model for the details table.
     *
     * @return the table model
     */
    public DetailsTableModel getScmModel() {
        return labelProvider.getScmModel(owner, getUrl(), result.getBlames());
    }

    private JSONObject toJsonArray(final List<List<String>> rows) {
        JSONArray array = new JSONArray();
        array.addAll(rows);
        JSONObject data = new JSONObject();
        data.put("data", array);
        return data;
    }

    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public JSONObject getTableModel(final String id) {
        List<List<String>> rows;
        if ("#issues".equals(id)) {
            rows = getIssuesModel().getContent(getIssues());
        }
        else {
            rows = getScmModel().getContent(getIssues());
        }

        return toJsonArray(rows);
    }

    /**
     * Returns the UI model for an ECharts doughnut chart that shows the severities.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public JSONArray getSeverityModel() {
        List<PieModel> model = new ArrayList<>();
        ImmutableSet<Severity> predefinedSeverities = Severity.getPredefinedValues();
        for (Severity severity : predefinedSeverities) {
            model.add(new PieModel(LocalizedSeverity.getLocalizedString(severity), report.getSizeOf(severity)));
        }

        return JSONArray.fromObject(model);
    }

    /**
     * Returns the UI model for an ECharts doughnut chart that shows the new, fixed, and outstanding issues.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public JSONArray getTrendModel() {
        List<PieModel> model = new ArrayList<>();
        model.add(new PieModel(Messages.New_Warnings_Short(), newIssues.size()));
        model.add(new PieModel(Messages.Outstanding_Warnings_Short(), outstandingIssues.size()));
        model.add(new PieModel(Messages.Fixed_Warnings_Short(), fixedIssues.size()));

        return JSONArray.fromObject(model);
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the issues stacked by severity.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public JSONObject getBuildTrend() {
        SeverityChart severityChart = new SeverityChart();

        AnalysisHistory history = new AnalysisHistory(owner, new ByIdResultSelector(report.getId()));
        return JSONObject.fromObject(severityChart.create(history));
    }

    /**
     * Returns all issues of the associated static analysis run.
     *
     * @return all issues
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Report getIssues() {
        return report;
    }

    /**
     * Returns all new issues of the associated static analysis run. I.e. all issues, that are part of the current
     * report but have not been shown up in the previous report.
     *
     * @return all new issues
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Report getNewIssues() {
        return newIssues;
    }

    /**
     * Returns all fixed issues of the associated static analysis run. I.e. all issues, that are part of the previous
     * report but are not present in the current report anymore.
     *
     * @return all fixed issues
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Report getFixedIssues() {
        return fixedIssues;
    }

    /**
     * Returns all outstanding issues of the associated static analysis run. I.e. all issues, that are part of the
     * current and previous report.
     *
     * @return all outstanding issues
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Report getOutstandingIssues() {
        return outstandingIssues;
    }

    /**
     * Returns whether author and commit information should be shown or not.
     *
     * @return on {@code true} the SCM will be called to obtain author and commit information, on {@code false} author
     *         and commit information are omitted
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean isAuthorInformationEnabled() {
        return !GlobalSettings.instance().getNoAuthors();
    }

    /**
     * Returns whether the affected file of the specified issue can be shown in the UI.
     *
     * @param issue
     *         the issue to get the affected file for
     *
     * @return {@code true} if the file could be shown, {@code false} otherwise
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean canDisplayFile(final Issue issue) {
        return ConsoleDetail.isInConsoleLog(issue.getFileName())
                || AffectedFilesResolver.hasAffectedFile(owner, issue)
                || new File(issue.getFileName()).exists();
    }

    /**
     * Returns the short name for an absolute path name.
     *
     * @param issue
     *         the issue to get the file name for
     *
     * @return the file name
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getFileDisplayName(final Issue issue) {
        return new FileNameRenderer(owner).getFileName(issue);
    }

    /**
     * Returns the label provider for the localized tab names.
     *
     * @return the label provider
     */
    @SuppressWarnings("unused") // Called by jelly view
    public TabLabelProvider getTabLabelProvider() {
        return new TabLabelProvider(getIssues());
    }

    /**
     * Sanitizes HTML elements in the specified HTML page so that the result contains only safe HTML tags.
     *
     * @param html
     *         the HTML page
     *
     * @return the sanitized HTML page
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String sanitize(final String html) {
        try {
            return sanitizer.translate(html);
        }
        catch (IOException ignore) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Returns a localized severity name.
     *
     * @param severity
     *         the severity
     *
     * @return localized severity name
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getLocalizedSeverity(final Severity severity) {
        return LocalizedSeverity.getLocalizedString(severity);
    }

    /**
     * Returns statics for the specified property.
     *
     * @param propertyName
     *         the name of the property
     *
     * @return the statistics
     */
    @SuppressWarnings("unused") // Called by jelly view
    public PropertyStatistics getDetails(final String propertyName) {
        Function<String, String> propertyFormatter;
        if ("fileName".equals(propertyName)) {
            propertyFormatter = new BaseNameMapper();
        }
        else if ("origin".equals(propertyName)) {
            propertyFormatter = origin -> new LabelProviderFactory().create(origin).getName();
        }
        else {
            propertyFormatter = Function.identity();
        }
        return new PropertyStatistics(report, propertyName, propertyFormatter);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a new sub page for the selected link.
     *
     * @param link
     *         the link to identify the sub page to show
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the new sub page
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        try {
            return new DetailFactory().createTrendDetails(link, owner, result,
                    report, newIssues, outstandingIssues, fixedIssues,
                    sourceEncoding, this);
        }
        catch (NoSuchElementException ignored) {
            try {
                response.sendRedirect2("../");
            }
            catch (IOException ignore) {
                // ignore
            }
            return this; // fallback on broken URLs
        }
    }

    // ------------------------------------ UI entry points for Stapler --------------------------------

    /**
     * Returns the build as owner of this object.
     *
     * @return the owner
     */
    public final Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this build is the last available build.
     *
     * @return {@code true} if this build is the last available build
     */
    public final boolean isCurrent() {
        return owner.getParent().getLastBuild().number == owner.number;
    }

    /**
     * Returns the (relative) URL of this model object.
     *
     * @return this model objects' URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the base name of a file name with absolute path.
     */
    private static class BaseNameMapper implements Function<String, String> {
        /**
         * Returns the base name of the file that contains this issue (i.e. the file name without the full path).
         *
         * @return the base name of the file that contains this issue
         */
        @Override
        public String apply(final String absolutePath) {
            try {
                Path baseName = Paths.get(absolutePath).getFileName();
                if (baseName == null) {
                    return absolutePath; // fallback
                }
                return baseName.toString();
            }
            catch (InvalidPathException e) {
                return absolutePath;
            }
        }
    }
}
