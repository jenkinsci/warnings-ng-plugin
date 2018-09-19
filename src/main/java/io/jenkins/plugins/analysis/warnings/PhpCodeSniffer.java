package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PHP_CodeSniffer. Delegates to {@link CheckStyleParser}.
 *
 * @author Ullrich Hafner
 */
public class PhpCodeSniffer extends StaticAnalysisTool {
    private static final long serialVersionUID = -7944828406964963020L;
    static final String ID = "php-code-sniffer";

    /** Creates a new instance of {@link PhpCodeSniffer}. */
    @DataBoundConstructor
    public PhpCodeSniffer() {
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
            return Messages.Warnings_PhpCodeSniffer_Name();
        }

        @Override
        public String getHelp() {
            return "Use option --report=checkstyle.";
        }

        @Override
        public String getUrl() {
            return "https://github.com/squizlabs/PHP_CodeSniffer";
        }
    }
}
