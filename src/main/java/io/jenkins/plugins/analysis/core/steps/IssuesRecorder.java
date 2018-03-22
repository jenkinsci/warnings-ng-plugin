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
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.HealthReportBuilder;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.Thresholds;
import io.jenkins.plugins.analysis.core.util.EnvironmentResolver;
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.LoggerFactory;
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
    private static final String NO_REFERENCE_JOB = "-";

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

    private String id;
    private String name;

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
     * Defines the ID of the results. The ID is used as URL of the results and as name in UI elements. If no ID is
     * given, then the ID of the associated result object is used.
     *
     * @param id
     *         the ID of the results
     */
    @DataBoundSetter
    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Defines the name of the results. The name is used for all labels in the UI. If no name is given, then the name of
     * the associated {@link StaticAnalysisLabelProvider} is used.
     *
     * @param name
     *         the name of the results
     */
    @DataBoundSetter
    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
        Issues<Issue> totalIssues = new Issues<>();
        for (ToolConfiguration toolConfiguration : tools) {
            totalIssues.addAll(scanWithTool(run, workspace, listener, toolConfiguration));
        }

        Logger logger = createLogger(listener, totalIssues.getId());
        Logger errorLogger = createLogger(listener, String.format("[%s] [ERROR]", totalIssues.getId()));
        IssuesPublisher publisher = new IssuesPublisher(totalIssues, getFilters(), run, workspace,
                new HealthDescriptor(healthy, unHealthy, minimumPriority),
                name, sourceCodeEncoding, new QualityGate(thresholds), referenceJobName, ignoreAnalysisResult,
                overallResultMustBeSuccess, logger, errorLogger);
        VirtualChannel channel = launcher.getChannel();
        if (channel != null) {
            publisher.attachAction(channel, new FilePath(run.getRootDir()));
        }
        else {
            publisher.attachAction();
        }
    }

    private Issues<?> scanWithTool(final @Nonnull Run<?, ?> run, final @Nonnull FilePath workspace,
            final @Nonnull TaskListener listener, final ToolConfiguration toolConfiguration)
            throws IOException, InterruptedException {
        StaticAnalysisTool tool = toolConfiguration.getTool();
        String pattern = toolConfiguration.getPattern();
        Logger logger = createLogger(listener, tool.getId());
        Logger errorLogger = createLogger(listener, String.format("[%s] [ERROR]", tool.getId()));

        IssuesScanner issuesScanner
                = new IssuesScanner(tool, workspace, reportEncoding, sourceCodeEncoding, logger, errorLogger);
        if (StringUtils.isBlank(pattern)) {
            return issuesScanner.scanInConsoleLog(run.getLogFile());
        }
        else {
            String expanded = new EnvironmentResolver()
                    .expandEnvironmentVariables(run.getEnvironment(listener), pattern);

            return issuesScanner.scanInWorkspace(expanded);
        }
    }

    private Logger createLogger(final TaskListener listener, final String name) {
        return new LoggerFactory().createLogger(listener.getLogger(), name);
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
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
            ComboBoxModel model = Jenkins.getInstance().getAllItems(Job.class).stream()
                    .map(AbstractItem::getFullName)
                    .distinct()
                    .collect(Collectors.toCollection(ComboBoxModel::new));
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
                    || new JenkinsFacade().getJob(referenceJob).isPresent()) {
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
