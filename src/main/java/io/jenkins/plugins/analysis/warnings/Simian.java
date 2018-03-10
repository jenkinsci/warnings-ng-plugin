package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.dry.simian.SimianParser;

/**
 * Provides a parser and customized messages for Simian duplication scanner.
 *
 * @author Ullrich Hafner
 */
public class Simian extends DuplicateCodeScanner {
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
