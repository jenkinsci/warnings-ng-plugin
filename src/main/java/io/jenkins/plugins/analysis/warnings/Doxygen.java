package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.Gcc4CompilerParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

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
    public IssueParser createParser() {
        return new Gcc4CompilerParser();
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

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Doxygen_ParserName();
        }
    }
}
