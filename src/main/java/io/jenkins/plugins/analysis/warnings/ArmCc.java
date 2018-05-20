package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.Armcc5CompilerParser;
import edu.hm.hafner.analysis.parser.ArmccCompilerParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Buckminster Compiler.
 *
 * @author Ullrich Hafner
 */
public class ArmCc extends StaticAnalysisToolSuite {
    private static final long serialVersionUID = 5712079077224290879L;
    static final String ID = "armcc";

    /** Creates a new instance of {@link ArmCc}. */
    @DataBoundConstructor
    public ArmCc() {
        // empty constructor required for stapler
    }

    @Override
    public Collection<? extends AbstractParser> getParsers() {
        return asList(new Armcc5CompilerParser(), new ArmccCompilerParser());
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Armcc_ParserName();
        }
    }
}
