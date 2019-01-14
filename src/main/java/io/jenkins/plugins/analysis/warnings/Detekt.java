package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Detekt. Delegates to {@link CheckStyleParser}.
 *
 * @author Ullrich Hafner
 */
public class Detekt extends ReportScanningTool {
    private static final long serialVersionUID = 2441989609462884392L;
    
    static final String ID = "detekt";

    /** Creates a new instance of {@link Detekt}. */
    @DataBoundConstructor
    public Detekt() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CheckStyleParser createParser() {
        return new CheckStyleParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("detekt")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Detekt_Name();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getHelp() {
            return "Use option --output-format xml.";
        }

        @Override
        public String getUrl() {
            return "https://arturbosch.github.io/detekt/";
        }
    }
}
