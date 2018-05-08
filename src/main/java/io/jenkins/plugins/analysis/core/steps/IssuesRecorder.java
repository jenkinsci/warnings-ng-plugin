package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.google.common.collect.Lists;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.RegexpFilter;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.HealthReportBuilder;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.Thresholds;
import io.jenkins.plugins.analysis.core.util.EnvironmentResolver;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * Freestyle or Maven job {@link Recorder} that scans report files or the console log for issues. Stores the created
 * issues in an {@link AnalysisResult}. The result is attached to the {@link Run} by registering a {@link ResultAction}.
 * <p>
 * Additional features:
 * <ul>
 * <li>It provides a {@link QualityGate} that is checked after each run. If the quality gate is not passed, then the build
 * will be set to {@link Result#UNSTABLE} or {@link Result#FAILURE}, depending on the configuration properties.</li>
 * <li>It provides thresholds for the build health that could be adjusted in the configuration screen.
 * These values are used by the {@link HealthReportBuilder} to compute the health and the health trend graph.
 * </li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
public class IssuesRecorder extends Recorder implements SimpleBuildStep {
    private static final Priority DEFAULT_MINIMUM_PRIORITY = Priority.LOW;

    @VisibleForTesting
    static final String NO_REFERENCE_JOB = "-";

    private String reportEncoding;
    private String sourceCodeEncoding;
    private List<ToolConfiguration> tools;

    private boolean ignoreAnalysisResult;
    private boolean overallResultMustBeSuccess;
    private String referenceJobName;

    private int healthy;
    private int unHealthy;
    private Priority minimumPriority = DEFAULT_MINIMUM_PRIORITY;
    private final Thresholds thresholds = new Thresholds();

    private boolean isEnabledForFailure;
    private boolean isAggregatingResults;

    @DataBoundConstructor
    public IssuesRecorder() {
        // empty constructor required for Stapler
    }

    @CheckForNull
    public List<ToolConfiguration> getTools() {
        return tools;
    }

    /**
     * Sets the static analysis tool that will scan files and create issues.
     *
     * @param tools
     *         the static analysis tool
     */
    @DataBoundSetter
    public void setTools(final List<ToolConfiguration> tools) {
        this.tools = new ArrayList<>(tools);
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
     * Sets the default encoding used to read the log files that contain the warnings.
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
     * Returns whether the results for each configured static analysis result should be aggregated into a single
     * result or if every tool should get an individual result.
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
     * @return {@code true}  if recording should be enabled for failed builds as well,
     *         {@code false} if recording is enabled for successful or unstable builds only
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
    public int getUnHealthy() {
        return unHealthy;
    }

    /**
     * Sets the healthy threshold, i.e. the number of issues when health is reported as 0%.
     *
     * @param unHealthy
     *         the number of issues when health is reported as 0%
     */
    @DataBoundSetter
    public void setUnHealthy(final int unHealthy) {
        this.unHealthy = unHealthy;
    }

    @CheckForNull
    public String getMinimumPriority() {
        return minimumPriority.name();
    }

    /**
     * Sets the minimum priority to consider when computing the health report. Issues with a priority less than this
     * value will be ignored.
     *
     * @param minimumPriority
     *         the priority to consider
     */
    @DataBoundSetter
    public void setMinimumPriority(final String minimumPriority) {
        this.minimumPriority = Priority.fromString(minimumPriority, Priority.LOW);
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

    private List<RegexpFilter> filters = Lists.newArrayList();

    public List<RegexpFilter> getFilters() {
        return filters;
    }

    @DataBoundSetter
    public void setFilters(final List<RegexpFilter> filters) {
        this.filters = new ArrayList<>(filters);
    }

    /** ------------------------------------------------------------ */

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
            throws InterruptedException, IOException {
        Result overallResult = run.getResult();
        if (isEnabledForFailure || overallResult == null || overallResult.isBetterOrEqualTo(Result.UNSTABLE)) {
            record(run, workspace, launcher, listener);
        }
        else {
            LogHandler logHandler = new LogHandler(listener, createLoggerPrefix());
            logHandler.log("Skipping execution of recorder since overall result is '%s'", overallResult);
        }
    }

    private void record(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener)
            throws IOException, InterruptedException {
        if (isAggregatingResults) {
            Issues<Issue> totalIssues = new Issues<>();
            for (ToolConfiguration toolConfiguration : tools) {
                totalIssues.addAll(scanWithTool(run, workspace, listener, toolConfiguration));
            }
            totalIssues.setOrigin("analysis");
            publishResult(run, workspace, launcher, listener, totalIssues, Messages.Tool_Default_Name());
        }
        else {
            for (ToolConfiguration toolConfiguration : tools) {
                Issues<?> issues = scanWithTool(run, workspace, listener, toolConfiguration);
                publishResult(run, workspace, launcher, listener, issues, StringUtils.EMPTY);
            }
        }
    }

    private Issues<?> scanWithTool(final Run<?, ?> run, final FilePath workspace, final TaskListener listener,
            final ToolConfiguration toolConfiguration)
            throws IOException, InterruptedException {
        ToolConfiguration configuration = toolConfiguration;
        IssuesScanner issuesScanner = new IssuesScanner(configuration.getTool(), workspace,
                getReportCharset(), getSourceCodeCharset(), new LogHandler(listener, configuration.getTool().getName()));
        return issuesScanner.scan(expandEnvironmentVariables(run, listener, configuration.getPattern()),
                run.getLogFile());
    }

    private Charset getSourceCodeCharset() {
        return EncodingValidator.defaultCharset(sourceCodeEncoding);
    }

    private Charset getReportCharset() {
        return EncodingValidator.defaultCharset(reportEncoding);
    }

    private void publishResult(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener, final Issues<?> issues, final String name)
            throws IOException, InterruptedException {
        IssuesPublisher publisher = new IssuesPublisher(run, report, getFilters(),
                new HealthDescriptor(healthy, unHealthy, minimumPriority), new QualityGate(thresholds), workspace,
                name, referenceJobName, ignoreAnalysisResult, overallResultMustBeSuccess, getSourceCodeCharset(),
                new LogHandler(listener, report.getOrigin()));

        VirtualChannel channel = launcher.getChannel();
        if (channel != null) {
            publisher.attachAction(channel, new FilePath(run.getRootDir()));
        }
        else {
            publisher.attachAction();
        }
    }

    private String createLoggerPrefix() {
        return tools.stream().map(tool -> tool.getTool().getId()).collect(Collectors.joining());
    }

    private String expandEnvironmentVariables(final Run<?, ?> run, final TaskListener listener, final String pattern)
            throws IOException, InterruptedException {
        return new EnvironmentResolver().expandEnvironmentVariables(run.getEnvironment(listener), pattern);
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension @Symbol("recordIssues")
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
        private final JenkinsFacade jenkins;

        /**
         * Creates a new instance of {@link Descriptor}.
         */
        public Descriptor() {
            this(new JenkinsFacade());
        }

        @VisibleForTesting
        Descriptor(final JenkinsFacade jenkins) {
            this.jenkins = jenkins;
        }

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
            return new ComboBoxModel(EncodingValidator.getAvailableCharsets());
        }

        /**
         * Returns a model with all available charsets.
         *
         * @return a model with all available charsets
         */
        public ComboBoxModel doFillSourceCodeEncodingItems() {
            return doFillReportEncodingItems();
        }

        /**
         * Returns a model with all available priority filters.
         *
         * @return a model with all available priority filters
         */
        public ListBoxModel doFillMinimumPriorityItems() {
            ListBoxModel options = new ListBoxModel();
            options.add(Messages.PriorityFilter_High(), Priority.HIGH.name());
            options.add(Messages.PriorityFilter_Normal(), Priority.NORMAL.name());
            options.add(Messages.PriorityFilter_Low(), Priority.LOW.name());
            return options;
        }

        /**
         * Returns the model with the possible reference jobs.
         *
         * @return the model with the possible reference jobs
         */
        public ComboBoxModel doFillReferenceJobItems() {
            ComboBoxModel model = new ComboBoxModel(jenkins.getAllJobs());
            model.add(0, NO_REFERENCE_JOB); // make sure that no input is valid
            return model;
        }

        /**
         * Performs on-the-fly validation of the reference job.
         *
         * @param referenceJob
         *         the reference job
         *
         * @return the validation result
         */
        public FormValidation doCheckReferenceJob(@QueryParameter final String referenceJob) {
            if (NO_REFERENCE_JOB.equals(referenceJob)
                    || StringUtils.isBlank(referenceJob)
                    || jenkins.getJob(referenceJob).isPresent()) {
                return FormValidation.ok();
            }
            return FormValidation.error(Messages.FieldValidator_Error_ReferenceJobDoesNotExist());
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
            try {
                if (StringUtils.isBlank(reportEncoding) || Charset.forName(reportEncoding).canEncode()) {
                    return FormValidation.ok();
                }
            }
            catch (IllegalCharsetNameException | UnsupportedCharsetException ignore) {
                // throw a FormValidation error
            }
            return FormValidation.errorWithMarkup(createWrongEncodingErrorMessage());
        }

        @VisibleForTesting
        String createWrongEncodingErrorMessage() {
            return Messages.FieldValidator_Error_DefaultEncoding(
                    "https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html");
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
            return doCheckReportEncoding(sourceCodeEncoding);
        }

        /**
         * Performs on-the-fly validation of the health report thresholds.
         *
         * @param healthy
         *         the healthy threshold
         * @param unHealthy
         *         the unhealthy threshold
         *
         * @return the validation result
         */
        public FormValidation doCheckHealthy(@QueryParameter final int healthy, @QueryParameter final int unHealthy) {
            if (healthy == 0 && unHealthy > 0) {
                return FormValidation.error(Messages.FieldValidator_Error_ThresholdHealthyMissing());
            }
            return validateHealthReportConstraints(healthy, healthy, unHealthy);
        }

        /**
         * Performs on-the-fly validation of the health report thresholds.
         *
         * @param healthy
         *         the healthy threshold
         * @param unHealthy
         *         the unhealthy threshold
         *
         * @return the validation result
         */
        public FormValidation doCheckUnHealthy(@QueryParameter final int healthy, @QueryParameter final int unHealthy) {
            if (healthy > 0 && unHealthy == 0) {
                return FormValidation.error(Messages.FieldValidator_Error_ThresholdUnhealthyMissing());
            }
            return validateHealthReportConstraints(unHealthy, healthy, unHealthy);
        }

        private FormValidation validateHealthReportConstraints(final int nonNegative,
                final int healthy, final int unHealthy) {
            if (nonNegative < 0) {
                return FormValidation.error(Messages.FieldValidator_Error_NegativeThreshold());
            }
            if (healthy == 0 && unHealthy == 0) {
                return FormValidation.ok();
            }
            if (healthy >= unHealthy) {
                return FormValidation.error(Messages.FieldValidator_Error_ThresholdOrder());
            }
            return FormValidation.ok();
        }
    }
}
