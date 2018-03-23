package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.analysis.core.model.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.Thresholds;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

/**
 * Publish issues created by a static analysis run. The recorded issues are stored as a {@link ResultAction} in the
 * associated run. If the set of issues to store has a unique ID, then the created action will use this ID as well.
 * Otherwise a default ID is used to publish the results. In any case, the computed ID can be overwritten by specifying
 * an ID as step parameter.
 */
@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class PublishIssuesStep extends Step {
    private static final Priority DEFAULT_MINIMUM_PRIORITY = Priority.LOW;

    private final Issues<Issue> issues;

    private boolean ignoreAnalysisResult;
    private boolean overallResultMustBeSuccess;

    private String defaultEncoding;

    private int healthy;
    private int unHealthy;
    private Priority minimumPriority = DEFAULT_MINIMUM_PRIORITY;
    private final Thresholds thresholds = new Thresholds();

    private String id;
    private String name;
    private String referenceJobName;

    /**
     * Creates a new instance of {@link PublishIssuesStep}.
     *
     * @param issues
     *         the issues to publish as {@link Action} in the {@link Job}.
     */
    @DataBoundConstructor
    @SafeVarargs
    public PublishIssuesStep(final Issues<Issue>... issues) {
        if (issues == null || issues.length == 0) {
            this.issues = new Issues<>();
        }
        else {
            this.issues = new Issues<>();
            for (Issues<Issue> issueSet : issues) {
                this.issues.addAll(issueSet);
            }
        }
    }

    public Issues<Issue> getIssues() {
        return issues;
    }

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
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Sets the default encoding used to read files (warnings, source code, etc.).
     *
     * @param defaultEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setDefaultEncoding(final String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
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

    @CheckForNull
    public Priority getMinimumPriorityAsPriority() {
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

    @Override
    public StepExecution start(final StepContext stepContext) {
        return new Execution(stepContext, this);
    }

    /**
     * Actually performs the execution of the associated step.
     */
    public static class Execution extends AnalysisExecution<ResultAction> {
        private final HealthDescriptor healthDescriptor;
        private final boolean overallResultMustBeSuccess;
        private final boolean ignoreAnalysisResult;
        private final String sourceCodeEncoding;
        private final Issues<Issue> issues;
        private final QualityGate qualityGate;
        private final List<RegexpFilter> filters;
        private final String name;
        private final Thresholds thresholds;
        private final String referenceJobName;

        protected Execution(@NonNull final StepContext context, final PublishIssuesStep step) {
            super(context);

            ignoreAnalysisResult = step.getIgnoreAnalysisResult();
            overallResultMustBeSuccess = step.getOverallResultMustBeSuccess();
            referenceJobName = step.getReferenceJobName();
            sourceCodeEncoding = step.getDefaultEncoding();
            healthDescriptor = new HealthDescriptor(step.getHealthy(), step.getUnHealthy(), step.getMinimumPriorityAsPriority());

            thresholds = step.getThresholds();
            qualityGate = new QualityGate(thresholds);
            name = StringUtils.defaultString(step.getName());
            issues = step.getIssues();
            if (StringUtils.isNotBlank(step.getId())) {
                issues.setId(step.getId());
            }
            filters = step.getFilters();
        }

        @Override
        protected ResultAction run() throws IOException, InterruptedException, IllegalStateException {
            IssuesPublisher publisher = new IssuesPublisher(getRun(), issues, filters, healthDescriptor, qualityGate,
                    getWorkspace(),
                    name, referenceJobName, ignoreAnalysisResult, overallResultMustBeSuccess, sourceCodeEncoding,
                    getTaskListener());
            Optional<VirtualChannel> channel = getChannel();
            if (channel.isPresent()) {
                return publisher.attachAction(channel.get(), getBuildFolder());

            }
            else {
                return publisher.attachAction();
            }
        }
    }

    /**
     * Descriptor for this step: defines the context and the UI labels.
     */
    @Extension
    public static class Descriptor extends StepDescriptor {
        @Override
        public Set<Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class, Computer.class);
        }

        @Override
        public String getFunctionName() {
            return "publishIssues";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.PublishIssues_DisplayName();
        }
    }
}
