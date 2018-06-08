package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.dry.simian.SimianParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Simian duplication scanner.
 *
 * @author Ullrich Hafner
 */
public class Simian extends DuplicateCodeScanner {
    private static final long serialVersionUID = 5817021796077055763L;
    static final String ID = "simian";

    /** Creates a new instance of {@link Simian}. */
    @DataBoundConstructor
    public Simian() {
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public SimianParser createParser() {
        return new SimianParser();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DryLabelProvider {
        LabelProvider() {
            super(ID);
        }
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends DryDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Simian_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }
    }
}
