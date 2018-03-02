package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.violations.DocFxAdapter;

/**
 * Provides a parser and customized messages for DocFX.
 *
 * @author Ullrich Hafner
 */
public class DocFx extends StaticAnalysisTool {
    static final String ID = "docfx";

    /** Creates a new instance of {@link DocFx}. */
    @DataBoundConstructor
    public DocFx() {
        // empty constructor required for stapler
    }

    @Override
    public DocFxAdapter createParser() {
        return new DocFxAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
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
