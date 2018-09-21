package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.HealthReportBuilder;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.Thresholds;
import io.jenkins.plugins.analysis.core.scm.BlameFactory;
import io.jenkins.plugins.analysis.core.scm.Blamer;
import io.jenkins.plugins.analysis.core.util.EnvironmentResolver;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import jenkins.tasks.SimpleBuildStep;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * Freestyle or Maven job {@link Recorder} that scans report files or the console log for issues. Stores the created
 * issues in an {@link AnalysisResult}. The result is attached to the {@link Run} by registering a {@link
 * ResultAction}.
 * <p>
 * Additional features:
 * <ul>
 * <li>It provides a {@link QualityGate} that is checked after each run. If the quality gate is not passed, then the
 * build will be set to {@link Result#UNSTABLE} or {@link Result#FAILURE}, depending on the configuration
 * properties.</li>
 * <li>It provides thresholds for the build health that could be adjusted in the configuration screen.
 * These values are used by the {@link HealthReportBuilder} to compute the health and the health trend graph.
 * </li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.ExcessiveImports"})
public class IssuesRecorder extends Recorder implements SimpleBuildStep {
    @VisibleForTesting
    static final String NO_REFERENCE_JOB = "-";

    private List<ToolConfiguration> tools;
    
    private String reportEncoding;
    private String sourceCodeEncoding;

    private boolean ignoreAnalysisResult;
    private boolean overallResultMustBeSuccess;
    private String referenceJobName;

    private int healthy;
    private int unhealthy;
    private Severity minimumSeverity = Severity.WARNING_LOW;
    private final Thresholds thresholds = new Thresholds();

    private List<RegexpFilter> filters = new ArrayList<>();

    private boolean isEnabledForFailure;
    private boolean isAggregatingResults;

    /**
     * Creates a new instance of {@link IssuesRecorder}.
     */
    @DataBoundConstructor
    public IssuesRecorder() {
        super();

        // empty constructor required for Stapler
    }

    @CheckForNull
    public List<ToolConfiguration> getTools() {
        return tools;
    }

    /**
     * Sets the static analysis tools that will scan files and create issues.
     *
     * @param tools
     *         the static analysis tools
     */
    @DataBoundSetter
    public void setTools(final List<ToolConfiguration> tools) {
        this.tools = new ArrayList<>(tools);
    }

    /**
     * Sets the static analysis tool that will scan files and create issues.
     * 
     * @param tool 
     *         the static analysis tool
     */
    public void setTool(final ToolConfiguration tool) {
        this.tools = Collections.singletonList(tool);
    }

    @CheckForNull
    public String getReportEncoding() {
        return reportEncoding;
    }


    /**
     * Sets the default encoding used to read the log files that contain the warnings.
     *
     * @param logFileEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setReportEncoding(final String logFileEncoding) {
        this.reportEncoding = logFileEncoding;
    }

    @CheckForNull
    public String getSourceCodeEncoding() {
        return sourceCodeEncoding;
    }

    /**
     * Sets the encoding to use to read source files.
     *
     * @param sourceCodeEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setSourceCodeEncoding(final String sourceCodeEncoding) {
        this.sourceCodeEncoding = sourceCodeEncoding;
    }

    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Returns whether the results for each configured static analysis result should be aggregated into a single result
     * or if every tool should get an individual result.
     *
     * @return {@code true}  if the results of each static analysis tool should be aggregated into a single result,
     *         {@code false} if every tool should get an individual result.
     */
    public boolean getAggregatingResults() {
        return isAggregatingResults;
    }

    @DataBoundSetter
    public void setAggregatingResults(final boolean aggregatingResults) {
        this.isAggregatingResults = aggregatingResults;
    }

    /**
     * Returns whether recording should be enabled for failed builds as well.
     *
     * @return {@code true}  if recording should be enabled for failed builds as well, {@code false} if recording is
     *         enabled for successful or unstable builds only
     */
    public boolean getEnabledForFailure() {
        return isEnabledForFailure;
    }

    @DataBoundSetter
    public void setEnabledForFailure(final boolean enabledForFailure) {
        this.isEnabledForFailure = enabledForFailure;
    }

    /**
     * If {@code true} then the result of the previous analysis run is ignored when searching for the reference,
     * otherwise the result of the static analysis reference must be {@link Result#SUCCESS}.
     *
     * @param ignoreAnalysisResult
     *         if {@code true} then the previous build is always used
     */
    @DataBoundSetter
    public void setIgnoreAnalysisResult(final boolean ignoreAnalysisResult) {
        this.ignoreAnalysisResult = ignoreAnalysisResult;
    }

    public boolean getIgnoreAnalysisResult() {
        return ignoreAnalysisResult;
    }

    /**
     * If {@code true} then only runs with an overall result of {@link Result#SUCCESS} are considered as a reference,
     * otherwise every run that contains results of the same static analysis configuration is considered.
     *
     * @param overallResultMustBeSuccess
     *         if {@code true} then a stable build is used as reference
     */
    @DataBoundSetter
    public void setOverallResultMustBeSuccess(final boolean overallResultMustBeSuccess) {
        this.overallResultMustBeSuccess = overallResultMustBeSuccess;
    }

    public boolean getOverallResultMustBeSuccess() {
        return overallResultMustBeSuccess;
    }

    /**
     * Sets the reference job to get the results for the issue difference computation.
     *
     * @param referenceJobName
     *         the name of reference job
     */
    @DataBoundSetter
    public void setReferenceJobName(final String referenceJobName) {
        if (NO_REFERENCE_JOB.equals(referenceJobName)) {
            this.referenceJobName = StringUtils.EMPTY;
        }
        this.referenceJobName = referenceJobName;
    }

    /**
     * Returns the reference job to get the results for the issue difference computation. If the job is not defined,
     * then {@link #NO_REFERENCE_JOB} is returned.
     *
     * @return the name of reference job, or {@link #NO_REFERENCE_JOB} if undefined
     */
    public String getReferenceJobName() {
        if (StringUtils.isBlank(referenceJobName)) {
            return NO_REFERENCE_JOB;
        }
        return referenceJobName;
    }

    @CheckForNull
    public int getHealthy() {
        return healthy;
    }

    /**
     * Sets the healthy threshold, i.e. the number of issues when health is reported as 100%.
     *
     * @param healthy
     *         the number of issues when health is reported as 100%
     */
    @DataBoundSetter
    public void setHealthy(final int healthy) {
        this.healthy = healthy;
    }

    @CheckForNull
    public int getUnhealthy() {
        return unhealthy;
    }

    /**
     * Sets the healthy threshold, i.e. the number of issues when health is reported as 0%.
     *
     * @param unhealthy
     *         the number of issues when health is reported as 0%
     */
    @DataBoundSetter
    public void setUnhealthy(final int unhealthy) {
        this.unhealthy = unhealthy;
    }

    @CheckForNull
    public String getMinimumSeverity() {
        return minimumSeverity.getName();
    }

    /**
     * Sets the minimum severity to consider when computing the health report. Issues with a severity less than this
     * value will be ignored.
     *
     * @param minimumSeverity
     *         the severity to consider
     */
    @DataBoundSetter
    public void setMinimumSeverity(final String minimumSeverity) {
        this.minimumSeverity = Severity.valueOf(minimumSeverity, Severity.WARNING_LOW);
    }

    Thresholds getThresholds() {
        return thresholds;
    }

    public int getUnstableTotalAll() {
        return getThresholds().unstableTotalAll;
    }

    @DataBoundSetter
    public void setUnstableTotalAll(final int unstableTotalAll) {
        getThresholds().unstableTotalAll = unstableTotalAll;
    }

    public int getUnstableTotalHigh() {
        return getThresholds().unstableTotalHigh;
    }

    @DataBoundSetter
    public void setUnstableTotalHigh(final int unstableTotalHigh) {
        getThresholds().unstableTotalHigh = unstableTotalHigh;
    }

    public int getUnstableTotalNormal() {
        return getThresholds().unstableTotalNormal;
    }

    @DataBoundSetter
    public void setUnstableTotalNormal(final int unstableTotalNormal) {
        getThresholds().unstableTotalNormal = unstableTotalNormal;
    }

    public int getUnstableTotalLow() {
        return getThresholds().unstableTotalLow;
    }

    @DataBoundSetter
    public void setUnstableTotalLow(final int unstableTotalLow) {
        getThresholds().unstableTotalLow = unstableTotalLow;
    }

    public int getUnstableNewAll() {
        return getThresholds().unstableNewAll;
    }

    @DataBoundSetter
    public void setUnstableNewAll(final int unstableNewAll) {
        getThresholds().unstableNewAll = unstableNewAll;
    }

    public int getUnstableNewHigh() {
        return getThresholds().unstableNewHigh;
    }

    @DataBoundSetter
    public void setUnstableNewHigh(final int unstableNewHigh) {
        getThresholds().unstableNewHigh = unstableNewHigh;
    }

    public int getUnstableNewNormal() {
        return getThresholds().unstableNewNormal;
    }

    @DataBoundSetter
    public void setUnstableNewNormal(final int unstableNewNormal) {
        getThresholds().unstableNewNormal = unstableNewNormal;
    }

    public int getUnstableNewLow() {
        return getThresholds().unstableNewLow;
    }

    @DataBoundSetter
    public void setUnstableNewLow(final int unstableNewLow) {
        getThresholds().unstableNewLow = unstableNewLow;
    }

    public int getFailedTotalAll() {
        return getThresholds().failedTotalAll;
    }

    @DataBoundSetter
    public void setFailedTotalAll(final int failedTotalAll) {
        getThresholds().failedTotalAll = failedTotalAll;
    }

    public int getFailedTotalHigh() {
        return getThresholds().failedTotalHigh;
    }

    @DataBoundSetter
    public void setFailedTotalHigh(final int failedTotalHigh) {
        getThresholds().failedTotalHigh = failedTotalHigh;
    }

    public int getFailedTotalNormal() {
        return getThresholds().failedTotalNormal;
    }

    @DataBoundSetter
    public void setFailedTotalNormal(final int failedTotalNormal) {
        getThresholds().failedTotalNormal = failedTotalNormal;
    }

    public int getFailedTotalLow() {
        return getThresholds().failedTotalLow;
    }

    @DataBoundSetter
    public void setFailedTotalLow(final int failedTotalLow) {
        getThresholds().failedTotalLow = failedTotalLow;
    }

    public int getFailedNewAll() {
        return getThresholds().failedNewAll;
    }

    @DataBoundSetter
    public void setFailedNewAll(final int failedNewAll) {
        getThresholds().failedNewAll = failedNewAll;
    }

    public int getFailedNewHigh() {
        return getThresholds().failedNewHigh;
    }

    @DataBoundSetter
    public void setFailedNewHigh(final int failedNewHigh) {
        getThresholds().failedNewHigh = failedNewHigh;
    }

    public int getFailedNewNormal() {
        return getThresholds().failedNewNormal;
    }

    @DataBoundSetter
    public void setFailedNewNormal(final int failedNewNormal) {
        getThresholds().failedNewNormal = failedNewNormal;
    }

    public int getFailedNewLow() {
        return getThresholds().failedNewLow;
    }

    @DataBoundSetter
    public void setFailedNewLow(final int failedNewLow) {
        getThresholds().failedNewLow = failedNewLow;
    }

    public List<RegexpFilter> getFilters() {
        return filters;
    }

    @DataBoundSetter
    public void setFilters(final List<RegexpFilter> filters) {
        this.filters = new ArrayList<>(filters);
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
            throws InterruptedException, IOException {
        Result overallResult = run.getResult();
        if (isEnabledForFailure || overallResult == null || overallResult.isBetterOrEqualTo(Result.UNSTABLE)) {
            record(run, workspace, listener);
        }
        else {
            LogHandler logHandler = new LogHandler(listener, createLoggerPrefix());
            logHandler.log("Skipping execution of recorder since overall result is '%s'", overallResult);
        }
    }

    private String createLoggerPrefix() {
        return tools.stream().map(ToolConfiguration::getActualName).collect(Collectors.joining());
    }

    private void record(final Run<?, ?> run, final FilePath workspace,
            final TaskListener listener)
            throws IOException, InterruptedException {
        if (isAggregatingResults) {
            Report totalIssues = new Report();
            totalIssues.setId("analysis");
            for (ToolConfiguration toolConfiguration : tools) {
                totalIssues.addAll(scanWithTool(run, workspace, listener, toolConfiguration));
            }
            publishResult(run, workspace, listener, Messages.Tool_Default_Name(),
                    totalIssues, Messages.Tool_Default_Name());
        }
        else {
            for (ToolConfiguration toolConfiguration : tools) {
                Report report = scanWithTool(run, workspace, listener, toolConfiguration);
                String actualName;
                if (toolConfiguration.hasName()) {
                    actualName = toolConfiguration.getName();
                }
                else {
                    actualName = StringUtils.EMPTY;
                }
                publishResult(run, workspace, listener, toolConfiguration.getActualName(), report, actualName);
            }
        }
    }

    private Report scanWithTool(final Run<?, ?> run, final FilePath workspace, final TaskListener listener,
            final ToolConfiguration toolConfiguration) throws IOException, InterruptedException {
        IssuesScanner issuesScanner = new IssuesScanner(toolConfiguration.getTool(), workspace,
                getReportCharset(), getSourceCodeCharset(), new FilePath(run.getRootDir()), 
                new LogHandler(listener, toolConfiguration.getActualName()));
        Report report = issuesScanner.scan(expandEnvironmentVariables(run, listener, toolConfiguration.getPattern()),
                run.getLogFile());
        if (toolConfiguration.hasId()) {
            report.setId(toolConfiguration.getId());
        }
        return report;
    }

    private EnvVars getEnvironment(final AbstractBuild build, final TaskListener listener) {
        try {
            return build.getEnvironment(listener);
        }
        catch (IOException | InterruptedException e) {
            // ignore
        }
        return new EnvVars();
    }

    private Charset getSourceCodeCharset() {
        return getCharset(sourceCodeEncoding);
    }

    private Charset getReportCharset() {
        return getCharset(reportEncoding);
    }

    private Charset getCharset(final String encoding) {
        return new JobConfigurationModel().getCharset(encoding);
    }

    /**
     * Publishes the results as {@link Action} in the job using an {@link IssuesPublisher}. Afterwards, all affected
     * files are copied to Jenkins' build folder so that they are available to show warnings in the UI.
     *
     * @param run
     *         the run
     * @param workspace
     *         the workspace for this build
     * @param listener
     *         the listener
     * @param loggerName
     *         the name of the logger
     * @param report
     *         the analysis report to publish
     * @param name
     *         the name of the report (might be empty)
     */
    public void publishResult(final Run<?, ?> run,
            final FilePath workspace, final TaskListener listener, final String loggerName, final Report report,
            final String name) {
        Blamer blamer = BlameFactory.createBlamer(run, workspace, listener);
        IssuesPublisher publisher = new IssuesPublisher(run, report, blamer.blame(report), getFilters(),
                new HealthDescriptor(healthy, unhealthy, minimumSeverity), new QualityGate(thresholds),
                name, referenceJobName, ignoreAnalysisResult, overallResultMustBeSuccess, getSourceCodeCharset(),
                new LogHandler(listener, loggerName, report));
        publisher.attachAction();
    }

    private String expandEnvironmentVariables(final Run<?, ?> run, final TaskListener listener, final String pattern)
            throws IOException, InterruptedException {
        return new EnvironmentResolver().expandEnvironmentVariables(run.getEnvironment(listener), pattern);
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    @Symbol("recordIssues")
    @SuppressWarnings("unused") // most methods are used by the corresponding jelly view
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
        private final JobConfigurationModel model = new JobConfigurationModel();

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ScanAndPublishIssues_DisplayName();
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Returns a model with all available charsets.
         *
         * @return a model with all available charsets
         */
        public ComboBoxModel doFillReportEncodingItems() {
            return model.getAllCharsets();
        }

        /**
         * Returns a model with all available charsets.
         *
         * @return a model with all available charsets
         */
        public ComboBoxModel doFillSourceCodeEncodingItems() {
            return model.getAllCharsets();
        }

        /**
         * Returns a model with all available severity filters.
         *
         * @return a model with all available severity filters
         */
        public ListBoxModel doFillMinimumSeverityItems() {
            return model.getAllSeverityFilters();
        }

        /**
         * Returns the model with the possible reference jobs.
         *
         * @return the model with the possible reference jobs
         */
        public ComboBoxModel doFillReferenceJobNameItems() {
            return model.getAllJobs();
        }

        /**
         * Performs on-the-fly validation of the reference job.
         *
         * @param referenceJobName
         *         the reference job
         *
         * @return the validation result
         */
        public FormValidation doCheckReferenceJobName(@QueryParameter final String referenceJobName) {
            return model.validateJob(referenceJobName);
        }

        /**
         * Performs on-the-fly validation of the character encoding.
         *
         * @param reportEncoding
         *         the character encoding
         *
         * @return the validation result
         */
        public FormValidation doCheckReportEncoding(@QueryParameter final String reportEncoding) {
            return model.validateCharset(reportEncoding);
        }

        /**
         * Performs on-the-fly validation on the character encoding.
         *
         * @param sourceCodeEncoding
         *         the character encoding
         *
         * @return the validation result
         */
        public FormValidation doCheckSourceCodeEncoding(@QueryParameter final String sourceCodeEncoding) {
            return model.validateCharset(sourceCodeEncoding);
        }

        /**
         * Performs on-the-fly validation of the health report thresholds.
         *
         * @param healthy
         *         the healthy threshold
         * @param unhealthy
         *         the unhealthy threshold
         *
         * @return the validation result
         */
        public FormValidation doCheckHealthy(@QueryParameter final int healthy, @QueryParameter final int unhealthy) {
            return model.validateHealthy(healthy, unhealthy);
        }

        /**
         * Performs on-the-fly validation of the health report thresholds.
         *
         * @param healthy
         *         the healthy threshold
         * @param unhealthy
         *         the unhealthy threshold
         *
         * @return the validation result
         */
        public FormValidation doCheckUnhealthy(@QueryParameter final int healthy, @QueryParameter final int unhealthy) {
            return model.validateUnhealthy(healthy, unhealthy);
        }
    }
}
