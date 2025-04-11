package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Polyspace tool.
 *
 * @author Eva Habeeb
 */
public class Polyspace extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 5776036181982740586L;
    private static final String ID = "polyspace-parser";

    /** Creates a new instance of {@link Polyspace}. */
    @DataBoundConstructor
    public Polyspace() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("polyspaceParser")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
