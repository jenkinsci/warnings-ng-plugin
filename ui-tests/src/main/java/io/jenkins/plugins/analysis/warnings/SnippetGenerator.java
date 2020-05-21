package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import edu.hm.hafner.analysis.Severity;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

/**
 * Page object for the SnippetGenerator to learning the available Pipeline steps.
 *
 * @author Lion Kosiuk
 */
public class SnippetGenerator extends PageObject {

    private static final String URI = "pipeline-syntax/";
    private static final String RECORD_ISSUES_OPTION = "recordIssues: Record compiler warnings and static analysis results";
    private final Control selectSampleStep = control("/");

    /**
     * Creates a new page object.
     *
     * @param context
     *         job context
     */
    public SnippetGenerator(WorkflowJob context) {
        super(context,  context.url(URI));
    }

    /**
     * Set the sample step of the SnippetGenerator to record Issues.
     *
     * @return issuesRecorder
     */
    public IssuesRecorder selectRecordIssues() {
        selectSampleStep.select(RECORD_ISSUES_OPTION);
        IssuesRecorder issuesRecorder = new IssuesRecorder(this, "/prototype");
        return issuesRecorder;
    }

    /**
     * Generates the sample pipeline script.
     *
     * @return script
     */
    public String generateScript() {
        WebElement button = find(By.xpath("//button[contains(text(),'Generate Pipeline Script')]"));
        button.click();

        WebElement textarea = find(By.xpath("//textarea[@name='_.']"));

        return textarea.getAttribute("value");
    }

    /**
     * Page area of a issues recorder configuration.
     */
    public static class IssuesRecorder extends PageAreaImpl {

        private final Control advancedButton = control("advanced-button");
        private final Control aggregatingResultsCheckBox = control("aggregatingResults");
        private final Control scmBlamesCheckBox = control("blameDisabled");
        private final Control scmForensicsCheckBox = control("forensicsDisabled");
        private final Control enabledForFailureCheckBox = control("enabledForFailure");
        private final Control ignoreFailedBuildsCheckBox = control("ignoreFailedBuilds");
        private final Control ignoreQualityGateCheckBox = control("ignoreQualityGate");
        private final Control referenceJobInput = control("referenceJobName");
        private final Control sourceCodeEncodingInput = control("sourceCodeEncoding");
        private final Control healthyThresholdInput = control("healthy");
        private final Control unhealthyThresholdInput = control("unhealthy");
        private final Control minimumSeveritySelect = control("minimumSeverity");

        /**
         * Creates a new page area object.
         *
         * @param snippetGenerator
         *         page object
         * @param path
         *         relative path
         */
        public IssuesRecorder(final PageObject snippetGenerator, final String path) {
            super(snippetGenerator, path);
            openAdvancedOptions();
        }

        /**
         * Sets the name of the static analysis tool to use.
         *
         * @param toolName
         *         the tool name
         *
         * @return issuesRecorder page area
         */
        public IssuesRecorder setTool(final String toolName) {
            StaticAnalysisTool tool = new StaticAnalysisTool(this, "toolProxies");
            tool.setTool(toolName);
            return this;
        }

        /**
         * Sets the name and the pattern of the static analysis tool to use.
         *
         * @param toolName
         *         the tool name
         * @param pattern
         *         the file name pattern
         *
         * @return issuesRecorder page area
         */
        public IssuesRecorder setToolWithPattern(final String toolName, final String pattern) {
            StaticAnalysisTool tool = new StaticAnalysisTool(this, "toolProxies");
            tool.setTool(toolName);
            tool.setPattern(pattern);

            return this;
        }

        /**
         * Enables or disables the checkbox 'aggregatingResultsCheckBox'.
         *
         * @param isChecked
         *         determines if the checkbox should be checked or not
         */
        public IssuesRecorder setAggregatingResults(final boolean isChecked) {
            aggregatingResultsCheckBox.check(isChecked);
            return this;
        }

        /**
         * Enables or disables the checkbox 'scmBlames'.
         *
         * @param isChecked
         *         determines if the checkbox should be checked or not
         */
        public IssuesRecorder setBlameDisabled(final boolean isChecked) {
            scmBlamesCheckBox.check(isChecked);
            return this;
        }

        /**
         * Enables or disables the checkbox 'scmForensics'.
         *
         * @param isChecked
         *         determines if the checkbox should be checked or not
         */
        public IssuesRecorder setForensicsDisabled(final boolean isChecked) {
            scmForensicsCheckBox.check(isChecked);
            return this;
        }

        /**
         * Enables or disables the checkbox 'enabledForFailure'.
         *
         * @param isChecked
         *         determines if the checkbox should be checked or not
         */
        public IssuesRecorder setEnabledForFailure(final boolean isChecked) {
            enabledForFailureCheckBox.check(isChecked);
            return this;
        }

        /**
         * Enables or disables the checkbox 'enabledForFailure'.
         *
         * @param isChecked
         *         determines if the checkbox should be checked or not
         */
        public IssuesRecorder setIgnoreFailedBuilds(final boolean isChecked) {
            ignoreFailedBuildsCheckBox.check(isChecked);
            return this;
        }

        /**
         * Enables or disables the checkbox 'ignoreQualityGate'.
         *
         * @param isChecked
         *         determines if the checkbox should be checked or not
         */
        public IssuesRecorder setIgnoreQualityGate(final boolean isChecked) {
            ignoreQualityGateCheckBox.check(isChecked);
            return this;
        }

        /**
         * Set the reference job name.
         *
         * @param referenceJobName
         *         reference job name
         */
        public IssuesRecorder setReferenceJobName(final String referenceJobName) {
            referenceJobInput.set(referenceJobName);
            return this;
        }

        /**
         * Set the source code encoding.
         *
         * @param sourceCodeEncoding
         *         source code encoding
         */
        public IssuesRecorder setSourceCodeEncoding(final String sourceCodeEncoding) {
            sourceCodeEncodingInput.set(sourceCodeEncoding);
            return this;
        }

        /**
         * Set the health report configuration.
         *
         * @param healthy
         *         healthy threshold
         * @param unhealthy
         *         unhealthy threshold
         * @param minimumSeverity
         *         health severities
         */
        public IssuesRecorder setHealthReport(final int healthy, final int unhealthy,
                final Severity minimumSeverity) {
            healthyThresholdInput.set(Integer.toString(healthy));
            unhealthyThresholdInput.set(Integer.toString(unhealthy));
            minimumSeveritySelect.select(minimumSeverity.getName());
            return this;
        }

        /**
         * Opens the advanced section.
         */
        private void openAdvancedOptions() {
            if (advancedButton != null && advancedButton.exists()) {
                advancedButton.click();
            }
        }

        /**
         * Page area of a static analysis tool configuration.
         */
        public static class StaticAnalysisTool extends PageAreaImpl {
            private final Control tool = control("");
            private final Control pattern = control("tool/pattern");

            /**
             * Creates a new page area object.
             *
             * @param issuesRecorder
             *         page object
             * @param path
             *         relative path
             */
            public StaticAnalysisTool(final IssuesRecorder issuesRecorder, final String path) {
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
        }
    }
}
