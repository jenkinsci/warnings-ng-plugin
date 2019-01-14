package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.StyleCopParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for StyleCop.
 *
 * @author Ullrich Hafner
 */
public class StyleCop extends ReportScanningTool {
    private static final long serialVersionUID = 230834175319040103L;
    static final String ID = "stylecop";

    /** Creates a new instance of {@link StyleCop}. */
    @DataBoundConstructor
    public StyleCop() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public StyleCopParser createParser() {
        return new StyleCopParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("styleCop")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_StyleCop_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
