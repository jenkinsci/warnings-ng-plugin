package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.PhpParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Php runtime errors and warnings.
 *
 * @author Ullrich Hafner
 */
public class Php extends ReportScanningTool {
    private static final long serialVersionUID = 7286546914256953672L;
    static final String ID = "php";

    /** Creates a new instance of {@link Php}. */
    @DataBoundConstructor
    public Php() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public PhpParser createParser() {
        return new PhpParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("php")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PHP_ParserName();
        }
    }
}
