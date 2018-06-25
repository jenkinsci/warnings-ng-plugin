package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.DocFxAdapter;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for DocFX.
 *
 * @author Ullrich Hafner
 */
public class DocFx extends StaticAnalysisTool {
    private static final long serialVersionUID = 268538556620830869L;
    static final String ID = "docfx";

    /** Creates a new instance of {@link DocFx}. */
    @DataBoundConstructor
    public DocFx() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public DocFxAdapter createParser() {
        return new DocFxAdapter();
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
            return Messages.Violations_DocFx();
        }
    }
}
