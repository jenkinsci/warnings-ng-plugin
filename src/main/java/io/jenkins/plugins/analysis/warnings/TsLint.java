package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for TSLint. Delegates to {@link CheckStyleParser}.
 *
 * @author Ullrich Hafner
 */
public class TsLint extends ReportScanningTool {
    private static final long serialVersionUID = -2834404931238461956L;
    
    static final String ID = "tslint";

    /** Creates a new instance of {@link TsLint}. */
    @DataBoundConstructor
    public TsLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public CheckStyleParser createParser() {
        return new CheckStyleParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("tsLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_TSLint_Name();
        }

        @Override
        public String getHelp() {
            return "Use option --format checkstyle.";
        }

        @Override
        public String getUrl() {
            return "https://palantir.github.io/tslint/";
        }
    }
}
