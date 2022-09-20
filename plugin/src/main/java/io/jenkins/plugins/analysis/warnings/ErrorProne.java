package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SymbolIconLabelProvider;

/**
 * Provides a parser and customized messages for Error Prone.
 *
 * @author Ullrich Hafner
 */
public class ErrorProne extends AnalysisModelParser {
    private static final long serialVersionUID = -511511623854186032L;
    private static final String ID = "error-prone";

    /** Creates a new instance of {@link ErrorProne}. */
    @DataBoundConstructor
    public ErrorProne() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("errorProne")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new SymbolIconLabelProvider(getId(), getDisplayName(), getDescriptionProvider(), "symbol-solid/bug plugin-font-awesome-api");
        }
    }
}
