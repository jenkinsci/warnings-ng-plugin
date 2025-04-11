package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for CPD.
 *
 * @author Ullrich Hafner
 */
public class Cpd extends DuplicateCodeScanner {
    @Serial
    private static final long serialVersionUID = -4121571018057432203L;
    private static final String ID = "cpd";

    /**
     * Creates a new instance of {@link Cpd}.
     */
    @DataBoundConstructor
    public Cpd() {
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
    @Symbol("cpd")
    @Extension
    public static class Descriptor extends DuplicateCodeDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider(getDisplayName());
        }
    }
}
