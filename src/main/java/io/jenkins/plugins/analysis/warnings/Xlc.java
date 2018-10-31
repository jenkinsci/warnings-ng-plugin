package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.XlcCompilerParser;
import edu.hm.hafner.analysis.parser.XlcLinkerParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for IBM xlC compiler and linker.
 *
 * @author Ullrich Hafner
 */
public class Xlc extends StaticAnalysisToolSuite {
    private static final long serialVersionUID = -3811101878455857601L;
    static final String ID = "xlc";

    /** Creates a new instance of {@link Xlc}. */
    @DataBoundConstructor
    public Xlc() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return asList(new XlcCompilerParser(), new XlcLinkerParser());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("xlc")
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Xlc_ParserName();
        }
    }
}
