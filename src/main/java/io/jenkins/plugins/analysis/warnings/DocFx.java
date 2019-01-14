package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.violations.DocFxAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for DocFX.
 *
 * @author Ullrich Hafner
 */
public class DocFx extends ReportScanningTool {
    private static final long serialVersionUID = 268538556620830869L;
    static final String ID = "docfx";

    /** Creates a new instance of {@link DocFx}. */
    @DataBoundConstructor
    public DocFx() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public DocFxAdapter createParser() {
        return new DocFxAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("docFx")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Violations_DocFx();
        }
    }
}
