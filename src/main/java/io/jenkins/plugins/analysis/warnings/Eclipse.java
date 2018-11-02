package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.EclipseParser;
import edu.hm.hafner.analysis.parser.EclipseXMLParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Eclipse Compiler.
 *
 * @author Ullrich Hafner
 */
public class Eclipse extends StaticAnalysisToolSuite {
    private static final long serialVersionUID = -2312612497121380654L;
    static final String ID = "eclipse";

    /** Creates a new instance of {@link Eclipse}. */
    @DataBoundConstructor
    public Eclipse() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return asList(new EclipseParser(), new EclipseXMLParser());
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
