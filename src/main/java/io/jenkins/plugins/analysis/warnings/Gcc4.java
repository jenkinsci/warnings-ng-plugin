package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.Gcc4CompilerParser;
import edu.hm.hafner.analysis.parser.Gcc4LinkerParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningToolSuite;

/**
 * Provides a parser and customized messages for the Gcc4Compiler and Gcc4Linker parsers.
 *
 * @author Raphael Furch
 */
public class Gcc4 extends ReportScanningToolSuite {
    private static final long serialVersionUID = 7699675509414211993L;
    static final String ID = "gcc4";

    /** Creates a new instance of {@link Gcc4}. */
    @DataBoundConstructor
    public Gcc4() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends IssueParser> getParsers() {
        return asList(new Gcc4CompilerParser(), new Gcc4LinkerParser());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("gcc4")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_gcc4_ParserName();
        }
    }
}
