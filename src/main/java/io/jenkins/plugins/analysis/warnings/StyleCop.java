package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.StyleCopParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for StyleCop.
 *
 * @author Ullrich Hafner
 */
public class StyleCop extends StaticAnalysisTool {
    private static final long serialVersionUID = 230834175319040103L;
    static final String ID = "stylecop";

    /** Creates a new instance of {@link StyleCop}. */
    @DataBoundConstructor
    public StyleCop() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public StyleCopParser createParser() {
        return new StyleCopParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("styleCop")
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_StyleCop_ParserName();
        }
    }
}
