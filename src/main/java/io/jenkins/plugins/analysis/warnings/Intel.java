package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.IntelParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Intel Compiler.
 *
 * @author Ullrich Hafner
 */
public class Intel extends ReportScanningTool {
    private static final long serialVersionUID = 8514076930043335408L;

    static final String ID = "intel";

    /** Creates a new instance of {@link Intel}. */
    @DataBoundConstructor
    public Intel() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IntelParser createParser() {
        return new IntelParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("intel")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Intel_ParserName();
        }
    }
}

