package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.StyleCopParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for StyleCop.
 *
 * @author Ullrich Hafner
 */
public class StyleCop extends StaticAnalysisTool {
    static final String ID = "stylecop";

    /** Creates a new instance of {@link StyleCop}. */
    @DataBoundConstructor
    public StyleCop() {
        // empty constructor required for stapler
    }

    @Override
    public StyleCopParser createParser() {
        return new StyleCopParser();
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
            return Messages.Warnings_StyleCop_ParserName();
        }
    }
}
