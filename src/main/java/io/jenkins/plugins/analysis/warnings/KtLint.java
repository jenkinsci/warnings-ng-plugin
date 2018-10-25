package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for ktlint. Delegates to {@link CheckStyleParser}.
 *
 * @author Ullrich Hafner
 */
public class KtLint extends StaticAnalysisTool {
    private static final long serialVersionUID = 1897385505660427545L;
    
    static final String ID = "ktlint";

    /** Creates a new instance of {@link KtLint}. */
    @DataBoundConstructor
    public KtLint() {
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
    @Symbol("ktLint")
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_KtLint_Name();
        }

        @Override
        public String getHelp() {
            return "Use option --reporter=checkstyle.";
        }

        @Override
        public String getUrl() {
            return "https://ktlint.github.io";
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }
    }
}
