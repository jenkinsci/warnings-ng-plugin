package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool.ReportingToolDescriptor;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.util.LogHandler;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;

/**
 * Provides a files scanner that detects open tasks in source code files.
 *
 * @author Ullrich Hafner
 */
public class OpenTasks extends Tool {
    private static final long serialVersionUID = 4692318309214830824L;
    static final String ID = "open-tasks";

    /** Tag identifiers indicating high priority. */
    private String high;
    /** Tag identifiers indicating normal priority. */
    private String normal;
    /** Tag identifiers indicating low priority. */
    private String low;
    /** Tag identifiers indicating case sensitivity. */
    private boolean ignoreCase;
    /** If the identifiers should be treated as regular expression. */
    private boolean asRegexp;
    /** Ant file-set pattern of files to work with. */
    private String pattern;
    /** Ant file-set pattern of files to exclude from work. */
    private String excludePattern;

    @Override
    public Report scan(final Run<?, ?> run, final FilePath workspace, final LogHandler logger) {
        return new Report();
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
            return "Open Tasks";
        }
    }
}
