package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.dry.dupfinder.DupFinderParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Resharper DupFinder.
 *
 * @author Ullrich Hafner
 */
public class DupFinder extends DuplicateCodeScanner {
    private static final long serialVersionUID = -1073794044577239113L;
    static final String ID = "dupfinder";

    /** Creates a new instance of {@link DupFinder}. */
    @DataBoundConstructor
    public DupFinder() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public DupFinderParser createParser() {
        return new DupFinderParser();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DryLabelProvider {
        LabelProvider() {
            super(ID, Messages.Warnings_DupFinder_ParserName());
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
            return Messages.Warnings_DupFinder_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }
    }
}
