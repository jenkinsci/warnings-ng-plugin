package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for Pylint.
 *
 * @author Ullrich Hafner
 */
public class PyLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 4578376477574960381L;
    private static final String ID = "pylint";

    /** Creates a new instance of {@link PyLint}. */
    @DataBoundConstructor
    public PyLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("pyLint")
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
