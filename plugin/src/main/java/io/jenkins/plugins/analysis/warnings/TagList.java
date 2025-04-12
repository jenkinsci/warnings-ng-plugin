package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Taglist Maven Plugin.
 *
 * @author Ullrich Hafner
 */
public class TagList extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 2696608544063390368L;

    private static final String ID = "taglist";

    /** Creates a new instance of {@link TagList}. */
    @DataBoundConstructor
    public TagList() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("tagList")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
