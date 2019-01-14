package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.GccParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Gcc3 Compiler.
 *
 * @author Raphael Furch
 */
public class Gcc3 extends ReportScanningTool {
    private static final long serialVersionUID = -8985462824184450486L;
    static final String ID = "gcc3";

    /** Creates a new instance of {@link Gcc3}. */
    @DataBoundConstructor
    public Gcc3() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public GccParser createParser() {
        return new GccParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("gcc3")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_gcc3_ParserName();
        }
    }
}
