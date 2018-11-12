package io.jenkins.plugins.analysis.core.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.Ensure;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import io.jenkins.plugins.analysis.core.steps.JobConfigurationModel;
import io.jenkins.plugins.analysis.core.util.LogHandler;
import io.jenkins.plugins.analysis.core.util.EnvironmentResolver;
import io.jenkins.plugins.analysis.core.util.FilesScanner;

import hudson.FilePath;
import hudson.console.ConsoleNote;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;

/**
 * Describes a static analysis tool that reports issues by scanning a report file. Report files are identified using an
 * Ant style pattern.
 *
 * @author Ullrich Hafner
 */
public abstract class ReportScanningTool extends Tool {
    private String pattern;
    private String reportEncoding;

    /**
     * Returns a new parser to scan a log file and return the issues reported in such a file.
     *
     * @return the parser to use
     */
    public abstract IssueParser createParser();

    /**
     * Sets the Ant file-set pattern of files to work with. If the pattern is undefined then the console log is
     * scanned.
     *
     * @param pattern
     *         the pattern to use
     */
    @DataBoundSetter
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    @CheckForNull
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns the actual pattern to work with. If no user defined pattern is given, then the default pattern is
     * returned.
     *
     * @return the name
     * @see #setPattern(String)
     * @see ReportingToolDescriptor#getPattern()
     */
    public String getActualPattern() {
        return StringUtils.defaultIfBlank(pattern, getDescriptor().getPattern());
    }

    /**
     * Sets the encoding to use to read the log files that contain the warnings.
     *
     * @param reportEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setReportEncoding(final String reportEncoding) {
        this.reportEncoding = reportEncoding;
    }

    @CheckForNull
    public String getReportEncoding() {
        return reportEncoding;
    }

    @Override
    public Report scan(final Run<?, ?> run, final FilePath workspace, final LogHandler logger) {
        String actualPattern = getActualPattern();
        if (StringUtils.isBlank(actualPattern)) {
            return scanInConsoleLog(workspace, run.getLogFile(), logger);
        }
        else {
            if (StringUtils.isBlank(getPattern())) {
                logger.log("Using default pattern '%s' since user defined pattern is not set",
                        getDescriptor().getPattern());
            }

            return scanInWorkspace(workspace, expandPattern(run, actualPattern), logger);
        }
    }

    private String expandPattern(final Run<?, ?> run, final String actualPattern) {
        try {
            EnvironmentResolver environmentResolver = new EnvironmentResolver();
    
            return environmentResolver.expandEnvironmentVariables(
                    run.getEnvironment(TaskListener.NULL), actualPattern);
        }
        catch (IOException | InterruptedException ignore) {
            return actualPattern; // fallback, no expansion
        }
    }

    private Report scanInWorkspace(final FilePath workspace, final String pattern, final LogHandler logger) {
        try {
            Report report = workspace.act(new FilesScanner(pattern, this, reportEncoding));
            
            logger.log(report);

            return report;
        }
        catch (IOException e) {
            throw new ParsingException(e);
        }
        catch (InterruptedException ignored) {
            throw new ParsingCanceledException();
        }
    }

    private Report scanInConsoleLog(final FilePath workspace, final File consoleLog, final LogHandler logger) {
        Ensure.that(canScanConsoleLog()).isTrue(
                "Static analysis tool %s cannot scan console log output, please define a file pattern",
                getName());

        waitForConsoleToFlush(logger);

        Report consoleReport = new Report();
        consoleReport.logInfo("Parsing console log (workspace: '%s')", workspace);
        logger.log(consoleReport);

        Report report = createParser().parse(consoleLog.toPath(),
                new JobConfigurationModel().getCharset(reportEncoding), ConsoleNote::removeNotes);

        consoleReport.addAll(report);

        logger.log(consoleReport);

        return consoleReport;
    }

    private void waitForConsoleToFlush(final LogHandler logger) {
        try {
            logger.log("Sleeping for 5 seconds due to JENKINS-32191...");
            Thread.sleep(5000);
        }
        catch (final InterruptedException ignored) {
            // ignore
        }
    }

    /**
     * Returns whether this parser can scan the console log. Typically, only line based parsers can scan the console
     * log. XML parsers should always parse a given file only.
     *
     * @return the parser to use
     */
    public boolean canScanConsoleLog() {
        return true;
    }

    @Override
    public ReportingToolDescriptor getDescriptor() {
        return (ReportingToolDescriptor) super.getDescriptor();
    }

    /** Descriptor for {@link ReportScanningTool}. **/
    public abstract static class ReportingToolDescriptor extends ToolDescriptor {
        private final JobConfigurationModel model = new JobConfigurationModel();

        /**
         * Creates a new instance of {@link ReportingToolDescriptor} with the given ID.
         *
         * @param id
         *         the unique ID of the tool
         */
        protected ReportingToolDescriptor(final String id) {
            super(id);
        }

        /**
         * Returns a model with all available charsets.
         *
         * @return a model with all available charsets
         */
        public ComboBoxModel doFillReportEncodingItems() {
            return model.getAllCharsets();
        }

        /**
         * Performs on-the-fly validation on the ant pattern for input files.
         *
         * @param project
         *         the project
         * @param pattern
         *         the file pattern
         *
         * @return the validation result
         */
        public FormValidation doCheckPattern(@AncestorInPath final AbstractProject<?, ?> project,
                @QueryParameter final String pattern) {
            if (project != null) { // there is no workspace in pipelines
                try {
                    FilePath workspace = project.getSomeWorkspace();
                    if (workspace != null && workspace.exists()) {
                        return validatePatternInWorkspace(pattern, workspace);
                    }
                }
                catch (InterruptedException | IOException ignore) {
                    // ignore and return ok
                }
            }

            return FormValidation.ok();
        }

        private FormValidation validatePatternInWorkspace(final @QueryParameter String pattern,
                final FilePath workspace) throws IOException, InterruptedException {
            String result = workspace.validateAntFileMask(pattern, FilePath.VALIDATE_ANT_FILE_MASK_BOUND);
            if (result != null) {
                return FormValidation.error(result);
            }
            return FormValidation.ok();
        }

        /**
         * Returns the default filename pattern for this tool. Override if your typically works on a specific file.
         * Note: if you provide a default pattern then it is not possible to scan Jenkins console log of a build.
         *
         * @return the default pattern
         */
        public String getPattern() {
            return StringUtils.EMPTY;
        }
    }
}
