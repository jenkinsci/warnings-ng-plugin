package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.factory.Sets;

import edu.hm.hafner.analysis.Severity;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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
import io.jenkins.plugins.analysis.core.util.PipelineResultHandler;
import io.jenkins.plugins.analysis.core.util.QualityGate;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateEvaluator;
import io.jenkins.plugins.analysis.core.util.StageResultHandler;
import io.jenkins.plugins.analysis.core.util.TrendChartType;

/**
 * Pipeline step that scans report files or the console log for issues. Stores the created
 * issues in an {@link AnalysisResult}. The result is attached to the {@link Run} by registering a {@link
 * ResultAction}.
 * <p>
 * Additional features:
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
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.ExcessiveImports", "PMD.TooManyFields", "PMD.DataClass"})
public class RecordIssuesStep extends Step implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Tool> analysisTools = new ArrayList<>();

    private String sourceCodeEncoding = StringUtils.EMPTY;
    private String sourceDirectory = StringUtils.EMPTY;

    private boolean ignoreQualityGate = false; // by default, a successful quality gate is mandatory;
    private boolean ignoreFailedBuilds = true; // by default, failed builds are ignored;
    private String referenceJobName;

    private int healthy;
    private int unhealthy;
    private Severity minimumSeverity = Severity.WARNING_LOW;

    private List<RegexpFilter> filters = new ArrayList<>();

    private boolean isEnabledForFailure;
    private boolean isAggregatingResults;

    private boolean isBlameDisabled;
    private boolean isForensicsDisabled;

    private String id;
    private String name;

    private List<QualityGate> qualityGates = new ArrayList<>();

    private TrendChartType trendChartType = TrendChartType.AGGREGATION_TOOLS;

    private boolean failOnError;

    /**
     * Creates a new instance of {@link RecordIssuesStep}.
     */
    @DataBoundConstructor
    public RecordIssuesStep() {
        super();

        // empty constructor required for Stapler
    }

    /**
     * Defines the optional list of quality gates.
     *
     * @param qualityGates
     *         the quality gates
     */
    @SuppressWarnings("unused") // used by Stapler view data binding
    @DataBoundSetter
    public void setQualityGates(final List<QualityGate> qualityGates) {
        this.qualityGates = qualityGates;
    }

    /**
     * Appends the specified quality gates to the end of the list of quality gates.
     *
     * @param size
     *         the minimum number of issues that fails the quality gate
     * @param type
     *         the type of the quality gate
     * @param result
     *         determines whether the quality gate is a warning or failure
     */
    public void addQualityGate(final int size, final QualityGateType type, final QualityGateResult result) {
        qualityGates.add(new QualityGate(size, type, result));
    }

    @SuppressWarnings({"unused", "WeakerAccess"}) // used by Stapler view data binding
    public List<QualityGate> getQualityGates() {
        return qualityGates;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setUnstableTotalAll(final int size) {
        addQualityGate(size, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getUnstableTotalAll() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setUnstableTotalHigh(final int size) {
        addQualityGate(size, QualityGateType.TOTAL_HIGH, QualityGateResult.UNSTABLE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getUnstableTotalHigh() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setUnstableNewAll(final int size) {
        addQualityGate(size, QualityGateType.NEW, QualityGateResult.UNSTABLE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getUnstableNewAll() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setUnstableTotalNormal(final int size) {
        addQualityGate(size, QualityGateType.TOTAL_NORMAL, QualityGateResult.UNSTABLE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getUnstableTotalNormal() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setUnstableTotalLow(final int size) {
        addQualityGate(size, QualityGateType.TOTAL_LOW, QualityGateResult.UNSTABLE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getUnstableTotalLow() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setUnstableNewHigh(final int size) {
        addQualityGate(size, QualityGateType.NEW_HIGH, QualityGateResult.UNSTABLE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getUnstableNewHigh() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setUnstableNewNormal(final int size) {
        addQualityGate(size, QualityGateType.NEW_NORMAL, QualityGateResult.UNSTABLE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getUnstableNewNormal() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setUnstableNewLow(final int size) {
        addQualityGate(size, QualityGateType.NEW_LOW, QualityGateResult.UNSTABLE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getUnstableNewLow() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setFailedTotalAll(final int size) {
        addQualityGate(size, QualityGateType.TOTAL, QualityGateResult.FAILURE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getFailedTotalAll() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setFailedTotalHigh(final int size) {
        addQualityGate(size, QualityGateType.TOTAL_HIGH, QualityGateResult.FAILURE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getFailedTotalHigh() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setFailedTotalNormal(final int size) {
        addQualityGate(size, QualityGateType.TOTAL_NORMAL, QualityGateResult.FAILURE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getFailedTotalNormal() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setFailedTotalLow(final int size) {
        addQualityGate(size, QualityGateType.TOTAL_LOW, QualityGateResult.FAILURE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getFailedTotalLow() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setFailedNewAll(final int size) {
        addQualityGate(size, QualityGateType.NEW, QualityGateResult.FAILURE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getFailedNewAll() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setFailedNewHigh(final int size) {
        addQualityGate(size, QualityGateType.NEW_HIGH, QualityGateResult.FAILURE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getFailedNewHigh() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setFailedNewNormal(final int size) {
        addQualityGate(size, QualityGateType.NEW_NORMAL, QualityGateResult.FAILURE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getFailedNewNormal() {
        return 0;
    }

    /**
     * Sets the quality gate.
     *
     * @param size
     *         number of issues
     *
     * @deprecated replaced by {@link RecordIssuesStep#addQualityGate(int, QualityGate.QualityGateType, QualityGate.QualityGateResult)}
     */
    @Deprecated
    @DataBoundSetter
    public void setFailedNewLow(final int size) {
        addQualityGate(size, QualityGateType.NEW_LOW, QualityGateResult.FAILURE);
    }

    /**
     * Gets the quality gate.
     *
     * @return 0
     * @deprecated replaced by {@link RecordIssuesStep#getQualityGates()}
     */
    @Deprecated
    public int getFailedNewLow() {
        return 0;
    }

    /**
     * Defines the ID of the results. The ID is used as URL of the results and as name in UI elements. If no ID is
     * given, then the ID of the associated result object is used.
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
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Defines the name of the results. The name is used for all labels in the UI. If no name is given, then the name of
     * the associated {@link StaticAnalysisLabelProvider} is used.
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
     * Gets the static analysis tools that will scan files and create issues.
     *
     * @return the static analysis tools (wrapped as {@link ToolProxy})
     * @see #getTools
     * @deprecated this method is only intended to be called by the UI
     */
    @Nullable
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
                    + ("Additionally check if your step is called 'checkStyle' and not 'checkstyle', "
                    + "since 'checkstyle' is a reserved keyword in the CheckStyle plugin!")
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
    @Nullable
    public Tool getTool() {
        return null;
    }

    @Nullable
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

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Sets the path to the folder that contains the source code. If not relative and thus not part of the workspace
     * then this folder needs to be added in Jenkins global configuration.
     *
     * @param sourceDirectory
     *         a folder containing the source code
     */
    @DataBoundSetter
    public void setSourceDirectory(final String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
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
     * Returns whether SCM blaming should be disabled.
     *
     * @return {@code true} if SCM blaming should be disabled
     */
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getBlameDisabled() {
        return isBlameDisabled;
    }

    @DataBoundSetter
    public void setBlameDisabled(final boolean blameDisabled) {
        isBlameDisabled = blameDisabled;
    }

    /**
     * Returns whether SCM forensics should be disabled.
     *
     * @return {@code true} if SCM forensics should be disabled
     */
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getForensicsDisabled() {
        return isForensicsDisabled;
    }

    @DataBoundSetter
    public void setForensicsDisabled(final boolean forensicsDisabled) {
        isForensicsDisabled = forensicsDisabled;
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
     * disabled by default so a failing quality gate will be passed from build to build until the original reason for
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
     * If {@code true}, then only successful or unstable reference builds will be considered. This option is enabled by
     * default, since analysis results might be inaccurate if the build failed. If {@code false}, every build that
     * contains a static analysis result is considered, even if the build failed.
     *
     * @param ignoreFailedBuilds
     *         if {@code true} then a stable build is used as reference
     */
    @DataBoundSetter
    public void setIgnoreFailedBuilds(final boolean ignoreFailedBuilds) {
        this.ignoreFailedBuilds = ignoreFailedBuilds;
    }

    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getIgnoreFailedBuilds() {
        return ignoreFailedBuilds;
    }

    /**
     * Determines whether to fail the build on errors during the step of recording issues.
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
     * Sets the reference job to get the results for the issue difference computation.
     *
     * @param referenceJobName
     *         the name of reference job
     */
    @DataBoundSetter
    public void setReferenceJobName(final String referenceJobName) {
        if (IssuesRecorder.NO_REFERENCE_JOB.equals(referenceJobName)) {
            this.referenceJobName = StringUtils.EMPTY;
        }
        this.referenceJobName = referenceJobName;
    }

    /**
     * Returns the reference job to get the results for the issue difference computation. If the job is not defined,
     * then {@link IssuesRecorder#NO_REFERENCE_JOB} is returned.
     *
     * @return the name of reference job, or {@link IssuesRecorder#NO_REFERENCE_JOB} if undefined
     */
    public String getReferenceJobName() {
        if (StringUtils.isBlank(referenceJobName)) {
            return IssuesRecorder.NO_REFERENCE_JOB;
        }
        return referenceJobName;
    }

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

    public int getUnhealthy() {
        return unhealthy;
    }

    /**
     * Sets the healthy threshold, i.e. the number of issues when health is reported as 0%.
     *
     * @param unhealthy
     *         the number of issues when health is reported as 0%
     */
    @DataBoundSetter
    public void setUnhealthy(final int unhealthy) {
        this.unhealthy = unhealthy;
    }

    @Nullable
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
    static class Execution extends AnalysisExecution<Void> {
        private static final long serialVersionUID = -2840020502160375407L;

        private final RecordIssuesStep step;

        Execution(@NonNull final StepContext context, final RecordIssuesStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws IOException, InterruptedException {
            IssuesRecorder recorder = new IssuesRecorder();
            recorder.setTools(step.getTools());
            recorder.setSourceCodeEncoding(step.getSourceCodeEncoding());
            recorder.setIgnoreQualityGate(step.getIgnoreQualityGate());
            recorder.setIgnoreFailedBuilds(step.getIgnoreFailedBuilds());
            recorder.setReferenceJobName(step.getReferenceJobName());
            recorder.setHealthy(step.getHealthy());
            recorder.setUnhealthy(step.getUnhealthy());
            recorder.setMinimumSeverity(step.getMinimumSeverity());
            recorder.setFilters(step.getFilters());
            recorder.setEnabledForFailure(step.getEnabledForFailure());
            recorder.setAggregatingResults(step.getAggregatingResults());
            recorder.setBlameDisabled(step.getBlameDisabled());
            recorder.setForensicsDisabled(step.getForensicsDisabled());
            recorder.setId(step.getId());
            recorder.setName(step.getName());
            recorder.setQualityGates(step.getQualityGates());
            recorder.setFailOnError(step.getFailOnError());
            recorder.setTrendChartType(step.getTrendChartType());
            recorder.setSourceDirectory(step.getSourceDirectory());
            StageResultHandler statusHandler = new PipelineResultHandler(getRun(),
                    getContext().get(FlowNode.class));

            FilePath workspace = getWorkspace();
            workspace.mkdirs();
            recorder.perform(getRun(), workspace, getTaskListener(), statusHandler);
            return null;
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
