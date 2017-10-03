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
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.history.ReferenceFinder;
import io.jenkins.plugins.analysis.core.history.ReferenceProvider;
import io.jenkins.plugins.analysis.core.history.ResultSelector;

import hudson.Extension;
import hudson.model.Run;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.Priority;

/*
 TODO:

 */
public class PublishWarningsStep extends Step {
    private static final String DEFAULT_MINIMUM_PRIORITY = "low";

    private ParserResult[] warnings;

    private boolean usePreviousBuildAsReference;
    private boolean useStableBuildAsReference;

    private String defaultEncoding;

    /** Report health as 100% when the number of warnings is less than this value. */
    private String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private String unHealthy;
    /** Determines which warning priorities should be considered when evaluating the build health. */
    private String minimumPriority = DEFAULT_MINIMUM_PRIORITY;

    @DataBoundConstructor
    public PublishWarningsStep(final ParserResult... warnings) {
        this.warnings = warnings;

        if (warnings == null || warnings.length == 0) {
            throw new IllegalArgumentException("No warnings provided");
        }
    }

    public ParserResult[] getWarnings() {
        return warnings;
    }

    public boolean getUsePreviousBuildAsReference() {
        return usePreviousBuildAsReference;
    }

    /**
     * Determines if the previous build should always be used as the reference build, no matter its overall result.
     *
     * @param usePreviousBuildAsReference if {@code true} then the previous build is always used
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
     * @param useStableBuildAsReference if {@code true} then a stable build is used as reference
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
     * @param defaultEncoding the encoding, e.g. "ISO-8859-1"
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
     * @param healthy the number of issues when health is reported as 100%
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
     * @param unHealthy the number of issues when health is reported as 0%
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
     * @param minimumPriority the priority to consider
     */
    @DataBoundSetter
    public void setMinimumPriority(final String minimumPriority) {
        this.minimumPriority = StringUtils.defaultIfEmpty(minimumPriority, DEFAULT_MINIMUM_PRIORITY);
    }

    @Override
    public StepExecution start(final StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    public static class Execution extends SynchronousNonBlockingStepExecution<PipelineResultAction> {
        private final HealthDescriptor healthDescriptor;
        private boolean useStableBuildAsReference;
        private boolean usePreviousBuildAsReference;
        private String defaultEncoding;
        private ParserResult[] warnings;

        protected Execution(@Nonnull final StepContext context, final PublishWarningsStep step) {
            super(context);

            usePreviousBuildAsReference = step.usePreviousBuildAsReference;
            useStableBuildAsReference = step.useStableBuildAsReference;
            defaultEncoding = step.defaultEncoding;
            healthDescriptor = new HealthDescriptor(step.healthy, step.unHealthy, getMinimumPriority(step.minimumPriority));

            warnings = step.warnings;
            if (warnings == null) {
                throw new NullPointerException("No warnings provided.");
            }
        }

        private Priority getMinimumPriority(final String minimumPriority) {
            return Priority.valueOf(StringUtils.upperCase(minimumPriority));
        }

        @Override
        protected PipelineResultAction run() throws Exception {
            Set<String> ids = new HashSet<String>();
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

        private PipelineResultAction publishMultipleParserResults() throws IOException, InterruptedException {
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

        private PipelineResultAction publishSingleParserResult(final String id) throws IOException, InterruptedException {
            return publishResult(id, getRun(), new ByIdResultSelector(id));
        }

        private Run getRun() throws IOException, InterruptedException {
            return getContext().get(Run.class);
        }

        private PipelineResultAction publishResult(final String id, final Run run, final ResultSelector selector) {
            ReferenceProvider referenceProvider = ReferenceFinder.create(run,
                    selector, usePreviousBuildAsReference, useStableBuildAsReference);
            BuildHistory buildHistory = new BuildHistory(run, selector);
            AnalysisResult result = new AnalysisResult(run, defaultEncoding, warnings, referenceProvider, buildHistory, id);

            PipelineResultAction action = new PipelineResultAction(run, result, id, healthDescriptor);
            run.addAction(action);

            return action;
        }
    }

    @Extension
    public static class Descriptor extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Sets.newHashSet(Run.class);
        }

        @Override
        public String getFunctionName() {
            return "publishCoreWarnings";
        }

        @Override
        public String getDisplayName() {
            return "Publish warnings created by core parser facade.";
        }
    }
}
