package io.jenkins.plugins.analysis.core.steps;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.factory.Sets;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.steps.WarningChecksPublisher.ChecksAnnotationScope;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.analysis.core.util.TrendChartType;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.checks.steps.ChecksInfo;
import io.jenkins.plugins.forensics.delta.DeltaCalculator;
import io.jenkins.plugins.forensics.delta.DeltaCalculatorFactory;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.ValidationUtilities;

/**
 * Publish issues created by a static analysis build. The recorded issues are stored as a {@link ResultAction} in the
 * associated Jenkins build. If the issues report has a unique ID, then the created action will use this ID as well.
 * Otherwise, a default ID is used to publish the results. In any case, the computed ID can be overwritten by specifying
 * an ID as step parameter.
 */
@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "PMD.DataClass", "PMD.TooManyFields"})
public class PublishIssuesStep extends Step implements Serializable {
    @Serial
    private static final long serialVersionUID = -1833335402353771148L;
    private static final ValidationUtilities VALIDATION_UTILITIES = new ValidationUtilities();

    private final List<AnnotatedReport> reports;

    private String sourceCodeEncoding = StringUtils.EMPTY;

    private boolean ignoreQualityGate; // by default, a successful quality gate is mandatory
    private boolean failOnError; // by default, it should not fail on error

    private boolean skipDeltaCalculation; // @since 11.5.0: by default, delta computation is enabled
    private boolean skipPublishingChecks; // by default, warnings should be published to SCM platforms
    private ChecksAnnotationScope checksAnnotationScope = ChecksAnnotationScope.NEW; // @since 11.0.0

    private boolean quiet; // by default, logger content goes to loghandler output

    private int healthy;
    private int unhealthy;
    private Severity minimumSeverity = Severity.WARNING_LOW;

    private List<WarningsQualityGate> qualityGates = new ArrayList<>();

    private TrendChartType trendChartType = TrendChartType.AGGREGATION_TOOLS;

    private String id = StringUtils.EMPTY;
    private String name = StringUtils.EMPTY;
    private String icon = StringUtils.EMPTY; // @since 12.0.0: by default no custom icon is set
    private String scm = StringUtils.EMPTY;

    /**
     * Creates a new instance of {@link PublishIssuesStep}.
     *
     * @param issues
     *         the reports to publish as {@link Action} in the {@link Job}.
     *
     * @throws IllegalArgumentException
     *         if the array of issues is {@code null} or empty
     */
    @DataBoundConstructor
    public PublishIssuesStep(@CheckForNull final List<AnnotatedReport> issues) {
        super();

        if (issues == null) {
            reports = new ArrayList<>();
        }
        else {
            reports = new ArrayList<>(issues);
        }
    }

    public List<AnnotatedReport> getIssues() {
        return new ArrayList<>(reports);
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
        VALIDATION_UTILITIES.ensureValidId(id);

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
     * Defines the custom icon of the results. If no icon is given, then the default icon of
     * the associated {@link StaticAnalysisLabelProvider} is used.
     *
     * @param icon
     *         the icon of the results
     */
    @DataBoundSetter
    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    /**
     * Sets the SCM that should be used to find the reference build for. The reference recorder will select the SCM
     * based on a substring comparison, there is no need to specify the full name.
     *
     * @param scm
     *         the ID of the SCM to use (a substring of the full ID)
     */
    @DataBoundSetter
    public void setScm(final String scm) {
        this.scm = scm;
    }

    public String getScm() {
        return scm;
    }

    /**
     * Determines whether to fail the step on errors during the step of recording issues.
     *
     * @param failOnError
     *         if {@code true} then the build will be failed on errors, {@code false} then errors are only reported in
     *         the UI
     */
    @DataBoundSetter
    @SuppressWarnings("unused") // Used by Stapler
    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }

    @SuppressWarnings({"PMD.BooleanGetMethodName", "WeakerAccess"})
    public boolean getFailOnError() {
        return failOnError;
    }

    /**
     * Returns whether the SCM delta calculation for the new issue detection should be disabled.
     *
     * @return {@code true} if the SCM delta calculation for the new issue detection should be disabled.
     */
    public boolean isSkipDeltaCalculation() {
        return skipDeltaCalculation;
    }

    @DataBoundSetter
    public void setSkipDeltaCalculation(final boolean skipDeltaCalculation) {
        this.skipDeltaCalculation = skipDeltaCalculation;
    }

    /**
     * Returns whether publishing checks should be skipped.
     *
     * @return {@code true} if publishing checks should be skipped, {@code false} otherwise
     */
    public boolean isSkipPublishingChecks() {
        return skipPublishingChecks;
    }

    @DataBoundSetter
    @SuppressWarnings("unused") // Used by Stapler
    public void setSkipPublishingChecks(final boolean skipPublishingChecks) {
        this.skipPublishingChecks = skipPublishingChecks;
    }

    /**
     * Sets the scope of the annotations that should be published to SCM checks.
     *
     * @param checksAnnotationScope
     *         the scope to use
     */
    @DataBoundSetter
    public void setChecksAnnotationScope(final ChecksAnnotationScope checksAnnotationScope) {
        this.checksAnnotationScope = checksAnnotationScope;
    }

    public ChecksAnnotationScope getChecksAnnotationScope() {
        return checksAnnotationScope;
    }

    /**
     * Returns whether all issues should be published using the Checks API. If set to {@code false} only new issues will
     * be published.
     *
     * @return {@code true} if all issues should be published, {@code false} if only new issues should be published
     * @deprecated use {@link #getChecksAnnotationScope()} instead
     */
    @Deprecated
    public boolean isPublishAllIssues() {
        return getChecksAnnotationScope() == ChecksAnnotationScope.ALL;
    }

    /**
     * Returns whether all issues should be published to SCM checks.
     *
     * @param publishAllIssues if {@code true} then all issues should be published, otherwise only new issues
     * @deprecated use {@link #setChecksAnnotationScope(ChecksAnnotationScope)} instead
     */
    @Deprecated
    @DataBoundSetter
    public void setPublishAllIssues(final boolean publishAllIssues) {
        checksAnnotationScope = publishAllIssues ? ChecksAnnotationScope.ALL : ChecksAnnotationScope.NEW;
    }

    /**
     * If {@code true}, then the result of the quality gate is ignored when selecting a reference build. This option is
     * disabled by default, so a failing quality gate will be passed from build to build until the original reason for
     * the failure has been resolved.
     *
     * @param ignoreQualityGate
     *         if {@code true} then the result of the quality gate is ignored, otherwise only build with a successful
     *         quality gate are selected
     */
    @DataBoundSetter
    @SuppressWarnings("unused") // Used by Stapler
    public void setIgnoreQualityGate(final boolean ignoreQualityGate) {
        this.ignoreQualityGate = ignoreQualityGate;
    }

    @SuppressWarnings({"PMD.BooleanGetMethodName", "WeakerAccess"})
    public boolean getIgnoreQualityGate() {
        return ignoreQualityGate;
    }

    /**
     * If {@code true}, then logger content is muted
     * If {@code false}, then logger content goes to loghandler output.
     *
     * @param quiet
     *         if {@code true} then logger content is muted.
     */
    @DataBoundSetter
    @SuppressWarnings("unused") // Used by Stapler
    public void setQuiet(final boolean quiet) {
        this.quiet = quiet;
    }

    public boolean isQuiet() {
        return quiet;
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

    public int getHealthy() {
        return healthy;
    }

    /**
     * Sets the healthy threshold, i.e., the number of issues when health is reported as 100%.
     *
     * @param healthy
     *         the number of issues when health is reported as 100%
     */
    @DataBoundSetter
    public void setHealthy(final int healthy) {
        this.healthy = healthy;
    }

    public int getUnhealthy() {
        return unhealthy;
    }

    /**
     * Sets the healthy threshold, i.e., the number of issues when health is reported as 0%.
     *
     * @param unhealthy
     *         the number of issues when health is reported as 0%
     */
    @DataBoundSetter
    public void setUnhealthy(final int unhealthy) {
        this.unhealthy = unhealthy;
    }

    @CheckForNull
    @SuppressWarnings("unused") // Used by Stapler
    public String getMinimumSeverity() {
        return minimumSeverity.getName();
    }

    @CheckForNull
    @SuppressWarnings("WeakerAccess") // Required by Stapler
    public Severity getMinimumSeverityAsSeverity() {
        return minimumSeverity;
    }

    /**
     * Sets the minimum severity to consider when computing the health report. Issues with a severity less than this
     * value will be ignored.
     *
     * @param minimumSeverity
     *         the severity to consider
     */
    @DataBoundSetter
    @SuppressWarnings("unused") // Used by Stapler
    public void setMinimumSeverity(final String minimumSeverity) {
        this.minimumSeverity = Severity.valueOf(minimumSeverity, Severity.WARNING_LOW);
    }

    /**
     * Sets the type of the trend chart that should be shown on the job page.
     *
     * @param trendChartType
     *         the type of the trend chart to use
     */
    @DataBoundSetter
    public void setTrendChartType(final TrendChartType trendChartType) {
        this.trendChartType = trendChartType;
    }

    public TrendChartType getTrendChartType() {
        return trendChartType;
    }

    /**
     * Defines the optional list of quality gates.
     *
     * @param qualityGates
     *         the quality gates
     */
    @DataBoundSetter
    @SuppressWarnings("unused") // Used by Stapler
    public void setQualityGates(final List<WarningsQualityGate> qualityGates) {
        this.qualityGates = qualityGates;
    }

    @SuppressWarnings("WeakerAccess") // Required by Stapler
    public List<WarningsQualityGate> getQualityGates() {
        return qualityGates;
    }

    @Override
    public StepExecution start(final StepContext stepContext) {
        return new Execution(stepContext, this);
    }

    /**
     * Actually performs the execution of the associated step.
     */
    @SuppressFBWarnings(value = "THROWS", justification = "false positive")
    static class Execution extends AnalysisExecution<ResultAction> {
        @Serial
        private static final long serialVersionUID = 6438321240776419897L;

        private final PublishIssuesStep step;

        /**
         * Creates a new instance of the step execution object.
         *
         * @param context
         *         context for this step
         * @param step
         *         the actual step to execute
         */
        Execution(@NonNull final StepContext context, final PublishIssuesStep step) {
            super(context);

            if (step.reports.isEmpty()) {
                throw new IllegalArgumentException(
                        "No reports provided in publish issues step, parameter 'issues' must be set!");
            }
            this.step = step;
        }

        @Override
        protected ResultAction run() throws IOException, InterruptedException, IllegalStateException {
            AnnotatedReport report;
            if (step.reports.size() > 1) {
                report = new AnnotatedReport(StringUtils.defaultIfEmpty(step.getId(), IssuesRecorder.DEFAULT_ID));
                report.logInfo("Aggregating reports of:");
                var factory = new LabelProviderFactory();
                for (AnnotatedReport subReport : step.reports) {
                    var labelProvider = factory.create(subReport.getId());
                    report.logInfo("-> %s", labelProvider.getToolTip(subReport.size()));
                }
            }
            else {
                report = new AnnotatedReport(StringUtils.defaultIfEmpty(step.getId(), step.reports.get(0).getId())); // use ID from single report
            }
            report.addAll(step.reports);

            var workspace = getContext().get(FilePath.class);
            var deltaCalculator = workspace == null || step.isSkipDeltaCalculation()
                    ? new DeltaCalculator.NullDeltaCalculator()
                    : DeltaCalculatorFactory.findDeltaCalculator(step.scm, getRun(), workspace, getTaskListener(), new FilteredLog());

            var publisher = new IssuesPublisher(getRun(), report,
                    deltaCalculator, new HealthDescriptor(step.getHealthy(), step.getUnhealthy(),
                            step.getMinimumSeverityAsSeverity()), step.getQualityGates(),
                    StringUtils.defaultString(step.getName()), step.getIcon(), step.getIgnoreQualityGate(),
                    getCharset(step.getSourceCodeEncoding()), getLogger(report), createResultHandler(), step.getFailOnError(),
                    List.of());
            var action = publisher.attachAction(step.getTrendChartType());

            if (!step.isSkipPublishingChecks()) {
                var checksPublisher = new WarningChecksPublisher(action, getTaskListener(), getContext().get(ChecksInfo.class));
                checksPublisher.publishChecks(step.getChecksAnnotationScope());
            }

            return action;
        }

        private LogHandler getLogger(final AnnotatedReport annotatedReport) throws InterruptedException {
            var toolName = new LabelProviderFactory().create(annotatedReport.getId(),
                    StringUtils.defaultString(step.getName())).getName();
            var logHandler = new LogHandler(getTaskListener(), toolName, annotatedReport.getLogger());
            logHandler.setQuiet(step.isQuiet());

            var report = annotatedReport.getReport();
            logHandler.logInfoMessages(report.getInfoMessages());
            logHandler.logErrorMessages(report.getErrorMessages());

            return logHandler;
        }
    }

    /**
     * Descriptor for this step: defines the context and the UI labels.
     */
    @Extension
    public static class Descriptor extends AnalysisStepDescriptor {
        @Override
        public Set<Class<?>> getRequiredContext() {
            return Sets.immutable.of(FlowNode.class, Run.class, TaskListener.class).castToSet();
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
