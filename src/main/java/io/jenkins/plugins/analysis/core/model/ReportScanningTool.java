package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.Ensure;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;

import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;
import io.jenkins.plugins.analysis.core.util.ConsoleLogReaderFactory;
import io.jenkins.plugins.analysis.core.util.EnvironmentResolver;
import io.jenkins.plugins.analysis.core.util.LogHandler;
import io.jenkins.plugins.analysis.core.util.ModelValidation;

/**
 * Describes a static analysis tool that reports issues by scanning a report file. Report files are identified using an
 * Ant style pattern.
 *
 * @author Ullrich Hafner
 */
public abstract class ReportScanningTool extends Tool {
    private static final long serialVersionUID = -1962476812276437235L;

    private String pattern = StringUtils.EMPTY;
    private String reportEncoding = StringUtils.EMPTY;
    // Use negative case to allow defaulting to false and defaulting to existing behaviour.
    private boolean skipSymbolicLinks = false;

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

    @Nullable
    public String getPattern() {
        return pattern;
    }

    /**
     * Specify if file scanning skip traversal of symbolic links.
     *
     * @param skipSymbolicLinks
     *         if symbolic links should be skipped during directory scanning.
     */
    @DataBoundSetter
    public void setSkipSymbolicLinks(final boolean skipSymbolicLinks) {
        this.skipSymbolicLinks = skipSymbolicLinks;
    }

    public boolean getSkipSymbolicLinks() {
        return skipSymbolicLinks;
    }

    private boolean followSymlinks() {
        return !getSkipSymbolicLinks();
    }

    /**
     * Returns the actual pattern to work with. If no user defined pattern is given, then the default pattern is
     * returned.
     *
     * @return the name
     * @see #setPattern(String)
     * @see ReportScanningToolDescriptor#getPattern()
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

    @Nullable
    public String getReportEncoding() {
        return reportEncoding;
    }

    @Override
    public Report scan(final Run<?, ?> run, final FilePath workspace, final Charset sourceCodeEncoding,
            final LogHandler logger) {
        String actualPattern = getActualPattern();
        if (StringUtils.isBlank(actualPattern)) {
            return scanInConsoleLog(workspace, run, logger);
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

    private Report scanInWorkspace(final FilePath workspace, final String expandedPattern, final LogHandler logger) {
        try {
            Report report = workspace.act(new FilesScanner(expandedPattern, this, reportEncoding, followSymlinks()));

            logger.log(report);

            return report;
        }
        catch (IOException e) {
            throw new ParsingException(e);
        }
        catch (InterruptedException e) {
            throw new ParsingCanceledException(e);
        }
    }

    private Report scanInConsoleLog(final FilePath workspace, final Run<?, ?> run, final LogHandler logger) {
        Ensure.that(getDescriptor().canScanConsoleLog()).isTrue(
                "Static analysis tool %s cannot scan console log output, please define a file pattern",
                getActualName());

        waitForConsoleToFlush(logger);

        Report consoleReport = new Report();
        consoleReport.logInfo("Parsing console log (workspace: '%s')", workspace);
        logger.log(consoleReport);

        Report report = createParser().parse(new ConsoleLogReaderFactory(run));

        if (getDescriptor().isConsoleLog()) {
            report.stream().filter(issue -> !issue.hasFileName())
                    .forEach(issue -> issue.setFileName(ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID));
        }

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

    @Override
    public ReportScanningToolDescriptor getDescriptor() {
        return (ReportScanningToolDescriptor) super.getDescriptor();
    }

    /** Descriptor for {@link ReportScanningTool}. **/
    public abstract static class ReportScanningToolDescriptor extends ToolDescriptor {
        private final ModelValidation model = new ModelValidation();

        /**
         * Creates a new instance of {@link ReportScanningToolDescriptor} with the given ID.
         *
         * @param id
         *         the unique ID of the tool
         */
        protected ReportScanningToolDescriptor(final String id) {
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
            return model.doCheckPattern(project, pattern);
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

        /**
         * Returns whether this parser can scan the console log. Typically, only line based parsers can scan the console
         * log. XML parsers should always parse a given file only.
         *
         * @return the parser to use
         */
        public boolean canScanConsoleLog() {
            return true;
        }

        /**
         * Returns whether the issues reported by this tool reference a location in the report itself (and not in an
         * external file). Then all these issues will get a synthetic file name so that the console log will be shown in
         * the UI.
         *
         * @return {@code true} if the issues reference the console log, {@code false} otherwise
         */
        protected boolean isConsoleLog() {
            return false;
        }
    }
}
