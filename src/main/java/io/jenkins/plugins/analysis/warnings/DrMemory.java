package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.DrMemoryParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Dr. Memory Errors.
 *
 * @author Ullrich Hafner
 */
public class DrMemory extends ReportScanningTool {
    private static final long serialVersionUID = -8292426833255285102L;
    static final String ID = "dr-memory";

    /** Creates a new instance of {@link DrMemory}. */
    @DataBoundConstructor
    public DrMemory() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public DrMemoryParser createParser() {
        return new DrMemoryParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("drMemory")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public String getHelp() {
            return Messages.Warning_SlowMultiLineParser();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_DrMemory_ParserName();
        }
    }
}
