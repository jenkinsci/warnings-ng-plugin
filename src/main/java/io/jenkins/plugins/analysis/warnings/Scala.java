package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.SbtScalacParser;
import edu.hm.hafner.analysis.parser.ScalacParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides a parser and customized messages for the Scala compiler.
 *
 * @author Ullrich Hafner
 */
public class Scala extends StaticAnalysisToolSuite {
    private static final long serialVersionUID = -3425343204163661812L;
    static final String ID = "scala";

    /** Creates a new instance of {@link Scala}. */
    @DataBoundConstructor
    public Scala() {
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return asList(new ScalacParser(), new SbtScalacParser());
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_ScalaParser_ParserName();
        }
    }
}
