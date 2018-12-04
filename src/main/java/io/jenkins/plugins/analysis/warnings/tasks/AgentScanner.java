package io.jenkins.plugins.analysis.warnings.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.steps.JobConfigurationModel;
import io.jenkins.plugins.analysis.core.util.FileFinder;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;
import jenkins.MasterToSlaveFileCallable;

import hudson.remoting.VirtualChannel;

/**
 * Searches in the workspace for files matching the given include and exclude pattern and scans each file for open
 * tasks.
 */
public class AgentScanner extends MasterToSlaveFileCallable<Report> {
    private final String high;
    private final String normal;
    private final String low;
    private final CaseMode caseMode;
    private final MatcherMode matcherMode;
    private final String includePattern;
    private final String excludePattern;
    private final String sourceCodeEncoding;

    /**
     * Creates a new {@link AgentScanner}.
     *
     * @param high
     *         high priority tag identifiers
     * @param normal
     *         normal priority tag identifiers
     * @param low
     *         low priority tag identifiers
     * @param caseMode
     *         determines whether the tag identifiers are case sensitive
     * @param matcherMode
     *         determines whether the tag identifiers are strings or regular expressions
     * @param includePattern
     *         the files to include
     * @param excludePattern
     *         the files to exclude
     * @param sourceCodeEncoding
     *         the encoding to use to read source files
     */
    public AgentScanner(final String high, final String normal, final String low, final CaseMode caseMode,
            final MatcherMode matcherMode, final String includePattern, final String excludePattern,
            final String sourceCodeEncoding) {
        this.high = high;
        this.normal = normal;
        this.low = low;
        this.caseMode = caseMode;
        this.matcherMode = matcherMode;
        this.includePattern = StringUtils.defaultString(includePattern);
        this.excludePattern = StringUtils.defaultString(excludePattern);
        this.sourceCodeEncoding = sourceCodeEncoding;
    }

    @Override
    public Report invoke(final File workspace, final VirtualChannel channel) throws IOException, InterruptedException {
        Report report = new Report();
        report.logInfo(
                "Searching for files in workspace '%s' that match the include pattern '%s' and exclude pattern '%s'",
                workspace, includePattern, excludePattern);

        FileFinder fileFinder = new FileFinder(includePattern, excludePattern);
        String[] fileNames = fileFinder.find(workspace);
        report.logInfo("-> found %d files that will be scanned", fileNames.length);
        Path root = workspace.toPath();
        TaskScanner scanner = createTaskScanner();
        report.logInfo("Scanning all %d files for open tasks", fileNames.length);
        for (String fileName : fileNames) {
            Path absolute = root.resolve(fileName);
            IssueBuilder issueBuilder = new IssueBuilder().setFileName(absolute.toString());
            report.addAll(scanner.scan(Files.newBufferedReader(absolute, getCharset()), issueBuilder));
            if (Thread.interrupted()) {
                throw new ParsingCanceledException();
            }
        }
        report.logInfo("-> found a total of %d open tasks", report.size());
        Map<String, Integer> countPerType = report.getPropertyCount(Issue::getType);
        for (Entry<String, Integer> entry : countPerType.entrySet()) {
            report.logInfo("   %s: %d open tasks", entry.getKey(), entry.getValue());
        }
        return report;
    }

    private Charset getCharset() {
        return new JobConfigurationModel().getCharset(sourceCodeEncoding);
    }

    private TaskScanner createTaskScanner() {
        TaskScannerBuilder builder = new TaskScannerBuilder();
        builder.setHigh(high)
                .setNormal(normal)
                .setLow(low)
                .setMatcherMode(matcherMode)
                .setCaseMode(caseMode);
        return builder.build();
    }
}
