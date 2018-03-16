package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.Nonnull;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import edu.hm.hafner.analysis.Issues;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.LoggerFactory;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import jenkins.tasks.SimpleBuildStep;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

/**
 * Freestyle or Maven job {@link Recorder} that scans files or the console log for issues. Publishes the created issues
 * in a {@link ResultAction} in the associated run.
 *
 * @author Ullrich Hafner
 */
public class IssuesPublisher extends Recorder implements SimpleBuildStep {
    private String logFileEncoding;
    private String sourceCodeEncoding;
    private String pattern;
    private StaticAnalysisTool tool;

    @DataBoundConstructor
    public IssuesPublisher() {
        // empty constructor required for Stapler
    }

    @CheckForNull
    public String getPattern() {
        return pattern;
    }

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
    public StaticAnalysisTool getTool() {
        return tool;
    }

    /**
     * Sets the static analysis tool that will scan files and create issues.
     *
     * @param tool
     *         the static analysis tool
     */
    @DataBoundSetter
    public void setTool(final StaticAnalysisTool tool) {
        this.tool = tool;
    }

    @CheckForNull
    public String getLogFileEncoding() {
        return logFileEncoding;
    }

    /**
     * Sets the default encoding used to read the log files that contain the warnings.
     *
     * @param logFileEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setLogFileEncoding(final String logFileEncoding) {
        this.logFileEncoding = logFileEncoding;
    }

    @CheckForNull
    public String getSourceCodeEncoding() {
        return sourceCodeEncoding;
    }

    /**
     * Sets the default encoding used to read the log files that contain the warnings.
     *
     * @param sourceCodeEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setSourceCodeEncoding(final String sourceCodeEncoding) {
        this.sourceCodeEncoding = sourceCodeEncoding;
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
            throws InterruptedException, IOException {
        IssuesScanner issuesScanner = new IssuesScanner(tool, workspace, logFileEncoding, sourceCodeEncoding,
                createLogger(listener, tool.getId()),
                createLogger(listener, String.format("[%s] [ERROR]", tool.getId())));
        Issues<?> issues;
        if (StringUtils.isBlank(pattern)) {
            issues = issuesScanner.scanInConsoleLog(run.getLogFile());
        }
        else {
            issues = issuesScanner.scanInWorkspace(pattern, run.getEnvironment(listener));
        }
    }

    private Logger createLogger(final TaskListener listener, final String name) {
        return new LoggerFactory().createLogger(listener.getLogger(), name);
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ScanAndPublishIssues_DisplayName();
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
