package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.dry.simian.SimianParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

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
        super();
        // empty constructor required for stapler
    }

    @Override
    public SimianParser createParser() {
        return new SimianParser(getHighThreshold(), getNormalThreshold());
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DryLabelProvider {
        LabelProvider() {
            super(ID, Messages.Warnings_Simian_ParserName());
        }
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("simian")
    @Extension
    public static class Descriptor extends DryDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Simian_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }
    }
}
