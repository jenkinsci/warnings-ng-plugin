package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashSet;
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
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.Thresholds;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Publish issues created by a static analysis run. The recorded issues are stored as a {@link ResultAction} in
 * the associated run.
 */
public class PublishIssuesStep extends Step {
    private static final String DEFAULT_MINIMUM_PRIORITY = "low";

    private final ParserResult[] issues;

    private boolean usePreviousBuildAsReference;
    private boolean useStableBuildAsReference;

    private String defaultEncoding;

    private String healthy;
    private String unHealthy;
    private String minimumPriority = DEFAULT_MINIMUM_PRIORITY;
    private Thresholds thresholds = new Thresholds();

    /**
     * Creates a new instance of {@link PublishIssuesStep}.
     *
     * @param issues
     *         the issues to publish as {@link Action} in the {@link Job}.
     */
    @DataBoundConstructor
    public PublishIssuesStep(final ParserResult... issues) {
        this.issues = issues;

        if (issues == null || issues.length == 0) {
            throw new IllegalArgumentException("No issues provided");
        }
    }

    public ParserResult[] getIssues() {
        return issues;
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
    public Thresholds getThresholds() {
        return thresholds;
    }

    /**
     * Sets the result threshold, i.e. the number of issues when to set the build result to {@link Result#SUCCESS},
     * {@link Result#UNSTABLE}, or {@link Result#FAILURE}.
     *
     * @param thresholds
     *         the number of issues required to change the build status
     */
    @DataBoundSetter
    public void setThresholds(final Thresholds thresholds) {
        this.thresholds = thresholds;
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
        private final ParserResult[] warnings;

        protected Execution(@Nonnull final StepContext context, final PublishIssuesStep step) {
            super(context);

            usePreviousBuildAsReference = step.usePreviousBuildAsReference;
            useStableBuildAsReference = step.useStableBuildAsReference;
            defaultEncoding = step.defaultEncoding;
            healthDescriptor = new HealthDescriptor(step.healthy, step.unHealthy, getMinimumPriority(step.minimumPriority));
            thresholds = step.thresholds;

            warnings = step.issues;
            if (warnings == null) {
                throw new NullPointerException("No warnings provided to step " + step);
            }
        }

        private Priority getMinimumPriority(final String minimumPriority) {
            return Priority.valueOf(StringUtils.upperCase(minimumPriority));
        }

        @Override
        protected ResultAction run() throws Exception {
            Set<String> ids = new HashSet<>();
            for (ParserResult result : warnings) {
                ids.add(result.getId());
            }

            if (ids.size() == 1) {
                return publishSingleParserResult(ids.iterator().next());
            }
            else {
                return publishMultipleParserResults();
            }
        }

        private ResultAction publishMultipleParserResults() throws IOException, InterruptedException {
            String id = "staticAnalysis";
            ResultSelector selector = new ByIdResultSelector(id);
            Run run = getRun();
            // FIXME: enable check again
            // ResultAction<? extends BuildResult> other = selector.get(run);
            // if (other != null) {
            //    throw new IllegalStateException("There is already an action registered with ID " + id);
            // }
            return publishResult(id, run, selector);
        }

        private ResultAction publishSingleParserResult(final String id) throws IOException, InterruptedException {
            return publishResult(id, getRun(), new ByIdResultSelector(id));
        }

        private Run getRun() throws IOException, InterruptedException {
            return getContext().get(Run.class);
        }

        private ResultAction publishResult(final String id, final Run run, final ResultSelector selector) throws IOException, InterruptedException {
            ReferenceProvider referenceProvider = ReferenceFinder.create(run,
                    selector, usePreviousBuildAsReference, useStableBuildAsReference);
            BuildHistory buildHistory = new BuildHistory(run, selector);

            TaskListener logger = getContext().get(TaskListener.class);
            ResultEvaluator resultEvaluator = new ResultEvaluator(id, thresholds, new PluginLogger(logger.getLogger(), id));

            AnalysisResult result = new AnalysisResult(id, run, referenceProvider, buildHistory.getPreviousResult(),
                    resultEvaluator, defaultEncoding, warnings);

            ResultAction action = new ResultAction(run, id, result, healthDescriptor);
            run.addAction(action);

            return action;
        }
    }

    // TODO: i18n
    @Extension
    public static class Descriptor extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Sets.newHashSet(Run.class, TaskListener.class);
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
