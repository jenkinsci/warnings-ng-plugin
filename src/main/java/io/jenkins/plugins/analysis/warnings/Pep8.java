package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.Pep8Parser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for PEP8 Python style guide.
 *
 * @author Joscha Behrmann
 */
public class Pep8 extends ReportScanningTool {
    private static final long serialVersionUID = -2199589729419226931L;
    static final String ID = "pep8";

    /** Creates a new instance of {@link NagFortran}. */
    @DataBoundConstructor
    public Pep8() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public Pep8Parser createParser() {
        return new Pep8Parser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("pep8")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Pep8_ParserName();
        }
    }
}
