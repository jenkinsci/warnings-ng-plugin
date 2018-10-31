package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.ErlcParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for the ERL Compiler.
 *
 * @author Ullrich Hafner
 */
public class Erlc extends StaticAnalysisTool {
    private static final long serialVersionUID = 8254330761908676605L;
    static final String ID = "erlc";

    /** Creates a new instance of {@link Erlc}. */
    @DataBoundConstructor
    public Erlc() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public ErlcParser createParser() {
        return new ErlcParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("erlc")
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Erlang_ParserName();
        }
    }
}
