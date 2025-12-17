package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Gcc3 Compiler.
 *
 * @author Raphael Furch
 */
public class Gcc3 extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -8985462824184450486L;
    private static final String ID = "gcc3";

    /** Creates a new instance of {@link Gcc3}. */
    @DataBoundConstructor
    public Gcc3() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("gcc3")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public String getHelp() {
            return "<p>Parses warnings and errors from legacy GCC compilers (versions older than GCC 4). "
                    + "This parser uses an older, simpler warning format.</p>"
                    + "<p>For modern GCC versions (GCC 4 and newer, including GCC 5–15+), use the 'gcc' parser instead, "
                    + "which supports the newer format with additional context like:</p>"
                    + "<p><code>file.c:10:5: warning: unused variable 'x' [-Wunused-variable]</code></p>";
        }
    }
}
