package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SvgIconLabelProvider;

/**
 * Provides a parser and customized messages for the Java compiler.
 *
 * @author Ullrich Hafner
 */
public class Java extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 2254154391638811877L;
    private static final String ID = "java";

    /** Creates a new instance of {@link NagFortran}. */
    @DataBoundConstructor
    public Java() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("java")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new SvgIconLabelProvider(getId(), getDisplayName(), getDescriptionProvider());
        }
    }
}
