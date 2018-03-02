package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.violations.PitAdapter;

/**
 * Provides a parser and customized messages for PIT.
 *
 * @author Ullrich Hafner
 */
public class Pit extends StaticAnalysisTool {
    static final String ID = "pit";

    /** Creates a new instance of {@link Pit}. */
    @DataBoundConstructor
    public Pit() {
        // empty constructor required for stapler
    }

    @Override
    public PitAdapter createParser() {
        return new PitAdapter();
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
            return Messages.Violations_PIT();
        }
    }
}
