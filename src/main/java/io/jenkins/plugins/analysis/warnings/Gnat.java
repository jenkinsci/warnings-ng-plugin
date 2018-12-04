package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.GnatParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

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

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_gnat_ParserName();
        }
    }
}
