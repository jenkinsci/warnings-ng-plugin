package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.DoxygenParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for Doxygen.
 *
 * @author Ullrich Hafner
 */
public class Doxygen extends ReportScanningTool {
    private static final long serialVersionUID = -958188599615335136L;
    static final String ID = "doxygen";

    /** Creates a new instance of {@link Doxygen}. */
    @DataBoundConstructor
    public Doxygen() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public DoxygenParser createParser() {
        return new DoxygenParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("doxygen")
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

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Doxygen_ParserName();
        }
    }
}
