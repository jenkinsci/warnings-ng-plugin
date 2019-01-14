package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.Armcc5CompilerParser;
import edu.hm.hafner.analysis.parser.ArmccCompilerParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningToolSuite;

/**
 * Provides a parser and customized messages for the ArmCc compiler.
 *
 * @author Ullrich Hafner
 */
public class ArmCc extends ReportScanningToolSuite {
    private static final long serialVersionUID = 5712079077224290879L;
    static final String ID = "armcc";

    /** Creates a new instance of {@link ArmCc}. */
    @DataBoundConstructor
    public ArmCc() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public Collection<? extends IssueParser> getParsers() {
        return asList(new Armcc5CompilerParser(), new ArmccCompilerParser());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("armCc")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Armcc_ParserName();
        }
    }
}
