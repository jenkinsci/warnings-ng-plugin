package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides parsers and customized messages for RuboCop.
 *
 * @author Ullrich Hafner
 */
public class RuboCop extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -6972204105563729273L;

    private static final String ID = "rubocop";

    /** Creates a new instance of {@link RuboCop}. */
    @DataBoundConstructor
    public RuboCop() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("ruboCop")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(ID, getDisplayName(), getDescriptionProvider());
        }
    }
}
