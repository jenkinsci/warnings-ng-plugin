package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.dry.cpd.CpdParser;

/**
 * Provides a parser and customized messages for CPD.
 *
 * @author Ullrich Hafner
 */
public class Cpd extends DuplicateCodeScanner {
    static final String ID = "cpd";

    /** Creates a new instance of {@link Cpd}.
     */
    @DataBoundConstructor
    public Cpd() {
        // empty constructor required for stapler
    }

    @Override
    public CpdParser createParser() {
        return new CpdParser();
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
            return Messages.Warnings_CPD_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }
    }
}
