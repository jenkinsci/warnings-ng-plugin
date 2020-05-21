package io.jenkins.plugins.analysis.warnings;

import java.util.function.Consumer;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Page object for the IssuesRecorder of the warnings plugin (white mountains release).
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Describable("Record compiler warnings and static analysis results")
public class IssuesRecorder extends AbstractStep implements PostBuildStep {
    private final Control toolsRepeatable = findRepeatableAddButtonFor("tools");
    private final Control filtersRepeatable = findRepeatableAddButtonFor("filters");
    private final Control qualityGatesRepeatable = findRepeatableAddButtonFor("qualityGates");
    private final Control advancedButton = control("advanced-button");
    private final Control enabledForFailureCheckBox = control("enabledForFailure");
    private final Control ignoreQualityGate = control("ignoreQualityGate");
    private final Control overallResultMustBeSuccessCheckBox = control("overallResultMustBeSuccess");
    private final Control referenceJobField = control("referenceJobName");
    private final Control aggregatingResultsCheckBox = control("aggregatingResults");
    private final Control sourceCodeEncoding = control("sourceCodeEncoding");
    private final Control sourceDirectory = control("sourceDirectory");
    private final Control blameDisabled = control("blameDisabled");
    private final Control forensicsDisabled = control("forensicsDisabled");
    private final Control ignoreFailedBuilds = control("ignoreFailedBuilds");
    private final Control failOnError = control("failOnError");
    private final Control reportFilePattern = control("/toolProxies/tool/pattern");
    private final Control trendChartType = control("trendChartType");
    private final Control healthyThreshold = control("healthy");
    private final Control unhealthyThreshold = control("unhealthy");
    private final Control healthSeverity = control("minimumSeverity");



    /**
     * Determines the result of the quality gate.
     */
    public enum QualityGateBuildResult {
        UNSTABLE,
        FAILED
    }

    /**
     * Returns the repeatable add button for the specified property.
     *
     * @param propertyName
     *         the name of the repeatable property
     *
     * @return the selected repeatable add button
     */
    protected Control findRepeatableAddButtonFor(final String propertyName) {
        return control(by.xpath("//div[@id='" + propertyName + "']//button[contains(@path,'-add')]"));
    }

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

        ScrollerUtil.hideScrollerTabBar(driver);
        openAdvancedOptions();
    }

    /**
     * Sets the name of the static analysis tool to use.
     *
     * @param toolName
     *         the tool name
     *
     * @return the sub page of the tool
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
     * @return the sub page of the tool
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
     * @return the sub page of the tool
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
     * @return the sub page of the tool
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
     * @return the sub page of the tool
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
     * @return the sub page of the tool
     */
    public StaticAnalysisTool addTool(final String toolName, final String pattern) {
        return addTool(toolName, tool -> tool.setPattern(pattern));
    }

    /**
     * Returns the value of the Source code encoding field
     *
     * @return the source code encoding
     */
    public String getSourceCodeEncoding(){return sourceCodeEncoding.get();}

    /**
     * Returns if the run always / enabled for failure checkbox is checked
     *
     * @return the enabled for failure checkbox value
     */
    public boolean getEnabledForFailure(){return enabledForFailureCheckBox.resolve().isSelected();}

    /**
     * Returns  if the aggregate results checkbox is checked
     *
     * @return the aggregate results checkbox value
     */
    public boolean getAggregatingResults(){return aggregatingResultsCheckBox.resolve().isSelected();}

    /**
     * Returns if the ignore quality gate checkbox is checked
     *
     * @return the ignore quality gate checkbox value
     */
    public boolean getIgnoreQualityGate(){return ignoreQualityGate.resolve().isSelected();}

    /**
     * Returns if
     *  TODO What checkbox is this?
     * @return
     */
    public String getOverallResultMustBeSuccess(){return overallResultMustBeSuccessCheckBox.get();}

    /**
     * Returns the value of the reference job field
     *
     * @return the reference job value
     */
    public String getReferenceJobField(){return referenceJobField.get();}

    public String getSourceDirectory() {
        return sourceDirectory.get();
    }

    public String getTrendChartType() {
        return trendChartType.get();
    }

    public boolean getBlameDisabled() {
        return blameDisabled.resolve().isSelected();
    }

    public boolean getForensicsDisabled() {
        return forensicsDisabled.resolve().isSelected();
    }

    public boolean getIgnoreFailedBuilds() {
        return ignoreFailedBuilds.resolve().isSelected();
    }

    public boolean getFailOnError() {
        return failOnError.resolve().isSelected();
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

    /**
     * Sets the source code encoding to the specified value.
     *
     * @param encoding
     *         the encoding to use when reading source files
     */
    public void setSourceCodeEncoding(final String encoding) {
        sourceCodeEncoding.set(encoding);
    }

    /**
     * Enables or disables the checkbox 'enabledForFailure'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setEnabledForFailure(final boolean isChecked) {
        enabledForFailureCheckBox.check(isChecked);
    }

    /**
     * Enables or disables the checkbox 'aggregatingResultsCheckBox'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setEnabledForAggregation(final boolean isChecked) {
        aggregatingResultsCheckBox.check(isChecked);
    }

    /**
     * Enables or disables the checkbox 'ignoreAnalysisResult'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setIgnoreQualityGate(final boolean isChecked) {
        ignoreQualityGate.check(isChecked);
    }

    /**
     * Enables or disables the checkbox 'overallResultMustBeSuccess'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setOverallResultMustBeSuccess(final boolean isChecked) {
        overallResultMustBeSuccessCheckBox.check(isChecked);
    }

    /**
     * Sets the value of the input field 'referenceJob'.
     *
     * @param referenceJob
     *         the name of the referenceJob
     */
    public void setReferenceJobField(final String referenceJob) {
        referenceJobField.set(referenceJob);
    }

    /**
     * Sets the value of the input field 'Source Directory'.
     *
     * @param directory
     *         the name of the sourceDirectory
     */
    public void setSourceDirectory(final String directory) {
        sourceDirectory.set(directory);
    }

    public void setAggregatingResults(final boolean isChecked) {
        aggregatingResultsCheckBox.check(isChecked);
    }

    public void setBlameDisabled(final boolean b) {
        blameDisabled.check(b);
    }

    public void setForensicsDisabled(final boolean b) {
        forensicsDisabled.check(b);
    }

    public void setIgnoreFailedBuilds(final boolean b) {
        ignoreFailedBuilds.check(b);
    }

    public void setFailOnError(final boolean b) {
        failOnError.check(b);
    }

    public void setReportFilePattern(final String pattern) {
        reportFilePattern.set(pattern);
    }

    public void setTrendChartType(final TrendChartType selection) {
        trendChartType.select(selection.toString());
    }

    public void setHealthReport(final int healthy, final int unhealthy, final String severity) {
        healthyThreshold.set(healthy);
        unhealthyThreshold.set(unhealthy);
        healthSeverity.select(severity);
    }

    /**
     * Opens the advanced section.
     */
    public void openAdvancedOptions() {
        if (advancedButton != null && advancedButton.exists()) {
            advancedButton.click();
        }
    }

    private StaticAnalysisTool createToolPageArea(final String toolName) {
        String path = createPageArea("toolProxies", () -> toolsRepeatable.click());

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
     */
    public void setToolWithPattern(final String toolName, final String pattern) {
        StaticAnalysisTool tool = new StaticAnalysisTool(this, "toolProxies");
        tool.setTool(toolName);
        tool.setPattern(pattern);
    }

    /**
     * Adds a new quality gate.
     *
     * @param threshold
     *         the minimum number of issues that fails the quality gate
     * @param type
     *         the type of the quality gate
     * @param result
     *         determines whether the quality gate sets the build result to Unstable or Failed
     */
    public void addQualityGateConfiguration(final int threshold, final QualityGateType type, final QualityGateBuildResult result) {
        String path = createPageArea("qualityGates", () -> qualityGatesRepeatable.click());
        QualityGatePanel qualityGate = new QualityGatePanel(this, path);
        qualityGate.setThreshold(threshold);
        qualityGate.setType(type);
        qualityGate.setUnstable(result == QualityGateBuildResult.UNSTABLE);
    }

    /**
     * Adds a new issue filter.
     *
     * @param filterName
     *         name of the filter
     * @param regex
     *         regular expression to apply
     */
    public void addIssueFilter(final String filterName, final String regex) {
        String path = createPageArea("filters", () -> filtersRepeatable.selectDropdownMenu(filterName));
        IssueFilterPanel filter = new IssueFilterPanel(this, path);
        filter.setFilter(regex);
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
         * Returns the localized human readable name of this type.
         *
         * @return human readable name
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

        StaticAnalysisTool(final PageArea issuesRecorder, final String path) {
            super(issuesRecorder, path);
        }

        /**
         * Sets the name of the tool.
         *
         * @param toolName
         *         the name of the tool, e.g. CheckStyle, CPD, etc.
         *
         * @return this
         */
        public StaticAnalysisTool setTool(final String toolName) {
            tool.select(toolName);
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
        private final Control threshold = control("threshold");
        private final Control type = control("type");

        QualityGatePanel(final PageArea area, final String path) {
            super(area, path);
        }

        public void setThreshold(final int threshold) {
            this.threshold.set(threshold);
        }

        public void setType(final QualityGateType type) {
            this.type.select(type.getDisplayName());
        }

        public void setUnstable(final boolean isUnstable) {
            self().findElement(by.xpath(".//input[@type='radio' and contains(@path,'unstable[" + isUnstable + "]')]")).click();
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
}
