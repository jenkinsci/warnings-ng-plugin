package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for TSLint. Delegates to {@link CheckStyleParser}.
 *
 * @author Ullrich Hafner
 */
public class TsLint extends StaticAnalysisTool {
    private static final long serialVersionUID = -7944828406964963020L;
    static final String ID = "tslint";

    /** Creates a new instance of {@link TsLint}. */
    @DataBoundConstructor
    public TsLint() {
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
            return Messages.Warnings_TSLint_Name();
        }

        @Override
        public String getHelp() {
            return "Use option --format checkstyle.";
        }

        @Override
        public String getUrl() {
            return "https://palantir.github.io/tslint/";
        }
    }
}
