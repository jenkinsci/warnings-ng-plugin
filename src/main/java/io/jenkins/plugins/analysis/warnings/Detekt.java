package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Detekt. Delegates to {@link CheckStyleParser}.
 *
 * @author Ullrich Hafner
 */
public class Detekt extends StaticAnalysisTool {
    private static final long serialVersionUID = 2441989609462884392L;
    
    static final String ID = "detekt";

    /** Creates a new instance of {@link Detekt}. */
    @DataBoundConstructor
    public Detekt() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public CheckStyleParser createParser() {
        return new CheckStyleParser();
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
            return Messages.Warnings_Detekt_Name();
        }

        @Override
        public String getHelp() {
            return "Use option --output-format xml.";
        }

        @Override
        public String getUrl() {
            return "https://arturbosch.github.io/detekt/";
        }
    }
}
