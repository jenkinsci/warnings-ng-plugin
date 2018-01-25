package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Issues.IssueFilterBuilder;
import io.jenkins.plugins.analysis.core.history.BuildHistory;
import io.jenkins.plugins.analysis.core.history.ReferenceFinder;
import io.jenkins.plugins.analysis.core.history.ReferenceProvider;
import io.jenkins.plugins.analysis.core.history.ResultSelector;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ByIdResultSelector;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.Thresholds;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.LoggerFactory;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.remoting.VirtualChannel;

/**
 * Publish issues created by a static analysis run. The recorded issues are stored as a {@link ResultAction} in the
 * associated run. If the set of issues to store has a unique ID, then the created action will use this ID as well.
 * Otherwise a default ID is used to publish the results. In any case, the computed ID can be overwritten by specifying
 * an ID as step parameter.
 */
@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class PublishIssuesStep extends Step {
    private static final String DEFAULT_ID = "analysis";
    private static final String DEFAULT_MINIMUM_PRIORITY = "low";

    private final Issues<Issue> issues;

    private boolean usePreviousBuildAsReference;
    private boolean useStableBuildAsReference;

    private String defaultEncoding;

    private String healthy;
    private String unHealthy;
    private String minimumPriority = DEFAULT_MINIMUM_PRIORITY;

    private final Thresholds thresholds = new Thresholds();
    private String id;
    private String name;

    /**
     * Creates a new instance of {@link PublishIssuesStep}.
     *
     * @param issues
     *         the issues to publish as {@link Action} in the {@link Job}.
     */
    @DataBoundConstructor @SafeVarargs
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
     * Defines the ID of the results. The ID is used as URL of the results and as name in UI elements. If
     * no ID is given, then the ID of the associated result object is used.
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
     * Defines the name of the results. The name is used for all labels in the UI. If
     * no name is given, then the name of the associated {@link StaticAnalysisLabelProvider} is used.
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

    public boolean getUsePreviousBuildAsReference() {
        return usePreviousBuildAsReference;
    }

    // TODO: use same naming as in BuildHistory?

    /**
     * Determines if the previous build should always be used as the reference build, no matter of its overall result.
     *
     * @param usePreviousBuildAsReference
     *         if {@code true} then the previous build is always used
     */
    @DataBoundSetter
    public void setUsePreviousBuildAsReference(final boolean usePreviousBuildAsReference) {
        this.usePreviousBuildAsReference = usePreviousBuildAsReference;
    }

    public boolean getUseStableBuildAsReference() {
        return useStableBuildAsReference;
    }

    /**
     * Determines whether only stable builds should be used as reference builds or not.
     *
     * @param useStableBuildAsReference
     *         if {@code true} then a stable build is used as reference
     */
    @DataBoundSetter
    public void setUseStableBuildAsReference(final boolean useStableBuildAsReference) {
        this.useStableBuildAsReference = useStableBuildAsReference;
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
    public void setFilters(final RegexpFilter[] filters) {
        if (filters != null && filters.length > 0) {
            this.filters.addAll(Arrays.asList(filters));
        }
    }

    @Override
    public StepExecution start(final StepContext stepContext) {
        return new Execution(stepContext, this);
    }

    /**
     * Actually performs the execution of the associated step.
     */
    public static class Execution extends SynchronousNonBlockingStepExecution<ResultAction> {
        private final HealthDescriptor healthDescriptor;
        private final boolean useStableBuildAsReference;
        private final boolean usePreviousBuildAsReference;
        private final String defaultEncoding;
        private final Issues<Issue> issues;
        private final QualityGate qualityGate;
        private final RegexpFilter[] filters;
        private final String name;

        protected Execution(@Nonnull final StepContext context, final PublishIssuesStep step) {
            super(context);

            usePreviousBuildAsReference = step.getUsePreviousBuildAsReference();
            useStableBuildAsReference = step.getUseStableBuildAsReference();
            defaultEncoding = step.getDefaultEncoding();
            healthDescriptor = new HealthDescriptor(step.getHealthy(), step.getUnHealthy(), step.getMinimumPriority());

            qualityGate = new QualityGate(step.getThresholds());
            name = StringUtils.defaultString(step.getName());
            issues = step.getIssues();
            if (StringUtils.isNotBlank(step.getId())) {
                issues.setId(step.getId());
            }
            filters = step.getFilters();
        }

        @Override
        protected ResultAction run() throws IOException, InterruptedException, IllegalStateException {
            ResultSelector selector = new ByIdResultSelector(issues.getId());
            Run<?, ?> run = getRun();
            Optional<ResultAction> other = selector.get(run);
            if (other.isPresent()) {
                throw new IllegalStateException(String.format("ID %s is already used by another action: %s%n",
                        issues.getId(), other.get()));
            }

            return publishResult(run, selector);
        }

        private Logger createLogger() throws IOException, InterruptedException {
            TaskListener listener = getContext().get(TaskListener.class);

            return new LoggerFactory().createLogger(listener.getLogger(), getTool(issues.getId()).getName());
        }

        private StaticAnalysisLabelProvider getTool(final String toolId) {
            return new LabelProviderFactory().findLabelProvider(toolId, name);
        }

        private Run<?, ?> getRun() throws IOException, InterruptedException {
            return getContext().get(Run.class);
        }

        private VirtualChannel getChannel() throws IOException, InterruptedException {
            return getContext().get(Computer.class).getChannel();
        }

        private ResultAction publishResult(final Run<?, ?> run, final ResultSelector selector)
                throws IOException, InterruptedException {
            Logger logger = createLogger();

            Instant startResult = Instant.now();
            IssueFilterBuilder builder = new IssueFilterBuilder();
            for (RegexpFilter filter : filters) {
                filter.apply(builder);
            }
            Issues<Issue> filtered = issues.filter(builder.build());
            logger.log("Applying %d filters on the set of %d issues (%d issues have been removed)",
                    filters.length, issues.size(), issues.size() - filtered.size());

            AnalysisResult result = createAnalysisResult(filtered, run, selector);
            logger.log("Created analysis result for %d issues (found %d new issues, fixed %d issues)",
                    result.getTotalSize(), result.getNewSize(), result.getFixedSize());
            logger.log("Creating analysis result took %s", getElapsedTime(startResult));

            Result pluginResult = result.getPluginResult();

            if (qualityGate.isEnabled()) {
                if (pluginResult.isBetterOrEqualTo(Result.SUCCESS)) {
                    logger.log("All quality gates have been passed");
                }
                else {
                    logger.log("Some quality gates have been missed: overall result is %s", pluginResult);
                }
            }
            else {
                    logger.log("No quality gates have been set - skipping");
            }

            FilePath workspace = getContext().get(FilePath.class);
            Set<String> files = result.getIssues().getFiles();

            Instant startCopy = Instant.now();
            String copyingLogMessage = new AffectedFilesResolver().copyFilesWithAnnotationsToBuildFolder(getChannel(),
                    getBuildFolder(), EncodingValidator.getEncoding(defaultEncoding), files);
            logger.log("Copied %d affected files from '%s' to build folder (%s)",
                    files.size(), workspace, copyingLogMessage);
            logger.log("Copying affected files took %s", getElapsedTime(startCopy));

            String id = filtered.getId();
            logger.log("Attaching ResultAction with ID '%s' to run '%s'.", id, run);
            ResultAction action = new ResultAction(run, result, healthDescriptor, id, name);
            run.addAction(action);

            return action;
        }

        private Duration getElapsedTime(final Instant startResult) {
            return Duration.between(startResult, Instant.now());
        }

        private FilePath getBuildFolder() throws IOException, InterruptedException {
            return new FilePath(getRun().getRootDir());
        }

        private AnalysisResult createAnalysisResult(final Issues<Issue> filtered,
                final Run<?, ?> run, final ResultSelector selector) {
            ReferenceProvider referenceProvider = ReferenceFinder.create(run,
                    selector, usePreviousBuildAsReference, useStableBuildAsReference);
            BuildHistory buildHistory = new BuildHistory(run, selector);
            return new AnalysisResult(name, run, referenceProvider, buildHistory.getPreviousResult(),
                    qualityGate, defaultEncoding, filtered);
        }
    }

    // TODO: i18n
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

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Publish issues created by a static analysis run";
        }
    }
}
