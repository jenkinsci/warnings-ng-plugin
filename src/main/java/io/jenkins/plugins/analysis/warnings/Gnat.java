package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.GnatParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Gnat Compiler.
 *
 * @author Michael Schmid
 */
public class Gnat extends ReportScanningTool {
    private static final long serialVersionUID = 1249773597483641464L;
    static final String ID = "gnat";

    /** Creates a new instance of {@link Gnat}. */
    @DataBoundConstructor
    public Gnat() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public GnatParser createParser() {
        return new GnatParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("gnat")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_gnat_ParserName();
        }
    }
}
