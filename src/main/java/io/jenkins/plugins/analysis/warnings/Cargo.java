package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.CargoCheckParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for {@code rustc} compiler messages emitted b {@code cargo check
 * --message-format json}.
 *
 * @author Ullrich Hafner
 */
public class Cargo extends ReportScanningTool {
    private static final long serialVersionUID = -3997235880208767455L;
    private static final String ID = "cargo";

    /** Creates a new instance of {@link Cargo}. */
    @DataBoundConstructor
    public Cargo() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CargoCheckParser createParser() {
        return new CargoCheckParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cargo")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Cargo_ParserName();
        }

        @Override
        public String getHelp() {
            return "Use commandline <code>cargo check --message-format json</code>";
        }
    }
}
