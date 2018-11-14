package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool.ReportingToolDescriptor;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.util.FileFinder;
import io.jenkins.plugins.analysis.core.util.LogHandler;
import io.jenkins.plugins.analysis.warnings.opentasks.TaskScanner;
import io.jenkins.plugins.analysis.warnings.opentasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.opentasks.TaskScanner.MatcherMode;
import io.jenkins.plugins.analysis.warnings.opentasks.TaskScannerBuilder;
import jenkins.MasterToSlaveFileCallable;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;

/**
 * Provides a files scanner that detects open tasks in source code files.
 *
 * @author Ullrich Hafner
 */
public class OpenTasks extends Tool {
    private static final long serialVersionUID = 4692318309214830824L;
    
    static final String ID = "open-tasks";

    private String high;
    private String normal;
    private String low;
    private boolean ignoreCase;
    private boolean asRegexp;
    private String includePattern;
    private String excludePattern;

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getIncludePattern() {
        return includePattern;
    }

    @DataBoundSetter
    public void setIncludePattern(final String includePattern) {
        this.includePattern = includePattern;
    }

    /**
     * Returns the Ant file-set pattern of files to exclude from work.
     *
     * @return Ant file-set pattern of files to exclude from work
     */
    public String getExcludePattern() {
        return excludePattern;
    }

    @DataBoundSetter
    public void setExcludePattern(final String excludePattern) {
        this.excludePattern = excludePattern;
    }

    /**
     * Returns the high priority tag identifiers.
     *
     * @return the high priority tag identifiers
     */
    public String getHigh() {
        return high;
    }

    @DataBoundSetter
    public void setHigh(final String high) {
        this.high = high;
    }

    /**
     * Returns the normal priority tag identifiers.
     *
     * @return the normal priority tag identifiers
     */
    public String getNormal() {
        return normal;
    }

    @DataBoundSetter
    public void setNormal(final String normal) {
        this.normal = normal;
    }

    /**
     * Returns the low priority tag identifiers.
     *
     * @return the low priority tag identifiers
     */
    public String getLow() {
        return low;
    }

    @DataBoundSetter
    public void setLow(final String low) {
        this.low = low;
    }

    /**
     * Returns whether case should be ignored during the scanning.
     *
     * @return {@code true}  if case should be ignored during the scanning
     */
    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    @DataBoundSetter
    public void setIgnoreCase(final boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    /**
     * Returns whether the identifiers should be treated as regular expression.
     *
     * @return {@code true} if the identifiers should be treated as regular expression
     */
    public boolean getAsRegexp() {
        return asRegexp;
    }

    @DataBoundSetter
    public void setAsRegexp(final boolean asRegexp) {
        this.asRegexp = asRegexp;
    }

    @Override
    public Report scan(final Run<?, ?> run, final FilePath workspace, final LogHandler logger) {
        try {
            return workspace.act(new AgentScanner(high, normal, low, 
                    ignoreCase ? CaseMode.IGNORE_CASE : CaseMode.CASE_SENSITIVE,
                    asRegexp ? MatcherMode.REGEXP_MATCH : MatcherMode.STRING_MATCH, 
                    includePattern, excludePattern));
        }
        catch (IOException e) {
            throw new ParsingException(e);
        }
        catch (InterruptedException ignored) {
            throw new ParsingCanceledException();
        }
    }

    /** Creates a new instance of {@link OpenTasks}. */
    @DataBoundConstructor
    public OpenTasks() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("openTasks")
    @Extension
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_OpenTasks_Name();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }
    }

    /**
     * Searches in the workspace for files matching the given include and exclude pattern and scans each file for open
     * tasks.
     */
    private static class AgentScanner extends MasterToSlaveFileCallable<Report> {
        private final String high;
        private final String normal;
        private final String low;
        private final CaseMode caseMode;
        private final MatcherMode matcherMode;
        private final String includePattern;
        private final String excludePattern;
    
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
         */
        AgentScanner(final String high, final String normal, final String low, final CaseMode caseMode,
                final MatcherMode matcherMode, final String includePattern, final String excludePattern) {
            this.high = high;
            this.normal = normal;
            this.low = low;
            this.caseMode = caseMode;
            this.matcherMode = matcherMode;
            this.includePattern = StringUtils.defaultString(includePattern);
            this.excludePattern = StringUtils.defaultString(excludePattern);
        }
    
        @Override
        public Report invoke(final File workspace, final VirtualChannel channel) throws IOException, InterruptedException {
            Report report = new Report();
            report.logInfo("Searching for files in workspace '%s' that match the include pattern '%s' and exclude pattern '%s'",
                    workspace, includePattern, excludePattern);
    
            FileFinder fileFinder = new FileFinder(includePattern, excludePattern);
            String[] fileNames = fileFinder.find(workspace);
            report.logInfo("-> found %d files that will be scanned", fileNames.length);
    
            Path root = workspace.toPath();
            TaskScanner scanner = createTaskScanner();
            for (String fileName : fileNames) {
                Path absolute = root.resolve(fileName);
                IssueBuilder issueBuilder = new IssueBuilder().setFileName(absolute.toString());
                report.addAll(scanner.scan(Files.newBufferedReader(absolute), issueBuilder));
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
}
