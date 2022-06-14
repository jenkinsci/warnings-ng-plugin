package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.AnalysisModelParser.AnalysisModelParserDescriptor;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for RevApi. Delegates to {@link }
 */
public class RevApi extends AnalysisModelParser {
    private static final long serialVersionUID = -8571635906342563283L;
    private static final String ID = "revapi";

    /** Creates a new instance of {@link RevApi}. */
    @DataBoundConstructor
    public RevApi() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("revapi")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}