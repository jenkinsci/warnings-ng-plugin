package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

import static j2html.TagCreator.*;

/**
 * Provides a parser for the native format of the Warnings Next Generation Plugin.
 *
 * @author Ullrich Hafner
 */
public class WarningsPlugin extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 8110398783405047555L;
    private static final String ID = "issues";

    /** Creates a new instance of {@link WarningsPlugin}. */
    @DataBoundConstructor
    public WarningsPlugin() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("issues")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID, "native");
        }

        @Override
        public boolean canScanConsoleLog() {
            return true;
        }

        @Override
        public String getHelp() {
            return p().withText("Create an output file that contains issues in the native Warnings Plugin format, "
                    + "in either XML or JSON. The supported format is identical to the format of the remote API calls. "
                    + "The parser is even capable of reading individual lines of a log file that contains issues in JSON format.").render();
        }
    }
}
