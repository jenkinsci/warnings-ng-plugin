package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.dry.dupfinder.DupFinderParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

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
    @Symbol("dupFinder")
    @Extension
    public static class Descriptor extends DryDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_DupFinder_ParserName();
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
