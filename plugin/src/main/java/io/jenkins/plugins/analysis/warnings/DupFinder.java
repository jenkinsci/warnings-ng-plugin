package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

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
    @Serial
    private static final long serialVersionUID = -1073794044577239113L;
    private static final String ID = "dupfinder";

    /** Creates a new instance of {@link DupFinder}. */
    @DataBoundConstructor
    public DupFinder() {
        super();
        // empty constructor required for stapler
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DryLabelProvider {
        LabelProvider(final String displayName) {
            super(ID, displayName);
        }
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("dupFinder")
    @Extension
    public static class Descriptor extends DuplicateCodeDescriptor {
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
            return new LabelProvider(getDisplayName());
        }
    }
}
