package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.Sets;

import io.jenkins.plugins.analysis.core.history.BuildHistory;
import io.jenkins.plugins.analysis.core.history.ReferenceFinder;
import io.jenkins.plugins.analysis.core.history.ReferenceProvider;
import io.jenkins.plugins.analysis.core.history.ResultSelector;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.Thresholds;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.plugins.analysis.util.Files;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.Priority;
import hudson.remoting.VirtualChannel;

/**
 * Publish issues created by a static analysis run. The recorded issues are stored as a {@link ResultAction} in the
 * associated run.
 */
@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class PublishIssuesStep extends Step {
    private static final String DEFAULT_MINIMUM_PRIORITY = "low";

    private final ParserResult[] issues;

    private boolean usePreviousBuildAsReference;
    private boolean useStableBuildAsReference;

    private String defaultEncoding;

    private String healthy;
    private String unHealthy;
    private String minimumPriority = DEFAULT_MINIMUM_PRIORITY;

    private final Thresholds thresholds = new Thresholds();
    private String id;

    /**
     * Creates a new instance of {@link PublishIssuesStep}.
     *
     * @param issues
     *         the issues to publish as {@link Action} in the {@link Job}.
     */
    @DataBoundConstructor
    public PublishIssuesStep(final ParserResult... issues) {
        this.issues = issues;
    }

    public ParserResult[] getIssues() {
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

    @CheckForNull
    public String getUnstableTotalAll() {
        return thresholds.unstableTotalAll;
    }

    @DataBoundSetter
    public void setUnstableTotalAll(final String unstableTotalAll) {
        thresholds.unstableTotalAll = unstableTotalAll;
    }

    @CheckForNull
    public String getUnstableTotalHigh() {
        return thresholds.unstableTotalHigh;
    }

    @DataBoundSetter
    public void setUnstableTotalHigh(final String unstableTotalHigh) {
        thresholds.unstableTotalHigh = unstableTotalHigh;
    }

    @CheckForNull
    public String getUnstableTotalNormal() {
        return thresholds.unstableTotalNormal;
    }

    @DataBoundSetter
    public void setUnstableTotalNormal(final String unstableTotalNormal) {
        thresholds.unstableTotalNormal = unstableTotalNormal;
    }

    @CheckForNull
    public String getUnstableTotalLow() {
        return thresholds.unstableTotalLow;
    }

    @DataBoundSetter
    public void setUnstableTotalLow(final String unstableTotalLow) {
        thresholds.unstableTotalLow = unstableTotalLow;
    }

    @CheckForNull
    public String getUnstableNewAll() {
        return thresholds.unstableNewAll;
    }

    @DataBoundSetter
    public void setUnstableNewAll(final String unstableNewAll) {
        thresholds.unstableNewAll = unstableNewAll;
    }

    @CheckForNull
    public String getUnstableNewHigh() {
        return thresholds.unstableNewHigh;
    }

    @DataBoundSetter
    public void setUnstableNewHigh(final String unstableNewHigh) {
        thresholds.unstableNewHigh = unstableNewHigh;
    }

    @CheckForNull
    public String getUnstableNewNormal() {
        return thresholds.unstableNewNormal;
    }

    @DataBoundSetter
    public void setUnstableNewNormal(final String unstableNewNormal) {
        thresholds.unstableNewNormal = unstableNewNormal;
    }

    @CheckForNull
    public String getUnstableNewLow() {
        return thresholds.unstableNewLow;
    }

    @DataBoundSetter
    public void setUnstableNewLow(final String unstableNewLow) {
        thresholds.unstableNewLow = unstableNewLow;
    }

    @CheckForNull
    public String getFailedTotalAll() {
        return thresholds.failedTotalAll;
    }

    @DataBoundSetter
    public void setFailedTotalAll(final String failedTotalAll) {
        thresholds.failedTotalAll = failedTotalAll;
    }

    @CheckForNull
    public String getFailedTotalHigh() {
        return thresholds.failedTotalHigh;
    }

    @DataBoundSetter
    public void setFailedTotalHigh(final String failedTotalHigh) {
        thresholds.failedTotalHigh = failedTotalHigh;
    }

    @CheckForNull
    public String getFailedTotalNormal() {
        return thresholds.failedTotalNormal;
    }

    @DataBoundSetter
    public void setFailedTotalNormal(final String failedTotalNormal) {
        thresholds.failedTotalNormal = failedTotalNormal;
    }

    @CheckForNull
    public String getFailedTotalLow() {
        return thresholds.failedTotalLow;
    }

    @DataBoundSetter
    public void setFailedTotalLow(final String failedTotalLow) {
        thresholds.failedTotalLow = failedTotalLow;
    }

    @CheckForNull
    public String getFailedNewAll() {
        return thresholds.failedNewAll;
    }

    @DataBoundSetter
    public void setFailedNewAll(final String failedNewAll) {
        thresholds.failedNewAll = failedNewAll;
    }

    @CheckForNull
    public String getFailedNewHigh() {
        return thresholds.failedNewHigh;
    }

    @DataBoundSetter
    public void setFailedNewHigh(final String failedNewHigh) {
        thresholds.failedNewHigh = failedNewHigh;
    }

    @CheckForNull
    public String getFailedNewNormal() {
        return thresholds.failedNewNormal;
    }

    @DataBoundSetter
    public void setFailedNewNormal(final String failedNewNormal) {
        thresholds.failedNewNormal = failedNewNormal;
    }

    @CheckForNull
    public String getFailedNewLow() {
        return thresholds.failedNewLow;
    }

    @DataBoundSetter
    public void setFailedNewLow(final String failedNewLow) {
        thresholds.failedNewLow = failedNewLow;
    }

    @Override
    public StepExecution start(final StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    public static class Execution extends SynchronousNonBlockingStepExecution<ResultAction> {
        private final HealthDescriptor healthDescriptor;
        private final Thresholds thresholds;
        private final boolean useStableBuildAsReference;
        private final boolean usePreviousBuildAsReference;
        private final String defaultEncoding;
        private final ParserResult[] issues;

        protected Execution(@Nonnull final StepContext context, final PublishIssuesStep step) {
            super(context);

            usePreviousBuildAsReference = step.usePreviousBuildAsReference;
            useStableBuildAsReference = step.useStableBuildAsReference;
            defaultEncoding = step.defaultEncoding;
            healthDescriptor = new HealthDescriptor(step.healthy, step.unHealthy, getMinimumPriority(step.minimumPriority));
            thresholds = step.thresholds;

            issues = step.issues;
            if (issues == null || issues.length == 0) {
                throw new NullPointerException("No issues to publish in step " + step);
            }
        }

        private Priority getMinimumPriority(final String minimumPriority) {
            return Priority.valueOf(StringUtils.upperCase(minimumPriority));
        }

        @Override
        protected ResultAction run() throws Exception {
            Set<String> ids = new HashSet<>();
            for (ParserResult result : issues) {
                ids.add(result.getId());
            }

            if (ids.size() == 1) {
                return publishSingleParserResult(ids.iterator().next());
            }
            else {
                return publishMultipleParserResults();
            }
        }

        private PluginLogger createPluginLogger(final String id) throws IOException, InterruptedException {
            TaskListener logger = getContext().get(TaskListener.class);
            return new PluginLogger(logger.getLogger(), StaticAnalysisTool.find(id).getName());
        }

        private Run getRun() throws IOException, InterruptedException {
            return getContext().get(Run.class);
        }

        private VirtualChannel getChannel() throws IOException, InterruptedException {
            return getContext().get(Computer.class).getChannel();
        }

        private ResultAction publishMultipleParserResults() throws IOException, InterruptedException {
            String id = "staticAnalysis";
            ResultSelector selector = new ByIdResultSelector(id);
            Run run = getRun();
            Optional<ResultAction> other = selector.get(run);
            if (other.isPresent()) {
                throw new IllegalStateException(String.format("ID %s is already used by another action: %s%n", id, other.get()));
            }
            return publishResult(id, run, selector);
        }

        private ResultAction publishSingleParserResult(final String id) throws IOException, InterruptedException {
            return publishResult(id, getRun(), new ByIdResultSelector(id));
        }

        private ResultAction publishResult(final String id, final Run run, final ResultSelector selector)
                throws IOException, InterruptedException {
            PluginLogger logger = createPluginLogger(id);

            logger.format("Creating analysis result for %d issues.", getTotalNumberOfIssues());
            AnalysisResult result = createAnalysisResult(id, run, selector);

            FilePath workspace = getContext().get(FilePath.class);
            AnnotationContainer container = result.getContainer();
            logger.format("Copying %d affected files from '%s' to build folder", container.getFiles().size(), workspace);

            new Files().copyFilesWithAnnotationsToBuildFolder(getChannel(), workspace,
                    container.getAnnotations(), EncodingValidator.getEncoding(defaultEncoding));

            logger.format("Attaching ResultAction with ID '%s' to run '%s'.", id, run);
            ResultAction action = new ResultAction(run, id, result, healthDescriptor);
            run.addAction(action);

            return action;
        }

        private AnalysisResult createAnalysisResult(final String id, final Run run, final ResultSelector selector)
                throws IOException, InterruptedException {
            ReferenceProvider referenceProvider = ReferenceFinder.create(run,
                    selector, usePreviousBuildAsReference, useStableBuildAsReference);
            BuildHistory buildHistory = new BuildHistory(run, selector);
            ResultEvaluator resultEvaluator = new ResultEvaluator(id, thresholds, createPluginLogger(id));
            return new AnalysisResult(id, run, referenceProvider, buildHistory.getPreviousResult(),
                    resultEvaluator, defaultEncoding, issues);
        }

        public int getTotalNumberOfIssues() {
            int sum = 0;
            for (ParserResult result : issues) {
                sum += result.getNumberOfAnnotations();
            }
            return sum;
        }
    }

    // TODO: i18n
    @Extension
    public static class Descriptor extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Sets.newHashSet(Run.class, TaskListener.class, Computer.class);
        }

        @Override
        public String getFunctionName() {
            return "publishIssues";
        }

        @Override
        public String getDisplayName() {
            return "Publish issues created by a static analysis run";
        }
    }
}
