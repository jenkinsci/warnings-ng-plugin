package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SymbolIconLabelProvider;

/**
 * Provides a parser and customized messages for Valgrind.
 *
 * @author Michael Trimarchi
 */
public class Valgrind extends AnalysisModelParser {
    private static final long serialVersionUID = 1L;
    private static final String ID = "valgrind";

    /** Creates a new instance of {@link Valgrind}. */
    @DataBoundConstructor
    public Valgrind() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("valgrind")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new SymbolIconLabelProvider(getId(), getDisplayName(), getDescriptionProvider(), "symbol-solid/bug-slash plugin-font-awesome-api");
        }
    }
}
