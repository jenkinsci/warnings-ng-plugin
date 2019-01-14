package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.TnsdlParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Texas Instruments Code Composer Studio compiler.
 *
 * @author Ullrich Hafner
 */
public class Tnsdl extends ReportScanningTool {
    private static final long serialVersionUID = 3738252418578966192L;
    static final String ID = "tnsdl";

    /** Creates a new instance of {@link Tnsdl}. */
    @DataBoundConstructor
    public Tnsdl() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public TnsdlParser createParser() {
        return new TnsdlParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("tnsdl")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_TNSDL_ParserName();
        }
    }
}
