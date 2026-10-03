package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import java.io.File;
import java.io.Serial;
import java.nio.charset.Charset;

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import io.jenkins.plugins.analysis.core.util.FileFinder;
import io.jenkins.plugins.util.ValidationUtilities;

/**
 * Searches in the workspace for files matching the given include and exclude patterns, then scans each file
 * line-by-line for occurrences of a configurable regular expression. Runs on the agent side so that files
 * residing only on remote nodes can be read without copying them to the master.
 *
 * @author Akash Manna
 */
class AgentGrepScanner extends MasterToSlaveFileCallable<Report> {
    @Serial
    private static final long serialVersionUID = -4023490687124215210L;

    private final String regexp;
    private final String severity;
    private final String messageTemplate;
    private final String includePattern;
    private final String excludePattern;
    private final String sourceCodeEncoding;

    /**
     * Creates a new {@link AgentGrepScanner}.
     *
     * @param regexp
     *         the regular expression to match against each line
     * @param severity
     *         the name of the {@link Severity} to assign to matched issues
     * @param messageTemplate
     *         optional message template for matched issues; blank means use the matched line
     * @param includePattern
     *         Ant file-set pattern specifying files to scan
     * @param excludePattern
     *         Ant file-set pattern specifying files to exclude
     * @param sourceCodeEncoding
     *         the encoding used to read files
     */
    @SuppressWarnings("ParameterNumber")
    AgentGrepScanner(final String regexp, final String severity, final String messageTemplate,
            final String includePattern, final String excludePattern, final String sourceCodeEncoding) {
        super();

        this.regexp = regexp;
        this.severity = severity;
        this.messageTemplate = messageTemplate;
        this.includePattern = StringUtils.defaultString(includePattern);
        this.excludePattern = StringUtils.defaultString(excludePattern);
        this.sourceCodeEncoding = sourceCodeEncoding;
    }

    @Override
    @SuppressWarnings("PMD.DoNotUseThreads")
    public Report invoke(final File workspace, final VirtualChannel channel) {
        var report = new Report();
        report.logInfo(
                "Searching for files in workspace '%s' that match the include pattern '%s' and exclude pattern '%s'",
                workspace, includePattern, excludePattern);

        var fileFinder = new FileFinder(includePattern, excludePattern);
        var fileNames = fileFinder.find(workspace);
        report.logInfo("-> found %d files that will be scanned", fileNames.length);

        var scanner = new GrepScanner(regexp, Severity.valueOf(severity, Severity.WARNING_NORMAL), messageTemplate);
        report.logInfo("Scanning all %d file(s) for pattern '%s'", fileNames.length, regexp);

        var root = workspace.toPath();
        var charset = getCharset();
        for (String fileName : fileNames) {
            report.addAll(scanner.scan(root.resolve(fileName), charset).get());

            if (Thread.interrupted()) {
                throw new ParsingCanceledException();
            }
        }
        report.logInfo("Found a total of %d grep matches", report.size());
        return report;
    }

    private Charset getCharset() {
        return new ValidationUtilities().getCharset(sourceCodeEncoding);
    }
}
