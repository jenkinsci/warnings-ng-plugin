package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.AjcParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the AspectJ (ajc) Compiler.
 *
 * @author Ullrich Hafner
 */
public class Ajc extends ReportScanningTool {
    private static final long serialVersionUID = 207829559393914788L;
    static final String ID = "aspectj";

    /** Creates a new instance of {@link Ajc}. */
    @DataBoundConstructor
    public Ajc() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public AjcParser createParser() {
        return new AjcParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("ajc")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_AjcParser_ParserName();
        }
    }
}
