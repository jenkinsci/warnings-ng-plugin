package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Severity;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.HealthReportBuilder;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.scm.BlameFactory;
import io.jenkins.plugins.analysis.core.scm.Blamer;
import io.jenkins.plugins.analysis.core.scm.NullBlamer;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.analysis.core.util.LogHandler;
import io.jenkins.plugins.analysis.core.util.ModelValidation;
import io.jenkins.plugins.analysis.core.util.QualityGate;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateEvaluator;
import io.jenkins.plugins.analysis.core.util.Thresholds;

/**
 * Freestyle or Maven job {@link Recorder} that scans report files or the console log for issues. Stores the created
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
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.ExcessiveImports", "PMD.TooManyFields", "PMD.DataClass", "classdataabstractioncoupling", "classfanoutcomplexity", "MissingDeprecated"})
public class IssuesRecorder extends Recorder implements SimpleBuildStep {
    private static final String NO_REFERENCE_JOB = "-";

    private List<Tool> analysisTools = new ArrayList<>();

    private String sourceCodeEncoding = StringUtils.EMPTY;

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

    private String id;
    private String name;

    private List<QualityGate> qualityGates = new ArrayList<>();

    /**
     * Creates a new instance of {@link IssuesRecorder}.
     */
    @DataBoundConstructor
    public IssuesRecorder() {
        super();

        // empty constructor required for Stapler
    }

    /**
     * Called after de-serialization to retain backward compatibility.
     *
     * @return this
     */
    protected Object readResolve() {
        if (analysisTools == null) {
            analysisTools = new ArrayList<>();
        }
        if (qualityGates == null) {
            qualityGates = new ArrayList<>();
            if (thresholds != null) {
                qualityGates.addAll(QualityGate.map(thresholds));
            }
        }
        return this;
    }

    /**
     * Defines the optional list of quality gates.
     *
     * @param qualityGates
     *         the quality gates
     */
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

    public List<QualityGate> getQualityGates() {
        return qualityGates;
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
        this.analysisTools = toolProxies.stream().map(ToolProxy::getTool).collect(Collectors.toList());
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
        this.analysisTools = new ArrayList<>(tools);
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

        this.analysisTools = Collections.singletonList(tool);
    }

    private void ensureThatToolIsValid(final Tool tool) {
        if (tool == null) {
            Jenkins instance = Jenkins.getInstance();
            String checkstyleError = "Additionally check if your step is called 'checkStyle' and not 'checkstyle', "
                    + "since 'checkstyle' is a reserved keyword in the CheckStyle plugin!";
            if (instance.getPlugin("pmd") != null) {
                throw new IllegalArgumentException("No valid tool defined! You probably used the symbol 'pmd' in "
                        + "your tool definition. This symbol is also used in the PMD plugin. In this case you must "
                        + "use the symbol 'pmdParser' instead, see JENKINS-55328. "
                        + checkstyleError);
            }
            if (instance.getPlugin("androidLint") != null) {
                throw new IllegalArgumentException(
                        "No valid tool defined! You probably used the symbol 'androidLint' in "
                                + "your tool definition. This symbol is also used in the Android Lint plugin. In this case you must "
                                + "use the symbol 'androidLintParser' instead, see JENKINS-55328. "
                                + checkstyleError);
            }
            throw new IllegalArgumentException("No valid tool defined! You probably used a symbol in the tools "
                    + "definition that is also a symbol in another plugin. "
                    + checkstyleError
                    + "If not please create a new bug report in Jenkins issue tracker.");
        }
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

    /* -------------------------------------------------------------------------------------------------------------- */

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
        this.isAggregatingResults = aggregatingResults;
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
        this.isBlameDisabled = blameDisabled;
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
        this.isEnabledForFailure = enabledForFailure;
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

    /**
     * Returns the reference job to get the results for the issue difference computation. If the job is not defined,
     * then {@link #NO_REFERENCE_JOB} is returned.
     *
     * @return the name of reference job, or {@link #NO_REFERENCE_JOB} if undefined
     */
    public String getReferenceJobName() {
        if (StringUtils.isBlank(referenceJobName)) {
            return NO_REFERENCE_JOB;
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

    public List<RegexpFilter> getFilters() {
        return new ArrayList<>(filters);
    }

    @DataBoundSetter
    public void setFilters(final List<RegexpFilter> filters) {
        this.filters = new ArrayList<>(filters);
    }

    @Override
    public Descriptor getDescriptor() {
        return (Descriptor) super.getDescriptor();
    }

    @Override
    public void perform(@NonNull final Run<?, ?> run, @NonNull final FilePath workspace,
            @NonNull final Launcher launcher, @NonNull final TaskListener listener)
            throws InterruptedException, IOException {
        Result overallResult = run.getResult();
        if (isEnabledForFailure || overallResult == null || overallResult.isBetterOrEqualTo(Result.UNSTABLE)) {
            record(run, workspace, listener);
        }
        else {
            LogHandler logHandler = new LogHandler(listener, createLoggerPrefix());
            logHandler.log("Skipping execution of recorder since overall result is '%s'", overallResult);
        }
    }

    private String createLoggerPrefix() {
        return analysisTools.stream().map(Tool::getActualName).collect(Collectors.joining());
    }

    private void record(final Run<?, ?> run, final FilePath workspace, final TaskListener listener)
            throws IOException, InterruptedException {
        for (Tool tool : getTools()) {
            ensureThatToolIsValid(tool);
        }
        if (isAggregatingResults && analysisTools.size() > 1) {
            AnnotatedReport totalIssues = new AnnotatedReport(StringUtils.defaultIfEmpty(id, "analysis"));
            for (Tool tool : analysisTools) {
                totalIssues.add(scanWithTool(run, workspace, listener, tool), tool.getActualId());
            }
            String toolName = StringUtils.defaultIfEmpty(getName(), Messages.Tool_Default_Name());
            publishResult(run, listener, toolName, totalIssues, toolName);
        }
        else {
            for (Tool tool : analysisTools) {
                AnnotatedReport report = new AnnotatedReport(tool.getActualId());
                if (isAggregatingResults) {
                    report.logInfo("Ignoring 'aggregatingResults' and ID '%s' since only a single tool is defined.",
                            id);
                }
                report.add(scanWithTool(run, workspace, listener, tool));
                if (StringUtils.isNotBlank(id) || StringUtils.isNotBlank(name)) {
                    report.logInfo("Ignoring name='%s' and id='%s' when publishing non-aggregating reports",
                            name, id);
                }
                publishResult(run, listener, tool.getActualName(), report, getReportName(tool));
            }
        }
    }

    /**
     * Returns the name of the tool. If no name has been set, then an empty string is returned so that the default name
     * will be used.
     *
     * @param tool
     *         the tool
     *
     * @return the name
     */
    private String getReportName(final Tool tool) {
        if (StringUtils.isBlank(tool.getName())) {
            return StringUtils.EMPTY;
        }
        else {
            return tool.getActualName();
        }
    }

    private AnnotatedReport scanWithTool(final Run<?, ?> run, final FilePath workspace, final TaskListener listener,
            final Tool tool) throws IOException, InterruptedException {
        IssuesScanner issuesScanner = new IssuesScanner(tool, getFilters(),
                getSourceCodeCharset(), new FilePath(run.getRootDir()), blame(run, workspace, listener));
        return issuesScanner.scan(run, workspace, new LogHandler(listener, tool.getActualName()));
    }

    private Blamer blame(final Run<?, ?> run, final FilePath workspace, final TaskListener listener) {
        if (isBlameDisabled) {
            return new NullBlamer();
        }
        return BlameFactory.createBlamer(run, workspace, listener);
    }

    private Charset getSourceCodeCharset() {
        return getCharset(sourceCodeEncoding);
    }

    private Charset getCharset(final String encoding) {
        return new ModelValidation().getCharset(encoding);
    }

    /**
     * Publishes the results as {@link Action} in the job using an {@link IssuesPublisher}. Afterwards, all affected
     * files are copied to Jenkins' build folder so that they are available to show warnings in the UI.
     *
     * @param run
     *         the run
     * @param listener
     *         the listener
     * @param loggerName
     *         the name of the logger
     * @param report
     *         the analysis report to publish
     * @param reportName
     *         the name of the report (might be empty)
     */
    void publishResult(final Run<?, ?> run, final TaskListener listener, final String loggerName,
            final AnnotatedReport report, final String reportName) {
        QualityGateEvaluator qualityGate = new QualityGateEvaluator();
        if (qualityGates.isEmpty()) {
            qualityGates.addAll(QualityGate.map(getThresholds()));
        }
        qualityGate.addAll(qualityGates);
        IssuesPublisher publisher = new IssuesPublisher(run, report,
                new HealthDescriptor(healthy, unhealthy, minimumSeverity), qualityGate,
                reportName, referenceJobName, ignoreQualityGate, ignoreFailedBuilds, getSourceCodeCharset(),
                new LogHandler(listener, loggerName, report.getReport()));
        publisher.attachAction();
    }

    /**
     * Not used anymore.
     *
     * @deprecated replaced by {@link #getQualityGates()}
     */
    @Deprecated
    private final transient Thresholds thresholds = new Thresholds(); // replaced by qualityGates

    /**
     * Returns the thresholds instance.
     *
     * @return thresholds
     * @deprecated replaced by {@link #getQualityGates()}
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    Thresholds getThresholds() {
        return thresholds;
    }

    @Deprecated
    public int getUnstableTotalAll() {
        return getThresholds().unstableTotalAll;
    }

    @Deprecated
    @DataBoundSetter
    public void setUnstableTotalAll(final int unstableTotalAll) {
        getThresholds().unstableTotalAll = unstableTotalAll;
    }

    @Deprecated
    public int getUnstableTotalHigh() {
        return getThresholds().unstableTotalHigh;
    }

    @Deprecated
    @DataBoundSetter
    public void setUnstableTotalHigh(final int unstableTotalHigh) {
        getThresholds().unstableTotalHigh = unstableTotalHigh;
    }

    @Deprecated
    public int getUnstableTotalNormal() {
        return getThresholds().unstableTotalNormal;
    }

    @Deprecated
    @DataBoundSetter
    public void setUnstableTotalNormal(final int unstableTotalNormal) {
        getThresholds().unstableTotalNormal = unstableTotalNormal;
    }

    @Deprecated
    public int getUnstableTotalLow() {
        return getThresholds().unstableTotalLow;
    }

    @Deprecated
    @DataBoundSetter
    public void setUnstableTotalLow(final int unstableTotalLow) {
        getThresholds().unstableTotalLow = unstableTotalLow;
    }

    @Deprecated
    public int getUnstableNewAll() {
        return getThresholds().unstableNewAll;
    }

    @Deprecated
    @DataBoundSetter
    public void setUnstableNewAll(final int unstableNewAll) {
        getThresholds().unstableNewAll = unstableNewAll;
    }

    @Deprecated
    public int getUnstableNewHigh() {
        return getThresholds().unstableNewHigh;
    }

    @Deprecated
    @DataBoundSetter
    public void setUnstableNewHigh(final int unstableNewHigh) {
        getThresholds().unstableNewHigh = unstableNewHigh;
    }

    @Deprecated
    public int getUnstableNewNormal() {
        return getThresholds().unstableNewNormal;
    }

    @Deprecated
    @DataBoundSetter
    public void setUnstableNewNormal(final int unstableNewNormal) {
        getThresholds().unstableNewNormal = unstableNewNormal;
    }

    @Deprecated
    public int getUnstableNewLow() {
        return getThresholds().unstableNewLow;
    }

    @Deprecated
    @DataBoundSetter
    public void setUnstableNewLow(final int unstableNewLow) {
        getThresholds().unstableNewLow = unstableNewLow;
    }

    @Deprecated
    public int getFailedTotalAll() {
        return getThresholds().failedTotalAll;
    }

    @Deprecated
    @DataBoundSetter
    public void setFailedTotalAll(final int failedTotalAll) {
        getThresholds().failedTotalAll = failedTotalAll;
    }

    @Deprecated
    public int getFailedTotalHigh() {
        return getThresholds().failedTotalHigh;
    }

    @Deprecated
    @DataBoundSetter
    public void setFailedTotalHigh(final int failedTotalHigh) {
        getThresholds().failedTotalHigh = failedTotalHigh;
    }

    @Deprecated
    public int getFailedTotalNormal() {
        return getThresholds().failedTotalNormal;
    }

    @Deprecated
    @DataBoundSetter
    public void setFailedTotalNormal(final int failedTotalNormal) {
        getThresholds().failedTotalNormal = failedTotalNormal;
    }

    @Deprecated
    public int getFailedTotalLow() {
        return getThresholds().failedTotalLow;
    }

    @Deprecated
    @DataBoundSetter
    public void setFailedTotalLow(final int failedTotalLow) {
        getThresholds().failedTotalLow = failedTotalLow;
    }

    @Deprecated
    public int getFailedNewAll() {
        return getThresholds().failedNewAll;
    }

    @Deprecated
    @DataBoundSetter
    public void setFailedNewAll(final int failedNewAll) {
        getThresholds().failedNewAll = failedNewAll;
    }

    @Deprecated
    public int getFailedNewHigh() {
        return getThresholds().failedNewHigh;
    }

    @Deprecated
    @DataBoundSetter
    public void setFailedNewHigh(final int failedNewHigh) {
        getThresholds().failedNewHigh = failedNewHigh;
    }

    @Deprecated
    public int getFailedNewNormal() {
        return getThresholds().failedNewNormal;
    }

    @Deprecated
    @DataBoundSetter
    public void setFailedNewNormal(final int failedNewNormal) {
        getThresholds().failedNewNormal = failedNewNormal;
    }

    @Deprecated
    public int getFailedNewLow() {
        return getThresholds().failedNewLow;
    }

    @Deprecated
    @DataBoundSetter
    public void setFailedNewLow(final int failedNewLow) {
        getThresholds().failedNewLow = failedNewLow;
    }
    // CHECKSTYLE:ON

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    @Symbol("recordIssues")
    @SuppressWarnings("unused") // most methods are used by the corresponding jelly view
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
        /** Retain backward compatibility. */
        @Initializer(before = InitMilestone.PLUGINS_STARTED)
        public static void addAliases() {
            Run.XSTREAM2.addCompatibilityAlias("io.jenkins.plugins.analysis.core.views.ResultAction",
                    ResultAction.class);
        }

        private final ModelValidation model = new ModelValidation();

        @NonNull
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
        public ComboBoxModel doFillSourceCodeEncodingItems() {
            return model.getAllCharsets();
        }

        /**
         * Returns a model with all available severity filters.
         *
         * @return a model with all available severity filters
         */
        public ListBoxModel doFillMinimumSeverityItems() {
            return model.getAllSeverityFilters();
        }

        /**
         * Returns the model with the possible reference jobs.
         *
         * @return the model with the possible reference jobs
         */
        public ComboBoxModel doFillReferenceJobNameItems() {
            return model.getAllJobs();
        }

        /**
         * Performs on-the-fly validation of the reference job.
         *
         * @param referenceJobName
         *         the reference job
         *
         * @return the validation result
         */
        public FormValidation doCheckReferenceJobName(@QueryParameter final String referenceJobName) {
            return model.validateJob(referenceJobName);
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
            return model.validateCharset(reportEncoding);
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
            return model.validateCharset(sourceCodeEncoding);
        }

        /**
         * Performs on-the-fly validation of the health report thresholds.
         *
         * @param healthy
         *         the healthy threshold
         * @param unhealthy
         *         the unhealthy threshold
         *
         * @return the validation result
         */
        public FormValidation doCheckHealthy(@QueryParameter final int healthy, @QueryParameter final int unhealthy) {
            return model.validateHealthy(healthy, unhealthy);
        }

        /**
         * Performs on-the-fly validation of the health report thresholds.
         *
         * @param healthy
         *         the healthy threshold
         * @param unhealthy
         *         the unhealthy threshold
         *
         * @return the validation result
         */
        public FormValidation doCheckUnhealthy(@QueryParameter final int healthy, @QueryParameter final int unhealthy) {
            return model.validateUnhealthy(healthy, unhealthy);
        }
    }
}
