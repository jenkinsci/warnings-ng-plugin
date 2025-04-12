package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.JacksonFacade;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.Api;
import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.charts.HealthTrendChart;
import io.jenkins.plugins.analysis.core.charts.ModifiedCodePieChart;
import io.jenkins.plugins.analysis.core.charts.NewVersusFixedPieChart;
import io.jenkins.plugins.analysis.core.charts.NewVersusFixedTrendChart;
import io.jenkins.plugins.analysis.core.charts.SeverityPieChart;
import io.jenkins.plugins.analysis.core.charts.SeverityTrendChart;
import io.jenkins.plugins.analysis.core.charts.ToolsTrendChart;
import io.jenkins.plugins.analysis.core.charts.TrendChart;
import io.jenkins.plugins.analysis.core.restapi.AnalysisResultApi;
import io.jenkins.plugins.analysis.core.restapi.ReportApi;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.BuildResultNavigator;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.datatables.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.forensics.util.CommitDecoratorFactory;

/**
 * Build view that shows the details for a subset of issues.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects", "PMD.GodClass", "ClassDataAbstractionCoupling", "ClassFanOutComplexity"})
public class IssuesDetail extends DefaultAsyncTableContentProvider implements ModelObject {
    private static final ResetQualityGateCommand RESET_QUALITY_GATE_COMMAND = new ResetQualityGateCommand();
    private static final JacksonFacade JACKSON_FACADE = new JacksonFacade();
    private static final String ISSUES_TABLE_ID = "issues";
    private static final String BLAMES_TABLE_ID = "blames";
    private static final String FORENSICS_TABLE_ID = "forensics";
    private static final String FILE_NAME_PROPERTY = "fileName";
    private static final String ORIGIN_PROPERTY = "origin";

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

    private final AnalysisResult result;

    private final HealthDescriptor healthDescriptor;

    /**
     * Creates a new detail model with the corresponding view {@code IssuesDetail/index.jelly}.
     *
     * @param owner
     *         the associated build/run of this view
     * @param result
     *         the analysis result
     * @param report
     *         all issues that should be shown in this details view
     * @param newIssues
     *         all new issues
     * @param outstandingIssues
     *         all outstanding issues
     * @param fixedIssues
     *         all fixed issues
     * @param url
     *         the relative URL of this view
     * @param displayName
     *         the human-readable name of this view (shown in breadcrumb)
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
        this(owner, result, report, newIssues, outstandingIssues, fixedIssues, displayName, url, labelProvider,
                sourceEncoding, new HealthDescriptor(0, 0, Severity.ERROR));
    }

    /**
     * Creates a new detail model with the corresponding view {@code IssuesDetail/index.jelly}.
     *
     * @param owner
     *         the associated build/run of this view
     * @param result
     *         the analysis result
     * @param report
     *         all issues that should be shown in this details view
     * @param newIssues
     *         all new issues
     * @param outstandingIssues
     *         all outstanding issues
     * @param fixedIssues
     *         all fixed issues
     * @param url
     *         the relative URL of this view
     * @param displayName
     *         the human-readable name of this view (shown in breadcrumb)
     * @param labelProvider
     *         the label provider for the static analysis tool
     * @param sourceEncoding
     *         the encoding to use when displaying source files
     * @param healthDescriptor
     *         health descriptor
     */
    @SuppressWarnings("ParameterNumber")
    public IssuesDetail(final Run<?, ?> owner, final AnalysisResult result,
            final Report report, final Report newIssues,
            final Report outstandingIssues, final Report fixedIssues,
            final String displayName, final String url, final StaticAnalysisLabelProvider labelProvider,
            final Charset sourceEncoding, final HealthDescriptor healthDescriptor) {
        super();

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
        this.healthDescriptor = healthDescriptor;
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
     * @param healthDescriptor
     *         the health descriptor
     * @param sourceEncoding
     *         the charset to visualize source files with
     */
    public IssuesDetail(final Run<?, ?> owner, final AnalysisResult result,
            final StaticAnalysisLabelProvider labelProvider,
            final HealthDescriptor healthDescriptor, final Charset sourceEncoding) {
        this(owner, result, result.getIssues(), result.getNewIssues(), result.getOutstandingIssues(),
                result.getFixedIssues(), labelProvider.getLinkName(), labelProvider.getId(),
                labelProvider, sourceEncoding, healthDescriptor);

        infoMessages.addAll(result.getInfoMessages().castToList());
        errorMessages.addAll(result.getErrorMessages().castToList());
    }

    AnalysisResult getResult() {
        return result;
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
        return new Api(new ReportApi(getIssues(), result.getBlames()));
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
     * Returns the UI model for the specified table.
     *
     * @param id
     *         the ID of the table
     *
     * @return the UI model as JSON
     */
    @Override
    public TableModel getTableModel(final String id) {
        if (ISSUES_TABLE_ID.equals(id)) {
            return labelProvider.getIssuesModel(owner, getUrl(), report);
        }
        else if (BLAMES_TABLE_ID.equals(id)) {
            return new BlamesModel(report, result.getBlames(),
                    labelProvider.getFileNameRenderer(owner),
                    labelProvider.getAgeBuilder(owner, getUrl()),
                    labelProvider,
                    CommitDecoratorFactory.findCommitDecorator(owner));
        }
        else if (FORENSICS_TABLE_ID.equals(id)) {
            return new ForensicsModel(report, result.getForensics(),
                    labelProvider.getFileNameRenderer(owner),
                    labelProvider.getAgeBuilder(owner, getUrl()),
                    labelProvider);
        }
        else {
            throw new NoSuchElementException("No such table model: " + id);
        }
    }

    /**
     * Resets the quality gate for the owner of this view.
     *
     * @return unused string (since Firefox requires that Ajax calls return something)
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String resetReference() {
        RESET_QUALITY_GATE_COMMAND.execute(owner, labelProvider.getId());

        return "{}";
    }

    /**
     * Returns the URL for same results of the selected build.
     *
     * @param build
     *         the selected build to open the new results for
     * @param detailsUrl
     *         the absolute URL to this details view results
     *
     * @return the URL to the results or an empty string if the results are not available
     */
    @JavaScriptMethod
    public String getUrlForBuild(final String build, final String detailsUrl) {
        var history = createHistory();
        for (BuildResult<AnalysisBuildResult> buildResult : history) {
            if (buildResult.getBuild().getDisplayName().equals(build)) {
                return new BuildResultNavigator().getSameUrlForOtherBuild(owner, detailsUrl, getResult().getId(),
                                buildResult.getBuild().getNumber()).orElse(StringUtils.EMPTY);
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns the UI model for an ECharts doughnut chart that shows the severities.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getSeverityModel() {
        return JACKSON_FACADE.toJson(new SeverityPieChart().create(report));
    }

    /**
     * Returns the UI model for an ECharts doughnut chart that shows the new, fixed, and outstanding issues.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getTrendModel() {
        return JACKSON_FACADE.toJson(new NewVersusFixedPieChart().create(newIssues, outstandingIssues, fixedIssues));
    }

    /**
     * Returns the UI model for an ECharts doughnut chart that shows the issues in modified code.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getModifiedModel() {
        return JACKSON_FACADE.toJson(new ModifiedCodePieChart().create(report));
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the issues stacked by severity.
     *
     * @param configuration
     *         determines whether the Jenkins build number should be used on the X-axis or the date
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getBuildTrend(final String configuration) {
        return createTrendAsJson(new SeverityTrendChart(), configuration);
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the issues by tool.
     *
     * @param configuration
     *         determines whether the Jenkins build number should be used on the X-axis or the date
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getToolsTrend(final String configuration) {
        return createTrendAsJson(new ToolsTrendChart(), configuration);
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the new and fixed issues.
     *
     * @param configuration
     *         determines whether the Jenkins build number should be used on the X-axis or the date
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getNewVersusFixedTrend(final String configuration) {
        return createTrendAsJson(new NewVersusFixedTrendChart(), configuration);
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the issues by tool.
     *
     * @param configuration
     *         determines whether the Jenkins build number should be used on the X-axis or the date
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getHealthTrend(final String configuration) {
        return createTrendAsJson(new HealthTrendChart(healthDescriptor), configuration);
    }

    /**
     * Returns whether a health report has been enabled.
     *
     * @return {@code true} if health reporting is enabled, {@code false} otherwise
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean isHealthReportEnabled() {
        return healthDescriptor.isEnabled();
    }

    private String createTrendAsJson(final TrendChart trendChart, final String configuration) {
        var history = createHistory();

        return new JacksonFacade().toJson(trendChart.create(history, ChartModelConfiguration.fromJson(configuration)));
    }

    private AnalysisHistory createHistory() {
        return new AnalysisHistory(owner, new ByIdResultSelector(result.getId()));
    }

    /**
     * Returns whether there are any issues in the associated static analysis run that are part of modified code.
     *
     * @return {@code true} if there are issues in modified code, {@code false} otherwise
     */
    public boolean hasIssuesInModifiedCode() {
        return result.getTotals().getTotalModifiedSize() > 0;
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
     * Returns all new issues of the associated static analysis run. I.e., all issues that are part of the current
     * report but have not been shown up in the previous report.
     *
     * @return all new issues
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Report getNewIssues() {
        return newIssues;
    }

    /**
     * Returns all fixed issues of the associated static analysis run. I.e., all issues that are part of the previous
     * report but are not present in the current report anymore.
     *
     * @return all fixed issues
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Report getFixedIssues() {
        return fixedIssues;
    }

    /**
     * Returns all outstanding issues of the associated static analysis run. I.e., all issues that are part of the
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
     * @return on {@code true} the SCM blames will be shown in the UI
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean isBlameVisible() {
        return !result.getBlames().isEmpty();
    }

    /**
     * Returns whether SCM forensics will be shown or not.
     *
     * @return on {@code true} the SCM forensics will be shown in the UI
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean isForensicsVisible() {
        return !result.getForensics().isEmpty();
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
        return ConsoleLogHandler.isInConsoleLog(issue.getFileName())
                || AffectedFilesResolver.hasAffectedFile(owner, issue)
                || new File(issue.getAbsolutePath()).exists();
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
        if (FILE_NAME_PROPERTY.equals(propertyName)) {
            propertyFormatter = new BaseNameMapper();
        }
        else if (ORIGIN_PROPERTY.equals(propertyName)) {
            propertyFormatter = origin -> new LabelProviderFactory().create(origin,
                    getIssues().getNameOfOrigin(origin)).getName();
        }
        else {
            propertyFormatter = Function.identity();
        }
        return new PropertyStatistics(report, newIssues, propertyName, propertyFormatter);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a new subpage for the selected link.
     *
     * @param link
     *         the link to identify the subpage to show
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the new subpage
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Object getDynamic(final String link, final StaplerRequest2 request, final StaplerResponse2 response) {
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
                var baseName = Path.of(absolutePath).getFileName();
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
