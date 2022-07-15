package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
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
    @Symbol("revApi")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
        //@Override
        //public StaticAnalysisLabelProvider getLabelProvider() {
            //return new RevApi.LabelProvider(getDisplayName());
       // }
    }
/*
    private static class LabelProvider extends CompatibilityLabelProvider {

        /**
         * /** Provides the labels for the static analysis tool.
         */
    //    LabelProvider(final String displayName) {
     //       super(ID, displayName);
      //  }


    //}
}