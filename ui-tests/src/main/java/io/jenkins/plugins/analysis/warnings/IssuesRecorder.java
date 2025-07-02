package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * {@link PageObject} representing the IssuesRecorder of the Jenkins Warnings Plugin.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"unused", "UnusedReturnValue", "PMD.GodClass", "PMD.ExcessivePublicCount"})
@Describable("Record compiler warnings and static analysis results")
public class IssuesRecorder extends AbstractStep implements PostBuildStep {
    private final Control toolsRepeatable = findRepeatableAddButtonFor("tools");
    private final Control filtersRepeatable = findRepeatableAddButtonFor("filters");
    private final Control filterRegex = control("/filters/pattern");
    private final Control qualityGatesRepeatable = findRepeatableAddButtonFor("qualityGates");
    private final Control qualityGateThreshold = control("/qualityGates/integerThreshold");
    private final Control qualityGateType = control("/qualityGates/type");
    private final Control qualityGateCriticality = control("/qualityGates/criticality");
    private final Control advancedButton = control("advanced-button");
    private final Control enabledForFailureCheckBox = control("enabledForFailure");
    private final Control ignoreQualityGate = control("ignoreQualityGate");
    private final Control aggregatingResults = control("aggregatingResults");
    private final Control sourceCodeEncoding = control("sourceCodeEncoding");
    private final Control sourceDirectories = findRepeatableAddButtonFor("sourceDirectories");
    private final Control sourceCodeRetention = control("sourceCodeRetention");
    private final Control skipBlames = control("skipBlames");
    private final Control skipPostProcessing = control("skipPostProcessing");
    private final Control failOnError = control("failOnError");
    private final Control skipPublishingChecks = control("skipPublishingChecks");
    private final Control checksAnnotationScope = control("checksAnnotationScope");
    private final Control reportFilePattern = control("/toolProxies/tool/pattern");
    private final Control trendChartType = control("trendChartType");
    private final Control healthyThreshold = control("healthy");
    private final Control unhealthyThreshold = control("unhealthy");
    private final Control healthSeverity = control("minimumSeverity");
    private final Control scm = control("scm");
    private final Control quiet = control("quiet");

    /**
     * Creates a new page object.
     *
     * @param parent
     *         parent page object
     * @param path
     *         path on the parent page
     */
    public IssuesRecorder(final Job parent, final String path) {
        super(parent, path);

        openAdvancedOptions();
    }

    private Control findRepeatableAddButtonFor(final String propertyName) {
        return control(by.xpath("//div[@id='" + propertyName + "']//button[contains(@path,'-add')]"));
    }

    /**
     * Sets the name of the static analysis tool to use.
     *
     * @param toolName
     *         the tool name
     *
     * @return the subpage of the tool
     */
    public StaticAnalysisTool setTool(final String toolName) {
        StaticAnalysisTool tool = new StaticAnalysisTool(this, "toolProxies");
        tool.setTool(toolName);
        return tool;
    }

    /**
     * Sets the name and the pattern of the static analysis tool to use.
     *
     * @param toolName
     *         the tool name
     * @param pattern
     *         the file name pattern
     *
     * @return the subpage of the tool
     */
    public StaticAnalysisTool setTool(final String toolName, final String pattern) {
        return setTool(toolName, tool -> tool.setPattern(pattern));
    }

    /**
     * Sets a static analysis tool configuration.
     *
     * @param toolName
     *         the tool name
     * @param configuration
     *         the additional configuration options for this tool
     *
     * @return the subpage of the tool
     */
    public StaticAnalysisTool setTool(final String toolName, final Consumer<StaticAnalysisTool> configuration) {
        StaticAnalysisTool tool = setTool(toolName);
        configuration.accept(tool);
        return tool;
    }

    /**
     * Adds a new static analysis tool configuration. The pattern will be empty, i.e. the console log is scanned.
     *
     * @param toolName
     *         the tool name
     *
     * @return the subpage of the tool
     */
    public StaticAnalysisTool addTool(final String toolName) {
        return createToolPageArea(toolName);
    }

    /**
     * Adds a new static analysis tool configuration.
     *
     * @param toolName
     *         the tool name
     * @param configuration
     *         the additional configuration options for this tool
     *
     * @return the subpage of the tool
     */
    public StaticAnalysisTool addTool(final String toolName, final Consumer<StaticAnalysisTool> configuration) {
        StaticAnalysisTool tool = addTool(toolName);
        configuration.accept(tool);
        return tool;
    }

    /**
     * Adds a new static analysis tool configuration.
     *
     * @param toolName
     *         the tool name
     * @param pattern
     *         the file name pattern
     *
     * @return the subpage of the tool
     */
    public StaticAnalysisTool addTool(final String toolName, final String pattern) {
        return addTool(toolName, tool -> tool.setPattern(pattern));
    }

    public String getSourceCodeEncoding() {
        return sourceCodeEncoding.get();
    }

    /**
     * Returns whether recording should be enabled for failed builds as well.
     *
     * @return {@code true}  if recording should be enabled for failed builds as well, {@code false} if recording is
     *         enabled for successful or unstable builds only
     */
    public boolean isEnabledForFailure() {
        return isChecked(enabledForFailureCheckBox);
    }

    /**
     * Returns whether the results for each configured static analysis result should be aggregated into a single result
     * or if every tool should get an individual result.
     *
     * @return {@code true}  if the results of each static analysis tool should be aggregated into a single result,
     *         {@code false} if every tool should get an individual result.
     */
    public boolean isAggregatingResults() {
        return isChecked(aggregatingResults);
    }

    /**
     * Returns whether the report results is in logger output.
     *
     * @return {@code true} then the report logging of each static analysis tool is muted
     *         {@code false} then reports logging goes to loghandler output
     */
    public boolean isQuiet() {
        return isChecked(quiet);
    }

    public boolean isIgnoringQualityGate() {
        return isChecked(ignoreQualityGate);
    }

    public List<String> getSourceDirectories() {
        return all(by.xpath("//div[@id='sourceDirectories']//input")).stream()
                .map(e -> e.getAttribute("value"))
                .collect(Collectors.toList());
    }

    public String getTrendChartType() {
        return trendChartType.get();
    }

    public String getScm() {
        return scm.get();
    }

    public boolean isSkipBlames() {
        return isChecked(skipBlames);
    }

    public boolean isSkipPostProcessing() {
        return isChecked(skipPostProcessing);
    }

    public boolean isSkipPublishingChecks() {
        return isChecked(skipPublishingChecks);
    }

    public String getChecksAnnotationScope() {
        return checksAnnotationScope.get();
    }

    private boolean isChecked(final Control control) {
        return control.resolve().isSelected();
    }

    public boolean isFailingOnError() {
        return isChecked(failOnError);
    }

    public String getHealthThreshold() {
        return healthyThreshold.get();
    }

    public String getUnhealthyThreshold() {
        return unhealthyThreshold.get();
    }

    public String getHealthSeverity() {
        return healthSeverity.get();
    }

    public String getReportFilePattern() {
        return reportFilePattern.get();
    }

    public String getFilterRegex() {
        return filterRegex.get();
    }

    public String getQualityGateThreshold() {
        return qualityGateThreshold.get();
    }

    public String getQualityGateType() {
        return qualityGateType.get();
    }

    public String getQualityGateCriticality() {
        return qualityGateCriticality.get();
    }

    public String getSourceCodeRetention() {
        return sourceCodeRetention.get();
    }

    /**
     * Sets the source code encoding to the specified value.
     *
     * @param encoding
     *         the encoding to use when reading source files
     *
     * @return this recorder
     */
    public IssuesRecorder setSourceCodeEncoding(final String encoding) {
        sourceCodeEncoding.set(encoding);

        return this;
    }

    /**
     * Enables or disables the checkbox 'enabledForFailure'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     *
     * @return this recorder
     */
    public IssuesRecorder setEnabledForFailure(final boolean isChecked) {
        enabledForFailureCheckBox.check(isChecked);

        return this;
    }

    /**
     * Enables or disables the checkbox 'aggregatingResultsCheckBox'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     *
     * @return this recorder
     */
    public IssuesRecorder setEnabledForAggregation(final boolean isChecked) {
        aggregatingResults.check(isChecked);

        return this;
    }

    /**
     * If {@code true}, then the result of the quality gate is ignored when selecting a reference build. This option is
     * disabled by default, so a failing quality gate will be passed from build to build until the original reason for
     * the failure has been resolved.
     *
     * @param ignoreQualityGate
     *         if {@code true} then the result of the quality gate is ignored, otherwise only build with a successful
     *         quality gate are selected
     *
     * @return this recorder
     */
    public IssuesRecorder setIgnoreQualityGate(final boolean ignoreQualityGate) {
        this.ignoreQualityGate.check(ignoreQualityGate);

        return this;
    }

    /**
     * Adds the path to the folder that contains the source code. If not relative and thus not part of the workspace
     * then this folder needs to be added in Jenkins global configuration.
     *
     * @param sourceDirectory
     *         a folder containing the source code
     *
     * @return this recorder
     */
    public IssuesRecorder addSourceDirectory(final String sourceDirectory) {
        String path = createPageArea("sourceDirectories", sourceDirectories::click);
        SourceCodeDirectoryPanel panel = new SourceCodeDirectoryPanel(this, path);
        panel.setPath(sourceDirectory);

        return this;
    }

    /**
     * Determines whether the results for each configured static analysis result should be aggregated into a single
     * result or if every tool should get an individual result.
     *
     * @param aggregatingResults
     *         if {@code true} then the results of each static analysis tool should be aggregated into a single result,
     *         if {@code false} then every tool should get an individual result.
     *
     * @return this recorder
     */
    public IssuesRecorder setAggregatingResults(final boolean aggregatingResults) {
        this.aggregatingResults.check(aggregatingResults);

        return this;
    }

    /**
     * Determines whether the report results will go to logger output.
     *
     * @param quiet
     *         if {@code true} then the report logging of each static analysis tool is muted
     *         if {@code false} then reports logging goes to loghandler output
     *
     * @return this recorder
     */
    public IssuesRecorder setQuiet(final boolean quiet) {
        this.quiet.check(quiet);

        return this;
    }

    /**
     * Sets the SCM that should be used to find the reference build for. The reference recorder will select the SCM
     * based on a substring comparison, there is no need to specify the full name.
     *
     * @param scm
     *         the ID of the SCM to use (a substring of the full ID)
     *
     * @return this recorder
     */
    public IssuesRecorder setScm(final String scm) {
        this.scm.set(scm);

        return this;
    }

    /**
     * Determines whether SCM blaming should be disabled or not.
     *
     * @param blameDisabled
     *         {@code true} if SCM blaming should be disabled, {@code false} otherwise
     *
     * @return this recorder
     */
    public IssuesRecorder setSkipBlames(final boolean blameDisabled) {
        skipBlames.check(blameDisabled);

        return this;
    }

    /**
     * Determines whether post-processing should be disabled or not.
     *
     * @param skipPostProcessing
     *         {@code true} if post-processing should be disabled, {@code false} otherwise
     *
     * @return this recorder
     */
    public IssuesRecorder setSkipPostProcessing(final boolean skipPostProcessing) {
        this.skipPostProcessing.check(skipPostProcessing);

        return this;
    }

    /**
     * Determines whether to fail the build on errors during the step of recording issues.
     *
     * @param failOnError
     *         if {@code true} then the build will be failed on errors, {@code false} then errors are only reported in
     *         the UI
     *
     * @return this recorder
     */
    public IssuesRecorder setFailOnError(final boolean failOnError) {
        this.failOnError.check(failOnError);

        return this;
    }

    /**
     * Determines whether skip publishing of checks.
     *
     * @param skipPublishingChecks
     *         if {@code true} then publishing checks should be skipped, {@code false} otherwise
     *
     * @return this recorder
     */
    public IssuesRecorder setSkipPublishingChecks(final boolean skipPublishingChecks) {
        this.skipPublishingChecks.check(skipPublishingChecks);

        return this;
    }

    /**
     * Determines which issues should be published using the Checks API as annotations.
     *
     * @param checksAnnotationScope
     *         determines which issues should be shown
     *
     * @return this recorder
     */
    public IssuesRecorder setChecksAnnotationScope(final ChecksAnnotationScope checksAnnotationScope) {
        this.checksAnnotationScope.select(checksAnnotationScope.toString());

        return this;
    }

    /**
     * Sets the report file pattern.
     *
     * @param pattern
     *         the pattern to set
     *
     * @return this recorder
     */
    public IssuesRecorder setReportFilePattern(final String pattern) {
        reportFilePattern.set(pattern);

        return this;
    }

    /**
     * Sets the type of the trend chart that should be shown on the job page.
     *
     * @param trendChartType
     *         the type of the trend chart to use
     *
     * @return this recorder
     */
    public IssuesRecorder setTrendChartType(final TrendChartType trendChartType) {
        this.trendChartType.select(trendChartType.toString());

        return this;
    }

    /**
     * Sets the source code retention strategy.
     *
     * @param sourceCodeRetention
     *         the type of strategy to use
     *
     * @return this recorder
     */
    public IssuesRecorder setSourceCodeRetention(final SourceCodeRetention sourceCodeRetention) {
        this.sourceCodeRetention.select(sourceCodeRetention.toString());

        return this;
    }

    /**
     * Sets the healthy report values.
     *
     * @param healthy
     *         the number of issues when health is reported as 100%
     * @param unhealthy
     *         the number of issues when health is reported as 0%
     * @param minimumSeverity
     *         the severity to consider
     *
     * @return this recorder
     */
    public IssuesRecorder setHealthReport(final int healthy, final int unhealthy, final String minimumSeverity) {
        healthyThreshold.set(healthy);
        unhealthyThreshold.set(unhealthy);
        healthSeverity.select(minimumSeverity);

        return this;
    }

    /**
     * Opens the advanced section.
     */
    public final void openAdvancedOptions() {
        if (advancedButton != null && advancedButton.exists()) {
            advancedButton.click();
        }
    }

    private StaticAnalysisTool createToolPageArea(final String toolName) {
        String path = createPageArea("toolProxies", toolsRepeatable::click);

        StaticAnalysisTool tool = new StaticAnalysisTool(this, path);
        tool.setTool(toolName);
        return tool;
    }

    /**
     * Sets the name of the static analysis tool to use and the pattern.
     *
     * @param toolName
     *         the tool name
     * @param pattern
     *         the pattern
     *
     * @return this recorder
     */
    public IssuesRecorder setToolWithPattern(final String toolName, final String pattern) {
        StaticAnalysisTool tool = new StaticAnalysisTool(this, "toolProxies");
        tool.setTool(toolName);
        tool.setPattern(pattern);

        return this;
    }

    /**
     * Adds a new quality gate.
     *
     * @param threshold
     *         the minimum number of issues that fails the quality gate
     * @param type
     *         the type of the quality gate
     * @param criticality
     *         determines whether the quality gate sets the build result to Unstable or Failed
     *
     * @return this recorder
     */
    public IssuesRecorder addQualityGateConfiguration(final int threshold, final QualityGateType type,
            final QualityGateCriticality criticality) {
        String path = createPageArea("qualityGates", qualityGatesRepeatable::click);
        QualityGatePanel qualityGate = new QualityGatePanel(this, path);
        qualityGate.setThreshold(threshold);
        qualityGate.setType(type);
        qualityGate.setCriticality(criticality);

        return this;
    }

    /**
     * Adds a new issue filter.
     *
     * @param filterName
     *         name of the filter
     * @param regex
     *         regular expression to apply
     *
     * @return this recorder
     */
    public IssuesRecorder addIssueFilter(final String filterName, final String regex) {
        String path = createPageArea("filters", () -> filtersRepeatable.selectDropdownMenu(filterName));
        IssueFilterPanel filter = new IssueFilterPanel(this, path);
        filter.setFilter(regex);

        return this;
    }

    static WebElement findUnstableRadioButton(final WebElement pageArea, final boolean isUnstable) {
        return pageArea.findElement(
                by.xpath(".//input[@type='radio' and contains(@path,'unstable[" + isUnstable + "]')]"));
    }

    /**
     * Available quality gate types.
     */
    public enum QualityGateType {
        TOTAL("Total (any severity)"),
        TOTAL_ERROR("Total (errors only)"),
        TOTAL_HIGH("Total (severity high only)"),
        TOTAL_NORMAL("Total (severity normal only)"),
        TOTAL_LOW("Total (severity low only)"),

        NEW("New (any severity)"),
        NEW_ERROR("New (errors only)"),
        NEW_HIGH("New (severity high only)"),
        NEW_NORMAL("New (severity normal only)"),
        NEW_LOW("New (severity low only)"),

        DELTA("Delta (any severity)"),
        DELTA_ERROR("Delta (errors only)"),
        DELTA_HIGH("Delta (severity high only)"),
        DELTA_NORMAL("Delta (severity normal only)"),
        DELTA_LOW("Delta (severity low only)");

        private final String displayName;

        QualityGateType(final String displayName) {
            this.displayName = displayName;
        }

        /**
         * Returns the localized human-readable name of this type.
         *
         * @return human-readable name
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Available quality gate types.
     */
    public enum QualityGateCriticality {
        FAILURE("Fail the build"),
        UNSTABLE("Mark the build as unstable"),
        PIPELINE_FAILURE("Fail the step and the build"),
        PIPELINE_UNSTABLE("Mark the step and the build as unstable");

        private final String displayName;

        QualityGateCriticality(final String displayName) {
            this.displayName = displayName;
        }

        /**
         * Returns the localized human-readable name of this criticality.
         *
         * @return human-readable name
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Page area of a static analysis tool configuration.
     */
    public static class StaticAnalysisTool extends PageAreaImpl {
        private final Control tool = control("");
        private final Control pattern = control("tool/pattern");
        private final Control normalThreshold = control("tool/normalThreshold");
        private final Control highThreshold = control("tool/highThreshold");
        private final Control id = control("tool/id");
        private final Control name = control("tool/name");
        private final Control icon = control("tool/icon");
        private final Control analysisModelId = control("tool/analysisModelId");
        private final Control skipSymbolicLinks = control("tool/skipSymbolicLinks");

        StaticAnalysisTool(final PageArea issuesRecorder, final String path) {
            super(issuesRecorder, path);
        }

        /**
         * Sets the name of the tool.
         *
         * @param toolName
         *         the name of the tool, e.g., CheckStyle, CPD, etc.
         *
         * @return this
         */
        public StaticAnalysisTool setTool(final String toolName) {
            tool.select(toolName);
            return this;
        }

        /**
         * Sets the custom ID of the tool.
         *
         * @param id
         *         the ID
         *
         * @return this
         */
        public StaticAnalysisTool setId(final String id) {
            this.id.set(id);

            return this;
        }

        /**
         * Sets the custom name of the tool.
         *
         * @param name
         *         the name
         *
         * @return this
         */
        public StaticAnalysisTool setName(final String name) {
            this.name.set(name);

            return this;
        }

        /**
         * Sets the custom name of the tool.
         *
         * @param icon
         *         the icon
         *
         * @return this
         */
        public StaticAnalysisTool setIcon(final String icon) {
            this.icon.set(icon);

            return this;
        }

        /**
         * Sets the pattern of the files to parse.
         *
         * @param pattern
         *         the pattern
         *
         * @return this
         */
        public StaticAnalysisTool setPattern(final String pattern) {
            this.pattern.set(pattern);

            return this;
        }

        /**
         * Sets the normal threshold for duplicate code warnings.
         *
         * @param normalThreshold
         *         threshold to be set
         *
         * @return this
         */
        public StaticAnalysisTool setNormalThreshold(final int normalThreshold) {
            this.normalThreshold.set(normalThreshold);

            return this;
        }

        /**
         * Sets the high threshold for duplicate code warnings.
         *
         * @param highThreshold
         *         threshold to be set
         *
         * @return this
         */
        public StaticAnalysisTool setHighThreshold(final int highThreshold) {
            this.highThreshold.set(highThreshold);

            return this;
        }

        /**
         * Sets the name of the parser in the analysis-model component.
         *
         * @param analysisModelName
         *         name of the parser
         *
         * @return this
         */
        public StaticAnalysisTool setAnalysisModelId(final String analysisModelName) {
            this.analysisModelId.select(analysisModelName);

            return this;
        }

        /**
         * Sets whether to ignore symbolic links.
         *
         * @param skipSymbolicLinks
         *         determines the check state
         *
         * @return this
         */
        public StaticAnalysisTool setSkipSymbolicLinks(final boolean skipSymbolicLinks) {
            this.skipSymbolicLinks.check(skipSymbolicLinks);

            return this;
        }
    }

    /**
     * Page area of a filter configuration.
     */
    private static class IssueFilterPanel extends PageAreaImpl {
        private final Control regexField = control("pattern");

        IssueFilterPanel(final PageArea area, final String path) {
            super(area, path);
        }

        private void setFilter(final String regex) {
            regexField.set(regex);
        }
    }

    /**
     * Page area of a quality gate configuration.
     */
    private static class QualityGatePanel extends PageAreaImpl {
        private final Control threshold = control("integerThreshold");
        private final Control type = control("type");
        private final Control criticality = control("criticality");

        QualityGatePanel(final PageArea area, final String path) {
            super(area, path);
        }

        public void setThreshold(final int threshold) {
            this.threshold.set(threshold);
        }

        public void setType(final QualityGateType type) {
            this.type.select(type.getDisplayName());
        }

        public void setCriticality(final QualityGateCriticality criticality) {
            this.criticality.select(criticality.getDisplayName());
        }
    }

    /**
     * Page area of a source code path configuration.
     */
    private static class SourceCodeDirectoryPanel extends PageAreaImpl {
        private final Control path = control("path");

        SourceCodeDirectoryPanel(final PageArea area, final String path) {
            super(area, path);
        }

        public void setPath(final String path) {
            this.path.set(path);
        }
    }

    /**
     * Defines the type of trend chart to use.
     */
    public enum TrendChartType {
        /** The aggregation trend is shown <b>before</b> all other analysis tool trend charts. */
        AGGREGATION_TOOLS,
        /** The aggregation trend is shown <b>after</b> all other analysis tool trend charts. */
        TOOLS_AGGREGATION,
        /** The aggregation trend is not shown, only the analysis tool trend charts are shown. */
        TOOLS_ONLY,
        /** Neither the aggregation trend nor analysis tool trend charts are shown. */
        NONE
    }

    /**
     * Defines the retention strategy for source code files.
     */
    public enum SourceCodeRetention {
        /** Never store source code files. */
        NEVER,
        /** Store source code files of the last build, delete older artifacts. */
        LAST_BUILD,
        /** Store source code files for all builds, never delete those files automatically. */
        EVERY_BUILD,
        /** Store only changed source code files for all builds, never delete those files automatically. */
        MODIFIED
    }

    /**
     * Defines the scope of SCM checks annotations.
     */
    public enum ChecksAnnotationScope {
        /** All issues, i.e., new and outstanding. */
        ALL,
        /** Only new issues. */
        NEW,
        /** Only issues in modified code. */
        MODIFIED,
        /** No annotations will be created. */
        SKIP
    }
}
