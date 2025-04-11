package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for PHPStan.
 *
 * @author Jeroen Jans
 */
public class PhpStan extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 2699509705079011738L;

    private static final String ID = "phpstan";

    /** Creates a new instance of {@link PhpStan}. */
    @DataBoundConstructor
    public PhpStan() {
        super();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("phpStan")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName(), getDescriptionProvider());
        }
    }
}
