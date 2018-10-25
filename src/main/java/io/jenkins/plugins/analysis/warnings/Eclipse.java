package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.EclipseParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for the Eclipse Compiler.
 *
 * @author Ullrich Hafner
 */
public class Eclipse extends StaticAnalysisTool {
    private static final long serialVersionUID = -2312612497121380654L;
    static final String ID = "eclipse";

    /** Creates a new instance of {@link Eclipse}. */
    @DataBoundConstructor
    public Eclipse() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public EclipseParser createParser() {
        return new EclipseParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("eclipse")
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
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
            return Messages.Warnings_EclipseParser_ParserName();
        }
    }
}
