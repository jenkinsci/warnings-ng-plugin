package io.jenkins.plugins.analysis.core.steps;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.factory.Sets;

import edu.hm.hafner.analysis.Severity;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;

import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.HealthReportBuilder;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.steps.WarningChecksPublisher.ChecksAnnotationScope;
import io.jenkins.plugins.analysis.core.util.TrendChartType;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.checks.steps.ChecksInfo;
import io.jenkins.plugins.prism.SourceCodeDirectory;
import io.jenkins.plugins.prism.SourceCodeRetention;
import io.jenkins.plugins.util.QualityGateEvaluator;
import io.jenkins.plugins.util.ValidationUtilities;

/**
 * Pipeline step that scans report files or the console log for issues. Stores the created issues in an {@link
 * AnalysisResult}. The result is attached to the {@link Run} by registering a {@link ResultAction}.
 *
 * <p>
 * Additional features:
 * </p>
 * <ul>
 * <li>It provides a {@link QualityGateEvaluator} that is checked after each run. If the quality gate is not passed,
 * then the
 * build will be set to {@link Result#UNSTABLE} or {@link Result#FAILURE}, depending on the configuration
 * properties.</li>
 * <li>It provides thresholds for the build health that could be adjusted in the configuration screen.
 * These values are used by the {@link HealthReportBuilder} to compute the health and the health trend graph.
 * </li>
 * </ul>
 */
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.ExcessiveImports", "PMD.TooManyFields", "PMD.DataClass", "PMD.CyclomaticComplexity", "PMD.ExcessiveClassLength", "PMD.GodClass"})
public class RecordIssuesStep extends Step implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final ValidationUtilities VALIDATION_UTILITIES = new ValidationUtilities();

    private List<Tool> analysisTools = new ArrayList<>();

    private String sourceCodeEncoding = StringUtils.EMPTY;
    private Set<SourceCodeDirectory> sourceDirectories = new HashSet<>(); // @since 9.11.0
    private SourceCodeRetention sourceCodeRetention = SourceCodeRetention.EVERY_BUILD;

    private boolean ignoreQualityGate; // by default, a successful quality gate is mandatory;

    private int healthy;
    private int unhealthy;
    private Severity minimumSeverity = Severity.WARNING_LOW;

    private List<RegexpFilter> filters = new ArrayList<>();

    private boolean isEnabledForFailure;
    private boolean isAggregatingResults;

    private boolean isBlameDisabled;

    private boolean skipPostProcessing; // @since 10.6.0: by default, post-processing will be enabled
    private boolean skipDeltaCalculation; // @since 11.5.0: by default, delta computation is enabled
    private boolean skipPublishingChecks; // by default, checks will be published
    private ChecksAnnotationScope checksAnnotationScope = ChecksAnnotationScope.NEW; // @since 11.0.0

    private String id = StringUtils.EMPTY;
    private String name = StringUtils.EMPTY;
    private String icon = StringUtils.EMPTY; // @since 12.0.0: by default no custom icon is set

    private List<WarningsQualityGate> qualityGates = new ArrayList<>();

    private TrendChartType trendChartType = TrendChartType.AGGREGATION_TOOLS;

    private boolean failOnError;
    private String scm = StringUtils.EMPTY;

    private boolean quiet;

    /**
     * Creates a new instance of {@link RecordIssuesStep}.
     */
    @DataBoundConstructor
    public RecordIssuesStep() {
        super();

        // empty constructor required for Stapler
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
     * Defines the optional list of quality gates.
     *
     * @param qualityGates
     *         the quality gates
     */
    @SuppressWarnings("unused") // used by Stapler view data binding
    @DataBoundSetter
    public void setQualityGates(final List<WarningsQualityGate> qualityGates) {
        this.qualityGates = qualityGates;
    }

    @SuppressWarnings({"unused", "WeakerAccess"}) // used by Stapler view data binding
    public List<WarningsQualityGate> getQualityGates() {
        return qualityGates;
    }

    /**
     * Defines the ID of the results. The ID is used as URL of the results and as name in UI elements. If no ID is
     * given, then the ID of the associated result object is used.
     *
     * <p>
     * Note: this property is not used if {@link #isAggregatingResults} is {@code false}. It is also not visible in the
     * UI in order to simplify the user interface.
     * </p>
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
     * <p>
     * Note: this property is not used if {@link #isAggregatingResults} is {@code false}. It is also not visible in the
     * UI in order to simplify the user interface.
     * </p>
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
     * Gets the static analysis tools that will scan files and create issues.
     *
     * @return the static analysis tools (wrapped as {@link ToolProxy})
     * @see #getTools
     * @deprecated this method is only intended to be called by the UI
     */
    @CheckForNull
    @Deprecated
    public List<ToolProxy> getToolProxies() {
        return analysisTools.stream().map(ToolProxy::new).collect(Collectors.toList());
    }

    /**
     * Sets the static analysis tools that will scan files and create issues.
     *
     * @param toolProxies
     *         the static analysis tools (wrapped as {@link ToolProxy})
     *
     * @see #setTools(List)
     * @see #setTool(Tool)
     * @deprecated this method is only intended to be called by the UI
     */
    @DataBoundSetter
    @Deprecated
    public void setToolProxies(final List<ToolProxy> toolProxies) {
        analysisTools = toolProxies.stream().map(ToolProxy::getTool).collect(Collectors.toList());
    }

    /**
     * Sets the static analysis tools that will scan files and create issues.
     *
     * @param tools
     *         the static analysis tools
     *
     * @see #setTool(Tool)
     */
    @DataBoundSetter
    public void setTools(final List<Tool> tools) {
        analysisTools = new ArrayList<>(tools);
    }

    /**
     * Sets the static analysis tools that will scan files and create issues.
     *
     * @param tool
     *         the static analysis tool
     * @param additionalTools
     *         additional static analysis tools (might be empty)
     *
     * @see #setTool(Tool)
     * @see #setTools(List)
     */
    public void setTools(final Tool tool, final Tool... additionalTools) {
        ensureThatToolIsValid(tool);
        for (Tool additionalTool : additionalTools) {
            ensureThatToolIsValid(additionalTool);
        }
        analysisTools = new ArrayList<>();
        analysisTools.add(tool);
        Collections.addAll(analysisTools, additionalTools);
    }

    private static void ensureThatToolIsValid(final Tool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("No valid tool defined! You probably used a symbol in the tools "
                    + "definition that is also a symbol in another plugin. "
                    + "Additionally check if your step is called 'checkStyle' and not 'checkstyle', "
                    + "since 'checkstyle' is a reserved keyword in the CheckStyle plugin!"
                    + "If not please create a new bug report in Jenkins issue tracker.");
        }
    }

    /**
     * Returns the static analysis tools that will scan files and create issues.
     *
     * @return the static analysis tools
     */
    public List<Tool> getTools() {
        return new ArrayList<>(analysisTools);
    }

    /**
     * Sets the static analysis tool that will scan files and create issues.
     *
     * @param tool
     *         the static analysis tool
     */
    @DataBoundSetter
    public void setTool(final Tool tool) {
        ensureThatToolIsValid(tool);

        analysisTools = Collections.singletonList(tool);
    }

    /**
     * Always returns {@code null}. Note: this method is required for Jenkins data binding.
     *
     * @return {@code null}
     */
    @CheckForNull
    public Tool getTool() {
        return null;
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

    /**
     * Sets the paths to the directories that contain the source code. If not relative and thus not part of the
     *  workspace, then these directories need to be added in Jenkins global configuration to prevent accessing of
     * forbidden resources.
     *
     * @param sourceDirectories
     *         directories containing the source code
     */
    @DataBoundSetter
    public void setSourceDirectories(final List<SourceCodeDirectory> sourceDirectories) {
        this.sourceDirectories = new HashSet<>(sourceDirectories);
    }

    public List<SourceCodeDirectory> getSourceDirectories() {
        return new ArrayList<>(sourceDirectories);
    }

    private List<SourceCodeDirectory> getAllSourceDirectories() {
        return new ArrayList<>(new HashSet<>(getSourceDirectories()));
    }

    /**
     * Defines the retention strategy for source code files.
     *
     * @param sourceCodeRetention
     *         the retention strategy for source code files
     */
    @DataBoundSetter
    public void setSourceCodeRetention(final SourceCodeRetention sourceCodeRetention) {
        this.sourceCodeRetention = sourceCodeRetention;
    }

    public SourceCodeRetention getSourceCodeRetention() {
        return sourceCodeRetention;
    }

    /**
     * Returns whether the results for each configured static analysis result should be aggregated into a single result
     * or if every tool should get an individual result.
     *
     * @return {@code true}  if the results of each static analysis tool should be aggregated into a single result,
     *         {@code false} if every tool should get an individual result.
     */
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getAggregatingResults() {
        return isAggregatingResults;
    }

    @DataBoundSetter
    public void setAggregatingResults(final boolean aggregatingResults) {
        isAggregatingResults = aggregatingResults;
    }

    /**
     * Returns whether report logging output should be enabled.
     *
     * @return {@code true}  if report logging is disabled
     *         {@code false} if report logging is enabled
     */
    public boolean isQuiet() {
        return quiet;
    }

    @DataBoundSetter
    public void setQuiet(final boolean quiet) {
        this.quiet = quiet;
    }

    /**
     * Returns whether SCM blaming should be disabled.
     *
     * @return {@code true} if SCM blaming should be disabled
     */
    public boolean isSkipBlames() {
        return isBlameDisabled;
    }

    @DataBoundSetter
    public void setSkipBlames(final boolean skipBlames) {
        isBlameDisabled = skipBlames;
    }

    /**
     * Returns whether post-processing of the issues should be disabled.
     *
     * @return {@code true} if post-processing of the issues should be disabled.
     */
    public boolean isSkipPostProcessing() {
        return skipPostProcessing;
    }

    @DataBoundSetter
    public void setSkipPostProcessing(final boolean skipPostProcessing) {
        this.skipPostProcessing = skipPostProcessing;
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
     * Returns whether recording should be enabled for failed builds as well.
     *
     * @return {@code true}  if recording should be enabled for failed builds as well, {@code false} if recording is
     *         enabled for successful or unstable builds only
     */
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getEnabledForFailure() {
        return isEnabledForFailure;
    }

    @DataBoundSetter
    public void setEnabledForFailure(final boolean enabledForFailure) {
        isEnabledForFailure = enabledForFailure;
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
    public void setIgnoreQualityGate(final boolean ignoreQualityGate) {
        this.ignoreQualityGate = ignoreQualityGate;
    }

    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getIgnoreQualityGate() {
        return ignoreQualityGate;
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

    public List<RegexpFilter> getFilters() {
        return new ArrayList<>(filters);
    }

    @DataBoundSetter
    public void setFilters(final List<RegexpFilter> filters) {
        this.filters = new ArrayList<>(filters);
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new Execution(context, this);
    }

    /**
     * Actually performs the execution of the associated step.
     */
    @SuppressFBWarnings(value = "THROWS", justification = "false positive")
    static class Execution extends AnalysisExecution<List<AnalysisResult>> {
        @Serial
        private static final long serialVersionUID = -2840020502160375407L;

        private final RecordIssuesStep step;

        Execution(@NonNull final StepContext context, final RecordIssuesStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected List<AnalysisResult> run() throws IOException, InterruptedException {
            IssuesRecorder recorder = new IssuesRecorder();
            recorder.setTools(step.getTools());
            recorder.setSourceCodeRetention(step.getSourceCodeRetention());
            recorder.setSourceCodeEncoding(step.getSourceCodeEncoding());
            recorder.setIgnoreQualityGate(step.getIgnoreQualityGate());
            recorder.setHealthy(step.getHealthy());
            recorder.setUnhealthy(step.getUnhealthy());
            recorder.setMinimumSeverity(step.getMinimumSeverity());
            recorder.setFilters(step.getFilters());
            recorder.setEnabledForFailure(step.getEnabledForFailure());
            recorder.setAggregatingResults(step.getAggregatingResults());
            recorder.setBlameDisabled(step.isSkipBlames());
            recorder.setSkipPostProcessing(step.isSkipPostProcessing());
            recorder.setSkipDeltaCalculation(step.isSkipDeltaCalculation());
            recorder.setScm(step.getScm());
            recorder.setSkipPublishingChecks(step.isSkipPublishingChecks());
            recorder.setChecksAnnotationScope(step.getChecksAnnotationScope());
            recorder.setId(step.getId());
            recorder.setName(step.getName());
            recorder.setIcon(step.getIcon());
            recorder.setQualityGates(step.getQualityGates());
            recorder.setFailOnError(step.getFailOnError());
            recorder.setTrendChartType(step.getTrendChartType());
            recorder.setSourceDirectories(step.getAllSourceDirectories());
            recorder.setChecksInfo(getContext().get(ChecksInfo.class));
            recorder.setQuiet(step.isQuiet());

            FilePath workspace = getWorkspace();
            workspace.mkdirs();

            return recorder.perform(getRun(), workspace, getTaskListener(), createResultHandler());
        }
    }

    /**
     * Descriptor for this step: defines the context and the UI labels.
     */
    @Extension
    @SuppressWarnings("unused") // most methods are used by the corresponding jelly view
    public static class Descriptor extends AnalysisStepDescriptor {
        @Override
        public String getFunctionName() {
            return "recordIssues";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.ScanAndPublishIssues_DisplayName();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Sets.immutable.of(FilePath.class, FlowNode.class, Run.class, TaskListener.class).castToSet();
        }
    }
}
