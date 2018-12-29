package io.jenkins.plugins.analysis.warnings.tasks;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.Report;

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import io.jenkins.plugins.analysis.core.util.FileFinder;
import io.jenkins.plugins.analysis.core.util.ModelValidation;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;

/**
 * Searches in the workspace for files matching the given include and exclude pattern and scans each file for open
 * tasks.
 */
class AgentScanner extends MasterToSlaveFileCallable<Report> {
    private static final long serialVersionUID = -4417487030800559491L;

    private final String highTasks;
    private final String normalTasks;
    private final String lowTasks;
    private final CaseMode caseMode;
    private final MatcherMode matcherMode;
    private final String includePattern;
    private final String excludePattern;
    private final String sourceCodeEncoding;

    /**
     * Creates a new {@link AgentScanner}.
     *
     * @param highTasks
     *         highTasks priority tag identifiers
     * @param normalTasks
     *         normalTasks priority tag identifiers
     * @param lowTasks
     *         lowTasks priority tag identifiers
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
    @SuppressWarnings("ParameterNumber")
    AgentScanner(final String highTasks, final String normalTasks, final String lowTasks, final CaseMode caseMode,
            final MatcherMode matcherMode, final String includePattern, final String excludePattern,
            final String sourceCodeEncoding) {
        super();

        this.highTasks = highTasks;
        this.normalTasks = normalTasks;
        this.lowTasks = lowTasks;
        this.caseMode = caseMode;
        this.matcherMode = matcherMode;
        this.includePattern = StringUtils.defaultString(includePattern);
        this.excludePattern = StringUtils.defaultString(excludePattern);
        this.sourceCodeEncoding = sourceCodeEncoding;
    }

    @Override
    public Report invoke(final File workspace, final VirtualChannel channel) {
        Report report = new Report();
        report.logInfo("Searching for files in workspace '%s' that match the include pattern '%s' and exclude pattern '%s'",
                workspace, includePattern, excludePattern);

        FileFinder fileFinder = new FileFinder(includePattern, excludePattern);
        String[] fileNames = fileFinder.find(workspace);
        report.logInfo("-> found %d files that will be scanned", fileNames.length);
        Path root = workspace.toPath();
        TaskScanner scanner = createTaskScanner();
        report.logInfo(scanner.getTaskTags());
        report.logInfo("Scanning all %d files for open tasks", fileNames.length);
        for (String fileName : fileNames) {
            report.addAll(scanner.scan(root.resolve(fileName), getCharset()));

            if (Thread.interrupted()) {
                throw new ParsingCanceledException();
            }
        }
        report.logInfo("Found a total of %d open tasks", report.size());
        Map<String, Integer> countPerType = report.getPropertyCount(Issue::getType);
        for (Entry<String, Integer> entry : countPerType.entrySet()) {
            report.logInfo("-> %s: %d open tasks", entry.getKey(), entry.getValue());
        }
        return report;
    }

    private Charset getCharset() {
        return new ModelValidation().getCharset(sourceCodeEncoding);
    }

    private TaskScanner createTaskScanner() {
        TaskScannerBuilder builder = new TaskScannerBuilder();
        builder.setHighTasks(highTasks)
                .setNormalTasks(normalTasks)
                .setLowTasks(lowTasks)
                .setMatcherMode(matcherMode)
                .setCaseMode(caseMode);
        return builder.build();
    }

    @Override @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AgentScanner that = (AgentScanner) o;

        if (highTasks != null ? !highTasks.equals(that.highTasks) : that.highTasks != null) {
            return false;
        }
        if (normalTasks != null ? !normalTasks.equals(that.normalTasks) : that.normalTasks != null) {
            return false;
        }
        if (lowTasks != null ? !lowTasks.equals(that.lowTasks) : that.lowTasks != null) {
            return false;
        }
        if (caseMode != that.caseMode) {
            return false;
        }
        if (matcherMode != that.matcherMode) {
            return false;
        }
        if (includePattern != null ? !includePattern.equals(that.includePattern) : that.includePattern != null) {
            return false;
        }
        if (excludePattern != null ? !excludePattern.equals(that.excludePattern) : that.excludePattern != null) {
            return false;
        }
        return sourceCodeEncoding != null ?
                sourceCodeEncoding.equals(that.sourceCodeEncoding) : that.sourceCodeEncoding == null;
    }

    @Override @SuppressWarnings("all")
    public int hashCode() {
        int result = highTasks != null ? highTasks.hashCode() : 0;
        result = 31 * result + (normalTasks != null ? normalTasks.hashCode() : 0);
        result = 31 * result + (lowTasks != null ? lowTasks.hashCode() : 0);
        result = 31 * result + (caseMode != null ? caseMode.hashCode() : 0);
        result = 31 * result + (matcherMode != null ? matcherMode.hashCode() : 0);
        result = 31 * result + (includePattern != null ? includePattern.hashCode() : 0);
        result = 31 * result + (excludePattern != null ? excludePattern.hashCode() : 0);
        result = 31 * result + (sourceCodeEncoding != null ? sourceCodeEncoding.hashCode() : 0);
        return result;
    }
}
