package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.IarCstatParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the IAR C-Stat static analysis tool.
 *
 * @author Lorenz Aebi
 */
public class IarCstat extends ReportScanningTool {
    private static final long serialVersionUID = 6672928932731913714L;
    private static final String ID = "iar-cstat";

    /** Creates a new instance of {@link IarCstat}. */
    @DataBoundConstructor
    public IarCstat() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new IarCstatParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("iarCstat")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_iarCstat_ParserName();
        }

        @Override
        public String getHelp() {
            return "<p>The IAR C-STAT static analysis tool finds potential issues in code by doing an analysis "
                    + "on the source code level. Use the following icstat command to generate the output on "
                    + "stdout in the correct format: <pre><code>"
                    + "icstat --db a.db --checks checks.ch commands commands.txt"
                    + "</code></pre> where the commands.txt contains: <pre><code>"
                    + "analyze - iccxxxxcompiler_opts cstat1.c\n"
                    + "analyze - iccxxxxcompiler_opts cstat2.c"
                    + "</pre></code>"
                    + "For details check the IAR C-STAT guide.</p>";
        }
    }
}
