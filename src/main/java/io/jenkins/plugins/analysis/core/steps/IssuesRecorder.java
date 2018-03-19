package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.Lists;

import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import io.jenkins.plugins.analysis.core.model.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.Thresholds;
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.LoggerFactory;
import io.jenkins.plugins.analysis.core.views.ResultAction;
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
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ComboBoxModel;
import hudson.util.ListBoxModel;

/**
 * Freestyle or Maven job {@link Recorder} that scans files or the console log for issues. Publishes the created issues
 * in a {@link ResultAction} in the associated run.
 *
 * @author Ullrich Hafner
 */
public class IssuesRecorder extends Recorder implements SimpleBuildStep {
    private static final String DEFAULT_MINIMUM_PRIORITY = Priority.LOW.name();

    private String reportEncoding;
    private String sourceCodeEncoding;
    private String pattern;
    private StaticAnalysisTool tool;

    private boolean ignoreAnalysisResult;
    private boolean overallResultMustBeSuccess;
    private String referenceJobName;

    private String healthy;
    private String unHealthy;
    private String minimumPriority = DEFAULT_MINIMUM_PRIORITY;
    private final Thresholds thresholds = new Thresholds();

    private String id;
    private String name;

    @DataBoundConstructor
    public IssuesRecorder() {
        // empty constructor required for Stapler
    }

    @CheckForNull
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the Ant file-set pattern of files to work with. If the pattern is undefined then the console log is
     * scanned.
     *
     * @param pattern
     *         the pattern to use
     */
    @DataBoundSetter
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    @CheckForNull
    public StaticAnalysisTool getTool() {
        return tool;
    }

    /**
     * Sets the static analysis tool that will scan files and create issues.
     *
     * @param tool
     *         the static analysis tool
     */
    @DataBoundSetter
    public void setTool(final StaticAnalysisTool tool) {
        this.tool = tool;
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

    /** ------------------------------------------------------------ */

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
        this.referenceJobName = referenceJobName;
    }

    public String getReferenceJobName() {
        return referenceJobName;
    }

    @CheckForNull
    public String getHealthy() {
        return healthy;
    }

    /**
     * Sets the healthy threshold, i.e. the number of issues when health is reported as 100%.
     *
     * @param healthy
     *         the number of issues when health is reported as 100%
     */
    @DataBoundSetter
    public void setHealthy(final String healthy) {
        this.healthy = healthy;
    }

    @CheckForNull
    public String getUnHealthy() {
        return unHealthy;
    }

    /**
     * Sets the healthy threshold, i.e. the number of issues when health is reported as 0%.
     *
     * @param unHealthy
     *         the number of issues when health is reported as 0%
     */
    @DataBoundSetter
    public void setUnHealthy(final String unHealthy) {
        this.unHealthy = unHealthy;
    }

    @CheckForNull
    public String getMinimumPriority() {
        return minimumPriority;
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
        this.minimumPriority = StringUtils.defaultIfEmpty(minimumPriority, DEFAULT_MINIMUM_PRIORITY);
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

    private final List<RegexpFilter> filters = Lists.newArrayList();

    public RegexpFilter[] getFilters() {
        return filters.toArray(new RegexpFilter[filters.size()]);
    }

    @DataBoundSetter
    public void setFilters(final RegexpFilter[] filters) { // FIXME: why not a collection?
        if (filters != null && filters.length > 0) {
            this.filters.addAll(Arrays.asList(filters));
        }
    }

    /** ------------------------------------------------------------ */

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
            throws InterruptedException, IOException {
        Logger logger = createLogger(listener, tool.getId());
        Logger errorLogger = createLogger(listener, String.format("[%s] [ERROR]", tool.getId()));

        IssuesScanner issuesScanner = new IssuesScanner(tool, workspace, reportEncoding, sourceCodeEncoding,
                logger, errorLogger);
        Issues<?> issues;
        if (StringUtils.isBlank(pattern)) {
            issues = issuesScanner.scanInConsoleLog(run.getLogFile());
        }
        else {
            issues = issuesScanner.scanInWorkspace(pattern, run.getEnvironment(listener));
        }

        IssuesPublisher publisher = new IssuesPublisher(issues, getFilters(), run, workspace,
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

        public ComboBoxModel doFillReportEncodingItems() {
            return new ComboBoxModel(EncodingValidator.getAvailableCharsets());
        }

        public ComboBoxModel doFillSourceCodeEncodingItems() {
            return doFillReportEncodingItems();
        }

        public ListBoxModel doFillMinimumPriorityItems() {
            ListBoxModel options = new ListBoxModel();
            options.add(Messages.PriorityFilter_High(), Priority.HIGH.name());
            options.add(Messages.PriorityFilter_Normal(), Priority.NORMAL.name());
            options.add(Messages.PriorityFilter_Low(), Priority.LOW.name());
            return options;
        }

        public ComboBoxModel doFillReferenceJobItems() {
            // replace this with whitespace
            return Jenkins.getInstance().getAllItems(Job.class).stream()
                    .map(AbstractItem::getFullName)
                    .distinct()
                    .collect(Collectors.toCollection(ComboBoxModel::new));
        }

    }
}
