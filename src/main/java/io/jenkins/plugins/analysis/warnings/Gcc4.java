package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.Gcc4CompilerParser;
import edu.hm.hafner.analysis.parser.Gcc4LinkerParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Gcc4Compiler and Gcc4Linker parsers.
 *
 * @author Raphael Furch
 */
public class Gcc4 extends StaticAnalysisToolSuite {
    static final String ID = "gcc4";

    /** Creates a new instance of {@link Gcc4}. */
    @DataBoundConstructor
    public Gcc4() {
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends AbstractParser<Issue>> getParsers() {
        return asList(new Gcc4CompilerParser(), new Gcc4LinkerParser());
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
            return Messages.Warnings_gcc4_ParserName();
        }
    }
}
