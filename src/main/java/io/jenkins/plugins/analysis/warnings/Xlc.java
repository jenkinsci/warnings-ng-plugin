package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;
import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.XlcCompilerParser;
import edu.hm.hafner.analysis.parser.XlcLinkerParser;

/**
 * Provides a parser and customized messages for IBM xlC compiler and linker.
 *
 * @author Ullrich Hafner
 */
public class Xlc extends StaticAnalysisToolSuite {
    static final String ID = "xlc";

    /** Creates a new instance of {@link Xlc}. */
    @DataBoundConstructor
    public Xlc() {
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return asList(new XlcCompilerParser(), new XlcLinkerParser());
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
            return Messages.Warnings_Xlc_ParserName();
        }
    }
}
